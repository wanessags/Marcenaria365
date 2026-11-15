package com.nexo.marcenaria365.api.repository;

import com.nexo.marcenaria365.api.entity.RecuperacaoSenha;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface RecuperacaoSenhaRepository extends JpaRepository<RecuperacaoSenha, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from RecuperacaoSenha r where r.tokenHash = :hash")
    Optional<RecuperacaoSenha> buscarParaConsumo(@Param("hash") String hash);
}
