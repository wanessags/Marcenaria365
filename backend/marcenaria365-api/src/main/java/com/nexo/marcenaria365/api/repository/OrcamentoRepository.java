package com.nexo.marcenaria365.api.repository;

import com.nexo.marcenaria365.api.entity.Orcamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrcamentoRepository extends JpaRepository<Orcamento, Long> {

    @Query(value = """
        SELECT o.*
        FROM orcamentos o
        JOIN clientes c ON c.id = o.cliente_id
        WHERE c.usuario_id = :usuarioId
        ORDER BY o.id DESC
        """, nativeQuery = true)
    List<Orcamento> listarDoUsuario(
        @Param("usuarioId") Long usuarioId
    );

    @Query(value = """
        SELECT o.*
        FROM orcamentos o
        JOIN clientes c ON c.id = o.cliente_id
        WHERE o.id = :id
          AND c.usuario_id = :usuarioId
        """, nativeQuery = true)
    Optional<Orcamento> buscarDoUsuario(
        @Param("id") Long id,
        @Param("usuarioId") Long usuarioId
    );
}