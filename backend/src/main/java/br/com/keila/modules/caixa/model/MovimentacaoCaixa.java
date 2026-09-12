package br.com.keila.modules.caixa.model;

import br.com.keila.modules.usuario.model.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** Entidade JPA para a tabela `movimentacoes_caixa` (V5__create_caixa_vendas.sql): sangria/suprimento/despesa. */
@Entity
@Table(name = "movimentacoes_caixa")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimentacaoCaixa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sessao_id", nullable = false)
    private SessaoCaixa sessao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoMovCaixa tipo;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Lob
    @Column(nullable = false)
    private String descricao;

    @Column(length = 60)
    private String categoria;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}
