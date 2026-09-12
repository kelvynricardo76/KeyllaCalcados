package br.com.keila.modules.estoque.model;

import br.com.keila.modules.loja.model.Loja;
import br.com.keila.modules.produto.model.VariacaoProduto;
import br.com.keila.modules.usuario.model.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

/** Entidade JPA para a tabela `movimentacoes_estoque` (V3__create_estoque.sql): kardex. */
@Entity
@Table(name = "movimentacoes_estoque")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimentacaoEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variacao_id", nullable = false)
    private VariacaoProduto variacao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loja_id", nullable = false)
    private Loja loja;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoMovEstoque tipo;

    @Column(nullable = false)
    private int quantidade;

    @Column(name = "quantidade_anterior", nullable = false)
    private int quantidadeAnterior;

    @Column(name = "quantidade_posterior", nullable = false)
    private int quantidadePosterior;

    @Column(name = "referencia_tipo", length = 30)
    private String referenciaTipo;

    @Column(name = "referencia_id")
    private Long referenciaId;

    @Lob
    private String motivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}
