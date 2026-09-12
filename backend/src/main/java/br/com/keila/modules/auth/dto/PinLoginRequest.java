package br.com.keila.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PinLoginRequest(
        @NotBlank @Pattern(regexp = "\\d{4,6}", message = "PIN deve ter entre 4 e 6 dígitos") String pin
) {
}
