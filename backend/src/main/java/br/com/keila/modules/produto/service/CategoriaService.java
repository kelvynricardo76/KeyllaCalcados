package br.com.keila.modules.produto.service;

import br.com.keila.modules.produto.dto.CategoriaRequest;
import br.com.keila.modules.produto.dto.CategoriaResponse;
import br.com.keila.modules.produto.model.Categoria;
import br.com.keila.modules.produto.repository.CategoriaRepository;
import br.com.keila.shared.exception.EntidadeNaoEncontradaException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar() {
        return categoriaRepository.findAllByOrderByOrdemAscNomeAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    public CategoriaResponse criar(CategoriaRequest request) {
        Categoria categoria = new Categoria();
        aplicarRequest(categoria, request);
        categoria.setAtivo(true);
        return toResponse(categoriaRepository.save(categoria));
    }

    public CategoriaResponse atualizar(Long id, CategoriaRequest request) {
        Categoria categoria = buscarOuFalhar(id);
        aplicarRequest(categoria, request);
        return toResponse(categoriaRepository.save(categoria));
    }

    public CategoriaResponse alterarAtivo(Long id, boolean ativo) {
        Categoria categoria = buscarOuFalhar(id);
        categoria.setAtivo(ativo);
        return toResponse(categoriaRepository.save(categoria));
    }

    private void aplicarRequest(Categoria categoria, CategoriaRequest request) {
        categoria.setNome(request.nome());
        categoria.setDescricao(request.descricao());
        categoria.setOrdem(request.ordem());
        if (request.categoriaPaiId() != null) {
            categoria.setCategoriaPai(buscarOuFalhar(request.categoriaPaiId()));
        } else {
            categoria.setCategoriaPai(null);
        }
    }

    private Categoria buscarOuFalhar(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Categoria não encontrada: " + id));
    }

    private CategoriaResponse toResponse(Categoria c) {
        Categoria pai = c.getCategoriaPai();
        return new CategoriaResponse(
                c.getId(), c.getNome(),
                pai != null ? pai.getId() : null,
                pai != null ? pai.getNome() : null,
                c.getDescricao(), c.getOrdem(), c.isAtivo());
    }
}
