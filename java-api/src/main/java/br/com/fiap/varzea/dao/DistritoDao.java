package br.com.fiap.varzea.dao;

import br.com.fiap.varzea.model.Distrito;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acesso à tabela T_VZ_DISTRITO. */
public interface DistritoDao extends JpaRepository<Distrito, Integer> {

    List<Distrito> findAllByOrderByNome();
}
