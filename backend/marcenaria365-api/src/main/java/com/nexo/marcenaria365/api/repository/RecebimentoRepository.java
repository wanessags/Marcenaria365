package com.nexo.marcenaria365.api.repository;

import com.nexo.marcenaria365.api.entity.Recebimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RecebimentoRepository
        extends JpaRepository<Recebimento, Long> {

    @Query(value = """
        SELECT r.*
        FROM recebimentos r
        JOIN servicos s ON s.id = r.servico_id
        JOIN clientes c ON c.id = s.cliente_id
        WHERE c.usuario_id = :usuarioId
        ORDER BY r.id DESC
        """, nativeQuery = true)
    List<Recebimento> listarDoUsuario(
        @Param("usuarioId") Long usuarioId
    );

    @Query(value = """
        SELECT r.*
        FROM recebimentos r
        JOIN servicos s ON s.id = r.servico_id
        JOIN clientes c ON c.id = s.cliente_id
        WHERE c.usuario_id = :usuarioId
          AND r.servico_id = :servicoId
        ORDER BY r.id DESC
        """, nativeQuery = true)
    List<Recebimento> listarPorServico(
        @Param("servicoId") Long servicoId,
        @Param("usuarioId") Long usuarioId
    );

    @Query(value = """
        SELECT r.*
        FROM recebimentos r
        JOIN servicos s ON s.id = r.servico_id
        JOIN clientes c ON c.id = s.cliente_id
        WHERE c.usuario_id = :usuarioId
          AND r.id = :id
        """, nativeQuery = true)
    Optional<Recebimento> buscarDoUsuario(
        @Param("id") Long id,
        @Param("usuarioId") Long usuarioId
    );
}