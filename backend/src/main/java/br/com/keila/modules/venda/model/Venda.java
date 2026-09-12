package br.com.keila.modules.venda.model;

import br.com.keila.modules.caixa.model.SessaoCaixa;
import br.com.keila.modules.cliente.model.Cliente;
import br.com.keila.modules.loja.model.Loja;
import br.com.keila.modules.usuario.model.Usuario;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade JPA para a tabela `vendas` (V5__create_caixa_vendas.sql).
 * Recursos de orçamento, cancelamento e troca/devolução da tabela ainda não são
 * mapeados aqui (ficam para uma extensão futura do módulo de vendas); todas as
 * colunas correspondentes têm default ou são nullable no schema.
 */
@Entity
@Table(name = "vendas")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Venda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sessao_id")
    private SessaoCaixa sessao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loja_id", nullable = false)
    private Loja loja;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusVenda status;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "desconto_geral", nullable = false, precision = 12, scale = 2)
    private BigDecimal descontoGeral;

    @Column(name = "valor_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorTotal;

    @Column(precision = 12, scale = 2)
    private BigDecimal troco;

    @Lob
    private String observacoes;

    @Builder.Default
    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemVenda> itens = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PagamentoVenda> pagamentos = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
