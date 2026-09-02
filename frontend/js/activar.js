(function () {
    activarAlternarContrasena();

    const form = document.getElementById('form-activar');
    const mensajeError = document.getElementById('mensaje-error');
    const mensajeExito = document.getElementById('mensaje-exito');
    const btnActivar = document.getElementById('btn-activar');

    // Obtener automáticamente el token desde la URL
    const parametros = new URLSearchParams(window.location.search);
    const token = parametros.get('token');

    // Verificar que el enlace tenga token
    if (!token) {
        mostrarError(
            mensajeError,
            new Error('El enlace de activación no contiene un token válido.')
        );

        btnActivar.disabled = true;
        return;
    }

    form.addEventListener('submit', async function (evento) {
        evento.preventDefault();

        ocultarMensaje(mensajeError);
        mensajeExito.classList.add('oculto');

        const contrasena =
            document.getElementById('contrasena').value;

        const confirmar =
            document.getElementById('confirmar').value;

        if (contrasena !== confirmar) {
            mostrarError(
                mensajeError,
                new Error('Las contraseñas no coinciden.')
            );
            return;
        }

        if (contrasena.length < 8) {
            mostrarError(
                mensajeError,
                new Error('La contraseña debe tener al menos 8 caracteres.')
            );
            return;
        }

        btnActivar.disabled = true;
        btnActivar.textContent = 'Activando...';

        try {

            const usuario = await apiFetch(
                '/api/usuarios/activacion',
                {
                    method: 'POST',
                    body: JSON.stringify({
                        token: token,
                        contrasena: contrasena
                    })
                }
            );

            form.classList.add('oculto');

            mensajeExito.textContent =
                'Cuenta activada para ' +
                usuario.correo +
                '. Ya puedes iniciar sesión.';

            mensajeExito.classList.remove('oculto');

        } catch (error) {

            mostrarError(mensajeError, error);

            btnActivar.disabled = false;
            btnActivar.textContent = 'Activar cuenta';
        }
    });
})();