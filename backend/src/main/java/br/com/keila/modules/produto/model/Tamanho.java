package br.com.keila.modules.produto.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Entidade JPA para a tabela `tamanhos` (V2__create_produtos.sql). Dado de referência, pouco mutável. */
@Entity
@Table(name = "tamanhos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tamanho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String valor;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "tipo_tamanho")
    private TipoTamanho tipo;

    @Column
    private Integer ordem;
}
