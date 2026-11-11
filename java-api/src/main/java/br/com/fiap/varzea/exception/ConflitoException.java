package br.com.fiap.varzea.exception;

/** A operação conflita com dados já registrados, como e-mail repetido. Vira HTTP 409. */
public class ConflitoException extends RuntimeException {

    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
