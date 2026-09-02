package com.soportenet.soportetecnico.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class FrontendConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // ==========================================
        // PAGINAS HTML
        // ==========================================

        registry.addResourceHandler(
                "/activar.html",
                "/login.html",
                "/admin.html",
                "/cliente.html",
                "/superusuario.html",
                "/tecnico.html"
        ).addResourceLocations(
                "file:frontend/views/"
        );


        // ==========================================
        // CSS
        // ==========================================

        registry.addResourceHandler(
                "/css/**"
        ).addResourceLocations(
                "file:frontend/css/"
        );


        // ==========================================
        // JAVASCRIPT
        // ==========================================

        registry.addResourceHandler(
                "/js/**"
        ).addResourceLocations(
                "file:frontend/js/"
        );
    }
}