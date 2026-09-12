package br.com.keila.modules.fiado.model;

import br.com.keila.modules.cliente.model.Cliente;
import br.com.keila.modules.loja.model.Loja;
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
import java.time.LocalDate;
import java.time.Instant;

/**
 * Entidade JPA para a tabela `fiados` (V4__create_clientes_fiado.sql).
 * `venda_id` não é mapeado como relação ainda (módulo de vendas/PDV não implementado);
 * fiados são lançados manualmente até que o PDV exista.
 */
@Entity
@Table(name = "fiados")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fiado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loja_id", nullable = false)
    private Loja loja;

    @Column(name = "venda_id")
    private Long vendaId;

    @Column(name = "valor_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "valor_pago", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorPago;

    @Column(name = "valor_restante", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorRestante;

    @Column(name = "data_lancamento", nullable = false)
    private LocalDate dataLancamento;

    @Column(name = "data_vencimento", nullable = false)
    private LocalDate dataVencimento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusFiado status;

    @Lob
    private String observacoes;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
