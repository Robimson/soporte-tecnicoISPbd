package com.soportenet.soportetecnico.repository;

import com.soportenet.soportetecnico.entity.Adjunto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AdjuntoRepository
        extends JpaRepository<Adjunto, Long> {

    // ============================================================
    // CONTAR ADJUNTOS
    // ============================================================

    long countByIdSolicitud(Long idSolicitud);

    // ============================================================
    // OBTENER ADJUNTOS DE UNA SOLICITUD
    // ============================================================

    @Query("""
            SELECT a
            FROM Adjunto a
            WHERE a.idSolicitud = :idSolicitud
            ORDER BY a.idAdjunto ASC
            """)
    List<Adjunto> findByIdSolicitud(
            @Param("idSolicitud") Long idSolicitud
    );

    // ============================================================
    // AGREGAR ADJUNTO
    // ============================================================

    @Query(
            value = """
                    SELECT sp_agregar_adjunto(
                        :idSolicitud,
                        :idUsuario,
                        :nombreArchivo,
                        :tipoArchivo,
                        :tamanoArchivo,
                        :urlAlmacenamiento
                    )
                    """,
            nativeQuery = true
    )
    Long agregarAdjunto(
            @Param("idSolicitud") Long idSolicitud,
            @Param("idUsuario") Long idUsuario,
            @Param("nombreArchivo") String nombreArchivo,
            @Param("tipoArchivo") String tipoArchivo,
            @Param("tamanoArchivo") Long tamanoArchivo,
            @Param("urlAlmacenamiento") String urlAlmacenamiento
    );
}