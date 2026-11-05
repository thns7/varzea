package br.com.fiap.varzea.exception;

/** O recurso pedido não existe. Vira HTTP 404. */
public class NaoEncontradoException extends RuntimeException {

    public NaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
