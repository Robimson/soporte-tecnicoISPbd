package com.soportenet.soportetecnico.repository;

import com.soportenet.soportetecnico.entity.Solicitud;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    /** Cliente: sus propias solicitudes (caso de uso 4.1.4). */
    Page<Solicitud> findByClienteIdUsuario(
            Long idCliente,
            Pageable pageable
    );

    /** Cliente: sus propias solicitudes, filtradas por estado. */
    Page<Solicitud> findByClienteIdUsuarioAndEstadoNombreEstado(
            Long idCliente,
            String nombreEstado,
            Pageable pageable
    );

    /** Administrador: todas las solicitudes, filtradas por estado. */
    Page<Solicitud> findByEstadoNombreEstado(
            String nombreEstado,
            Pageable pageable
    );

    /**
     * Tecnico: "Mis tareas".
     * Devuelve solicitudes asignadas directamente al tecnico
     * o a uno de los grupos de los que forma parte.
     */
    @Query(
            value = """
                    SELECT s.*
                    FROM solicitud s
                    JOIN asignacion_solicitud a
                        ON a.id_solicitud = s.id_solicitud
                        AND a.vigente = true
                    LEFT JOIN tecnico_grupo tg
                        ON tg.id_grupo = a.id_grupo
                        AND tg.id_usuario = :idTecnico
                    LEFT JOIN prioridad p
                        ON p.id_prioridad = s.id_prioridad
                    WHERE a.id_tecnico = :idTecnico
                       OR tg.id_usuario = :idTecnico
                    ORDER BY COALESCE(p.orden, 0) DESC,
                             s.fecha_creacion ASC
                    """,
            countQuery = """
                    SELECT count(*)
                    FROM solicitud s
                    JOIN asignacion_solicitud a
                        ON a.id_solicitud = s.id_solicitud
                        AND a.vigente = true
                    LEFT JOIN tecnico_grupo tg
                        ON tg.id_grupo = a.id_grupo
                        AND tg.id_usuario = :idTecnico
                    WHERE a.id_tecnico = :idTecnico
                       OR tg.id_usuario = :idTecnico
                    """,
            nativeQuery = true
    )
    Page<Solicitud> findMisTareas(
            @Param("idTecnico") Long idTecnico,
            Pageable pageable
    );

    /**
     * Comprueba si una solicitud esta asignada actualmente
     * al tecnico.
     *
     * Puede estar asignada:
     * - directamente al tecnico
     * - a uno de los grupos a los que pertenece
     */
    @Query(
            value = """
                    SELECT EXISTS (
                        SELECT 1
                        FROM asignacion_solicitud a
                        LEFT JOIN tecnico_grupo tg
                            ON tg.id_grupo = a.id_grupo
                            AND tg.id_usuario = :idTecnico
                        WHERE a.id_solicitud = :idSolicitud
                          AND a.vigente = true
                          AND (
                                a.id_tecnico = :idTecnico
                                OR tg.id_usuario = :idTecnico
                          )
                    )
                    """,
            nativeQuery = true
    )
    boolean tecnicoTieneAcceso(
            @Param("idSolicitud") Long idSolicitud,
            @Param("idTecnico") Long idTecnico
    );

    /**
     * Cierra automaticamente las solicitudes cuyo plazo
     * de confirmacion del cliente ya vencio.
     */
    @Query(
            value = "SELECT sp_cierre_automatico_por_vencimiento()",
            nativeQuery = true
    )
    Integer cerrarSolicitudesVencidas();

    /**
     * Crea una solicitud mediante el procedimiento almacenado.
     */
    @Query(
            value = """
                    SELECT sp_crear_solicitud(
                        :idCliente,
                        :descripcion,
                        :idCategoria
                    )
                    """,
            nativeQuery = true
    )
    Long crearSolicitud(
            @Param("idCliente") Long idCliente,
            @Param("descripcion") String descripcion,
            @Param("idCategoria") Integer idCategoria
    );

    /**
     * Asigna o reasigna una solicitud.
     */
    @Query(
            value = """
                    SELECT sp_asignar_solicitud(
                        :idSolicitud,
                        :idAdministrador,
                        :idTecnico,
                        :idGrupo,
                        :idPrioridad,
                        :motivoReasignacion
                    )
                    """,
            nativeQuery = true
    )
    void asignarSolicitud(
            @Param("idSolicitud") Long idSolicitud,
            @Param("idAdministrador") Long idAdministrador,
            @Param("idTecnico") Long idTecnico,
            @Param("idGrupo") Long idGrupo,
            @Param("idPrioridad") Integer idPrioridad,
            @Param("motivoReasignacion") String motivoReasignacion
    );

    /**
     * Cliente confirma o rechaza la solucion de una solicitud.
     */
    @Query(
            value = """
                    SELECT sp_confirmar_cliente(
                        :idSolicitud,
                        :idCliente,
                        :problemaResuelto
                    )
                    """,
            nativeQuery = true
    )
    void confirmarCliente(
            @Param("idSolicitud") Long idSolicitud,
            @Param("idCliente") Long idCliente,
            @Param("problemaResuelto") Boolean problemaResuelto
    );
}