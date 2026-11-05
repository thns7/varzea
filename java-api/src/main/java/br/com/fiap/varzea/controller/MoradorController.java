package br.com.fiap.varzea.controller;

import br.com.fiap.varzea.bo.MoradorBO;
import br.com.fiap.varzea.dto.Requisicoes.AtualizaMorador;
import br.com.fiap.varzea.dto.Requisicoes.NovoMorador;
import br.com.fiap.varzea.model.Morador;
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
@RequestMapping("/api/moradores")
public class MoradorController {

    private final MoradorBO bo;

    public MoradorController(MoradorBO bo) {
        this.bo = bo;
    }

    @GetMapping
    public List<Morador> listar(@RequestParam(required = false) Integer distritoId) {
        return bo.listar(distritoId);
    }

    @GetMapping("/{id}")
    public Morador buscar(@PathVariable long id) {
        return bo.buscar(id);
    }

    @PostMapping
    public ResponseEntity<Morador> criar(@Valid @RequestBody NovoMorador dados) {
        Morador criado = bo.criar(dados);
        return ResponseEntity.created(URI.create("/api/moradores/" + criado.getId())).body(criado);
    }

    @PutMapping("/{id}")
    public Morador atualizar(@PathVariable long id, @Valid @RequestBody AtualizaMorador dados) {
        return bo.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable long id) {
        bo.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
