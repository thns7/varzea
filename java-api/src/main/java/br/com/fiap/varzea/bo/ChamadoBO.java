package br.com.fiap.varzea.bo;

import br.com.fiap.varzea.dao.CampeonatoDao;
import br.com.fiap.varzea.dao.CatalogoDao;
import br.com.fiap.varzea.dao.ChamadoDao;
import br.com.fiap.varzea.dao.ConfirmacaoDao;
import br.com.fiap.varzea.dto.Requisicoes.AtualizaChamado;
import br.com.fiap.varzea.dto.Requisicoes.NovoChamado;
import br.com.fiap.varzea.dto.Requisicoes.Voto;
import br.com.fiap.varzea.dto.Respostas.ResultadoVoto;
import br.com.fiap.varzea.exception.NaoEncontradoException;
import br.com.fiap.varzea.exception.RegraNegocioException;
import br.com.fiap.varzea.model.Chamado;
import br.com.fiap.varzea.model.Confirmacao;
import br.com.fiap.varzea.model.Morador;
import br.com.fiap.varzea.model.ResultadoVar;
import br.com.fiap.varzea.model.Rodada;
import br.com.fiap.varzea.model.Servico;
import br.com.fiap.varzea.model.StatusChamado;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/** Regras do jogo: abertura de chamados, marcação como resolvido, VAR da torcida e gols. */
@Service
public class ChamadoBO {

    /** Votos necessários para validar o gol ou reabrir o chamado. */
    static final int VOTOS_NECESSARIOS = 2;

    private final ChamadoDao chamadoDao;
    private final ConfirmacaoDao confirmacaoDao;
    private final CampeonatoDao campeonatoDao;
    private final CatalogoDao catalogoDao;
    private final MoradorBO moradorBO;

    public ChamadoBO(ChamadoDao chamadoDao, ConfirmacaoDao confirmacaoDao, CampeonatoDao campeonatoDao,
                     CatalogoDao catalogoDao, MoradorBO moradorBO) {
        this.chamadoDao = chamadoDao;
        this.confirmacaoDao = confirmacaoDao;
        this.campeonatoDao = campeonatoDao;
        this.catalogoDao = catalogoDao;
        this.moradorBO = moradorBO;
    }

    public List<Chamado> listar(Integer distritoId, StatusChamado status) {
        return chamadoDao.listar(distritoId, status);
    }

    public Chamado buscar(long id) {
        return chamadoDao.buscar(id).orElseThrow(() -> new NaoEncontradoException("Chamado não encontrado."));
    }

    public List<Confirmacao> listarConfirmacoes(long id) {
        buscar(id);
        return confirmacaoDao.listarPorChamado(id);
    }

    /** O chamado sempre pertence ao distrito do morador que o abriu. */
    @Transactional
    public Chamado criar(NovoChamado dados) {
        Morador morador = buscarMoradorDaRequisicao(dados.moradorId());
        exigirServico(dados.servicoId());
        long id = chamadoDao.inserir(dados.servicoId(), morador.getDistritoId(), morador.getId(), dados.referencia().trim(), LocalDate.now());
        return buscar(id);
    }

    @Transactional
    public Chamado atualizar(long id, AtualizaChamado dados) {
        exigirAberto(buscar(id), "Só um chamado aberto pode ser editado.");
        exigirServico(dados.servicoId());
        chamadoDao.atualizar(id, dados.servicoId(), dados.referencia().trim());
        return buscar(id);
    }

    @Transactional
    public void excluir(long id) {
        exigirAberto(buscar(id), "Só um chamado aberto pode ser excluído.");
        chamadoDao.excluir(id);
    }

    @Transactional
    public Chamado marcarResolvido(long id, long moradorId) {
        travar(id);
        Chamado chamado = buscar(id);
        exigirAberto(chamado, "Só um chamado aberto pode ser marcado como resolvido.");
        Morador morador = buscarMoradorDaRequisicao(moradorId);
        exigirMesmoDistrito(chamado, morador, "Só moradores deste distrito podem marcar o chamado.");
        chamadoDao.marcarResolvido(id, moradorId, LocalDate.now());
        return buscar(id);
    }

