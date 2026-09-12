package br.com.keila.modules.venda.model;

import br.com.keila.modules.produto.model.Produto;
import br.com.keila.modules.produto.model.VariacaoProduto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Entidade JPA para a tabela `itens_venda` (V5__create_caixa_vendas.sql).
 * Guarda um snapshot do produto/variação no momento da venda, pois o catálogo
 * pode mudar depois (preço, nome) sem afetar vendas já registradas.
 */
@Entity
@Table(name = "itens_venda")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemVenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venda_id", nullable = false)
    private Venda venda;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variacao_id")
    private VariacaoProduto variacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id")
    private Produto produto;

    @Column(name = "nome_produto_snapshot", nullable = false, length = 200)
    private String nomeProdutoSnapshot;

    @Column(name = "sku_snapshot", length = 80)
    private String skuSnapshot;

    @Column(name = "tamanho_snapshot", length = 10)
    private String tamanhoSnapshot;

    @Column(name = "cor_snapshot", length = 50)
    private String corSnapshot;

    @Column(nullable = false)
    private int quantidade;

    @Column(name = "preco_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precoUnitario;

    @Column(name = "desconto_item", nullable = false, precision = 12, scale = 2)
    private BigDecimal descontoItem;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}
