package br.com.keila.modules.caixa.model;

import br.com.keila.modules.loja.model.Loja;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidade JPA para a tabela `caixas` (V5__create_caixa_vendas.sql). */
@Entity
@Table(name = "caixas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Caixa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loja_id", nullable = false)
    private Loja loja;

    @Column(nullable = false, length = 60)
    private String nome;

    @Column(nullable = false)
    private boolean ativo;
}
