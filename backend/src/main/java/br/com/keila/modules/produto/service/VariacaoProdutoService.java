package br.com.keila.modules.produto.service;

import br.com.keila.modules.produto.dto.VariacaoRequest;
import br.com.keila.modules.produto.dto.VariacaoResponse;
import br.com.keila.modules.produto.model.Cor;
import br.com.keila.modules.produto.model.Produto;
import br.com.keila.modules.produto.model.Tamanho;
import br.com.keila.modules.produto.model.VariacaoProduto;
import br.com.keila.modules.produto.repository.CorRepository;
import br.com.keila.modules.produto.repository.ProdutoRepository;
import br.com.keila.modules.produto.repository.TamanhoRepository;
import br.com.keila.modules.produto.repository.VariacaoProdutoRepository;
import br.com.keila.shared.exception.EntidadeNaoEncontradaException;
import br.com.keila.shared.exception.RegraNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class VariacaoProdutoService {

    private final VariacaoProdutoRepository variacaoRepository;
    private final ProdutoRepository produtoRepository;
    private final TamanhoRepository tamanhoRepository;
    private final CorRepository corRepository;

    @Transactional(readOnly = true)
    public List<VariacaoResponse> listarPorProduto(Long produtoId) {
        return variacaoRepository.findByProdutoIdOrderByTamanhoOrdemAscCorNomeAsc(produtoId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VariacaoResponse> listarTodasAtivas() {
        return variacaoRepository.findAll().stream()
                .filter(VariacaoProduto::isAtivo)
                .sorted((a, b) -> a.getProduto().getNome().compareToIgnoreCase(b.getProduto().getNome()))
                .map(this::toResponse)
                .toList();
    }

    public VariacaoResponse criar(Long produtoId, VariacaoRequest request) {
        if (variacaoRepository.existsBySku(request.sku())) {
            throw new RegraNegocioException("Já existe uma variação com o SKU '" + request.sku() + "'.");
        }
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Produto não encontrado: " + produtoId));

        VariacaoProduto variacao = VariacaoProduto.builder()
                .produto(produto)
                .tamanho(request.tamanhoId() != null ? buscarTamanho(request.tamanhoId()) : null)
                .cor(request.corId() != null ? buscarCor(request.corId()) : null)
                .sku(request.sku())
                .codigoBarras(request.codigoBarras())
                .precoCustoOverride(request.precoCustoOverride())
                .precoVendaOverride(request.precoVendaOverride())
                .ativo(true)
                .build();

        return toResponse(variacaoRepository.save(variacao));
    }

    public VariacaoResponse alterarAtivo(Long id, boolean ativo) {
        VariacaoProduto variacao = variacaoRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Variação não encontrada: " + id));
        variacao.setAtivo(ativo);
        return toResponse(variacaoRepository.save(variacao));
    }

    public void excluir(Long id) {
        if (!variacaoRepository.existsById(id)) {
            throw new EntidadeNaoEncontradaException("Variação não encontrada: " + id);
        }
        variacaoRepository.deleteById(id);
    }

    private Tamanho buscarTamanho(Long id) {
        return tamanhoRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Tamanho não encontrado: " + id));
    }

    private Cor buscarCor(Long id) {
        return corRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Cor não encontrada: " + id));
    }

    private VariacaoResponse toResponse(VariacaoProduto v) {
        Tamanho tamanho = v.getTamanho();
        Cor cor = v.getCor();
        return new VariacaoResponse(
                v.getId(), v.getProduto().getId(), v.getProduto().getNome(),
                tamanho != null ? tamanho.getId() : null,
                tamanho != null ? tamanho.getValor() : null,
                cor != null ? cor.getId() : null,
                cor != null ? cor.getNome() : null,
                cor != null ? cor.getHexCode() : null,
                v.getSku(), v.getCodigoBarras(),
                v.getPrecoCustoOverride(), v.getPrecoVendaOverride(), v.isAtivo());
    }
}
