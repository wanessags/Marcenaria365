package com.nexo.marcenaria365.api.repository;

import com.nexo.marcenaria365.api.entity.PedidoRecuperacaoSenha;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.*;

public interface PedidoRecuperacaoSenhaRepository extends JpaRepository<PedidoRecuperacaoSenha, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PedidoRecuperacaoSenha p where p.id = :id")
    Optional<PedidoRecuperacaoSenha> bloquear(@Param("id") String id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2"))
    @Query("select p from PedidoRecuperacaoSenha p where p.pendente = true and p.proximaTentativa <= :agora order by p.proximaTentativa, p.id")
    List<PedidoRecuperacaoSenha> proximos(@Param("agora") Instant agora, Pageable pagina);

    long countByPendenteTrue();
}
