package com.soportenet.soportetecnico.repository;

import com.soportenet.soportetecnico.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
}
