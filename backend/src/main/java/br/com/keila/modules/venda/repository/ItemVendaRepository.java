package br.com.keila.modules.venda.repository;

import br.com.keila.modules.venda.model.ItemVenda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface ItemVendaRepository extends JpaRepository<ItemVenda, Long> {

    @Query("""
            select i.nomeProdutoSnapshot as nome, sum(i.quantidade) as quantidade, sum(i.subtotal) as total
            from ItemVenda i
            where i.venda.status = 'FECHADA' and i.venda.createdAt between :inicio and :fim
            group by i.nomeProdutoSnapshot
            order by sum(i.quantidade) desc
            """)
    List<RankingProduto> ranquearProdutos(@Param("inicio") Instant inicio, @Param("fim") Instant fim);

    interface RankingProduto {
        String getNome();
        Long getQuantidade();
        java.math.BigDecimal getTotal();
    }
}
