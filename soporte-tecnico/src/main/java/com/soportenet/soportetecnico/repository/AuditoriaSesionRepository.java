package com.soportenet.soportetecnico.repository;

import com.soportenet.soportetecnico.entity.AuditoriaSesion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditoriaSesionRepository
        extends JpaRepository<AuditoriaSesion, Long> {

    @Query(
            value = "SELECT sp_abrir_sesion(:idUsuario, CAST(:ipOrigen AS inet))",
            nativeQuery = true
    )
    Long abrirSesion(
            @Param("idUsuario") Long idUsuario,
            @Param("ipOrigen") String ipOrigen
    );

    @Query(
            value = "SELECT sp_cerrar_sesion(:idSesion)",
            nativeQuery = true
    )
    void cerrarSesion(
            @Param("idSesion") Long idSesion
    );

    @Query(
            value = "SELECT fn_tocar_actividad_sesion(:idSesion)",
            nativeQuery = true
    )
    void tocarActividad(
            @Param("idSesion") Long idSesion
    );

    Page<AuditoriaSesion> findByIdUsuarioOrderByFechaEntradaDesc(
            Long idUsuario,
            Pageable pageable
    );

    Page<AuditoriaSesion> findByFechaSalidaIsNullOrderByFechaEntradaDesc(
            Pageable pageable
    );

    Page<AuditoriaSesion> findAllByOrderByFechaEntradaDesc(
            Pageable pageable
    );

    long countByFechaSalidaIsNull();
}