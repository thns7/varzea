package br.com.fiap.varzea.dao;

import br.com.fiap.varzea.model.Chamado;
import br.com.fiap.varzea.model.StatusChamado;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/** Acesso à tabela T_VZ_CHAMADO. */
public interface ChamadoDao extends JpaRepository<Chamado, Long> {

    @Query("""
            select c from Chamado c
             where (:distritoId is null or c.distrito.id = :distritoId)
               and (:status is null or c.status = :status)
             order by c.dataAbertura desc, c.id desc
            """)
    List<Chamado> filtrar(@Param("distritoId") Integer distritoId, @Param("status") StatusChamado status, Pageable pagina);

    /** Trava a linha até o fim da transação, para dois votos simultâneos não gerarem dois gols. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Chamado c where c.id = :id")
    Optional<Chamado> travar(@Param("id") Long id);

    boolean existsByMorador_IdOrMoradorResolveu_Id(Long moradorId, Long moradorResolveuId);
}
