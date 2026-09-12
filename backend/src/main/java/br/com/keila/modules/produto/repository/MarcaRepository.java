package br.com.keila.modules.produto.repository;

import br.com.keila.modules.produto.model.Marca;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MarcaRepository extends JpaRepository<Marca, Long> {
    List<Marca> findAllByOrderByNomeAsc();
}
