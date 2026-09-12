package br.com.keila.modules.loja.repository;

import br.com.keila.modules.loja.model.Loja;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LojaRepository extends JpaRepository<Loja, Long> {
    List<Loja> findAllByOrderByNomeAsc();
}
