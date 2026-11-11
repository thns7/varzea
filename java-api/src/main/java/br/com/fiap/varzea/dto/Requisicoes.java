package br.com.fiap.varzea.dto;

import br.com.fiap.varzea.model.ResultadoVar;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Corpos das requisições, validados com Bean Validation.
 * Usar registros próprios evita que o cliente envie campos que não deve controlar, como perfil e status.
 */
public final class Requisicoes {

    private Requisicoes() {
    }

    public record Login(
            @NotBlank(message = "Informe o e-mail.") String email,
            @NotBlank(message = "Informe a senha.") String senha) {
    }

    public record NovoMorador(
            @NotNull(message = "Informe o distrito.") Integer distritoId,
            @NotBlank(message = "Informe o nome.") @Size(min = 2, max = 80, message = "O nome deve ter de 2 a 80 caracteres.") String nome,
            @NotBlank(message = "Informe o e-mail.") @Email(message = "E-mail inválido.") @Size(max = 120, message = "E-mail muito longo.") String email,
            @NotBlank(message = "Informe a senha.") @Size(min = 8, max = 60, message = "A senha deve ter de 8 a 60 caracteres.") String senha) {
    }

    public record AtualizaMorador(
            @NotBlank(message = "Informe o nome.") @Size(min = 2, max = 80, message = "O nome deve ter de 2 a 80 caracteres.") String nome,
            @NotBlank(message = "Informe o e-mail.") @Email(message = "E-mail inválido.") @Size(max = 120, message = "E-mail muito longo.") String email) {
    }

    public record NovoChamado(
            @NotNull(message = "Informe o serviço.") Integer servicoId,
            @NotBlank(message = "Informe uma referência.") @Size(min = 5, max = 120, message = "A referência deve ter de 5 a 120 caracteres.") String referencia) {
    }

    public record Voto(
            @NotNull(message = "Informe CONFIRMA ou CONTESTA.") ResultadoVar resultado,
            @NotBlank(message = "A foto é obrigatória.") @Size(max = 200, message = "Endereço da foto muito longo.")
            @Pattern(regexp = "^https://\\S+$", message = "A foto deve ser um endereço https.") String fotoUrl) {
    }
}
