package br.com.keila.modules.fornecedor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FornecedorRequest(
        @NotBlank @Size(max = 120) String razaoSocial,
        String cnpj,
        String telefone,
        String email,
        String endereco,
        String contato,
        String observacoes
) {
}
