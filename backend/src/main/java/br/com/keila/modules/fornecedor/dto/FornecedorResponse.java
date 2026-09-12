package br.com.keila.modules.fornecedor.dto;

public record FornecedorResponse(
        Long id,
        String razaoSocial,
        String cnpj,
        String telefone,
        String email,
        String endereco,
        String contato,
        String observacoes,
        boolean ativo
) {
}
