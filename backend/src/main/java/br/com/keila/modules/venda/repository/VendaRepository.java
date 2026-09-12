package br.com.keila.modules.venda.repository;

import br.com.keila.modules.venda.model.StatusVenda;
import br.com.keila.modules.venda.model.Venda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public interface VendaRepository extends JpaRepository<Venda, Long> {

    List<Venda> findBySessaoIdOrderByCreatedAtDesc(Long sessaoId);

    List<Venda> findByStatusOrderByCreatedAtDesc(StatusVenda status);

    List<Venda> findByStatusAndCreatedAtBetween(StatusVenda status, OffsetDateTime inicio, OffsetDateTime fim);

    @Query("select coalesce(sum(v.troco), 0) from Venda v where v.sessao.id = :sessaoId and v.status = 'FECHADA'")
    BigDecimal somarTrocoPorSessao(@Param("sessaoId") Long sessaoId);
}
