package br.com.fiap.varzea.exception;

/** E-mail ou senha incorretos no login. Vira HTTP 401. */
public class CredenciaisInvalidasException extends RuntimeException {

    public CredenciaisInvalidasException() {
        super("Credenciais inválidas.");
    }
}
