package br.com.keila.modules.produto.repository;

import br.com.keila.modules.produto.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    List<Categoria> findAllByOrderByOrdemAscNomeAsc();
}
