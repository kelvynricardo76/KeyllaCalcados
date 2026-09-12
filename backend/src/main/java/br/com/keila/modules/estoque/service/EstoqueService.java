package br.com.keila.modules.estoque.service;

import br.com.keila.modules.estoque.dto.AjusteEstoqueRequest;
import br.com.keila.modules.estoque.dto.EstoqueResponse;
import br.com.keila.modules.estoque.dto.MovimentacaoResponse;
import br.com.keila.modules.estoque.model.Estoque;
import br.com.keila.modules.estoque.model.MovimentacaoEstoque;
import br.com.keila.modules.estoque.model.TipoMovEstoque;
import br.com.keila.modules.estoque.repository.EstoqueRepository;
import br.com.keila.modules.estoque.repository.MovimentacaoEstoqueRepository;
import br.com.keila.modules.loja.model.Loja;
import br.com.keila.modules.loja.repository.LojaRepository;
import br.com.keila.modules.produto.model.Cor;
import br.com.keila.modules.produto.model.Tamanho;
import br.com.keila.modules.produto.model.VariacaoProduto;
import br.com.keila.modules.produto.repository.VariacaoProdutoRepository;
import br.com.keila.modules.usuario.model.Usuario;
import br.com.keila.shared.exception.EntidadeNaoEncontradaException;
import br.com.keila.shared.exception.RegraNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class EstoqueService {

    private static final Set<TipoMovEstoque> TIPOS_ENTRADA =
            Set.of(TipoMovEstoque.ENTRADA, TipoMovEstoque.AJUSTE_POSITIVO,
                    TipoMovEstoque.DEVOLUCAO, TipoMovEstoque.TRANSFERENCIA_ENTRADA);

    private final EstoqueRepository estoqueRepository;
    private final MovimentacaoEstoqueRepository movimentacaoRepository;
    private final VariacaoProdutoRepository variacaoRepository;
    private final LojaRepository lojaRepository;

    @Transactional(readOnly = true)
    public List<EstoqueResponse> listar() {
        return estoqueRepository.findAll().stream()
                .sorted((a, b) -> a.getVariacao().getProduto().getNome()
                        .compareToIgnoreCase(b.getVariacao().getProduto().getNome()))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MovimentacaoResponse> listarMovimentacoes(Long estoqueId) {
        Estoque estoque = buscarOuFalhar(estoqueId);
        return movimentacaoRepository
                .findByVariacaoIdAndLojaIdOrderByCreatedAtDesc(estoque.getVariacao().getId(), estoque.getLoja().getId())
                .stream()
                .map(this::toMovimentacaoResponse)
                .toList();
    }

    public EstoqueResponse ajustar(AjusteEstoqueRequest request) {
        Estoque estoque = aplicarMovimento(request.variacaoId(), request.lojaId(), request.tipo(),
                request.quantidade(), "AJUSTE_MANUAL", null, request.motivo());
        return toResponse(estoque);
    }

    /** Baixa de estoque por venda fechada no PDV. Usado internamente pelo módulo de vendas. */
    public void baixarPorVenda(Long variacaoId, Long lojaId, int quantidade, Long vendaId) {
        aplicarMovimento(variacaoId, lojaId, TipoMovEstoque.SAIDA, quantidade, "VENDA", vendaId, null);
    }

    /** Entrada de estoque por recebimento de compra. Usado internamente pelo módulo de fornecedores/compras. */
    public void entradaPorCompra(Long variacaoId, Long lojaId, int quantidade, Long compraId) {
        aplicarMovimento(variacaoId, lojaId, TipoMovEstoque.ENTRADA, quantidade, "COMPRA", compraId, null);
    }

    /** Entrada de estoque por troca/devolução de venda. Usado internamente pelo módulo de vendas. */
    public void entradaPorDevolucao(Long variacaoId, Long lojaId, int quantidade, Long devolucaoId) {
        aplicarMovimento(variacaoId, lojaId, TipoMovEstoque.DEVOLUCAO, quantidade, "DEVOLUCAO", devolucaoId, null);
    }

    @Transactional(readOnly = true)
    public int saldoDisponivel(Long variacaoId, Long lojaId) {
        return estoqueRepository.findByVariacaoIdAndLojaId(variacaoId, lojaId)
                .map(Estoque::getQuantidade)
                .orElse(0);
    }

    private Estoque aplicarMovimento(Long variacaoId, Long lojaId, TipoMovEstoque tipo, int quantidade,
                                      String referenciaTipo, Long referenciaId, String motivo) {
        VariacaoProduto variacao = variacaoRepository.findById(variacaoId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Variação não encontrada: " + variacaoId));
        Loja loja = lojaRepository.findById(lojaId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Loja não encontrada: " + lojaId));

        Estoque estoque = estoqueRepository.findByVariacaoIdAndLojaId(variacaoId, lojaId)
                .orElseGet(() -> Estoque.builder()
                        .variacao(variacao).loja(loja).quantidade(0).estoqueMinimo(1).build());

        int anterior = estoque.getQuantidade();
        int delta = TIPOS_ENTRADA.contains(tipo) ? quantidade : -quantidade;
        int posterior = anterior + delta;

        if (posterior < 0) {
            throw new RegraNegocioException(
                    "Estoque insuficiente: saldo atual é " + anterior + ", não é possível remover " + quantidade + ".");
        }

        estoque.setQuantidade(posterior);
        estoque = estoqueRepository.save(estoque);

        MovimentacaoEstoque movimentacao = MovimentacaoEstoque.builder()
                .variacao(variacao)
                .loja(loja)
                .tipo(tipo)
                .quantidade(quantidade)
                .quantidadeAnterior(anterior)
                .quantidadePosterior(posterior)
                .referenciaTipo(referenciaTipo)
                .referenciaId(referenciaId)
                .motivo(motivo)
                .usuario(usuarioAtual())
                .build();
        movimentacaoRepository.save(movimentacao);

        return estoque;
    }

    private Usuario usuarioAtual() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal instanceof Usuario usuario ? usuario : null;
    }

    private Estoque buscarOuFalhar(Long id) {
        return estoqueRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Registro de estoque não encontrado: " + id));
    }

    private EstoqueResponse toResponse(Estoque e) {
        VariacaoProduto variacao = e.getVariacao();
        Tamanho tamanho = variacao.getTamanho();
        Cor cor = variacao.getCor();
        String status = e.getQuantidade() == 0 ? "ZERADO"
                : e.getQuantidade() <= e.getEstoqueMinimo() ? "BAIXO"
                : "OK";
        return new EstoqueResponse(
                e.getId(), variacao.getId(), variacao.getProduto().getNome(),
                tamanho != null ? tamanho.getValor() : null,
                cor != null ? cor.getNome() : null,
                variacao.getSku(),
                e.getLoja().getId(), e.getLoja().getNome(),
                e.getQuantidade(), e.getEstoqueMinimo(), status, e.getUpdatedAt());
    }

    private MovimentacaoResponse toMovimentacaoResponse(MovimentacaoEstoque m) {
        return new MovimentacaoResponse(
                m.getId(), m.getTipo(), m.getQuantidade(), m.getQuantidadeAnterior(), m.getQuantidadePosterior(),
                m.getMotivo(), m.getUsuario() != null ? m.getUsuario().getNome() : null, m.getCreatedAt());
    }
}
