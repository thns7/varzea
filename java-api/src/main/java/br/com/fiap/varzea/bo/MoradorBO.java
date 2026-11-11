package br.com.fiap.varzea.bo;

import br.com.fiap.varzea.dao.ChamadoDao;
import br.com.fiap.varzea.dao.ConfirmacaoDao;
import br.com.fiap.varzea.dao.DistritoDao;
import br.com.fiap.varzea.dao.MoradorDao;
import br.com.fiap.varzea.dto.Requisicoes.AtualizaMorador;
import br.com.fiap.varzea.dto.Requisicoes.NovoMorador;
import br.com.fiap.varzea.exception.ConflitoException;
import br.com.fiap.varzea.exception.CredenciaisInvalidasException;
import br.com.fiap.varzea.exception.NaoEncontradoException;
import br.com.fiap.varzea.exception.RegraNegocioException;
import br.com.fiap.varzea.model.Morador;
import br.com.fiap.varzea.model.Perfil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Regras de negócio dos moradores: cadastro, login e manutenção. O time não muda depois de escolhido. */
@Service
public class MoradorBO {

    private final MoradorDao moradorDao;
    private final DistritoDao distritoDao;
    private final ChamadoDao chamadoDao;
    private final ConfirmacaoDao confirmacaoDao;
    private final PasswordEncoder passwordEncoder;

    public MoradorBO(MoradorDao moradorDao, DistritoDao distritoDao, ChamadoDao chamadoDao,
                     ConfirmacaoDao confirmacaoDao, PasswordEncoder passwordEncoder) {
        this.moradorDao = moradorDao;
        this.distritoDao = distritoDao;
        this.chamadoDao = chamadoDao;
        this.confirmacaoDao = confirmacaoDao;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Morador> listar(Integer distritoId) {
        return distritoId == null ? moradorDao.findAllByOrderByNome() : moradorDao.findByDistrito_IdOrderByNome(distritoId);
    }

    public Morador buscar(Long id) {
        return moradorDao.findById(id).orElseThrow(() -> new NaoEncontradoException("Morador não encontrado."));
    }

    /** Confere a senha com o hash BCrypt, sem nunca descriptografar nada. */
    public Morador autenticar(String email, String senha) {
        Morador morador = moradorDao.findByEmail(normalizar(email)).orElseThrow(CredenciaisInvalidasException::new);
        if (!passwordEncoder.matches(senha, morador.getSenha())) {
            throw new CredenciaisInvalidasException();
        }
        return morador;
    }

    @Transactional
    public Morador criar(NovoMorador dados) {
        String email = normalizar(dados.email());
        if (moradorDao.existsByEmail(email)) {
            throw new ConflitoException("Este e-mail já está cadastrado.");
        }
        Morador morador = new Morador();
        morador.setDistrito(distritoDao.findById(dados.distritoId())
                .orElseThrow(() -> new RegraNegocioException("Distrito não encontrado.")));
        morador.setNome(dados.nome().trim());
        morador.setEmail(email);
        morador.setSenha(passwordEncoder.encode(dados.senha()));
        morador.setPerfil(Perfil.USER);
        return moradorDao.save(morador);
    }

    @Transactional
    public Morador atualizar(Long id, AtualizaMorador dados) {
        Morador morador = buscar(id);
        String email = normalizar(dados.email());
        if (!email.equals(morador.getEmail()) && moradorDao.existsByEmail(email)) {
            throw new ConflitoException("Este e-mail já está cadastrado.");
        }
        morador.setNome(dados.nome().trim());
        morador.setEmail(email);
        return moradorDao.save(morador);
    }

    /** Quem já abriu, resolveu ou votou em chamados fica no histórico do jogo e não pode ser excluído. */
    @Transactional
    public void excluir(Long id) {
        buscar(id);
        if (chamadoDao.existsByMorador_IdOrMoradorResolveu_Id(id, id) || confirmacaoDao.existsByMorador_Id(id)) {
            throw new ConflitoException("Este morador já participou do jogo e não pode ser excluído.");
        }
        moradorDao.deleteById(id);
    }

    private String normalizar(String email) {
        return email.trim().toLowerCase();
    }
}
