package br.com.fiap.varzea.bo;

import br.com.fiap.varzea.dao.CatalogoDao;
import br.com.fiap.varzea.dao.MoradorDao;
import br.com.fiap.varzea.dto.Requisicoes.AtualizaMorador;
import br.com.fiap.varzea.dto.Requisicoes.NovoMorador;
import br.com.fiap.varzea.exception.NaoEncontradoException;
import br.com.fiap.varzea.exception.RegraNegocioException;
import br.com.fiap.varzea.model.Morador;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/** Regras de negócio dos moradores. O time não muda depois de escolhido. */
@Service
public class MoradorBO {

    private final MoradorDao moradorDao;
    private final CatalogoDao catalogoDao;

    public MoradorBO(MoradorDao moradorDao, CatalogoDao catalogoDao) {
        this.moradorDao = moradorDao;
        this.catalogoDao = catalogoDao;
    }

    public List<Morador> listar(Integer distritoId) {
        return moradorDao.listar(distritoId);
    }

    public Morador buscar(long id) {
        return moradorDao.buscar(id).orElseThrow(() -> new NaoEncontradoException("Morador não encontrado."));
    }

    public Morador criar(NovoMorador dados) {
        if (!catalogoDao.distritoExiste(dados.distritoId())) {
            throw new RegraNegocioException("Distrito não encontrado.");
        }
        long id = moradorDao.inserir(dados.distritoId(), dados.nome().trim(), dados.email().trim().toLowerCase(), LocalDate.now());
        return buscar(id);
    }

    public Morador atualizar(long id, AtualizaMorador dados) {
        buscar(id);
        moradorDao.atualizar(id, dados.nome().trim(), dados.email().trim().toLowerCase());
        return buscar(id);
    }

    public void excluir(long id) {
        buscar(id);
        moradorDao.excluir(id);
    }
}
