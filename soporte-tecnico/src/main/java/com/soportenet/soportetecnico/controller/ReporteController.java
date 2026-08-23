package com.soportenet.soportetecnico.controller;

import com.soportenet.soportetecnico.dto.AprobarReporteRequest;
import com.soportenet.soportetecnico.dto.EnviarReporteRequest;
import com.soportenet.soportetecnico.dto.RechazarReporteRequest;
import com.soportenet.soportetecnico.dto.ReporteResponse;
import com.soportenet.soportetecnico.entity.ReporteSolicitud;
import com.soportenet.soportetecnico.enums.EstadoAprobacion;
import com.soportenet.soportetecnico.repository.ReporteSolicitudRepository;
import com.soportenet.soportetecnico.repository.SolicitudRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
public class ReporteController {

    private static final int DIAS_PLAZO_CONFIRMACION_DEFAULT = 3;

    private final ReporteSolicitudRepository reporteSolicitudRepository;
    private final SolicitudRepository solicitudRepository;

    public ReporteController(
            ReporteSolicitudRepository reporteSolicitudRepository,
            SolicitudRepository solicitudRepository
    ) {
        this.reporteSolicitudRepository = reporteSolicitudRepository;
        this.solicitudRepository = solicitudRepository;
    }

    /**
     * Tecnico envia un reporte de solucion.
     */
    @PostMapping("/api/solicitudes/{id}/reportes")
    @Transactional
    public ResponseEntity<ReporteResponse> enviar(
            @PathVariable Long id,
            @Valid @RequestBody EnviarReporteRequest request,
            Authentication authentication
    ) {

        Long idTecnico = Long.valueOf(authentication.getName());

        Long idReporte = reporteSolicitudRepository.enviarReporte(
                id,
                idTecnico,
                request.getDetalleReporte()
        );

        ReporteSolicitud creado = reporteSolicitudRepository
                .findById(idReporte)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "El reporte se creo pero no se pudo recuperar (id="
                                        + idReporte + ")"
                        )
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ReporteResponse.fromEntity(creado));
    }

    /**
     * Consulta un reporte por id.
     *
     * ADMINISTRADOR / SUPERUSUARIO:
     * pueden consultar cualquier reporte.
     *
     * CLIENTE:
     * solo puede consultar reportes correspondientes
     * a sus propias solicitudes.
     *
     * TECNICO:
     * solo puede consultar reportes de solicitudes
     * que tenga asignadas actualmente.
     */
    @GetMapping("/api/reportes/{id}")
    public ResponseEntity<ReporteResponse> obtener(
            @PathVariable Long id,
            Authentication authentication
    ) {

        ReporteSolicitud reporte = reporteSolicitudRepository
                .findById(id)
                .orElse(null);

        if (reporte == null) {
            return ResponseEntity.notFound().build();
        }

        /*
         * Administrador y Superusuario:
         * acceso completo.
         */
        if (tieneRol(authentication, "ADMINISTRADOR")
                || tieneRol(authentication, "SUPERUSUARIO")) {

            return ResponseEntity.ok(
                    ReporteResponse.fromEntity(reporte)
            );
        }

        Long idUsuario = Long.valueOf(authentication.getName());

        /*
         * Cliente:
         * solo reportes de solicitudes que le pertenecen.
         */
        if (tieneRol(authentication, "CLIENTE")) {

            if (reporte.getSolicitud() != null
                    && reporte.getSolicitud().getCliente() != null
                    && reporte.getSolicitud()
                    .getCliente()
                    .getIdUsuario() != null
                    && reporte.getSolicitud()
                    .getCliente()
                    .getIdUsuario()
                    .equals(idUsuario)) {

                return ResponseEntity.ok(
                        ReporteResponse.fromEntity(reporte)
                );
            }

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();
        }

        /*
         * Tecnico:
         * solo si tiene acceso vigente a la solicitud.
         */
        if (tieneRol(authentication, "TECNICO")) {

            if (reporte.getSolicitud() == null
                    || reporte.getSolicitud().getIdSolicitud() == null) {

                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .build();
            }

            Long idSolicitud =
                    reporte.getSolicitud().getIdSolicitud();

            boolean tieneAcceso =
                    solicitudRepository.tecnicoTieneAcceso(
                            idSolicitud,
                            idUsuario
                    );

            if (tieneAcceso) {
                return ResponseEntity.ok(
                        ReporteResponse.fromEntity(reporte)
                );
            }

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();
        }

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .build();
    }

    /**
     * Administrador:
     * lista reportes filtrados por estado de aprobacion.
     * Sin filtro devuelve todos.
     */
    @GetMapping("/api/reportes")
    public ResponseEntity<Page<ReporteResponse>> listar(
            @RequestParam(required = false)
            EstadoAprobacion estado,

            @PageableDefault(
                    size = 20,
                    sort = "fechaEnvio"
            )
            Pageable pageable
    ) {

        Page<ReporteSolicitud> pagina =
                (estado != null)
                        ? reporteSolicitudRepository
                        .findByEstadoAprobacion(
                                estado,
                                pageable
                        )
                        : reporteSolicitudRepository
                        .findAll(pageable);

        return ResponseEntity.ok(
                pagina.map(ReporteResponse::fromEntity)
        );
    }

    /**
     * Administrador aprueba un reporte.
     */
    @PostMapping("/api/reportes/{id}/aprobacion")
    @Transactional
    public ResponseEntity<ReporteResponse> aprobar(
            @PathVariable Long id,
            @Valid @RequestBody AprobarReporteRequest request,
            Authentication authentication
    ) {

        Long idAdministrador =
                Long.valueOf(authentication.getName());

        Integer diasPlazoConfirmacion =
                request.getDiasPlazoConfirmacion() != null
                        ? request.getDiasPlazoConfirmacion()
                        : DIAS_PLAZO_CONFIRMACION_DEFAULT;

        reporteSolicitudRepository.aprobarReporte(
                id,
                idAdministrador,
                diasPlazoConfirmacion
        );

        ReporteSolicitud actualizado =
                reporteSolicitudRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "El reporte se aprobo pero no "
                                                + "se pudo recuperar (id="
                                                + id + ")"
                                )
                        );

        return ResponseEntity.ok(
                ReporteResponse.fromEntity(actualizado)
        );
    }

    /**
     * Administrador rechaza un reporte.
     */
    @PostMapping("/api/reportes/{id}/rechazo")
    @Transactional
    public ResponseEntity<ReporteResponse> rechazar(
            @PathVariable Long id,
            @Valid @RequestBody RechazarReporteRequest request,
            Authentication authentication
    ) {

        Long idAdministrador =
                Long.valueOf(authentication.getName());

        reporteSolicitudRepository.rechazarReporte(
                id,
                idAdministrador,
                request.getComentarioRechazo()
        );

        ReporteSolicitud actualizado =
                reporteSolicitudRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "El reporte se rechazo pero no "
                                                + "se pudo recuperar (id="
                                                + id + ")"
                                )
                        );

        return ResponseEntity.ok(
                ReporteResponse.fromEntity(actualizado)
        );
    }

    /**
     * Comprueba si el usuario autenticado
     * posee un rol determinado.
     */
    private boolean tieneRol(
            Authentication authentication,
            String rol
    ) {

        String authority = "ROLE_" + rol;

        for (GrantedAuthority ga :
                authentication.getAuthorities()) {

            if (ga.getAuthority().equals(authority)) {
                return true;
            }
        }

        return false;
    }
}