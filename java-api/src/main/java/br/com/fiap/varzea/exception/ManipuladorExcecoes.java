package br.com.fiap.varzea.exception;

import br.com.fiap.varzea.dto.Respostas.Erro;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

/** Camada de tratamento de exceções: toda falha vira uma resposta JSON clara, sem detalhes internos. */
@RestControllerAdvice
public class ManipuladorExcecoes {

    private static final Logger LOG = LoggerFactory.getLogger(ManipuladorExcecoes.class);

    private ResponseEntity<Erro> resposta(HttpStatus status, String mensagem, List<String> detalhes) {
        return ResponseEntity.status(status).body(new Erro(status.value(), mensagem, detalhes));
    }

    @ExceptionHandler(NaoEncontradoException.class)
    public ResponseEntity<Erro> naoEncontrado(NaoEncontradoException e) {
        return resposta(HttpStatus.NOT_FOUND, e.getMessage(), List.of());
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<Erro> regraNegocio(RegraNegocioException e) {
        return resposta(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage(), List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Erro> validacao(MethodArgumentNotValidException e) {
        List<String> detalhes = e.getBindingResult().getFieldErrors().stream()
                .map(campo -> campo.getField() + ": " + campo.getDefaultMessage())
                .toList();
        return resposta(HttpStatus.BAD_REQUEST, "Dados inválidos.", detalhes);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Erro> corpoIlegivel(HttpMessageNotReadableException e) {
        return resposta(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido.", List.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Erro> parametroInvalido(MethodArgumentTypeMismatchException e) {
        return resposta(HttpStatus.BAD_REQUEST, "Valor inválido para o parâmetro " + e.getName() + ".", List.of());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Erro> rotaInexistente(NoResourceFoundException e) {
        return resposta(HttpStatus.NOT_FOUND, "Rota não encontrada.", List.of());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Erro> metodoInvalido(HttpRequestMethodNotSupportedException e) {
        return resposta(HttpStatus.METHOD_NOT_ALLOWED, "Método não permitido nesta rota.", List.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Erro> integridade(DataIntegrityViolationException e) {
        LOG.warn("Violação de integridade: {}", e.getMostSpecificCause().getMessage());
        return resposta(HttpStatus.CONFLICT, "A operação conflita com dados já registrados.", List.of());
    }

    @ExceptionHandler({CannotGetJdbcConnectionException.class, DataAccessResourceFailureException.class, CannotAcquireLockException.class})
    public ResponseEntity<Erro> bancoIndisponivel(DataAccessException e) {
        LOG.error("Banco indisponível", e);
        return resposta(HttpStatus.SERVICE_UNAVAILABLE, "Banco de dados indisponível. Tente de novo em instantes.", List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Erro> inesperado(Exception e) {
        LOG.error("Erro inesperado", e);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno. Tente de novo.", List.of());
    }
}
