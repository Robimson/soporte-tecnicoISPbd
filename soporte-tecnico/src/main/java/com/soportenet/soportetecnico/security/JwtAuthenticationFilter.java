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

        SecurityContextHolder.clearContext();

        String header =
                request.getHeader("Authorization");

        if (header == null ||
                !header.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        String token =
                header.substring(7).trim();

        if (token.isEmpty()) {

            filterChain.doFilter(request, response);
            return;
        }

        try {

            Claims claims =
                    jwtService.validarYObtenerClaims(token);

            String subject =
                    claims.getSubject();

            if (subject == null ||
                    subject.isBlank()) {

                filterChain.doFilter(request, response);
                return;
            }

            Long idUsuario =
                    Long.valueOf(subject);

            Optional<Usuario> usuarioOpt =
                    usuarioRepository.findById(idUsuario);

            if (usuarioOpt.isEmpty()) {

                filterChain.doFilter(request, response);
                return;
            }

            Usuario usuario =
                    usuarioOpt.get();

            if (usuario.getEstadoCuenta()
                    != EstadoCuenta.activo) {

                filterChain.doFilter(request, response);
                return;
            }

            if (usuario.getRol() == null) {

                filterChain.doFilter(request, response);
                return;
            }

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

            System.out.println(
                    "JWT OK -> usuario=" +
                            idUsuario +
                            " rol=ROLE_" +
                            rol +
                            " metodo=" +
                            request.getMethod() +
                            " ruta=" +
                            request.getRequestURI()
            );

        } catch (
                JwtException |
                IllegalArgumentException ex
        ) {

            SecurityContextHolder.clearContext();

            System.out.println(
                    "JWT INVALIDO -> " +
                            ex.getMessage()
            );
        }

        filterChain.doFilter(
                request,
                response
        );
    }
}