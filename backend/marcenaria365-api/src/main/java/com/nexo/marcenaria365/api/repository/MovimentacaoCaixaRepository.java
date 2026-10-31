package com.nexo.marcenaria365.api.repository;

import com.nexo.marcenaria365.api.entity.MovimentacaoCaixa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MovimentacaoCaixaRepository
        extends JpaRepository<MovimentacaoCaixa, Long> {

    List<MovimentacaoCaixa>
        findByUsuarioIdOrderByDataMovimentacaoDescIdDesc(
            Long usuarioId
        );

    Optional<MovimentacaoCaixa> findByIdAndUsuarioId(
        Long id,
        Long usuarioId
    );
}