package br.com.fiap.varzea.controller;

import br.com.fiap.varzea.bo.ChamadoBO;
import br.com.fiap.varzea.dto.Requisicoes.AtualizaChamado;
import br.com.fiap.varzea.dto.Requisicoes.MarcaResolvido;
import br.com.fiap.varzea.dto.Requisicoes.NovoChamado;
import br.com.fiap.varzea.dto.Requisicoes.Voto;
import br.com.fiap.varzea.dto.Respostas.ResultadoVoto;
import br.com.fiap.varzea.model.Chamado;
import br.com.fiap.varzea.model.Confirmacao;
import br.com.fiap.varzea.model.StatusChamado;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/chamados")
public class ChamadoController {

    private final ChamadoBO bo;

    public ChamadoController(ChamadoBO bo) {
        this.bo = bo;
    }

    @GetMapping
    public List<Chamado> listar(@RequestParam(required = false) Integer distritoId,
                                @RequestParam(required = false) StatusChamado status) {
        return bo.listar(distritoId, status);
    }

    @GetMapping("/{id}")
    public Chamado buscar(@PathVariable long id) {
        return bo.buscar(id);
    }

    @PostMapping
    public ResponseEntity<Chamado> criar(@Valid @RequestBody NovoChamado dados) {
        Chamado criado = bo.criar(dados);
        return ResponseEntity.created(URI.create("/api/chamados/" + criado.getId())).body(criado);
    }

    @PutMapping("/{id}")
    public Chamado atualizar(@PathVariable long id, @Valid @RequestBody AtualizaChamado dados) {
        return bo.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable long id) {
        bo.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/resolver")
    public Chamado marcarResolvido(@PathVariable long id, @Valid @RequestBody MarcaResolvido dados) {
        return bo.marcarResolvido(id, dados.moradorId());
    }

    @GetMapping("/{id}/confirmacoes")
    public List<Confirmacao> listarConfirmacoes(@PathVariable long id) {
        return bo.listarConfirmacoes(id);
    }

    @PostMapping("/{id}/confirmacoes")
    public ResultadoVoto votar(@PathVariable long id, @Valid @RequestBody Voto voto) {
        return bo.votar(id, voto);
    }
}
