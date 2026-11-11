package br.com.fiap.varzea.bo;

import br.com.fiap.varzea.dao.ChamadoDao;
import br.com.fiap.varzea.dao.ConfirmacaoDao;
import br.com.fiap.varzea.dao.GolDao;
import br.com.fiap.varzea.dao.RodadaDao;
import br.com.fiap.varzea.dao.ServicoDao;
import br.com.fiap.varzea.dto.Requisicoes.NovoChamado;
import br.com.fiap.varzea.dto.Requisicoes.Voto;
import br.com.fiap.varzea.dto.Respostas.ResultadoVoto;
import br.com.fiap.varzea.exception.NaoEncontradoException;
import br.com.fiap.varzea.exception.RegraNegocioException;
import br.com.fiap.varzea.model.Chamado;
import br.com.fiap.varzea.model.Confirmacao;
import br.com.fiap.varzea.model.Gol;
import br.com.fiap.varzea.model.Morador;
import br.com.fiap.varzea.model.ResultadoVar;
import br.com.fiap.varzea.model.Rodada;
import br.com.fiap.varzea.model.Servico;
import br.com.fiap.varzea.model.StatusChamado;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/** Regras do jogo: abertura de chamados, marcação como resolvido, VAR da torcida e gols. */
@Service
public class ChamadoBO {

    /** Votos necessários para validar o gol ou reabrir o chamado. */
    static final int VOTOS_NECESSARIOS = 2;

    private final ChamadoDao chamadoDao;
    private final ConfirmacaoDao confirmacaoDao;
    private final GolDao golDao;
    private final RodadaDao rodadaDao;
    private final ServicoDao servicoDao;
    private final MoradorBO moradorBO;

    public ChamadoBO(ChamadoDao chamadoDao, ConfirmacaoDao confirmacaoDao, GolDao golDao, RodadaDao rodadaDao,
                     ServicoDao servicoDao, MoradorBO moradorBO) {
        this.chamadoDao = chamadoDao;
        this.confirmacaoDao = confirmacaoDao;
        this.golDao = golDao;
        this.rodadaDao = rodadaDao;
        this.servicoDao = servicoDao;
        this.moradorBO = moradorBO;
    }

    @Transactional(readOnly = true)
    public List<Chamado> listar(Integer distritoId, StatusChamado status) {
        List<Chamado> chamados = chamadoDao.filtrar(distritoId, status, PageRequest.of(0, 100));
        chamados.forEach(this::marcarGol);
        return chamados;
    }

    @Transactional(readOnly = true)
    public Chamado buscar(Long id) {
        return marcarGol(chamadoDao.findById(id).orElseThrow(() -> new NaoEncontradoException("Chamado não encontrado.")));
    }

    @Transactional(readOnly = true)
    public List<Confirmacao> listarConfirmacoes(Long id) {
        buscar(id);
        return confirmacaoDao.findByChamado_IdOrderById(id);
    }

    /** O chamado sempre pertence ao distrito de quem o abriu. */
    @Transactional
    public Chamado criar(NovoChamado dados, Long moradorId) {
        Morador morador = moradorBO.buscar(moradorId);
        Chamado chamado = new Chamado();
        chamado.setServico(buscarServico(dados.servicoId()));
        chamado.setDistrito(morador.getDistrito());
        chamado.setMorador(morador);
        chamado.setReferencia(dados.referencia().trim());
        return marcarGol(chamadoDao.save(chamado));
    }

    /** Só o autor do chamado ou um ADMIN podem editá-lo, e só enquanto estiver aberto. */
    @Transactional
    public Chamado atualizar(Long id, NovoChamado dados, Long moradorId, boolean admin) {
        Chamado chamado = buscar(id);
        exigirAutorOuAdmin(chamado, moradorId, admin);
        exigirAberto(chamado, "Só um chamado aberto pode ser editado.");
        chamado.setServico(buscarServico(dados.servicoId()));
        chamado.setReferencia(dados.referencia().trim());
        return marcarGol(chamadoDao.save(chamado));
    }

    @Transactional
    public void excluir(Long id, Long moradorId, boolean admin) {
        Chamado chamado = buscar(id);
        exigirAutorOuAdmin(chamado, moradorId, admin);
        exigirAberto(chamado, "Só um chamado aberto pode ser excluído.");
        chamadoDao.delete(chamado);
    }

