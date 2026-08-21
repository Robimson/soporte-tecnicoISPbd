package com.soportenet.soportetecnico.dto;

public class LoginResponse {

    private final String token;
    private final Long idUsuario;
    private final String rol;

    public LoginResponse(String token, Long idUsuario, String rol) {
        this.token = token;
        this.idUsuario = idUsuario;
        this.rol = rol;
    }

    public String getToken() {
        return token;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public String getRol() {
        return rol;
    }
}
