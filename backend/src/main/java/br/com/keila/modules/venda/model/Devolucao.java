package br.com.keila.modules.venda.model;

import br.com.keila.modules.usuario.model.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Entidade JPA para a tabela `devolucoes` (V5__create_caixa_vendas.sql).
 * O schema registra a devolução em nível de transação (valor total + motivo),
 * sem uma tabela própria de itens devolvidos; o detalhamento por item fica
 * registrado no kardex (`movimentacoes_estoque`, referencia_tipo = 'DEVOLUCAO').
 */
@Entity
@Table(name = "devolucoes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Devolucao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venda_origem_id", nullable = false)
    private Venda vendaOrigem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venda_nova_id")
    private Venda vendaNova;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoDevolucao tipo;

    @Lob
    @Column(nullable = false)
    private String motivo;

    @Column(name = "valor_devolvido", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorDevolvido;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}
