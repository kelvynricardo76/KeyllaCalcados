package br.com.keila.modules.venda.service;

import br.com.keila.modules.caixa.model.SessaoCaixa;
import br.com.keila.modules.caixa.model.StatusSessao;
import br.com.keila.modules.caixa.repository.SessaoCaixaRepository;
import br.com.keila.modules.cliente.model.Cliente;
import br.com.keila.modules.cliente.repository.ClienteRepository;
import br.com.keila.modules.estoque.service.EstoqueService;
import br.com.keila.modules.fiado.service.FiadoService;
import br.com.keila.modules.loja.model.Loja;
import br.com.keila.modules.loja.repository.LojaRepository;
import br.com.keila.modules.produto.model.VariacaoProduto;
import br.com.keila.modules.produto.repository.VariacaoProdutoRepository;
import br.com.keila.modules.usuario.model.Usuario;
import br.com.keila.modules.venda.dto.*;
import br.com.keila.modules.venda.model.*;
import br.com.keila.modules.venda.repository.ItemVendaRepository;
import br.com.keila.modules.venda.repository.PagamentoVendaRepository;
import br.com.keila.modules.venda.repository.VendaRepository;
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
public class VendaService {

    private final VendaRepository vendaRepository;
    private final SessaoCaixaRepository sessaoRepository;
    private final ClienteRepository clienteRepository;
    private final LojaRepository lojaRepository;
    private final VariacaoProdutoRepository variacaoRepository;
    private final EstoqueService estoqueService;
    private final FiadoService fiadoService;
    private final ItemVendaRepository itemVendaRepository;
    private final PagamentoVendaRepository pagamentoVendaRepository;

