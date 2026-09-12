package br.com.keila.modules.produto.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidade JPA para a tabela `cores` (V2__create_produtos.sql). Dado de referência, pouco mutável. */
@Entity
@Table(name = "cores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String nome;

    @Column(name = "hex_code", length = 7)
    private String hexCode;
}
