package br.com.keila.modules.fiado.service;

import br.com.keila.modules.cliente.model.Cliente;
import br.com.keila.modules.cliente.repository.ClienteRepository;
import br.com.keila.modules.fiado.dto.FiadoRequest;
import br.com.keila.modules.fiado.dto.FiadoResponse;
import br.com.keila.modules.fiado.dto.PagamentoRequest;
import br.com.keila.modules.fiado.dto.PagamentoResponse;
import br.com.keila.modules.fiado.model.Fiado;
import br.com.keila.modules.fiado.model.PagamentoFiado;
import br.com.keila.modules.fiado.model.StatusFiado;
import br.com.keila.modules.fiado.repository.FiadoRepository;
import br.com.keila.modules.fiado.repository.PagamentoFiadoRepository;
import br.com.keila.modules.loja.model.Loja;
import br.com.keila.modules.loja.repository.LojaRepository;
import br.com.keila.modules.usuario.model.Usuario;
import br.com.keila.shared.exception.EntidadeNaoEncontradaException;
import br.com.keila.shared.exception.RegraNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FiadoService {

    private final FiadoRepository fiadoRepository;
    private final PagamentoFiadoRepository pagamentoRepository;
    private final ClienteRepository clienteRepository;
    private final LojaRepository lojaRepository;

    @Transactional(readOnly = true)
    public List<FiadoResponse> listar() {
        return fiadoRepository.findAllByOrderByDataVencimentoAsc().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<PagamentoResponse> listarPagamentos(Long fiadoId) {
        return pagamentoRepository.findByFiadoIdOrderByDataPagamentoDescCreatedAtDesc(fiadoId).stream()
                .map(this::toPagamentoResponse)
                .toList();
    }

    public FiadoResponse criar(FiadoRequest request) {
        Cliente cliente = clienteRepository.findById(request.clienteId())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Cliente não encontrado: " + request.clienteId()));
        Loja loja = lojaRepository.findById(request.lojaId())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Loja não encontrada: " + request.lojaId()));

        Fiado fiado = Fiado.builder()
                .cliente(cliente)
                .loja(loja)
                .valorTotal(request.valorTotal())
                .valorPago(BigDecimal.ZERO)
                .valorRestante(request.valorTotal())
                .dataLancamento(LocalDate.now())
                .dataVencimento(request.dataVencimento())
                .status(StatusFiado.ABERTO)
                .observacoes(request.observacoes())
                .build();

        return toResponse(fiadoRepository.save(fiado));
    }

    /** Usado internamente pelo módulo de vendas ao fechar uma venda com pagamento em fiado. */
    public void criarParaVenda(Long clienteId, Long lojaId, BigDecimal valor, LocalDate vencimento, Long vendaId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Cliente não encontrado: " + clienteId));
        Loja loja = lojaRepository.findById(lojaId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Loja não encontrada: " + lojaId));

        Fiado fiado = Fiado.builder()
                .cliente(cliente)
                .loja(loja)
                .vendaId(vendaId)
                .valorTotal(valor)
                .valorPago(BigDecimal.ZERO)
                .valorRestante(valor)
                .dataLancamento(LocalDate.now())
                .dataVencimento(vencimento)
                .status(StatusFiado.ABERTO)
                .observacoes("Gerado automaticamente pela venda #" + vendaId)
                .build();

        fiadoRepository.save(fiado);
    }

    public FiadoResponse registrarPagamento(Long fiadoId, PagamentoRequest request) {
        Fiado fiado = fiadoRepository.findById(fiadoId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Fiado não encontrado: " + fiadoId));

        if (fiado.getStatus() == StatusFiado.PAGO) {
            throw new RegraNegocioException("Este fiado já está totalmente quitado.");
        }
        if (request.valor().compareTo(fiado.getValorRestante()) > 0) {
            throw new RegraNegocioException(
                    "O valor do pagamento (" + request.valor() + ") é maior que o saldo devedor (" + fiado.getValorRestante() + ").");
        }

        fiado.setValorPago(fiado.getValorPago().add(request.valor()));
        fiado.setValorRestante(fiado.getValorRestante().subtract(request.valor()));
        fiado.setStatus(fiado.getValorRestante().compareTo(BigDecimal.ZERO) == 0 ? StatusFiado.PAGO : StatusFiado.PARCIAL);
        fiadoRepository.save(fiado);

        PagamentoFiado pagamento = PagamentoFiado.builder()
                .fiado(fiado)
                .usuario(usuarioAtual())
                .valor(request.valor())
                .formaPagamento(request.formaPagamento())
                .dataPagamento(LocalDate.now())
                .observacoes(request.observacoes())
                .build();
        pagamentoRepository.save(pagamento);

        return toResponse(fiado);
    }

    private Usuario usuarioAtual() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal instanceof Usuario usuario ? usuario : null;
    }

    private FiadoResponse toResponse(Fiado f) {
        boolean vencido = f.getStatus() != StatusFiado.PAGO && f.getDataVencimento().isBefore(LocalDate.now());
        return new FiadoResponse(
                f.getId(), f.getCliente().getId(), f.getCliente().getNome(),
                f.getLoja().getId(), f.getLoja().getNome(),
                f.getValorTotal(), f.getValorPago(), f.getValorRestante(),
                f.getDataLancamento(), f.getDataVencimento(), f.getStatus(), vencido, f.getObservacoes());
    }

    private PagamentoResponse toPagamentoResponse(PagamentoFiado p) {
        return new PagamentoResponse(
                p.getId(), p.getValor(), p.getFormaPagamento(), p.getDataPagamento(),
                p.getUsuario() != null ? p.getUsuario().getNome() : null, p.getObservacoes(), p.getCreatedAt());
    }
}