    @Transactional(readOnly = true)
    public List<VendaResponse> listarPorSessao(Long sessaoId) {
        return vendaRepository.findBySessaoIdOrderByCreatedAtDesc(sessaoId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public VendaResponse buscarPorId(Long id) {
        return toResponse(buscarOuFalhar(id));
    }

    public VendaResponse abrir(AbrirVendaRequest request) {
        SessaoCaixa sessao = sessaoRepository.findById(request.sessaoId())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Sessão de caixa não encontrada: " + request.sessaoId()));
        if (sessao.getStatus() != StatusSessao.ABERTA) {
            throw new RegraNegocioException("A sessão de caixa selecionada não está aberta.");
        }
        Loja loja = lojaRepository.findById(request.lojaId())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Loja não encontrada: " + request.lojaId()));
        Cliente cliente = request.clienteId() != null
                ? clienteRepository.findById(request.clienteId())
                        .orElseThrow(() -> new EntidadeNaoEncontradaException("Cliente não encontrado: " + request.clienteId()))
                : null;

        Venda venda = Venda.builder()
                .sessao(sessao)
                .cliente(cliente)
                .usuario(usuarioAtual())
                .loja(loja)
                .status(StatusVenda.ABERTA)
                .subtotal(BigDecimal.ZERO)
                .descontoGeral(BigDecimal.ZERO)
                .valorTotal(BigDecimal.ZERO)
                .build();

        return toResponse(vendaRepository.save(venda));
    }

    public VendaResponse adicionarItem(Long vendaId, AdicionarItemRequest request) {
        Venda venda = buscarOuFalhar(vendaId);
        garantirAberta(venda);

        VariacaoProduto variacao = variacaoRepository.findById(request.variacaoId())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Variação não encontrada: " + request.variacaoId()));

        int disponivel = estoqueService.saldoDisponivel(variacao.getId(), venda.getLoja().getId());
        if (disponivel < request.quantidade()) {
            throw new RegraNegocioException(
                    "Estoque insuficiente para " + variacao.getProduto().getNome() + " (disponível: " + disponivel + ").");
        }

        BigDecimal precoUnitario = variacao.getPrecoVendaOverride() != null
                ? variacao.getPrecoVendaOverride()
                : variacao.getProduto().getPrecoVenda();
        BigDecimal descontoItem = request.descontoItem() != null ? request.descontoItem() : BigDecimal.ZERO;
        BigDecimal subtotalItem = precoUnitario.multiply(BigDecimal.valueOf(request.quantidade())).subtract(descontoItem);

        if (subtotalItem.compareTo(BigDecimal.ZERO) < 0) {
            throw new RegraNegocioException("O desconto do item não pode ser maior que o valor total do item.");
        }

        ItemVenda item = ItemVenda.builder()
                .venda(venda)
                .variacao(variacao)
                .produto(variacao.getProduto())
                .nomeProdutoSnapshot(variacao.getProduto().getNome())
                .skuSnapshot(variacao.getSku())
                .tamanhoSnapshot(variacao.getTamanho() != null ? variacao.getTamanho().getValor() : null)
                .corSnapshot(variacao.getCor() != null ? variacao.getCor().getNome() : null)
                .quantidade(request.quantidade())
                .precoUnitario(precoUnitario)
                .descontoItem(descontoItem)
                .subtotal(subtotalItem)
                .build();

        // Persistimos o item explicitamente antes de adicioná-lo à coleção da venda:
        // com orphanRemoval=true, o Hibernate tenta computar "órfãos" a cada flush da
        // coleção, e um item novo (ainda sem id) nessa checagem dispara
        // TransientObjectException. Salvando primeiro, ele já chega com id à coleção.
        item = itemVendaRepository.save(item);
        venda.getItens().add(item);
        recalcularTotais(venda);

        return toResponse(vendaRepository.save(venda));
    }

    public VendaResponse removerItem(Long vendaId, Long itemId) {
        Venda venda = buscarOuFalhar(vendaId);
        garantirAberta(venda);

        boolean removido = venda.getItens().removeIf(i -> i.getId().equals(itemId));
        if (!removido) {
            throw new EntidadeNaoEncontradaException("Item não encontrado nesta venda: " + itemId);
        }
        recalcularTotais(venda);

        return toResponse(vendaRepository.save(venda));
    }

    public VendaResponse finalizar(Long vendaId, FinalizarVendaRequest request) {
        Venda venda = buscarOuFalhar(vendaId);
        garantirAberta(venda);

        if (venda.getItens().isEmpty()) {
            throw new RegraNegocioException("Adicione ao menos um item antes de finalizar a venda.");
        }

        BigDecimal descontoGeral = request.descontoGeral() != null ? request.descontoGeral() : BigDecimal.ZERO;
        if (descontoGeral.compareTo(venda.getSubtotal()) > 0) {
            throw new RegraNegocioException("O desconto geral não pode ser maior que o subtotal da venda.");
        }
        BigDecimal valorTotal = venda.getSubtotal().subtract(descontoGeral);

        BigDecimal somaPagamentos = request.pagamentos().stream()
                .map(PagamentoInput::valor).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (somaPagamentos.compareTo(valorTotal) < 0) {
            throw new RegraNegocioException(
                    "A soma dos pagamentos (" + somaPagamentos + ") é menor que o total da venda (" + valorTotal + ").");
        }

        BigDecimal troco = somaPagamentos.subtract(valorTotal);
        if (troco.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal totalDinheiro = request.pagamentos().stream()
                    .filter(p -> p.forma() == FormaPagamento.DINHEIRO)
                    .map(PagamentoInput::valor).reduce(BigDecimal.ZERO, BigDecimal::add);
            if (totalDinheiro.compareTo(troco) < 0) {
                throw new RegraNegocioException("Só é possível dar troco quando há dinheiro suficiente entre os pagamentos.");
            }
        }

        int diasVencimentoFiado = request.fiadoVencimentoDias() != null ? request.fiadoVencimentoDias() : 30;
        for (PagamentoInput pagamento : request.pagamentos()) {
            if (pagamento.forma() == FormaPagamento.FIADO) {
                if (venda.getCliente() == null) {
                    throw new RegraNegocioException("Selecione um cliente para vender fiado.");
                }
                fiadoService.criarParaVenda(venda.getCliente().getId(), venda.getLoja().getId(),
                        pagamento.valor(), LocalDate.now().plusDays(diasVencimentoFiado), venda.getId());
            }

            PagamentoVenda pagamentoVenda = pagamentoVendaRepository.save(PagamentoVenda.builder()
                    .venda(venda)
                    .forma(pagamento.forma())
                    .valor(pagamento.valor())
                    .parcelas(pagamento.parcelas() != null ? pagamento.parcelas() : 1)
                    .referencia(pagamento.referencia())
                    .build());
            venda.getPagamentos().add(pagamentoVenda);
        }

        for (ItemVenda item : venda.getItens()) {
            estoqueService.baixarPorVenda(item.getVariacao().getId(), venda.getLoja().getId(), item.getQuantidade(), venda.getId());
        }

        venda.setDescontoGeral(descontoGeral);
        venda.setValorTotal(valorTotal);
        venda.setTroco(troco);
        venda.setStatus(StatusVenda.FECHADA);

        return toResponse(vendaRepository.save(venda));
    }

    public VendaResponse cancelar(Long vendaId) {
        Venda venda = buscarOuFalhar(vendaId);
        if (venda.getStatus() != StatusVenda.ABERTA) {
            throw new RegraNegocioException("Só é possível cancelar vendas que ainda não foram finalizadas.");
        }
        venda.setStatus(StatusVenda.CANCELADA);
        return toResponse(vendaRepository.save(venda));
    }

    private void garantirAberta(Venda venda) {
        if (venda.getStatus() != StatusVenda.ABERTA) {
            throw new RegraNegocioException("Esta venda não está mais aberta para alterações.");
        }
    }

    private void recalcularTotais(Venda venda) {
        BigDecimal subtotal = venda.getItens().stream().map(ItemVenda::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        venda.setSubtotal(subtotal);
        venda.setValorTotal(subtotal.subtract(venda.getDescontoGeral()));
    }

    private Usuario usuarioAtual() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal instanceof Usuario usuario ? usuario : null;
    }

    private Venda buscarOuFalhar(Long id) {
        return vendaRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Venda não encontrada: " + id));
    }

    private VendaResponse toResponse(Venda v) {
        List<ItemVendaResponse> itens = v.getItens().stream()
                .map(i -> new ItemVendaResponse(
                        i.getId(), i.getVariacao() != null ? i.getVariacao().getId() : null,
                        i.getNomeProdutoSnapshot(), i.getSkuSnapshot(), i.getTamanhoSnapshot(), i.getCorSnapshot(),
                        i.getQuantidade(), i.getPrecoUnitario(), i.getDescontoItem(), i.getSubtotal()))
                .toList();
        List<PagamentoVendaResponse> pagamentos = v.getPagamentos().stream()
                .map(p -> new PagamentoVendaResponse(p.getId(), p.getForma(), p.getValor(), p.getParcelas(), p.getReferencia()))
                .toList();
        return new VendaResponse(
                v.getId(), v.getSessao() != null ? v.getSessao().getId() : null,
                v.getCliente() != null ? v.getCliente().getId() : null,
                v.getCliente() != null ? v.getCliente().getNome() : null,
                v.getUsuario().getNome(), v.getLoja().getId(), v.getStatus(),
                v.getSubtotal(), v.getDescontoGeral(), v.getValorTotal(), v.getTroco(), v.getObservacoes(),
                itens, pagamentos, v.getCreatedAt());
    }
}
