package br.com.keila.modules.fornecedor.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

/** Entidade JPA para a tabela `fornecedores` (V6__create_fornecedores_compras.sql). */
@Entity
@Table(name = "fornecedores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fornecedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "razao_social", nullable = false, length = 120)
    private String razaoSocial;

    @Column(length = 18)
    private String cnpj;

    @Column(length = 20)
    private String telefone;

    @Column(length = 120)
    private String email;

    @Column(length = 200)
    private String endereco;

    @Column(length = 80)
    private String contato;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @Column(nullable = false)
    private boolean ativo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}
