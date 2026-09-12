package br.com.keila.modules.produto.service;

import br.com.keila.modules.produto.dto.ProdutoRequest;
import br.com.keila.modules.produto.dto.ProdutoResponse;
import br.com.keila.modules.produto.model.Categoria;
import br.com.keila.modules.produto.model.Marca;
import br.com.keila.modules.produto.model.Produto;
import br.com.keila.modules.produto.repository.CategoriaRepository;
import br.com.keila.modules.produto.repository.MarcaRepository;
import br.com.keila.modules.produto.repository.ProdutoRepository;
import br.com.keila.shared.exception.EntidadeNaoEncontradaException;
import br.com.keila.shared.exception.RegraNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final MarcaRepository marcaRepository;
    private final CategoriaRepository categoriaRepository;

    @Transactional(readOnly = true)
    public List<ProdutoResponse> listar(String busca) {
        List<Produto> produtos = StringUtils.hasText(busca)
                ? produtoRepository.findByNomeContainingIgnoreCaseOrSkuContainingIgnoreCaseOrderByNomeAsc(busca, busca)
                : produtoRepository.findAllByOrderByNomeAsc();
        return produtos.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProdutoResponse buscarPorId(Long id) {
        return toResponse(buscarOuFalhar(id));
    }

    public ProdutoResponse criar(ProdutoRequest request) {
        validarSkuUnico(request.sku(), null);
        Produto produto = new Produto();
        aplicarRequest(produto, request);
        produto.setAtivo(true);
        return toResponse(produtoRepository.save(produto));
    }

    public ProdutoResponse atualizar(Long id, ProdutoRequest request) {
        Produto produto = buscarOuFalhar(id);
        validarSkuUnico(request.sku(), produto.getId());
        aplicarRequest(produto, request);
        return toResponse(produtoRepository.save(produto));
    }

    public ProdutoResponse alterarAtivo(Long id, boolean ativo) {
        Produto produto = buscarOuFalhar(id);
        produto.setAtivo(ativo);
        return toResponse(produtoRepository.save(produto));
    }

    private void validarSkuUnico(String sku, Long idAtual) {
        if (!StringUtils.hasText(sku)) return;
        boolean duplicado = idAtual == null
                ? produtoRepository.existsBySku(sku)
                : produtoRepository.existsBySkuAndIdNot(sku, idAtual);
        if (duplicado) {
            throw new RegraNegocioException("Já existe um produto com o SKU '" + sku + "'.");
        }
    }

    private void aplicarRequest(Produto produto, ProdutoRequest request) {
        produto.setNome(request.nome());
        produto.setDescricao(request.descricao());
        produto.setCodigoBarras(request.codigoBarras());
        produto.setSku(request.sku());
        produto.setPrecoCusto(request.precoCusto());
        produto.setPrecoVenda(request.precoVenda());
        produto.setTemGrade(request.temGrade());
        produto.setMarca(request.marcaId() != null ? buscarMarca(request.marcaId()) : null);
        produto.setCategoria(request.categoriaId() != null ? buscarCategoria(request.categoriaId()) : null);
    }

    private Marca buscarMarca(Long id) {
        return marcaRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Marca não encontrada: " + id));
    }

    private Categoria buscarCategoria(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Categoria não encontrada: " + id));
    }

    private Produto buscarOuFalhar(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Produto não encontrado: " + id));
    }

    private ProdutoResponse toResponse(Produto p) {
        BigDecimal margem = BigDecimal.ZERO;
        if (p.getPrecoVenda() != null && p.getPrecoVenda().compareTo(BigDecimal.ZERO) > 0 && p.getPrecoCusto() != null) {
            margem = p.getPrecoVenda().subtract(p.getPrecoCusto())
                    .divide(p.getPrecoVenda(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }
        Marca marca = p.getMarca();
        Categoria categoria = p.getCategoria();
        return new ProdutoResponse(
                p.getId(), p.getNome(), p.getDescricao(),
                marca != null ? marca.getId() : null,
                marca != null ? marca.getNome() : null,
                categoria != null ? categoria.getId() : null,
                categoria != null ? categoria.getNome() : null,
                p.getCodigoBarras(), p.getSku(), p.getPrecoCusto(), p.getPrecoVenda(),
                margem, p.isTemGrade(), p.getFotoPrincipalUrl(), p.isAtivo());
    }
}
