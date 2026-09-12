package br.com.keila.modules.estoque.repository;

import br.com.keila.modules.estoque.model.MovimentacaoEstoque;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long> {
    List<MovimentacaoEstoque> findByVariacaoIdAndLojaIdOrderByCreatedAtDesc(Long variacaoId, Long lojaId);
}
