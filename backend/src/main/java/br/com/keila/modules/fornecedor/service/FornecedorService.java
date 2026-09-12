package br.com.keila.modules.fornecedor.service;

import br.com.keila.modules.fornecedor.dto.FornecedorRequest;
import br.com.keila.modules.fornecedor.dto.FornecedorResponse;
import br.com.keila.modules.fornecedor.model.Fornecedor;
import br.com.keila.modules.fornecedor.repository.FornecedorRepository;
import br.com.keila.shared.exception.EntidadeNaoEncontradaException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FornecedorService {

    private final FornecedorRepository fornecedorRepository;

    @Transactional(readOnly = true)
    public List<FornecedorResponse> listar() {
        return fornecedorRepository.findAllByOrderByRazaoSocialAsc().stream().map(this::toResponse).toList();
    }

    public FornecedorResponse criar(FornecedorRequest request) {
        Fornecedor fornecedor = Fornecedor.builder()
                .razaoSocial(request.razaoSocial())
                .cnpj(vazioParaNull(request.cnpj()))
                .telefone(request.telefone())
                .email(request.email())
                .endereco(request.endereco())
                .contato(request.contato())
                .observacoes(request.observacoes())
                .ativo(true)
                .build();
        return toResponse(fornecedorRepository.save(fornecedor));
    }

    public FornecedorResponse atualizar(Long id, FornecedorRequest request) {
        Fornecedor fornecedor = buscarOuFalhar(id);
        fornecedor.setRazaoSocial(request.razaoSocial());
        fornecedor.setCnpj(vazioParaNull(request.cnpj()));
        fornecedor.setTelefone(request.telefone());
        fornecedor.setEmail(request.email());
        fornecedor.setEndereco(request.endereco());
        fornecedor.setContato(request.contato());
        fornecedor.setObservacoes(request.observacoes());
        return toResponse(fornecedorRepository.save(fornecedor));
    }

    public FornecedorResponse alterarAtivo(Long id, boolean ativo) {
        Fornecedor fornecedor = buscarOuFalhar(id);
        fornecedor.setAtivo(ativo);
        return toResponse(fornecedorRepository.save(fornecedor));
    }

    private String vazioParaNull(String valor) {
        return StringUtils.hasText(valor) ? valor : null;
    }

    private Fornecedor buscarOuFalhar(Long id) {
        return fornecedorRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Fornecedor não encontrado: " + id));
    }

    private FornecedorResponse toResponse(Fornecedor f) {
        return new FornecedorResponse(f.getId(), f.getRazaoSocial(), f.getCnpj(), f.getTelefone(),
                f.getEmail(), f.getEndereco(), f.getContato(), f.getObservacoes(), f.isAtivo());
    }
}
