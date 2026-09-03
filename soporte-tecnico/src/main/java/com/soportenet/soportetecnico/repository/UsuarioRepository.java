package com.soportenet.soportetecnico.repository;

import com.soportenet.soportetecnico.entity.Usuario;
import java.util.List;
import com.soportenet.soportetecnico.entity.Usuario;
import com.soportenet.soportetecnico.enums.RolUsuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreo(String correo);

    /** Superusuario: listado de usuarios, filtrable por rol (caso de uso 4.4.3). */
    Page<Usuario> findByRol(RolUsuario rol, Pageable pageable);

    /**
     * Invoca sp_invitar_usuario(...). Valida que quien invita sea un
     * superusuario activo y que el correo no este en uso; crea el usuario
     * 'inactivo' sin contrasena y devuelve el token de invitacion de un solo
     * uso (nunca se genera una contrasena aqui, seccion 2.4 del documento).
     */
    @Query(value = "SELECT sp_invitar_usuario(:idSuperusuario, :nombreUsuario, :correo, CAST(:rol AS rol_usuario_tipo), :diasValidez)",
           nativeQuery = true)
    String invitarUsuario(
            @Param("idSuperusuario") Long idSuperusuario,
            @Param("nombreUsuario") String nombreUsuario,
            @Param("correo") String correo,
            @Param("rol") String rol,
            @Param("diasValidez") Integer diasValidez
    );

    /**
     * Invoca sp_activar_cuenta(...). Valida que el token exista, no este
     * usado y no haya vencido; guarda el hash ya calculado en el backend
     * (BCrypt) y activa la cuenta. Devuelve el id del usuario activado.
     */
    @Query(value = "SELECT sp_activar_cuenta(:token, :contrasenaHash)",
           nativeQuery = true)
    Long activarCuenta(
            @Param("token") String token,
            @Param("contrasenaHash") String contrasenaHash
    );

    /**
     * Invoca sp_cambiar_estado_cuenta(...). Valida que quien cambia el
     * estado sea un superusuario activo, que el usuario objetivo exista, y
     * que un superusuario no se desactive a si mismo.
     */
    @Query(value = "SELECT sp_cambiar_estado_cuenta(:idSuperusuario, :idUsuarioObjetivo, CAST(:nuevoEstado AS estado_cuenta_tipo))",
           nativeQuery = true)
    void cambiarEstadoCuenta(
            @Param("idSuperusuario") Long idSuperusuario,
            @Param("idUsuarioObjetivo") Long idUsuarioObjetivo,
            @Param("nuevoEstado") String nuevoEstado
    );

    List<Usuario> findTop8ByNombreUsuarioContainingIgnoreCaseOrderByNombreUsuario(String nombre);
}
