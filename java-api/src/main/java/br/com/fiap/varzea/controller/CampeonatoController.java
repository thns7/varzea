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

/** Consultas públicas do campeonato. */
@RestController
@RequestMapping("/api")
public class CampeonatoController {

    private final CampeonatoBO campeonatoBO;
    private final JdbcTemplate jdbc;

    public CampeonatoController(CampeonatoBO campeonatoBO, JdbcTemplate jdbc) {
        this.campeonatoBO = campeonatoBO;
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
        return campeonatoBO.listarDistritos();
    }

    @GetMapping("/categorias")
    public List<Categoria> categorias() {
        return campeonatoBO.listarCategorias();
    }

    @GetMapping("/servicos")
    public List<Servico> servicos() {
        return campeonatoBO.listarServicos();
    }

    @GetMapping("/servicos/{id}")
    public Servico servico(@PathVariable Integer id) {
        return campeonatoBO.buscarServico(id);
    }

    @GetMapping("/rodadas")
    public List<Rodada> rodadas() {
        return campeonatoBO.listarRodadas();
    }

    @GetMapping("/rodadas/{id}/partidas")
    public List<Partida> partidas(@PathVariable Integer id) {
        return campeonatoBO.listarPartidas(id);
    }

    @GetMapping("/gols")
    public List<Gol> gols() {
        return campeonatoBO.listarGols();
    }

    @GetMapping("/classificacao")
    public List<LinhaClassificacao> classificacao() {
        return campeonatoBO.classificacao();
    }
}