    /** Dois votos CONFIRMA geram o gol, e dois votos CONTESTA reabrem o chamado. */
    @Transactional
    public ResultadoVoto votar(long id, Voto voto) {
        travar(id);
        Chamado chamado = buscar(id);
        if (chamado.getStatus() != StatusChamado.RESOLVIDO || chamado.getVirouGol()) {
            throw new RegraNegocioException("Este chamado não está aguardando o VAR.");
        }
        Morador morador = buscarMoradorDaRequisicao(voto.moradorId());
        exigirMesmoDistrito(chamado, morador, "Só vizinhos deste distrito podem votar.");
        if (morador.getId().equals(chamado.getMoradorId()) || morador.getId().equals(chamado.getMoradorResolveuId())) {
            throw new RegraNegocioException("Quem relatou ou marcou como resolvido não pode votar neste chamado.");
        }
        List<Confirmacao> votos = confirmacaoDao.listarPorChamado(id);
        if (votos.stream().anyMatch(v -> v.getMoradorId().equals(morador.getId()))) {
            throw new RegraNegocioException("Você já votou neste chamado.");
        }

        LocalDate hoje = LocalDate.now();
        confirmacaoDao.inserir(id, morador.getId(), voto.resultado(), voto.fotoUrl(), hoje);

        long confirmacoes = contar(votos, ResultadoVar.CONFIRMA) + (voto.resultado() == ResultadoVar.CONFIRMA ? 1 : 0);
        long contestacoes = contar(votos, ResultadoVar.CONTESTA) + (voto.resultado() == ResultadoVar.CONTESTA ? 1 : 0);

        if (confirmacoes >= VOTOS_NECESSARIOS && confirmacoes > contestacoes) {
            Rodada rodada = campeonatoDao.rodadaEm(hoje)
                    .orElseThrow(() -> new RegraNegocioException("O campeonato ainda não começou."));
            campeonatoDao.inserirGol(id, rodada.getId(), chamado.getGols(), hoje);
            return new ResultadoVoto("GOL", confirmacoes, contestacoes, chamado.getGols());
        }
        if (contestacoes >= VOTOS_NECESSARIOS) {
            confirmacaoDao.excluirPorChamado(id);
            chamadoDao.reabrir(id);
            return new ResultadoVoto("ANULADO", confirmacoes, contestacoes, null);
        }
        return new ResultadoVoto("AGUARDANDO", confirmacoes, contestacoes, null);
    }

    private long contar(List<Confirmacao> votos, ResultadoVar resultado) {
        return votos.stream().filter(v -> v.getResultado() == resultado).count();
    }

    private void travar(long id) {
        if (!chamadoDao.travar(id)) {
            throw new NaoEncontradoException("Chamado não encontrado.");
        }
    }

    private void exigirAberto(Chamado chamado, String mensagem) {
        boolean aberto = chamado.getStatus() == StatusChamado.ABERTO || chamado.getStatus() == StatusChamado.EM_ANDAMENTO;
        if (!aberto) {
            throw new RegraNegocioException(mensagem);
        }
    }

    private void exigirMesmoDistrito(Chamado chamado, Morador morador, String mensagem) {
        if (!morador.getDistritoId().equals(chamado.getDistritoId())) {
            throw new RegraNegocioException(mensagem);
        }
    }

    private void exigirServico(int servicoId) {
        Servico servico = catalogoDao.buscarServico(servicoId).orElse(null);
        if (servico == null) {
            throw new RegraNegocioException("Serviço não encontrado.");
        }
    }

    private Morador buscarMoradorDaRequisicao(long moradorId) {
        try {
            return moradorBO.buscar(moradorId);
        } catch (NaoEncontradoException e) {
            throw new RegraNegocioException("Morador não encontrado.");
        }
    }
}
