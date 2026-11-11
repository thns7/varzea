package br.com.fiap.varzea.controller;

import br.com.fiap.varzea.bo.ChamadoBO;
import br.com.fiap.varzea.dto.Requisicoes.NovoChamado;
import br.com.fiap.varzea.dto.Requisicoes.Voto;
import br.com.fiap.varzea.dto.Respostas.ResultadoVoto;
import br.com.fiap.varzea.model.Chamado;
import br.com.fiap.varzea.model.Confirmacao;
import br.com.fiap.varzea.model.StatusChamado;
import br.com.fiap.varzea.security.UsuarioLogado;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
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

/** Chamados e VAR. O morador de cada ação vem do token, e não do corpo da requisição. */
@RestController
@RequestMapping("/api/chamados")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class ChamadoController {

    private final ChamadoBO chamadoBO;

    public ChamadoController(ChamadoBO chamadoBO) {
        this.chamadoBO = chamadoBO;
    }

    @GetMapping
    public List<Chamado> listar(@RequestParam(required = false) Integer distritoId,
                                @RequestParam(required = false) StatusChamado status) {
        return chamadoBO.listar(distritoId, status);
    }

    @GetMapping("/{id}")
    public Chamado buscar(@PathVariable Long id) {
        return chamadoBO.buscar(id);
    }

    @PostMapping
    public ResponseEntity<Chamado> criar(@RequestBody @Valid NovoChamado dados) {
        Chamado criado = chamadoBO.criar(dados, UsuarioLogado.id());
        return ResponseEntity.created(URI.create("/api/chamados/" + criado.getId())).body(criado);
    }

    @PutMapping("/{id}")
    public Chamado atualizar(@PathVariable Long id, @RequestBody @Valid NovoChamado dados) {
        return chamadoBO.atualizar(id, dados, UsuarioLogado.id(), UsuarioLogado.isAdmin());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        chamadoBO.excluir(id, UsuarioLogado.id(), UsuarioLogado.isAdmin());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/resolver")
    public Chamado marcarResolvido(@PathVariable Long id) {
        return chamadoBO.marcarResolvido(id, UsuarioLogado.id());
    }

    @GetMapping("/{id}/confirmacoes")
    public List<Confirmacao> listarConfirmacoes(@PathVariable Long id) {
        return chamadoBO.listarConfirmacoes(id);
    }

    @PostMapping("/{id}/confirmacoes")
    public ResultadoVoto votar(@PathVariable Long id, @RequestBody @Valid Voto voto) {
        return chamadoBO.votar(id, voto, UsuarioLogado.id());
    }
}
