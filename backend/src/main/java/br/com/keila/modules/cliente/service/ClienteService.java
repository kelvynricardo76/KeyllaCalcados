package br.com.keila.modules.cliente.service;

import br.com.keila.modules.cliente.dto.ClienteRequest;
import br.com.keila.modules.cliente.dto.ClienteResponse;
import br.com.keila.modules.cliente.model.Cliente;
import br.com.keila.modules.cliente.repository.ClienteRepository;
import br.com.keila.shared.exception.EntidadeNaoEncontradaException;
import br.com.keila.shared.exception.RegraNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ClienteService {

    private final ClienteRepository clienteRepository;

    @Transactional(readOnly = true)
    public List<ClienteResponse> listar(String busca) {
        List<Cliente> clientes = StringUtils.hasText(busca)
                ? clienteRepository.findByNomeContainingIgnoreCaseOrCpfContainingOrTelefoneContainingOrderByNomeAsc(busca, busca, busca)
                : clienteRepository.findAllByOrderByNomeAsc();
        return clientes.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscarPorId(Long id) {
        return toResponse(buscarOuFalhar(id));
    }

    public ClienteResponse criar(ClienteRequest request) {
        validar(request, null);
        Cliente cliente = new Cliente();
        aplicarRequest(cliente, request);
        cliente.setAtivo(true);
        return toResponse(clienteRepository.save(cliente));
    }

    public ClienteResponse atualizar(Long id, ClienteRequest request) {
        Cliente cliente = buscarOuFalhar(id);
        validar(request, id);
        aplicarRequest(cliente, request);
        return toResponse(clienteRepository.save(cliente));
    }

    public ClienteResponse alterarAtivo(Long id, boolean ativo) {
        Cliente cliente = buscarOuFalhar(id);
        cliente.setAtivo(ativo);
        return toResponse(clienteRepository.save(cliente));
    }

    private void validar(ClienteRequest request, Long idAtual) {
        boolean semDocumento = !StringUtils.hasText(request.cpf())
                && !StringUtils.hasText(request.cnpj())
                && !StringUtils.hasText(request.telefone());
        if (semDocumento) {
            throw new RegraNegocioException("Informe pelo menos um CPF, CNPJ ou telefone para o cliente.");
        }

        if (StringUtils.hasText(request.cpf())) {
            boolean duplicado = idAtual == null
                    ? clienteRepository.existsByCpf(request.cpf())
                    : clienteRepository.existsByCpfAndIdNot(request.cpf(), idAtual);
            if (duplicado) throw new RegraNegocioException("Já existe um cliente com o CPF '" + request.cpf() + "'.");
        }

        if (StringUtils.hasText(request.cnpj())) {
            boolean duplicado = idAtual == null
                    ? clienteRepository.existsByCnpj(request.cnpj())
                    : clienteRepository.existsByCnpjAndIdNot(request.cnpj(), idAtual);
            if (duplicado) throw new RegraNegocioException("Já existe um cliente com o CNPJ '" + request.cnpj() + "'.");
        }
    }

    private void aplicarRequest(Cliente cliente, ClienteRequest request) {
        cliente.setNome(request.nome());
        cliente.setCpf(vazioParaNull(request.cpf()));
        cliente.setCnpj(vazioParaNull(request.cnpj()));
        cliente.setTelefone(vazioParaNull(request.telefone()));
        cliente.setEmail(vazioParaNull(request.email()));
        cliente.setDataNascimento(request.dataNascimento());
        cliente.setCep(request.cep());
        cliente.setLogradouro(request.logradouro());
        cliente.setNumero(request.numero());
        cliente.setComplemento(request.complemento());
        cliente.setBairro(request.bairro());
        cliente.setCidade(request.cidade());
        cliente.setUf(request.uf());
        cliente.setLimiteFiado(request.limiteFiado());
        cliente.setObservacoes(request.observacoes());
    }

    private String vazioParaNull(String valor) {
        return StringUtils.hasText(valor) ? valor : null;
    }

    private Cliente buscarOuFalhar(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Cliente não encontrado: " + id));
    }

    private ClienteResponse toResponse(Cliente c) {
        return new ClienteResponse(
                c.getId(), c.getNome(), c.getCpf(), c.getCnpj(), c.getTelefone(), c.getEmail(),
                c.getDataNascimento(), c.getCep(), c.getLogradouro(), c.getNumero(), c.getComplemento(),
                c.getBairro(), c.getCidade(), c.getUf(), c.getLimiteFiado(), c.getObservacoes(), c.isAtivo());
    }
}
