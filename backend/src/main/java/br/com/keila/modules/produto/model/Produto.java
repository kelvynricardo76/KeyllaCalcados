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
import java.time.Instant;
import java.time.LocalDate;

/** Entidade JPA para a tabela `produtos` (V2__create_produtos.sql). */
@Entity
@Table(name = "produtos")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String nome;

    @Lob
    private String descricao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marca_id")
    private Marca marca;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @Column(name = "codigo_barras", length = 50)
    private String codigoBarras;

    @Column(length = 60, unique = true)
    private String sku;

    @Column(name = "preco_custo", nullable = false, precision = 12, scale = 2)
    private BigDecimal precoCusto;

    @Column(name = "preco_venda", nullable = false, precision = 12, scale = 2)
    private BigDecimal precoVenda;

    @Column(name = "tem_grade", nullable = false)
    private boolean temGrade;

    /** Guarda tanto uma URL quanto uma imagem em data URI (base64) selecionada pelo usuário. */
    @Lob
    @Column(name = "foto_principal_url")
    private String fotoPrincipalUrl;

    /** Opcional — só se aplica a produtos perecíveis/com validade (a maioria dos calçados não usa). */
    @Column(name = "data_validade")
    private LocalDate dataValidade;

    @Column(nullable = false)
    private boolean ativo;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
