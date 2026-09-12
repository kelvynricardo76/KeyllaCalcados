package br.com.keila.modules.estoque.model;

import br.com.keila.modules.loja.model.Loja;
import br.com.keila.modules.produto.model.VariacaoProduto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

/**
 * Entidade JPA para a tabela `estoque` (V3__create_estoque.sql): saldo atual
 * por variação + loja. `localizacao_id` não é mapeado (recurso futuro, coluna nullable).
 */
@Entity
@Table(name = "estoque")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Estoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variacao_id", nullable = false)
    private VariacaoProduto variacao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loja_id", nullable = false)
    private Loja loja;

    @Column(nullable = false)
    private int quantidade;

    @Column(name = "estoque_minimo", nullable = false)
    private int estoqueMinimo;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    @PreUpdate
    void marcarAtualizacao() {
        updatedAt = OffsetDateTime.now();
    }
}
