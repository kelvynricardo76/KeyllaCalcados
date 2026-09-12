package br.com.keila.modules.fornecedor.repository;

import br.com.keila.modules.fornecedor.model.Fornecedor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FornecedorRepository extends JpaRepository<Fornecedor, Long> {
    List<Fornecedor> findAllByOrderByRazaoSocialAsc();
}
