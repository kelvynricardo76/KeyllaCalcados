package br.com.keila.modules.produto.repository;

import br.com.keila.modules.produto.model.Cor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CorRepository extends JpaRepository<Cor, Long> {
    List<Cor> findAllByOrderByNomeAsc();
}
