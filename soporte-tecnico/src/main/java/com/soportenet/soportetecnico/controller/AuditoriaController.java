package com.soportenet.soportetecnico.controller;

import com.soportenet.soportetecnico.dto.*;
import com.soportenet.soportetecnico.entity.AuditoriaDatos;
import com.soportenet.soportetecnico.entity.AuditoriaSesion;
import com.soportenet.soportetecnico.entity.HistorialEstado;
import com.soportenet.soportetecnico.entity.Usuario;
import com.soportenet.soportetecnico.enums.OperacionAuditoria;
import com.soportenet.soportetecnico.repository.AuditoriaDatosRepository;
import com.soportenet.soportetecnico.repository.AuditoriaSesionRepository;
import com.soportenet.soportetecnico.repository.HistorialEstadoRepository;
import com.soportenet.soportetecnico.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;



@RestController
@RequestMapping("/api/auditoria")
public class AuditoriaController {

    private final AuditoriaDatosRepository auditoriaDatosRepository;
    private final AuditoriaSesionRepository auditoriaSesionRepository;
    private final HistorialEstadoRepository historialEstadoRepository;
    private final UsuarioRepository usuarioRepository;

    public AuditoriaController(
            AuditoriaDatosRepository auditoriaDatosRepository,
            AuditoriaSesionRepository auditoriaSesionRepository,
            HistorialEstadoRepository historialEstadoRepository,
            UsuarioRepository usuarioRepository) {

        this.auditoriaDatosRepository = auditoriaDatosRepository;
        this.auditoriaSesionRepository = auditoriaSesionRepository;
        this.historialEstadoRepository = historialEstadoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // ============================================================
    // RESUMEN / MÉTRICAS
    // ============================================================

    @GetMapping("/resumen")
    public ResponseEntity<ResumenAuditoriaDTO> resumen() {

        OffsetDateTime inicioHoy = OffsetDateTime.now()
                .withHour(0).withMinute(0).withSecond(0).withNano(0);

        ResumenAuditoriaDTO dto = new ResumenAuditoriaDTO();

        dto.setSesionesActivas(auditoriaSesionRepository.countByFechaSalidaIsNull());
        dto.setCambiosHoy(auditoriaDatosRepository.countByFechaAfter(inicioHoy));
        dto.setInserts(auditoriaDatosRepository.countByOperacionAndFechaAfter(OperacionAuditoria.INSERT, inicioHoy));
        dto.setUpdates(auditoriaDatosRepository.countByOperacionAndFechaAfter(OperacionAuditoria.UPDATE, inicioHoy));
        dto.setEliminaciones(auditoriaDatosRepository.countByOperacionAndFechaAfter(OperacionAuditoria.DELETE, inicioHoy));
        dto.setAccionesSistema(auditoriaDatosRepository.countByIdUsuarioResponsableIsNullAndFechaAfter(inicioHoy));

        return ResponseEntity.ok(dto);
    }

    // ============================================================
    // AUDITORÍA DE DATOS
    // ============================================================

    @GetMapping("/datos")
    public ResponseEntity<Page<AuditoriaDatosDTO>> listarAuditoriaDatos(
            @RequestParam(required = false) String tabla,
            @RequestParam(required = false) OperacionAuditoria operacion,
            @RequestParam(required = false) Long usuario,
            @PageableDefault(size = 20, sort = "fecha") Pageable pageable) {

        Page<AuditoriaDatos> pagina;

        if (tabla != null && !tabla.isBlank()) {
            pagina = auditoriaDatosRepository.findByTablaAfectadaOrderByFechaDesc(tabla, pageable);
        } else if (operacion != null) {
            pagina = auditoriaDatosRepository.findByOperacionOrderByFechaDesc(operacion, pageable);
        } else if (usuario != null) {
            pagina = auditoriaDatosRepository.findByIdUsuarioResponsableOrderByFechaDesc(usuario, pageable);
        } else {
            pagina = auditoriaDatosRepository.findAllByOrderByFechaDesc(pageable);
        }

        Map<Long, Usuario> usuariosPorId = cargarUsuarios(
                pagina.getContent().stream()
                        .map(AuditoriaDatos::getIdUsuarioResponsable)
                        .collect(Collectors.toList())
        );

        Page<AuditoriaDatosDTO> paginaDTO = pagina.map(a -> mapearAuditoriaDatos(a, usuariosPorId));

        return ResponseEntity.ok(paginaDTO);
    }

    private AuditoriaDatosDTO mapearAuditoriaDatos(AuditoriaDatos a, Map<Long, Usuario> usuariosPorId) {

        AuditoriaDatosDTO dto = new AuditoriaDatosDTO();
        dto.setIdAuditoria(a.getIdAuditoria());
        dto.setFecha(a.getFecha());
        dto.setTablaAfectada(a.getTablaAfectada());
        dto.setOperacion(a.getOperacion());
        dto.setDatosAnteriores(a.getDatosAnteriores());
        dto.setDatosNuevos(a.getDatosNuevos());
        dto.setIdUsuarioResponsable(a.getIdUsuarioResponsable());

        Usuario u = a.getIdUsuarioResponsable() != null
                ? usuariosPorId.get(a.getIdUsuarioResponsable())
                : null;

        if (u != null) {
            dto.setNombreUsuario(u.getNombreUsuario());
            dto.setCorreo(u.getCorreo());
            dto.setRol(u.getRol().name());
        } else {
            dto.setNombreUsuario("Sistema");
        }

        return dto;
    }

    // ============================================================
    // AUDITORÍA DE SESIONES
    // ============================================================

    @GetMapping("/sesiones")
    public ResponseEntity<Page<AuditoriaSesionDTO>> listarSesiones(
            @RequestParam(required = false) Long usuario,
            @RequestParam(required = false) Boolean activas,
            @PageableDefault(size = 20, sort = "fechaEntrada") Pageable pageable) {

        Page<AuditoriaSesion> pagina;

        if (usuario != null) {
            pagina = auditoriaSesionRepository.findByIdUsuarioOrderByFechaEntradaDesc(usuario, pageable);
        } else if (Boolean.TRUE.equals(activas)) {
            pagina = auditoriaSesionRepository.findByFechaSalidaIsNullOrderByFechaEntradaDesc(pageable);
        } else {
            pagina = auditoriaSesionRepository.findAllByOrderByFechaEntradaDesc(pageable);
        }

        Map<Long, Usuario> usuariosPorId = cargarUsuarios(
                pagina.getContent().stream()
                        .map(AuditoriaSesion::getIdUsuario)
                        .collect(Collectors.toList())
        );

        Page<AuditoriaSesionDTO> paginaDTO = pagina.map(s -> mapearSesion(s, usuariosPorId));

        return ResponseEntity.ok(paginaDTO);
    }

    private AuditoriaSesionDTO mapearSesion(AuditoriaSesion s, Map<Long, Usuario> usuariosPorId) {

        AuditoriaSesionDTO dto = new AuditoriaSesionDTO();
        dto.setIdSesion(s.getIdSesion());
        dto.setIdUsuario(s.getIdUsuario());
        dto.setIpOrigen(s.getIpOrigen());
        dto.setFechaEntrada(s.getFechaEntrada());
        dto.setUltimaActividad(s.getUltimaActividad());
        dto.setFechaSalida(s.getFechaSalida());
        dto.setActiva(s.getFechaSalida() == null);

        Usuario u = usuariosPorId.get(s.getIdUsuario());

        if (u != null) {
            dto.setNombreUsuario(u.getNombreUsuario());
            dto.setCorreo(u.getCorreo());
            dto.setRol(u.getRol().name());
        }

        return dto;
    }

    // ============================================================
    // HISTORIAL DE ESTADOS DE UNA SOLICITUD
    // ============================================================

    @GetMapping("/historial/{idSolicitud}")
    public ResponseEntity<List<HistorialEstadoDTO>> historialSolicitud(
            @PathVariable Long idSolicitud) {

        List<HistorialEstado> historial =
                historialEstadoRepository.findBySolicitudOrdenado(idSolicitud);

        List<HistorialEstadoDTO> dtos = historial.stream()
                .map(this::mapearHistorial)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    private HistorialEstadoDTO mapearHistorial(HistorialEstado h) {

        HistorialEstadoDTO dto = new HistorialEstadoDTO();
        dto.setIdHistorial(h.getIdHistorial());
        dto.setIdSolicitud(h.getSolicitud().getIdSolicitud());
        dto.setEstadoAnterior(h.getEstadoAnterior() != null ? h.getEstadoAnterior().getNombreEstado() : null);
        dto.setEstadoNuevo(h.getEstadoNuevo().getNombreEstado());
        dto.setFechaCambio(h.getFechaCambio());

        if (h.getUsuarioResponsable() != null) {
            dto.setNombreUsuarioResponsable(h.getUsuarioResponsable().getNombreUsuario());
            dto.setRolResponsable(h.getUsuarioResponsable().getRol().name());
        } else {
            dto.setNombreUsuarioResponsable("Sistema");
        }

        return dto;
    }

    // ============================================================
    // UTILIDAD COMPARTIDA
    // ============================================================

    private Map<Long, Usuario> cargarUsuarios(List<Long> ids) {

        List<Long> idsLimpios = ids.stream()
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        if (idsLimpios.isEmpty()) {
            return new HashMap<>();
        }

        Map<Long, Usuario> mapa = new HashMap<>();
        usuarioRepository.findAllById(idsLimpios)
                .forEach(u -> mapa.put(u.getIdUsuario(), u));

        return mapa;
    }

    @GetMapping("/usuarios/buscar")
    public ResponseEntity<List<UsuarioBusquedaDTO>> buscarUsuarios(
            @RequestParam String nombre) {

        if (nombre == null || nombre.trim().length() < 2) {
            return ResponseEntity.ok(List.of());
        }

        String termino = nombre.trim();

        List<Usuario> usuarios =
                usuarioRepository.findTop8ByNombreUsuarioContainingIgnoreCaseOrCorreoContainingIgnoreCaseOrderByNombreUsuario(
                        termino,
                        termino
                );

        List<UsuarioBusquedaDTO> dtos = usuarios.stream()
                .map(u -> {
                    UsuarioBusquedaDTO dto = new UsuarioBusquedaDTO();
                    dto.setIdUsuario(u.getIdUsuario());
                    dto.setNombreUsuario(u.getNombreUsuario());
                    dto.setCorreo(u.getCorreo());
                    dto.setRol(u.getRol().name());
                    return dto;
                })
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(dtos);
    }
}