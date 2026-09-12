package br.com.keila.modules.caixa.repository;

import br.com.keila.modules.caixa.model.Caixa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CaixaRepository extends JpaRepository<Caixa, Long> {
    List<Caixa> findAllByOrderByNomeAsc();
}
