package br.com.fiap.varzea.exception;

/** A operação viola uma regra do jogo. Vira HTTP 422. */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
