package br.com.fiap.varzea.dao;

import br.com.fiap.varzea.model.Categoria;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acesso à tabela T_VZ_CATEGORIA. */
public interface CategoriaDao extends JpaRepository<Categoria, Integer> {

    List<Categoria> findAllByOrderByNome();
}
