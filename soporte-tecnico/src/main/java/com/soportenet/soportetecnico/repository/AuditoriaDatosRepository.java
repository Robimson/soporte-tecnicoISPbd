package com.soportenet.soportetecnico.repository;

import com.soportenet.soportetecnico.entity.AuditoriaDatos;
import com.soportenet.soportetecnico.enums.OperacionAuditoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;

public interface AuditoriaDatosRepository
        extends JpaRepository<AuditoriaDatos, Long> {

    Page<AuditoriaDatos> findAllByOrderByFechaDesc(
            Pageable pageable
    );

    Page<AuditoriaDatos> findByTablaAfectadaOrderByFechaDesc(
            String tablaAfectada,
            Pageable pageable
    );

    Page<AuditoriaDatos> findByOperacionOrderByFechaDesc(
            OperacionAuditoria operacion,
            Pageable pageable
    );

    Page<AuditoriaDatos> findByIdUsuarioResponsableOrderByFechaDesc(
            Long idUsuarioResponsable,
            Pageable pageable
    );

    long countByFechaAfter(OffsetDateTime fecha);

    long countByOperacionAndFechaAfter(
            OperacionAuditoria operacion,
            OffsetDateTime fecha
    );

    long countByIdUsuarioResponsableIsNullAndFechaAfter(
            OffsetDateTime fecha
    );
}