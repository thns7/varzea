package br.com.fiap.varzea.controller;

import br.com.fiap.varzea.bo.CampeonatoBO;
import br.com.fiap.varzea.dto.Respostas.LinhaClassificacao;
import br.com.fiap.varzea.dto.Respostas.Status;
import br.com.fiap.varzea.model.Categoria;
import br.com.fiap.varzea.model.Distrito;
import br.com.fiap.varzea.model.Gol;
import br.com.fiap.varzea.model.Partida;
import br.com.fiap.varzea.model.Rodada;
import br.com.fiap.varzea.model.Servico;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CampeonatoController {

    private final CampeonatoBO bo;
    private final JdbcTemplate jdbc;

    public CampeonatoController(CampeonatoBO bo, JdbcTemplate jdbc) {
        this.bo = bo;
        this.jdbc = jdbc;
    }

    /** Sempre responde 200, para mostrar se o problema está na API ou no banco. */
    @GetMapping("/status")
    public Status status() {
        try {
            jdbc.queryForObject("SELECT 1 FROM dual", Integer.class);
            return new Status("no ar", "conectado");
        } catch (DataAccessException e) {
            return new Status("no ar", "indisponível");
        }
    }

    @GetMapping("/distritos")
    public List<Distrito> distritos() {
        return bo.listarDistritos();
    }

    @GetMapping("/categorias")
    public List<Categoria> categorias() {
        return bo.listarCategorias();
    }

    @GetMapping("/servicos")
    public List<Servico> servicos() {
        return bo.listarServicos();
    }

    @GetMapping("/servicos/{id}")
    public Servico servico(@PathVariable int id) {
        return bo.buscarServico(id);
    }

    @GetMapping("/rodadas")
    public List<Rodada> rodadas() {
        return bo.listarRodadas();
    }

    @GetMapping("/rodadas/{id}/partidas")
    public List<Partida> partidas(@PathVariable int id) {
        return bo.listarPartidas(id);
    }

    @GetMapping("/gols")
    public List<Gol> gols() {
        return bo.listarGols();
    }

    @GetMapping("/classificacao")
    public List<LinhaClassificacao> classificacao() {
        return bo.classificacao();
    }
}
