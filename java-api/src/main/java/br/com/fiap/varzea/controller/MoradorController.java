package br.com.fiap.varzea.controller;

import br.com.fiap.varzea.bo.MoradorBO;
import br.com.fiap.varzea.dto.Requisicoes.AtualizaMorador;
import br.com.fiap.varzea.dto.Requisicoes.NovoMorador;
import br.com.fiap.varzea.model.Morador;
import br.com.fiap.varzea.security.UsuarioLogado;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/moradores")
public class MoradorController {

    private final MoradorBO moradorBO;

    public MoradorController(MoradorBO moradorBO) {
        this.moradorBO = moradorBO;
    }

    /** Lista com e-mails: só para ADMIN. */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Morador> listar(@RequestParam(required = false) Integer distritoId) {
        return moradorBO.listar(distritoId);
    }

    @GetMapping("/eu")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public Morador eu() {
        return moradorBO.buscar(UsuarioLogado.id());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public Morador buscar(@PathVariable Long id) {
        exigirProprioOuAdmin(id);
        return moradorBO.buscar(id);
    }

    /** Cadastro público: todo novo morador entra com perfil USER. */
    @PostMapping
    public ResponseEntity<Morador> criar(@RequestBody @Valid NovoMorador dados) {
        Morador criado = moradorBO.criar(dados);
        return ResponseEntity.created(URI.create("/api/moradores/" + criado.getId())).body(criado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public Morador atualizar(@PathVariable Long id, @RequestBody @Valid AtualizaMorador dados) {
        exigirProprioOuAdmin(id);
        return moradorBO.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        moradorBO.excluir(id);
        return ResponseEntity.noContent().build();
    }

    private void exigirProprioOuAdmin(Long id) {
        if (!UsuarioLogado.isAdmin() && !id.equals(UsuarioLogado.id())) {
            throw new AccessDeniedException("Acesso apenas aos próprios dados.");
        }
    }
}
