package com.soportenet.soportetecnico.repository;

import com.soportenet.soportetecnico.entity.HistorialEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HistorialEstadoRepository
        extends JpaRepository<HistorialEstado, Long> {

    @Query(
            "SELECT h FROM HistorialEstado h " +
                    "LEFT JOIN FETCH h.estadoAnterior " +
                    "JOIN FETCH h.estadoNuevo " +
                    "LEFT JOIN FETCH h.usuarioResponsable " +
                    "WHERE h.solicitud.idSolicitud = :idSolicitud " +
                    "ORDER BY h.fechaCambio ASC"
    )
    List<HistorialEstado> findBySolicitudOrdenado(
            @Param("idSolicitud") Long idSolicitud
    );
}