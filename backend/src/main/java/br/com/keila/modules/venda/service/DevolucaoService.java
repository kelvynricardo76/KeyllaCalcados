package br.com.keila.modules.venda.service;

import br.com.keila.modules.estoque.model.MovimentacaoEstoque;
import br.com.keila.modules.estoque.repository.MovimentacaoEstoqueRepository;
import br.com.keila.modules.estoque.service.EstoqueService;
import br.com.keila.modules.usuario.model.Usuario;
import br.com.keila.modules.venda.dto.DevolucaoRequest;
import br.com.keila.modules.venda.dto.DevolucaoResponse;
import br.com.keila.modules.venda.dto.ItemDevolucaoRequest;
import br.com.keila.modules.venda.dto.ItemDevolvidoResumo;
import br.com.keila.modules.venda.model.Devolucao;
import br.com.keila.modules.venda.model.ItemVenda;
import br.com.keila.modules.venda.model.StatusVenda;
import br.com.keila.modules.venda.model.Venda;
import br.com.keila.modules.venda.repository.DevolucaoRepository;
import br.com.keila.modules.venda.repository.VendaRepository;
import br.com.keila.shared.exception.EntidadeNaoEncontradaException;
import br.com.keila.shared.exception.RegraNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DevolucaoService {

    private final DevolucaoRepository devolucaoRepository;
    private final VendaRepository vendaRepository;
    private final EstoqueService estoqueService;
    private final MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

    @Transactional(readOnly = true)
    public List<DevolucaoResponse> listarPorVenda(Long vendaId) {
        return devolucaoRepository.findByVendaOrigemIdOrderByCreatedAtDesc(vendaId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DevolucaoResponse> listarTodas() {
        return devolucaoRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    public DevolucaoResponse registrar(Long vendaOrigemId, DevolucaoRequest request) {
        Venda vendaOrigem = vendaRepository.findById(vendaOrigemId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Venda não encontrada: " + vendaOrigemId));

        if (vendaOrigem.getStatus() != StatusVenda.FECHADA) {
            throw new RegraNegocioException("Só é possível registrar troca/devolução para vendas finalizadas.");
        }

        BigDecimal valorDevolvido = BigDecimal.ZERO;
        for (ItemDevolucaoRequest itemRequest : request.itens()) {
            ItemVenda itemOriginal = vendaOrigem.getItens().stream()
                    .filter(i -> i.getVariacao() != null && i.getVariacao().getId().equals(itemRequest.variacaoId()))
                    .findFirst()
                    .orElseThrow(() -> new RegraNegocioException(
                            "Esta venda não contém a variação informada (id " + itemRequest.variacaoId() + ")."));

            if (itemRequest.quantidade() > itemOriginal.getQuantidade()) {
                throw new RegraNegocioException(
                        "Quantidade a devolver (" + itemRequest.quantidade() + ") maior que a quantidade vendida (" +
                                itemOriginal.getQuantidade() + ") para " + itemOriginal.getNomeProdutoSnapshot() + ".");
            }

            valorDevolvido = valorDevolvido.add(itemOriginal.getPrecoUnitario().multiply(BigDecimal.valueOf(itemRequest.quantidade())));
        }

        Devolucao devolucao = Devolucao.builder()
                .vendaOrigem(vendaOrigem)
                .usuario(usuarioAtual())
                .tipo(request.tipo())
                .motivo(request.motivo())
                .valorDevolvido(valorDevolvido)
                .build();
        devolucao = devolucaoRepository.save(devolucao);

        for (ItemDevolucaoRequest itemRequest : request.itens()) {
            estoqueService.entradaPorDevolucao(itemRequest.variacaoId(), vendaOrigem.getLoja().getId(),
                    itemRequest.quantidade(), devolucao.getId());
        }

        return toResponse(devolucao);
    }

    private Usuario usuarioAtual() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal instanceof Usuario usuario ? usuario : null;
    }

    private DevolucaoResponse toResponse(Devolucao d) {
        List<ItemDevolvidoResumo> itens = movimentacaoEstoqueRepository
                .findByReferenciaTipoAndReferenciaIdOrderByIdAsc("DEVOLUCAO", d.getId()).stream()
                .map(this::toItemDevolvido)
                .toList();
        return new DevolucaoResponse(
                d.getId(), d.getVendaOrigem().getId(),
                d.getVendaOrigem().getCliente() != null ? d.getVendaOrigem().getCliente().getNome() : "Consumidor final",
                d.getTipo(), d.getMotivo(), d.getValorDevolvido(),
                d.getUsuario() != null ? d.getUsuario().getNome() : null, d.getCreatedAt(), itens);
    }

    private ItemDevolvidoResumo toItemDevolvido(MovimentacaoEstoque m) {
        var variacao = m.getVariacao();
        return new ItemDevolvidoResumo(
                variacao.getProduto().getNome(),
                variacao.getTamanho() != null ? variacao.getTamanho().getValor() : null,
                variacao.getCor() != null ? variacao.getCor().getNome() : null,
                m.getQuantidade());
    }
}
