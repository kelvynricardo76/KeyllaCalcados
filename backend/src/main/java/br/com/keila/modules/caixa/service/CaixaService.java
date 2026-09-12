package br.com.keila.modules.caixa.service;

import br.com.keila.modules.caixa.dto.*;
import br.com.keila.modules.caixa.model.Caixa;
import br.com.keila.modules.caixa.model.MovimentacaoCaixa;
import br.com.keila.modules.caixa.model.SessaoCaixa;
import br.com.keila.modules.caixa.model.StatusSessao;
import br.com.keila.modules.caixa.model.TipoMovCaixa;
import br.com.keila.modules.caixa.repository.CaixaRepository;
import br.com.keila.modules.caixa.repository.MovimentacaoCaixaRepository;
import br.com.keila.modules.caixa.repository.SessaoCaixaRepository;
import br.com.keila.modules.usuario.model.Usuario;
import br.com.keila.modules.venda.repository.PagamentoVendaRepository;
import br.com.keila.modules.venda.repository.VendaRepository;
import br.com.keila.shared.exception.EntidadeNaoEncontradaException;
import br.com.keila.shared.exception.RegraNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class CaixaService {

    private final CaixaRepository caixaRepository;
    private final SessaoCaixaRepository sessaoRepository;
    private final MovimentacaoCaixaRepository movimentacaoRepository;
    private final VendaRepository vendaRepository;
    private final PagamentoVendaRepository pagamentoVendaRepository;

    @Transactional(readOnly = true)
    public List<CaixaResponse> listarCaixas() {
        return caixaRepository.findAllByOrderByNomeAsc().stream().map(this::toCaixaResponse).toList();
    }

    @Transactional(readOnly = true)
    public SessaoResponse buscarSessao(Long sessaoId) {
        return toSessaoResponse(buscarSessaoOuFalhar(sessaoId));
    }

    public SessaoResponse abrirSessao(Long caixaId, AbrirSessaoRequest request) {
        Caixa caixa = caixaRepository.findById(caixaId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Caixa não encontrado: " + caixaId));

        sessaoRepository.findByCaixaIdAndStatus(caixaId, StatusSessao.ABERTA).ifPresent(s -> {
            throw new RegraNegocioException("Já existe uma sessão aberta para o caixa '" + caixa.getNome() + "'.");
        });

        SessaoCaixa sessao = SessaoCaixa.builder()
                .caixa(caixa)
                .usuario(usuarioAtual())
                .valorAbertura(request.valorAbertura())
                .status(StatusSessao.ABERTA)
                .build();

        return toSessaoResponse(sessaoRepository.save(sessao));
    }

    public SessaoResponse fecharSessao(Long sessaoId, FecharSessaoRequest request) {
        SessaoCaixa sessao = buscarSessaoOuFalhar(sessaoId);
        if (sessao.getStatus() == StatusSessao.FECHADA) {
            throw new RegraNegocioException("Esta sessão já está fechada.");
        }

        BigDecimal entradasSuprimento = somarMovimentacoes(sessaoId, TipoMovCaixa.SUPRIMENTO, TipoMovCaixa.RECEITA_AVULSA);
        BigDecimal saidasSangriaDespesa = somarMovimentacoes(sessaoId, TipoMovCaixa.SANGRIA, TipoMovCaixa.DESPESA);
        BigDecimal vendasDinheiro = pagamentoVendaRepository.somarPagamentosDinheiroPorSessao(sessaoId);
        BigDecimal trocoPago = vendaRepository.somarTrocoPorSessao(sessaoId);

        BigDecimal calculado = sessao.getValorAbertura()
                .add(entradasSuprimento)
                .add(vendasDinheiro)
                .subtract(saidasSangriaDespesa)
                .subtract(trocoPago);

        sessao.setValorFechamentoCalculado(calculado);
        sessao.setValorFechamentoInformado(request.valorFechamentoInformado());
        sessao.setDiferenca(calculado.subtract(request.valorFechamentoInformado()));
        sessao.setDataFechamento(OffsetDateTime.now());
        sessao.setStatus(StatusSessao.FECHADA);
        sessao.setObservacoes(request.observacoes());

        return toSessaoResponse(sessaoRepository.save(sessao));
    }

    public MovimentacaoCaixaResponse lancarMovimentacao(Long sessaoId, MovimentacaoCaixaRequest request) {
        SessaoCaixa sessao = buscarSessaoOuFalhar(sessaoId);
        if (sessao.getStatus() != StatusSessao.ABERTA) {
            throw new RegraNegocioException("Não é possível lançar movimentações em uma sessão fechada.");
        }

        MovimentacaoCaixa movimentacao = MovimentacaoCaixa.builder()
                .sessao(sessao)
                .usuario(usuarioAtual())
                .tipo(request.tipo())
                .valor(request.valor())
                .descricao(request.descricao())
                .categoria(request.categoria())
                .build();

        return toMovimentacaoResponse(movimentacaoRepository.save(movimentacao));
    }

    @Transactional(readOnly = true)
    public List<MovimentacaoCaixaResponse> listarMovimentacoes(Long sessaoId) {
        return movimentacaoRepository.findBySessaoIdOrderByCreatedAtDesc(sessaoId).stream()
                .map(this::toMovimentacaoResponse)
                .toList();
    }

    private BigDecimal somarMovimentacoes(Long sessaoId, TipoMovCaixa... tipos) {
        Set<TipoMovCaixa> filtro = Set.of(tipos);
        return movimentacaoRepository.findBySessaoIdOrderByCreatedAtDesc(sessaoId).stream()
                .filter(m -> filtro.contains(m.getTipo()))
                .map(MovimentacaoCaixa::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Usuario usuarioAtual() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal instanceof Usuario usuario ? usuario : null;
    }

    private SessaoCaixa buscarSessaoOuFalhar(Long id) {
        return sessaoRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Sessão de caixa não encontrada: " + id));
    }

    private CaixaResponse toCaixaResponse(Caixa c) {
        Long sessaoAbertaId = sessaoRepository.findByCaixaIdAndStatus(c.getId(), StatusSessao.ABERTA)
                .map(SessaoCaixa::getId).orElse(null);
        return new CaixaResponse(c.getId(), c.getNome(), c.isAtivo(), c.getLoja().getId(), sessaoAbertaId);
    }

    private SessaoResponse toSessaoResponse(SessaoCaixa s) {
        return new SessaoResponse(
                s.getId(), s.getCaixa().getId(), s.getCaixa().getNome(), s.getUsuario().getNome(),
                s.getDataAbertura(), s.getValorAbertura(), s.getDataFechamento(),
                s.getValorFechamentoInformado(), s.getValorFechamentoCalculado(), s.getDiferenca(),
                s.getStatus(), s.getObservacoes());
    }

    private MovimentacaoCaixaResponse toMovimentacaoResponse(MovimentacaoCaixa m) {
        return new MovimentacaoCaixaResponse(
                m.getId(), m.getTipo(), m.getValor(), m.getDescricao(), m.getCategoria(),
                m.getUsuario().getNome(), m.getCreatedAt());
    }
}
