package br.com.keila.modules.caixa.repository;

import br.com.keila.modules.caixa.model.MovimentacaoCaixa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;

public interface MovimentacaoCaixaRepository extends JpaRepository<MovimentacaoCaixa, Long> {
    List<MovimentacaoCaixa> findBySessaoIdOrderByCreatedAtDesc(Long sessaoId);

    List<MovimentacaoCaixa> findByCreatedAtBetween(OffsetDateTime inicio, OffsetDateTime fim);
}
