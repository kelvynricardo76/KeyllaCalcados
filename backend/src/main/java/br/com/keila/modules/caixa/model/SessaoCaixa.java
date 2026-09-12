package br.com.keila.modules.caixa.model;

import br.com.keila.modules.usuario.model.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** Entidade JPA para a tabela `sessoes_caixa` (V5__create_caixa_vendas.sql). */
@Entity
@Table(name = "sessoes_caixa")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessaoCaixa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "caixa_id", nullable = false)
    private Caixa caixa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "data_abertura", nullable = false)
    private OffsetDateTime dataAbertura;

    @Column(name = "valor_abertura", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorAbertura;

    @Column(name = "data_fechamento")
    private OffsetDateTime dataFechamento;

    @Column(name = "valor_fechamento_informado", precision = 12, scale = 2)
    private BigDecimal valorFechamentoInformado;

    @Column(name = "valor_fechamento_calculado", precision = 12, scale = 2)
    private BigDecimal valorFechamentoCalculado;

    @Column(precision = 12, scale = 2)
    private BigDecimal diferenca;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "status_sessao")
    private StatusSessao status;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @PrePersist
    void prePersist() {
        if (dataAbertura == null) {
            dataAbertura = OffsetDateTime.now();
        }
    }
}
