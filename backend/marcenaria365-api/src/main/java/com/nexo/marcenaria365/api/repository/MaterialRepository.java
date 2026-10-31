package com.nexo.marcenaria365.api.repository;

import com.nexo.marcenaria365.api.entity.Material;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MaterialRepository
        extends JpaRepository<Material, Long> {

    List<Material> findByUsuarioIdOrderByNomeAsc(
            Long usuarioId
    );

    Optional<Material> findByIdAndUsuarioId(
            Long id,
            Long usuarioId
    );
}