package br.com.fiap.varzea.bo;

import br.com.fiap.varzea.dao.CategoriaDao;
import br.com.fiap.varzea.dao.DistritoDao;
import br.com.fiap.varzea.dao.GolDao;
import br.com.fiap.varzea.dao.PartidaDao;
import br.com.fiap.varzea.dao.RodadaDao;
import br.com.fiap.varzea.dao.ServicoDao;
import br.com.fiap.varzea.dto.Respostas.LinhaClassificacao;
import br.com.fiap.varzea.exception.NaoEncontradoException;
import br.com.fiap.varzea.model.Categoria;
import br.com.fiap.varzea.model.Distrito;
import br.com.fiap.varzea.model.Gol;
import br.com.fiap.varzea.model.Partida;
import br.com.fiap.varzea.model.Rodada;
import br.com.fiap.varzea.model.Servico;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/** Consultas do campeonato e dos catálogos. */
@Service
@Transactional(readOnly = true)
public class CampeonatoBO {

    private final DistritoDao distritoDao;
    private final CategoriaDao categoriaDao;
    private final ServicoDao servicoDao;
    private final RodadaDao rodadaDao;
    private final PartidaDao partidaDao;
    private final GolDao golDao;

    public CampeonatoBO(DistritoDao distritoDao, CategoriaDao categoriaDao, ServicoDao servicoDao,
                        RodadaDao rodadaDao, PartidaDao partidaDao, GolDao golDao) {
        this.distritoDao = distritoDao;
        this.categoriaDao = categoriaDao;
        this.servicoDao = servicoDao;
        this.rodadaDao = rodadaDao;
        this.partidaDao = partidaDao;
        this.golDao = golDao;
    }

    public List<Distrito> listarDistritos() {
        return distritoDao.findAllByOrderByNome();
    }

    public List<Categoria> listarCategorias() {
        return categoriaDao.findAllByOrderByNome();
    }

    public List<Servico> listarServicos() {
        return servicoDao.findAllByOrderByGolsDescNomeAsc();
    }

    public Servico buscarServico(Integer id) {
        return servicoDao.findById(id).orElseThrow(() -> new NaoEncontradoException("Serviço não encontrado."));
    }

    public List<Rodada> listarRodadas() {
        return rodadaDao.findAllByOrderByNumero();
    }

    /** O placar de cada partida é a soma dos gols de cada distrito na rodada. */
    public List<Partida> listarPartidas(Integer rodadaId) {
        if (!rodadaDao.existsById(rodadaId)) {
            throw new NaoEncontradoException("Rodada não encontrada.");
        }
        List<Partida> partidas = partidaDao.findByRodada_IdOrderById(rodadaId);
        for (Partida p : partidas) {
            p.setGolsCasa(golDao.somarGols(rodadaId, p.getCasa().getId()));
            p.setGolsFora(golDao.somarGols(rodadaId, p.getFora().getId()));
        }
        return partidas;
    }

    public List<Gol> listarGols() {
        return golDao.findAllByOrderByDataValidacaoDescIdDesc();
    }

    public List<LinhaClassificacao> classificacao() {
        List<LinhaClassificacao> linhas = new ArrayList<>();
        for (Object[] l : golDao.classificacao()) {
            linhas.add(new LinhaClassificacao(linhas.size() + 1, ((Number) l[0]).intValue(), (String) l[1], (String) l[2],
                    ((Number) l[3]).longValue(), ((Number) l[4]).longValue()));
        }
        return linhas;
    }
}
