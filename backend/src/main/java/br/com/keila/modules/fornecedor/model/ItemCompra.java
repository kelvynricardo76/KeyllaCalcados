package br.com.keila.modules.fornecedor.model;

import br.com.keila.modules.produto.model.VariacaoProduto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** Entidade JPA para a tabela `itens_compra` (V6__create_fornecedores_compras.sql). */
@Entity
@Table(name = "itens_compra")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "compra_id", nullable = false)
    private Compra compra;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variacao_id", nullable = false)
    private VariacaoProduto variacao;

    @Column(nullable = false)
    private int quantidade;

    @Column(name = "quantidade_recebida", nullable = false)
    private int quantidadeRecebida;

    @Column(name = "preco_custo_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precoCustoUnitario;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;
}
