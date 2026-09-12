package br.com.keila.modules.produto.service;

import br.com.keila.modules.produto.dto.MarcaRequest;
import br.com.keila.modules.produto.dto.MarcaResponse;
import br.com.keila.modules.produto.model.Marca;
import br.com.keila.modules.produto.repository.MarcaRepository;
import br.com.keila.shared.exception.EntidadeNaoEncontradaException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MarcaService {

    private final MarcaRepository marcaRepository;

    @Transactional(readOnly = true)
    public List<MarcaResponse> listar() {
        return marcaRepository.findAllByOrderByNomeAsc().stream().map(this::toResponse).toList();
    }

    public MarcaResponse criar(MarcaRequest request) {
        Marca marca = Marca.builder()
                .nome(request.nome())
                .descricao(request.descricao())
                .logoUrl(request.logoUrl())
                .ativo(true)
                .build();
        return toResponse(marcaRepository.save(marca));
    }

    public MarcaResponse atualizar(Long id, MarcaRequest request) {
        Marca marca = buscarOuFalhar(id);
        marca.setNome(request.nome());
        marca.setDescricao(request.descricao());
        marca.setLogoUrl(request.logoUrl());
        return toResponse(marcaRepository.save(marca));
    }

    public MarcaResponse alterarAtivo(Long id, boolean ativo) {
        Marca marca = buscarOuFalhar(id);
        marca.setAtivo(ativo);
        return toResponse(marcaRepository.save(marca));
    }

    private Marca buscarOuFalhar(Long id) {
        return marcaRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Marca não encontrada: " + id));
    }

    private MarcaResponse toResponse(Marca m) {
        return new MarcaResponse(m.getId(), m.getNome(), m.getDescricao(), m.getLogoUrl(), m.isAtivo());
    }
}
