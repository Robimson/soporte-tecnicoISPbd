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

/**
 * Configuracion principal de seguridad.
 *
 * La autenticacion es Stateless mediante JWT.
 *
 * SecurityConfig controla el acceso general por rol.
 * Los controladores y procedimientos SQL realizan
 * las validaciones adicionales sobre el recurso:
 *
 * - Cliente solo puede acceder a sus propias solicitudes.
 * - Tecnico solo puede acceder a solicitudes asignadas.
 * - Cliente solo puede consultar reportes de sus solicitudes.
 * - Tecnico solo puede consultar reportes de solicitudes asignadas.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Codificador utilizado para almacenar y comprobar contrasenas.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configuracion CORS.
     *
     * Durante desarrollo permitimos cualquier origen para facilitar
     * las pruebas desde frontend local.
     *
     * En produccion se recomienda reemplazar "*" por los dominios
     * reales permitidos.
     */
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

        UrlBasedCorsConfigurationSource fuente =
                new UrlBasedCorsConfigurationSource();

        fuente.registerCorsConfiguration(
                "/**",
                configuracion
        );

        return fuente;
    }

    /**
     * Cadena principal de seguridad.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            CorsConfigurationSource corsConfigurationSource
    ) throws Exception {

        http

                /*
                 * CORS.
                 */
                .cors(cors ->
                        cors.configurationSource(
                                corsConfigurationSource
                        )
                )

                /*
                 * API REST con JWT:
                 * no utilizamos sesiones ni formularios tradicionales.
                 */
                .csrf(csrf ->
                        csrf.disable()
                )

                /*
                 * JWT Stateless.
                 */
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                /*
                 * Reglas de autorizacion.
                 */
                .authorizeHttpRequests(auth -> auth

                        /*
                         * =====================================================
                         * AUTENTICACION
                         * =====================================================
                         */

                        .requestMatchers(
                                "/api/auth/**"
                        )
                        .permitAll()

                        .requestMatchers(
                                "/api/usuarios/activacion"
                        )
                        .permitAll()


                        /*
                         * =====================================================
                         * USUARIOS
                         * =====================================================
                         */

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/usuarios/invitaciones"
                        )
                        .hasRole("SUPERUSUARIO")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/usuarios/*/estado"
                        )
                        .hasRole("SUPERUSUARIO")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/usuarios"
                        )
                        .hasRole("SUPERUSUARIO")


                        /*
                         * =====================================================
                         * GRUPOS TECNICOS
                         * =====================================================
                         */

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/grupos-tecnicos/**"
                        )
                        .hasRole("SUPERUSUARIO")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/grupos-tecnicos/**"
                        )
                        .hasRole("SUPERUSUARIO")


                        /*
                         * =====================================================
                         * SOLICITUDES
                         * =====================================================
                         */

                        /*
                         * Cliente crea una solicitud.
                         */
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/solicitudes"
                        )
                        .hasRole("CLIENTE")

                        /*
                         * Cliente confirma la solucion.
                         */
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/solicitudes/*/confirmacion"
                        )
                        .hasRole("CLIENTE")

                        /*
                         * Tecnico consulta sus tareas.
                         *
                         * Esta regla debe aparecer antes de
                         * /api/solicitudes/*.
                         */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/solicitudes/mis-tareas"
                        )
                        .hasRole("TECNICO")

                        /*
                         * Administrador asigna solicitudes.
                         */
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/solicitudes/*/asignaciones"
                        )
                        .hasRole("ADMINISTRADOR")

                        /*
                         * Tecnico envia reporte.
                         */
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/solicitudes/*/reportes"
                        )
                        .hasRole("TECNICO")

                        /*
                         * Lista general de solicitudes.
                         *
                         * Cliente:
                         * solo las suyas.
                         *
                         * Administrador/Superusuario:
                         * todas.
                         *
                         * Tecnico debe usar /mis-tareas.
                         */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/solicitudes"
                        )
                        .hasAnyRole(
                                "CLIENTE",
                                "ADMINISTRADOR",
                                "SUPERUSUARIO"
                        )

                        /*
                         * Consulta individual.
                         *
                         * El controlador comprueba despues
                         * si el usuario puede acceder a ESE recurso.
                         */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/solicitudes/*"
                        )
                        .hasAnyRole(
                                "CLIENTE",
                                "TECNICO",
                                "ADMINISTRADOR",
                                "SUPERUSUARIO"
                        )


                        /*
                         * =====================================================
                         * REPORTES
                         * =====================================================
                         */

                        /*
                         * Administrador lista reportes.
                         */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/reportes"
                        )
                        .hasRole("ADMINISTRADOR")

                        /*
                         * Administrador aprueba reporte.
                         */
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/reportes/*/aprobacion"
                        )
                        .hasRole("ADMINISTRADOR")

                        /*
                         * Administrador rechaza reporte.
                         */
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/reportes/*/rechazo"
                        )
                        .hasRole("ADMINISTRADOR")

                        /*
                         * Consulta individual de reporte.
                         *
                         * ReporteController comprueba posteriormente
                         * la pertenencia/asignacion del recurso.
                         */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/reportes/*"
                        )
                        .hasAnyRole(
                                "CLIENTE",
                                "TECNICO",
                                "ADMINISTRADOR",
                                "SUPERUSUARIO"
                        )


                        /*
                         * =====================================================
                         * RESTO DE ENDPOINTS
                         * =====================================================
                         *
                         * Por ahora requieren autenticacion.
                         *
                         * Posteriormente revisaremos los otros
                         * controladores para eliminar progresivamente
                         * esta regla generica.
                         */
                        .anyRequest()
                        .authenticated()
                )

                /*
                 * Nuestro filtro JWT debe ejecutarse antes
                 * del filtro estandar de autenticacion.
                 */
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}