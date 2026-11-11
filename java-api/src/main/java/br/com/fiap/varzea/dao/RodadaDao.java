package br.com.fiap.varzea.dao;

import br.com.fiap.varzea.model.Rodada;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acesso à tabela T_VZ_RODADA. */
public interface RodadaDao extends JpaRepository<Rodada, Integer> {

    List<Rodada> findAllByOrderByNumero();
    /** A rodada vigente na data, ou a última já iniciada. */
    Optional<Rodada> findFirstByInicioLessThanEqualOrderByNumeroDesc(LocalDate data);
}
