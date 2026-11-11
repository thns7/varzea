package br.com.fiap.varzea.dao;

import br.com.fiap.varzea.model.Partida;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acesso à tabela T_VZ_PARTIDA. */
public interface PartidaDao extends JpaRepository<Partida, Long> {

    List<Partida> findByRodada_IdOrderById(Integer rodadaId);
}
