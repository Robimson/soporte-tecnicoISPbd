package com.soportenet.soportetecnico.security;

import com.soportenet.soportetecnico.entity.Usuario;
import com.soportenet.soportetecnico.enums.EstadoCuenta;
import com.soportenet.soportetecnico.enums.RolUsuario;
import com.soportenet.soportetecnico.repository.UsuarioRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    private JwtService jwtService;
    private UsuarioRepository usuarioRepository;
    private JwtAuthenticationFilter filter;
    private FilterChain filterChain;

    @BeforeEach
    void configurar() {

        jwtService = Mockito.mock(JwtService.class);
        usuarioRepository = Mockito.mock(UsuarioRepository.class);
        filterChain = Mockito.mock(FilterChain.class);

        filter = new JwtAuthenticationFilter(
                jwtService,
                usuarioRepository
        );

        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void usuarioActivoConTokenValidoQuedaAutenticado() throws Exception {

        Long idUsuario = 10L;

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer TOKEN_VALIDO"
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        Claims claims = Mockito.mock(Claims.class);

        when(jwtService.validarYObtenerClaims("TOKEN_VALIDO"))
                .thenReturn(claims);

        when(claims.getSubject())
                .thenReturn(idUsuario.toString());

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(idUsuario);
        usuario.setEstadoCuenta(EstadoCuenta.activo);
        usuario.setRol(RolUsuario.tecnico);

        when(usuarioRepository.findById(idUsuario))
                .thenReturn(Optional.of(usuario));

        filter.doFilter(
                request,
                response,
                filterChain
        );

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        assertNotNull(authentication);

        assertEquals(
                idUsuario.toString(),
                authentication.getName()
        );

        assertTrue(
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(a ->
                                a.getAuthority()
                                        .equals("ROLE_TECNICO")
                        )
        );

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void usuarioSuspendidoNoQuedaAutenticado() throws Exception {

        Long idUsuario = 11L;

        MockHttpServletRequest request =
                requestConToken("TOKEN_SUSPENDIDO");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        Claims claims = Mockito.mock(Claims.class);

        when(jwtService.validarYObtenerClaims("TOKEN_SUSPENDIDO"))
                .thenReturn(claims);

        when(claims.getSubject())
                .thenReturn(idUsuario.toString());

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(idUsuario);
        usuario.setEstadoCuenta(EstadoCuenta.suspendido);
        usuario.setRol(RolUsuario.tecnico);

        when(usuarioRepository.findById(idUsuario))
                .thenReturn(Optional.of(usuario));

        filter.doFilter(
                request,
                response,
                filterChain
        );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void usuarioDesactivadoNoQuedaAutenticado() throws Exception {

        Long idUsuario = 12L;

        MockHttpServletRequest request =
                requestConToken("TOKEN_DESACTIVADO");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        Claims claims = Mockito.mock(Claims.class);

        when(jwtService.validarYObtenerClaims("TOKEN_DESACTIVADO"))
                .thenReturn(claims);

        when(claims.getSubject())
                .thenReturn(idUsuario.toString());

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(idUsuario);
        usuario.setEstadoCuenta(EstadoCuenta.inactivo);
        usuario.setRol(RolUsuario.cliente);

        when(usuarioRepository.findById(idUsuario))
                .thenReturn(Optional.of(usuario));

        filter.doFilter(
                request,
                response,
                filterChain
        );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );
    }

    @Test
    void usuarioInexistenteNoQuedaAutenticado() throws Exception {

        Long idUsuario = 99L;

        MockHttpServletRequest request =
                requestConToken("TOKEN_USUARIO_INEXISTENTE");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        Claims claims = Mockito.mock(Claims.class);

        when(jwtService.validarYObtenerClaims(
                "TOKEN_USUARIO_INEXISTENTE"
        )).thenReturn(claims);

        when(claims.getSubject())
                .thenReturn(idUsuario.toString());

        when(usuarioRepository.findById(idUsuario))
                .thenReturn(Optional.empty());

        filter.doFilter(
                request,
                response,
                filterChain
        );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );
    }

    @Test
    void tokenInvalidoNoQuedaAutenticado() throws Exception {

        MockHttpServletRequest request =
                requestConToken("TOKEN_INVALIDO");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        when(jwtService.validarYObtenerClaims("TOKEN_INVALIDO"))
                .thenThrow(
                        new IllegalArgumentException(
                                "Token invalido"
                        )
                );

        filter.doFilter(
                request,
                response,
                filterChain
        );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );

        verify(usuarioRepository, never())
                .findById(anyLong());
    }

    @Test
    void usaRolActualDeBaseDeDatos() throws Exception {

        Long idUsuario = 20L;

        MockHttpServletRequest request =
                requestConToken("TOKEN_ROL_ANTIGUO");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        Claims claims = Mockito.mock(Claims.class);

        when(jwtService.validarYObtenerClaims(
                "TOKEN_ROL_ANTIGUO"
        )).thenReturn(claims);

        when(claims.getSubject())
                .thenReturn(idUsuario.toString());

        Usuario usuario = new Usuario();

        usuario.setIdUsuario(idUsuario);
        usuario.setEstadoCuenta(EstadoCuenta.activo);

        /*
         * Supongamos que originalmente era tecnico,
         * pero ahora PostgreSQL dice administrador.
         */
        usuario.setRol(RolUsuario.administrador);

        when(usuarioRepository.findById(idUsuario))
                .thenReturn(Optional.of(usuario));

        filter.doFilter(
                request,
                response,
                filterChain
        );

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        assertNotNull(authentication);

        assertTrue(
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(a ->
                                a.getAuthority()
                                        .equals("ROLE_ADMINISTRADOR")
                        )
        );
    }

    private MockHttpServletRequest requestConToken(
            String token
    ) {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer " + token
        );

        return request;
    }
}
