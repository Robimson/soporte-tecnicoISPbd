package com.soportenet.soportetecnico.controller;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manejo global de errores de la API.
 *
 * Convierte errores de:
 * - PostgreSQL / procedimientos almacenados
 * - restricciones de integridad
 * - validaciones de DTO
 *
 * en respuestas HTTP claras para el frontend.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Errores de integridad:
     * FK, UNIQUE, CHECK, etc.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDbError(
            DataIntegrityViolationException ex
    ) {

        return respuestaError(
                HttpStatus.BAD_REQUEST,
                extraerMensajePostgres(ex)
        );
    }

    /**
     * Errores SQL relacionados con uso incorrecto
     * de funciones, consultas o recursos SQL.
     */
    @ExceptionHandler(InvalidDataAccessResourceUsageException.class)
    public ResponseEntity<Map<String, Object>> handleSqlError(
            InvalidDataAccessResourceUsageException ex
    ) {

        return respuestaError(
                HttpStatus.BAD_REQUEST,
                extraerMensajePostgres(ex)
        );
    }

    /**
     * Captura general de errores de acceso a datos.
     *
     * Incluye los RAISE EXCEPTION de PostgreSQL
     * cuyo SQLSTATE suele ser P0001.
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Map<String, Object>> handleDataAccessError(
            DataAccessException ex
    ) {

        return respuestaError(
                HttpStatus.BAD_REQUEST,
                extraerMensajePostgres(ex)
        );
    }

    /**
     * Errores producidos por @Valid.
     *
     * Devuelve todos los errores de campos encontrados,
     * no solamente el primero.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex
    ) {

        Map<String, String> erroresCampos =
                new LinkedHashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        erroresCampos.put(
                                error.getField(),
                                error.getDefaultMessage() != null
                                        ? error.getDefaultMessage()
                                        : "Valor invalido"
                        )
                );

        Map<String, Object> body =
                new LinkedHashMap<>();

        body.put(
                "error",
                "Datos invalidos"
        );

        body.put(
                "campos",
                erroresCampos
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(body);
    }

    /**
     * IllegalStateException es utilizada en varios
     * controladores cuando una operacion se completo
     * pero posteriormente no pudo recuperarse la entidad.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(
            IllegalStateException ex
    ) {

        return respuestaError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getMessage() != null
                        ? ex.getMessage()
                        : "Ocurrio un error interno."
        );
    }

    /**
     * Construye una respuesta de error consistente.
     */
    private ResponseEntity<Map<String, Object>> respuestaError(
            HttpStatus status,
            String mensaje
    ) {

        String mensajeSeguro =
                (mensaje == null || mensaje.isBlank())
                        ? "No se pudo completar la operacion."
                        : limpiarMensaje(mensaje);

        Map<String, Object> body =
                new LinkedHashMap<>();

        body.put(
                "status",
                status.value()
        );

        body.put(
                "error",
                mensajeSeguro
        );

        return ResponseEntity
                .status(status)
                .body(body);
    }

    /**
     * Busca el mensaje de la causa raiz,
     * normalmente una PSQLException.
     */
    private String extraerMensajePostgres(
            Throwable ex
    ) {

        Throwable actual = ex;

        while (actual.getCause() != null) {
            actual = actual.getCause();
        }

        String mensaje =
                actual.getMessage();

        if (mensaje == null || mensaje.isBlank()) {
            return "Error al procesar la operacion en la base de datos.";
        }

        return mensaje;
    }

    /**
     * Elimina prefijos tecnicos comunes de PostgreSQL
     * para devolver un mensaje mas limpio al frontend.
     */
    private String limpiarMensaje(
            String mensaje
    ) {

        String resultado =
                mensaje.trim();

        if (resultado.startsWith("ERROR: ")) {
            resultado =
                    resultado.substring(
                            "ERROR: ".length()
                    );
        }

        /*
         * PostgreSQL puede agregar:
         *
         * Where: PL/pgSQL function ...
         *
         * No necesitamos exponer esa informacion
         * interna al usuario final.
         */
        int posicionWhere =
                resultado.indexOf("\n  Where:");

        if (posicionWhere >= 0) {
            resultado =
                    resultado.substring(
                            0,
                            posicionWhere
                    ).trim();
        }

        return resultado;
    }
}