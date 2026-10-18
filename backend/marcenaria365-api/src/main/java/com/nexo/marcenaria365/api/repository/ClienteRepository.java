package com.nexo.marcenaria365.api.repository;

import com.nexo.marcenaria365.api.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    List<Cliente> findByUsuarioIdOrderByNomeAsc(Long usuarioId);

    Optional<Cliente> findByIdAndUsuarioId(Long id, Long usuarioId);
}