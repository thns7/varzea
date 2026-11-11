package br.com.fiap.varzea.dao;

import br.com.fiap.varzea.model.Servico;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acesso à tabela T_VZ_SERVICO. */
public interface ServicoDao extends JpaRepository<Servico, Integer> {

    List<Servico> findAllByOrderByGolsDescNomeAsc();
}
