package com.soportenet.soportetecnico.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void configurar() {
        handler = new GlobalExceptionHandler();
    }

    /**
     * Un error de PostgreSQL debe convertirse
     * en HTTP 400 y devolver el mensaje limpio.
     */
    @Test
    void errorPostgresDevuelveBadRequest() {

        RuntimeException causaPostgres =
                new RuntimeException(
                        "ERROR: El cliente no tiene una cuenta activa.\n  Where: PL/pgSQL function sp_crear_solicitud()"
                );

        DataIntegrityViolationException excepcion =
                new DataIntegrityViolationException(
                        "Error de base de datos",
                        causaPostgres
                );

        ResponseEntity<Map<String, Object>> respuesta =
                handler.handleDbError(excepcion);

        assertEquals(
                HttpStatus.BAD_REQUEST,
                respuesta.getStatusCode()
        );

        assertNotNull(respuesta.getBody());

        assertEquals(
                400,
                respuesta.getBody().get("status")
        );

        assertEquals(
                "El cliente no tiene una cuenta activa.",
                respuesta.getBody().get("error")
        );
    }

    /**
     * IllegalStateException debe convertirse
     * en HTTP 500.
     */
    @Test
    void illegalStateDevuelveInternalServerError() {

        IllegalStateException excepcion =
                new IllegalStateException(
                        "La solicitud se creo pero no se pudo recuperar"
                );

        ResponseEntity<Map<String, Object>> respuesta =
                handler.handleIllegalState(excepcion);

        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                respuesta.getStatusCode()
        );

        assertNotNull(respuesta.getBody());

        assertEquals(
                500,
                respuesta.getBody().get("status")
        );

        assertEquals(
                "La solicitud se creo pero no se pudo recuperar",
                respuesta.getBody().get("error")
        );
    }

    /**
     * Si una excepcion interna no trae mensaje,
     * nunca debemos intentar devolver null.
     */
    @Test
    void mensajeNuloDevuelveMensajeSeguro() {

        IllegalStateException excepcion =
                new IllegalStateException();

        ResponseEntity<Map<String, Object>> respuesta =
                handler.handleIllegalState(excepcion);

        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                respuesta.getStatusCode()
        );

        assertNotNull(respuesta.getBody());

        assertEquals(
                "Ocurrio un error interno.",
                respuesta.getBody().get("error")
        );
    }

    /**
     * @Valid puede producir varios errores.
     * El handler debe devolverlos todos.
     */
    @Test
    void validacionDevuelveTodosLosCampos() {

        MethodArgumentNotValidException excepcion =
                Mockito.mock(
                        MethodArgumentNotValidException.class
                );

        BindingResult bindingResult =
                Mockito.mock(BindingResult.class);

        FieldError correo =
                new FieldError(
                        "usuario",
                        "correo",
                        "El correo es obligatorio"
                );

        FieldError contrasena =
                new FieldError(
                        "usuario",
                        "contrasena",
                        "La contrasena es obligatoria"
                );

        when(excepcion.getBindingResult())
                .thenReturn(bindingResult);

        when(bindingResult.getFieldErrors())
                .thenReturn(
                        List.of(
                                correo,
                                contrasena
                        )
                );

        ResponseEntity<Map<String, Object>> respuesta =
                handler.handleValidation(excepcion);

        assertEquals(
                HttpStatus.BAD_REQUEST,
                respuesta.getStatusCode()
        );

        assertNotNull(respuesta.getBody());

        assertEquals(
                400,
                respuesta.getStatusCode().value()
        );

        assertEquals(
                "Datos invalidos",
                respuesta.getBody().get("error")
        );

        @SuppressWarnings("unchecked")
        Map<String, String> campos =
                (Map<String, String>)
                        respuesta.getBody().get("campos");

        assertNotNull(campos);

        assertEquals(
                "El correo es obligatorio",
                campos.get("correo")
        );

        assertEquals(
                "La contrasena es obligatoria",
                campos.get("contrasena")
        );
    }
}