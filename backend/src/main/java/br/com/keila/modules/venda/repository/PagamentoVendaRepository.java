package br.com.keila.modules.venda.repository;

import br.com.keila.modules.venda.model.PagamentoVenda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface PagamentoVendaRepository extends JpaRepository<PagamentoVenda, Long> {

    @Query("""
            select coalesce(sum(p.valor), 0) from PagamentoVenda p
            where p.venda.sessao.id = :sessaoId and p.forma = 'DINHEIRO' and p.venda.status = 'FECHADA'
            """)
    BigDecimal somarPagamentosDinheiroPorSessao(@Param("sessaoId") Long sessaoId);
}
