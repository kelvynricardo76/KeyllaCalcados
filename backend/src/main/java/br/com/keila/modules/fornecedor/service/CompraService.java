package br.com.keila.modules.fornecedor.service;

import br.com.keila.modules.estoque.service.EstoqueService;
import br.com.keila.modules.fornecedor.dto.CompraRequest;
import br.com.keila.modules.fornecedor.dto.CompraResponse;
import br.com.keila.modules.fornecedor.dto.ItemCompraRequest;
import br.com.keila.modules.fornecedor.dto.ItemCompraResponse;
import br.com.keila.modules.fornecedor.model.Compra;
import br.com.keila.modules.fornecedor.model.Fornecedor;
import br.com.keila.modules.fornecedor.model.ItemCompra;
import br.com.keila.modules.fornecedor.model.StatusCompra;
import br.com.keila.modules.fornecedor.repository.CompraRepository;
import br.com.keila.modules.fornecedor.repository.FornecedorRepository;
import br.com.keila.modules.loja.model.Loja;
import br.com.keila.modules.loja.repository.LojaRepository;
import br.com.keila.modules.produto.model.VariacaoProduto;
import br.com.keila.modules.produto.repository.VariacaoProdutoRepository;
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
public class CompraService {

    private final CompraRepository compraRepository;
    private final FornecedorRepository fornecedorRepository;
    private final LojaRepository lojaRepository;
    private final VariacaoProdutoRepository variacaoRepository;
    private final EstoqueService estoqueService;

    @Transactional(readOnly = true)
    public List<CompraResponse> listar() {
        return compraRepository.findAllByOrderByDataCompraDesc().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CompraResponse buscarPorId(Long id) {
        return toResponse(buscarOuFalhar(id));
    }

    public CompraResponse criar(CompraRequest request) {
        Fornecedor fornecedor = request.fornecedorId() != null
                ? fornecedorRepository.findById(request.fornecedorId())
                        .orElseThrow(() -> new EntidadeNaoEncontradaException("Fornecedor não encontrado: " + request.fornecedorId()))
                : null;
        Loja loja = lojaRepository.findById(request.lojaId())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Loja não encontrada: " + request.lojaId()));

        Compra compra = Compra.builder()
                .fornecedor(fornecedor)
                .loja(loja)
                .usuario(usuarioAtual())
                .dataCompra(LocalDate.now())
                .dataEntrega(request.dataEntrega())
                .numeroNf(request.numeroNf())
                .status(StatusCompra.PENDENTE)
                .valorTotal(BigDecimal.ZERO)
                .observacoes(request.observacoes())
                .build();

        BigDecimal valorTotal = BigDecimal.ZERO;
        for (ItemCompraRequest itemRequest : request.itens()) {
            VariacaoProduto variacao = variacaoRepository.findById(itemRequest.variacaoId())
                    .orElseThrow(() -> new EntidadeNaoEncontradaException("Variação não encontrada: " + itemRequest.variacaoId()));
            BigDecimal subtotal = itemRequest.precoCustoUnitario().multiply(BigDecimal.valueOf(itemRequest.quantidade()));
            valorTotal = valorTotal.add(subtotal);

            compra.getItens().add(ItemCompra.builder()
                    .compra(compra)
                    .variacao(variacao)
                    .quantidade(itemRequest.quantidade())
                    .quantidadeRecebida(0)
                    .precoCustoUnitario(itemRequest.precoCustoUnitario())
                    .subtotal(subtotal)
                    .build());
        }
        compra.setValorTotal(valorTotal);

        return toResponse(compraRepository.save(compra));
    }

    /**
     * Recebimento simplificado: confirma o recebimento total da compra de uma vez,
     * dando entrada em estoque de todos os itens. Recebimento parcial por item fica
     * para uma evolução futura deste módulo.
     */
    public CompraResponse receber(Long id) {
        Compra compra = buscarOuFalhar(id);
        if (compra.getStatus() != StatusCompra.PENDENTE) {
            throw new RegraNegocioException("Apenas compras pendentes podem ser recebidas.");
        }

        for (ItemCompra item : compra.getItens()) {
            estoqueService.entradaPorCompra(item.getVariacao().getId(), compra.getLoja().getId(),
                    item.getQuantidade(), compra.getId());
            item.setQuantidadeRecebida(item.getQuantidade());
        }
        compra.setStatus(StatusCompra.RECEBIDA);
        compra.setDataEntrega(LocalDate.now());

        return toResponse(compraRepository.save(compra));
    }

    public CompraResponse cancelar(Long id) {
        Compra compra = buscarOuFalhar(id);
        if (compra.getStatus() != StatusCompra.PENDENTE) {
            throw new RegraNegocioException("Apenas compras pendentes podem ser canceladas.");
        }
        compra.setStatus(StatusCompra.CANCELADA);
        return toResponse(compraRepository.save(compra));
    }

    private Usuario usuarioAtual() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal instanceof Usuario usuario ? usuario : null;
    }

    private Compra buscarOuFalhar(Long id) {
        return compraRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Compra não encontrada: " + id));
    }

    private CompraResponse toResponse(Compra c) {
        List<ItemCompraResponse> itens = c.getItens().stream().map(i -> {
            VariacaoProduto v = i.getVariacao();
            return new ItemCompraResponse(
                    i.getId(), v.getId(), v.getProduto().getNome(),
                    v.getTamanho() != null ? v.getTamanho().getValor() : null,
                    v.getCor() != null ? v.getCor().getNome() : null,
                    v.getSku(), i.getQuantidade(), i.getQuantidadeRecebida(), i.getPrecoCustoUnitario(), i.getSubtotal());
        }).toList();

        return new CompraResponse(
                c.getId(), c.getFornecedor() != null ? c.getFornecedor().getId() : null,
                c.getFornecedor() != null ? c.getFornecedor().getRazaoSocial() : null,
                c.getLoja().getId(), c.getDataCompra(), c.getDataEntrega(), c.getNumeroNf(),
                c.getStatus(), c.getValorTotal(), c.getObservacoes(), itens);
    }
}
