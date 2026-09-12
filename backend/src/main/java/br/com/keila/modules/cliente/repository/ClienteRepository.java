package br.com.keila.modules.cliente.repository;

import br.com.keila.modules.cliente.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    List<Cliente> findAllByOrderByNomeAsc();

    List<Cliente> findByNomeContainingIgnoreCaseOrCpfContainingOrTelefoneContainingOrderByNomeAsc(
            String nome, String cpf, String telefone);

    boolean existsByCpf(String cpf);

    boolean existsByCpfAndIdNot(String cpf, Long id);

    boolean existsByCnpj(String cnpj);

    boolean existsByCnpjAndIdNot(String cnpj, Long id);
}
