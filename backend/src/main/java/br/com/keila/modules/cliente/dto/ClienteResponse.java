package br.com.keila.modules.cliente.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ClienteResponse(
        Long id,
        String nome,
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
        String observacoes,
        boolean ativo
) {
}
