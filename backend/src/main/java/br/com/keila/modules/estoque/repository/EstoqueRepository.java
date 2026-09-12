package br.com.keila.modules.estoque.repository;

import br.com.keila.modules.estoque.model.Estoque;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstoqueRepository extends JpaRepository<Estoque, Long> {

    Optional<Estoque> findByVariacaoIdAndLojaId(Long variacaoId, Long lojaId);
}
