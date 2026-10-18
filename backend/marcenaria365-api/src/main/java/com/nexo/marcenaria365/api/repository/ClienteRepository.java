package com.nexo.marcenaria365.api.repository;

import com.nexo.marcenaria365.api.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
}
