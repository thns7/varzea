package br.com.fiap.varzea.dao;

import br.com.fiap.varzea.model.Confirmacao;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acesso à tabela T_VZ_CONFIRMACAO. */
public interface ConfirmacaoDao extends JpaRepository<Confirmacao, Long> {

    List<Confirmacao> findByChamado_IdOrderById(Long chamadoId);
    boolean existsByChamado_IdAndMorador_Id(Long chamadoId, Long moradorId);
    boolean existsByMorador_Id(Long moradorId);
    void deleteByChamado_Id(Long chamadoId);
}