    @Transactional
    public Chamado marcarResolvido(Long id, Long moradorId) {
        Chamado chamado = travar(id);
        exigirAberto(chamado, "Só um chamado aberto pode ser marcado como resolvido.");
        Morador morador = moradorBO.buscar(moradorId);
        exigirMesmoDistrito(chamado, morador, "Só moradores deste distrito podem marcar o chamado.");
        chamado.setStatus(StatusChamado.RESOLVIDO);
        chamado.setDataResolucao(LocalDate.now());
        chamado.setMoradorResolveu(morador);
        return marcarGol(chamadoDao.save(chamado));
    }

    /** Dois votos CONFIRMA geram o gol, e dois votos CONTESTA reabrem o chamado. */
    @Transactional
    public ResultadoVoto votar(Long id, Voto voto, Long moradorId) {
        Chamado chamado = travar(id);
        if (chamado.getStatus() != StatusChamado.RESOLVIDO || golDao.existsByChamado_Id(id)) {
            throw new RegraNegocioException("Este chamado não está aguardando o VAR.");
        }
        Morador morador = moradorBO.buscar(moradorId);
        exigirMesmoDistrito(chamado, morador, "Só vizinhos deste distrito podem votar.");
        if (morador.getId().equals(chamado.getMoradorId()) || morador.getId().equals(chamado.getMoradorResolveuId())) {
            throw new RegraNegocioException("Quem relatou ou marcou como resolvido não pode votar neste chamado.");
        }
        if (confirmacaoDao.existsByChamado_IdAndMorador_Id(id, morador.getId())) {
            throw new RegraNegocioException("Você já votou neste chamado.");
        }

        LocalDate hoje = LocalDate.now();
        Confirmacao confirmacao = new Confirmacao();
        confirmacao.setChamado(chamado);
        confirmacao.setMorador(morador);
        confirmacao.setResultado(voto.resultado());
        confirmacao.setFotoUrl(voto.fotoUrl());
        confirmacao.setData(hoje);
        confirmacaoDao.save(confirmacao);

        List<Confirmacao> votos = confirmacaoDao.findByChamado_IdOrderById(id);
        long confirmacoes = votos.stream().filter(v -> v.getResultado() == ResultadoVar.CONFIRMA).count();
        long contestacoes = votos.size() - confirmacoes;

        if (confirmacoes >= VOTOS_NECESSARIOS && confirmacoes > contestacoes) {
            Rodada rodada = rodadaDao.findFirstByInicioLessThanEqualOrderByNumeroDesc(hoje)
                    .orElseThrow(() -> new RegraNegocioException("O campeonato ainda não começou."));
            Gol gol = new Gol();
            gol.setChamado(chamado);
            gol.setRodada(rodada);
            gol.setValor(chamado.getServico().getGols());
            gol.setDataValidacao(hoje);
            golDao.save(gol);
            return new ResultadoVoto("GOL", confirmacoes, contestacoes, gol.getValor());
        }
        if (contestacoes >= VOTOS_NECESSARIOS) {
            confirmacaoDao.deleteByChamado_Id(id);
            chamado.setStatus(StatusChamado.ABERTO);
            chamado.setDataResolucao(null);
            chamado.setMoradorResolveu(null);
            chamadoDao.save(chamado);
            return new ResultadoVoto("ANULADO", confirmacoes, contestacoes, null);
        }
        return new ResultadoVoto("AGUARDANDO", confirmacoes, contestacoes, null);
    }

    private Chamado travar(Long id) {
        return chamadoDao.travar(id).orElseThrow(() -> new NaoEncontradoException("Chamado não encontrado."));
    }

    private Chamado marcarGol(Chamado chamado) {
        chamado.setVirouGol(golDao.existsByChamado_Id(chamado.getId()));
        return chamado;
    }

    private Servico buscarServico(Integer servicoId) {
        return servicoDao.findById(servicoId).orElseThrow(() -> new RegraNegocioException("Serviço não encontrado."));
    }

    private void exigirAutorOuAdmin(Chamado chamado, Long moradorId, boolean admin) {
        if (!admin && !Objects.equals(chamado.getMoradorId(), moradorId)) {
            throw new AccessDeniedException("Só o autor ou um administrador podem alterar este chamado.");
        }
    }

    private void exigirAberto(Chamado chamado, String mensagem) {
        if (chamado.getStatus() != StatusChamado.ABERTO && chamado.getStatus() != StatusChamado.EM_ANDAMENTO) {
            throw new RegraNegocioException(mensagem);
        }
    }

    private void exigirMesmoDistrito(Chamado chamado, Morador morador, String mensagem) {
        if (!morador.getDistrito().getId().equals(chamado.getDistrito().getId())) {
            throw new RegraNegocioException(mensagem);
        }
    }
}
