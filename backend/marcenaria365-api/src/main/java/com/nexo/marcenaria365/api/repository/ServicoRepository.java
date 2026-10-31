package com.nexo.marcenaria365.api.repository;

import com.nexo.marcenaria365.api.entity.Servico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ServicoRepository extends JpaRepository<Servico, Long> {

    @Query(
        value = """
            SELECT s.*
            FROM servicos s
            JOIN clientes c ON c.id = s.cliente_id
            WHERE c.usuario_id = :usuarioId
            ORDER BY s.id DESC
            """,
        nativeQuery = true
    )
    List<Servico> listarDoUsuario(
        @Param("usuarioId") Long usuarioId
    );

    @Query(
        value = """
            SELECT s.*
            FROM servicos s
            JOIN clientes c ON c.id = s.cliente_id
            WHERE s.id = :id
              AND c.usuario_id = :usuarioId
            """,
        nativeQuery = true
    )
    Optional<Servico> buscarDoUsuario(
        @Param("id") Long id,
        @Param("usuarioId") Long usuarioId
    );
}