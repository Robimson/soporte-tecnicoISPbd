package com.soportenet.soportetecnico.security;

import com.soportenet.soportetecnico.entity.Usuario;
import com.soportenet.soportetecnico.enums.EstadoCuenta;
import com.soportenet.soportetecnico.repository.UsuarioRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Valida el JWT enviado en:
 *
 * Authorization: Bearer <token>
 *
 * Ademas de validar firma y expiracion del token,
 * comprueba en PostgreSQL que el usuario:
 *
 * - siga existiendo
 * - tenga la cuenta activa
 *
 * El rol se obtiene de la base de datos y no se confia
 * exclusivamente en el rol almacenado dentro del JWT.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UsuarioRepository usuarioRepository
    ) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        /*
         * Si no existe Bearer token, continuamos sin autenticar.
         * SecurityConfig decidira si la ruta permite o no el acceso.
         */
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring("Bearer ".length()).trim();

        if (token.isEmpty()) {
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
        }

        try {

            /*
             * Comprueba firma, estructura y expiracion.
             */
            Claims claims =
                    jwtService.validarYObtenerClaims(token);

            String subject =
                    claims.getSubject();

            if (subject == null || subject.isBlank()) {
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            Long idUsuario =
                    Long.valueOf(subject);

            /*
             * Comprobacion contra la base de datos.
             *
             * Esto permite invalidar inmediatamente el acceso
             * de un usuario suspendido o desactivado aunque
             * conserve un JWT que todavia no haya expirado.
             */
            Optional<Usuario> usuarioOpt =
                    usuarioRepository.findById(idUsuario);

            if (usuarioOpt.isEmpty()) {
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            Usuario usuario =
                    usuarioOpt.get();

            /*
             * Solamente cuentas activas pueden autenticarse.
             */
            if (usuario.getEstadoCuenta() != EstadoCuenta.activo) {
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            /*
             * Utilizamos el rol ACTUAL de PostgreSQL.
             *
             * No dependemos del claim "rol" del token para
             * decidir los permisos.
             */
            String rol =
                    usuario.getRol()
                            .name()
                            .toUpperCase();

            SimpleGrantedAuthority authority =
                    new SimpleGrantedAuthority(
                            "ROLE_" + rol
                    );

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            idUsuario.toString(),
                            null,
                            List.of(authority)
                    );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);

        } catch (
                JwtException
                | IllegalArgumentException ex
        ) {

            /*
             * Token vencido, corrupto, firma invalida,
             * subject incorrecto, etc.
             */
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(
                request,
                response
        );
    }
}
