package br.com.keila.shared.exception;

/** Violação de uma regra de negócio (ex.: SKU duplicado). Mapeada para HTTP 409. */
public class RegraNegocioException extends RuntimeException {
    public RegraNegocioException(String message) {
        super(message);
    }
}
