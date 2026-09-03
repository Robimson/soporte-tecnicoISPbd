package com.soportenet.soportetecnico.enums;

/**
 * Operaciones registradas por la auditoría de datos.
 *
 * Debe coincidir con el ENUM:
 * operacion_auditoria_tipo
 * de PostgreSQL.
 */
public enum OperacionAuditoria {

    INSERT,
    UPDATE,
    DELETE
}