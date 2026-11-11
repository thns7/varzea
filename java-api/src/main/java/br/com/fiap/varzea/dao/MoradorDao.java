package br.com.fiap.varzea.dao;

import br.com.fiap.varzea.model.Morador;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acesso à tabela T_VZ_MORADOR. */
public interface MoradorDao extends JpaRepository<Morador, Long> {

    Optional<Morador> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Morador> findAllByOrderByNome();
    List<Morador> findByDistrito_IdOrderByNome(Integer distritoId);
}
