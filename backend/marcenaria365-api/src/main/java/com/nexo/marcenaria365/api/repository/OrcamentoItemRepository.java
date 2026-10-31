package com.nexo.marcenaria365.api.repository;

import com.nexo.marcenaria365.api.entity.OrcamentoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrcamentoItemRepository
        extends JpaRepository<OrcamentoItem, Long> {

    List<OrcamentoItem> findByOrcamentoIdOrderByIdAsc(
        Long orcamentoId
    );

    void deleteByOrcamentoId(Long orcamentoId);
}