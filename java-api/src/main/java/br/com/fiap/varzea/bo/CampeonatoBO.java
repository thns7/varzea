package br.com.fiap.varzea.bo;

import br.com.fiap.varzea.dao.CampeonatoDao;
import br.com.fiap.varzea.dao.CatalogoDao;
import br.com.fiap.varzea.dto.Respostas.LinhaClassificacao;
import br.com.fiap.varzea.exception.NaoEncontradoException;
import br.com.fiap.varzea.model.Categoria;
import br.com.fiap.varzea.model.Distrito;
import br.com.fiap.varzea.model.Gol;
import br.com.fiap.varzea.model.Partida;
import br.com.fiap.varzea.model.Rodada;
import br.com.fiap.varzea.model.Servico;
import org.springframework.stereotype.Service;

import java.util.List;

/** Consultas do campeonato e dos catálogos. */
@Service
public class CampeonatoBO {

    private final CampeonatoDao campeonatoDao;
    private final CatalogoDao catalogoDao;

    public CampeonatoBO(CampeonatoDao campeonatoDao, CatalogoDao catalogoDao) {
        this.campeonatoDao = campeonatoDao;
        this.catalogoDao = catalogoDao;
    }

    public List<Distrito> listarDistritos() {
        return catalogoDao.listarDistritos();
    }

    public List<Categoria> listarCategorias() {
        return catalogoDao.listarCategorias();
    }

    public List<Servico> listarServicos() {
        return catalogoDao.listarServicos();
    }

    public Servico buscarServico(int id) {
        return catalogoDao.buscarServico(id).orElseThrow(() -> new NaoEncontradoException("Serviço não encontrado."));
    }

    public List<Rodada> listarRodadas() {
        return campeonatoDao.listarRodadas();
    }

    public List<Partida> listarPartidas(int rodadaId) {
        return campeonatoDao.listarPartidas(rodadaId);
    }

    public List<Gol> listarGols() {
        return campeonatoDao.listarGols();
    }

    public List<LinhaClassificacao> classificacao() {
        return campeonatoDao.classificacao();
    }
}
