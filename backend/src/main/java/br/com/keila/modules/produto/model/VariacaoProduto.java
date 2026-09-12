package br.com.keila.modules.produto.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** Entidade JPA para a tabela `variacoes_produto` (V3__create_estoque.sql): grade produto+tamanho+cor. */
@Entity
@Table(name = "variacoes_produto")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VariacaoProduto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tamanho_id")
    private Tamanho tamanho;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cor_id")
    private Cor cor;

    @Column(nullable = false, unique = true, length = 80)
    private String sku;

    @Column(name = "codigo_barras", length = 50)
    private String codigoBarras;

    @Column(name = "preco_custo_override", precision = 12, scale = 2)
    private BigDecimal precoCustoOverride;

    @Column(name = "preco_venda_override", precision = 12, scale = 2)
    private BigDecimal precoVendaOverride;

    @Column(nullable = false)
    private boolean ativo;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
