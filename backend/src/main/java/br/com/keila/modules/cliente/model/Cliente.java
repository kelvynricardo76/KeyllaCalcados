package br.com.keila.modules.cliente.model;

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

/** Entidade JPA para a tabela `clientes` (V4__create_clientes_fiado.sql). */
@Entity
@Table(name = "clientes")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(length = 14)
    private String cpf;

    @Column(length = 18)
    private String cnpj;

    @Column(length = 20)
    private String telefone;

    @Column(length = 120)
    private String email;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    @Column(length = 9)
    private String cep;

    @Column(length = 120)
    private String logradouro;

    @Column(length = 10)
    private String numero;

    @Column(length = 60)
    private String complemento;

    @Column(length = 80)
    private String bairro;

    @Column(length = 80)
    private String cidade;

    @Column(length = 2)
    private String uf;

    @Column(name = "limite_fiado", precision = 12, scale = 2)
    private BigDecimal limiteFiado;

    @Lob
    private String observacoes;

    @Column(nullable = false)
    private boolean ativo;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
