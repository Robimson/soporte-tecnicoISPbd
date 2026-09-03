package com.soportenet.soportetecnico.config;

import com.soportenet.soportetecnico.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuracion =
                new CorsConfiguration();

        configuracion.setAllowedOriginPatterns(
                List.of("*")
        );

        configuracion.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "PATCH",
                        "OPTIONS"
                )
        );

        configuracion.setAllowedHeaders(
                List.of("*")
        );

        configuracion.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource fuente =
                new UrlBasedCorsConfigurationSource();

        fuente.registerCorsConfiguration(
                "/**",
                configuracion
        );

        return fuente;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            CorsConfigurationSource corsConfigurationSource
    ) throws Exception {

        http
                .cors(cors ->
                        cors.configurationSource(
                                corsConfigurationSource
                        )
                )

                .csrf(csrf ->
                        csrf.disable()
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // =================================================
                        // LOGIN / AUTENTICACIÓN
                        // =================================================

                        .requestMatchers(
                                "/api/auth/**"
                        ).permitAll()


                        // =================================================
                        // ARCHIVOS DEL FRONTEND
                        // =================================================

                        .requestMatchers(
                                "/activar.html",
                                "/login.html",
                                "/admin.html",
                                "/cliente.html",
                                "/superusuario.html",
                                "/tecnico.html",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/favicon.ico"
                        ).permitAll()


                        // =================================================
                        // CORS PREFLIGHT
                        // =================================================

                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        ).permitAll()


                        // =================================================
                        // ACTIVACIÓN
                        // =================================================

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/usuarios/activacion"
                        ).permitAll()


                        // =================================================
                        // SOLICITUDES - CLIENTE
                        // =================================================

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/solicitudes"
                        ).hasRole("CLIENTE")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/solicitudes"
                        ).hasAnyRole(
                                "CLIENTE",
                                "TECNICO",
                                "ADMINISTRADOR",
                                "SUPERUSUARIO"
                        )

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/solicitudes/*"
                        ).hasAnyRole(
                                "CLIENTE",
                                "TECNICO",
                                "ADMINISTRADOR",
                                "SUPERUSUARIO"
                        )

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/solicitudes/*/adjuntos"
                        ).hasAnyRole(
                                "CLIENTE",
                                "TECNICO",
                                "ADMINISTRADOR",
                                "SUPERUSUARIO"
                        )

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/solicitudes/*/confirmacion"
                        ).hasRole("CLIENTE")


                        // =================================================
                        // MIS TAREAS
                        // =================================================

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/solicitudes/mis-tareas"
                        ).hasRole("TECNICO")


                        // =================================================
                        // CATEGORÍAS
                        // =================================================

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/categorias/**"
                        ).authenticated()


                        // =================================================
                        // ESTADOS
                        // =================================================

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/estados/**"
                        ).authenticated()


                        // =================================================
                        // USUARIOS
                        // =================================================

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/usuarios/invitaciones"
                        ).hasRole("SUPERUSUARIO")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/usuarios/*/estado"
                        ).hasRole("SUPERUSUARIO")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/usuarios"
                        ).hasRole("SUPERUSUARIO")



                        // =================================================
                        // AUDITORÍA
                        // =================================================

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/auditoria/datos"
                        ).hasRole("SUPERUSUARIO")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/auditoria/sesiones"
                        ).hasRole("SUPERUSUARIO")


                        // =================================================
                        // RESTO DE LA API
                        // =================================================

                        .anyRequest().authenticated()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}