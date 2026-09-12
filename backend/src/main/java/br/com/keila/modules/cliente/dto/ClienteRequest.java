package br.com.keila.modules.cliente.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ClienteRequest(
        @NotBlank @Size(max = 120) String nome,
        String cpf,
        String cnpj,
        String telefone,
        String email,
        LocalDate dataNascimento,
        String cep,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String uf,
        BigDecimal limiteFiado,
        String observacoes
) {
}
