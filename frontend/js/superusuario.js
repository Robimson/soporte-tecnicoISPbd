(function () {
    if (!exigirSesion('SUPERUSUARIO')) return;

    document.getElementById('texto-usuario').textContent = 'Superusuario #' + obtenerIdUsuario();

    let paginaUsuarios = 0;

    // ---------- Resumen ----------

    const filaMetricas = document.getElementById('fila-metricas');

    async function cargarMetricas() {
        filaMetricas.innerHTML =
            '<div class="tarjeta-metrica"><div class="valor">—</div><div class="etiqueta">Usuarios totales</div></div>' +
            '<div class="tarjeta-metrica"><div class="valor">—</div><div class="etiqueta">Clientes</div></div>' +
            '<div class="tarjeta-metrica"><div class="valor">—</div><div class="etiqueta">Técnicos</div></div>' +
            '<div class="tarjeta-metrica"><div class="valor">—</div><div class="etiqueta">Grupos técnicos</div></div>';

        try {
            const [total, clientes, tecnicos, grupos] = await Promise.all([
                apiFetch('/api/usuarios?size=1'),
                apiFetch('/api/usuarios?rol=cliente&size=1'),
                apiFetch('/api/usuarios?rol=tecnico&size=1'),
                apiFetch('/api/grupos-tecnicos')
            ]);

            filaMetricas.innerHTML =
                '<div class="tarjeta-metrica"><div class="valor">' + total.totalElements + '</div><div class="etiqueta">Usuarios totales</div></div>' +
                '<div class="tarjeta-metrica"><div class="valor">' + clientes.totalElements + '</div><div class="etiqueta">Clientes</div></div>' +
                '<div class="tarjeta-metrica"><div class="valor">' + tecnicos.totalElements + '</div><div class="etiqueta">Técnicos</div></div>' +
                '<div class="tarjeta-metrica"><div class="valor">' + grupos.length + '</div><div class="etiqueta">Grupos técnicos</div></div>';
        } catch (error) {
            console.error('No se pudieron cargar las métricas:', error);
        }
    }

    // ---------- Invitar usuario ----------

    const mensajeErrorInvitar = document.getElementById('mensaje-error-invitar');
    const tokenGenerado = document.getElementById('token-generado');

    document.getElementById('form-invitar').addEventListener('submit', async function (evento) {
        evento.preventDefault();
        ocultarMensaje(mensajeErrorInvitar);
        tokenGenerado.classList.add('oculto');

        const btnInvitar = document.getElementById('btn-invitar');
        btnInvitar.disabled = true;
        btnInvitar.textContent = 'Enviando...';

        try {
            const nombreUsuario = document.getElementById('nombre-usuario').value.trim();
            const correo = document.getElementById('correo-invitar').value.trim();
            const rol = document.getElementById('rol-invitar').value;

            const respuesta = await apiFetch('/api/usuarios/invitaciones', {
                method: 'POST',
                body: JSON.stringify({ nombreUsuario: nombreUsuario, correo: correo, rol: rol })
            });

            document.getElementById('form-invitar').reset();
            tokenGenerado.innerHTML =
                'Invitación enviada correctamente a <strong>' +
                escaparHtml(respuesta.correo) +
                '</strong>. ' +
                'La persona recibirá un correo con el enlace para activar su cuenta.';

            tokenGenerado.classList.remove('oculto');

            cargarUsuarios();
        } catch (error) {
            mostrarError(mensajeErrorInvitar, error);
        } finally {
            btnInvitar.disabled = false;
            btnInvitar.textContent = 'Enviar invitación';
        }
    });

    // ---------- Listado de usuarios ----------

    const mensajeErrorUsuarios = document.getElementById('mensaje-error-usuarios');
    const contenedorTablaUsuarios = document.getElementById('contenedor-tabla-usuarios');
    const paginacionUsuarios = document.getElementById('paginacion-usuarios');
    const filtroRol = document.getElementById('filtro-rol');

    const ESTADOS_CUENTA = ['activo', 'suspendido', 'inactivo'];

    function filaUsuario(u) {
        const opcionesEstado = ESTADOS_CUENTA.map(function (e) {
            return '<option value="' + e + '" ' + (e === u.estadoCuenta ? 'selected' : '') + '>' + e + '</option>';
        }).join('');

        return '<tr>' +
            '<td>#' + u.idUsuario + '</td>' +
            '<td>' + escaparHtml(u.nombreUsuario) + '</td>' +
            '<td>' + escaparHtml(u.correo) + '</td>' +
            '<td>' + escaparHtml(u.rol) + '</td>' +
            '<td><span class="badge ' + claseBadgeEstadoCuenta(u.estadoCuenta) + '">' + escaparHtml(u.estadoCuenta) + '</span></td>' +
            '<td><div class="acciones-fila">' +
            '<select class="select-nuevo-estado" data-id="' + u.idUsuario + '">' + opcionesEstado + '</select>' +
            '<button class="btn-cambiar-estado secundario" data-id="' + u.idUsuario + '">Cambiar</button>' +
            '</div></td>' +
            '</tr>';
    }

    async function cargarUsuarios() {
        ocultarMensaje(mensajeErrorUsuarios);
        contenedorTablaUsuarios.innerHTML = htmlCargando();

        try {
            let ruta = '/api/usuarios?page=' + paginaUsuarios + '&size=10';
            if (filtroRol.value) {
                ruta += '&rol=' + encodeURIComponent(filtroRol.value);
            }

            const pagina = await apiFetch(ruta);

            if (!pagina.content || pagina.content.length === 0) {
                contenedorTablaUsuarios.innerHTML = '<div class="vacio">No hay usuarios.</div>';
                paginacionUsuarios.innerHTML = '';
                return;
            }

            contenedorTablaUsuarios.innerHTML =
                '<div class="tabla-scroll"><table><thead><tr>' +
                '<th>ID</th><th>Nombre</th><th>Correo</th><th>Rol</th><th>Estado</th><th></th>' +
                '</tr></thead><tbody>' + pagina.content.map(filaUsuario).join('') + '</tbody></table></div>';

            renderizarPaginacion(pagina, paginacionUsuarios, function (nueva) { paginaUsuarios = nueva; cargarUsuarios(); });

            contenedorTablaUsuarios.querySelectorAll('.btn-cambiar-estado').forEach(function (boton) {
                boton.addEventListener('click', manejarCambiarEstado);
            });
        } catch (error) {
            contenedorTablaUsuarios.innerHTML = '';
            mostrarError(mensajeErrorUsuarios, error);
        }
    }

    async function manejarCambiarEstado(evento) {
        const boton = evento.currentTarget;
        const id = boton.getAttribute('data-id');
        const select = contenedorTablaUsuarios.querySelector('.select-nuevo-estado[data-id="' + id + '"]');
        const nuevoEstado = select.value;

        boton.disabled = true;
        try {
            await apiFetch('/api/usuarios/' + id + '/estado', {
                method: 'POST',
                body: JSON.stringify({ estadoCuenta: nuevoEstado })
            });
            cargarUsuarios();
        } catch (error) {
            mostrarError(mensajeErrorUsuarios, error);
        } finally {
            boton.disabled = false;
        }
    }

    filtroRol.addEventListener('change', function () { paginaUsuarios = 0; cargarUsuarios(); });

    // ---------- Grupos tecnicos ----------

    const mensajeErrorGrupo = document.getElementById('mensaje-error-grupo');
    const mensajeErrorMiembro = document.getElementById('mensaje-error-miembro');
    const contenedorTablaGrupos = document.getElementById('contenedor-tabla-grupos');
    const selectGrupoMiembro = document.getElementById('select-grupo-miembro');
    const selectTecnicoMiembro = document.getElementById('select-tecnico-miembro');

    async function cargarGrupos() {
        try {
            const grupos = await apiFetch('/api/grupos-tecnicos');

            contenedorTablaGrupos.innerHTML = grupos.length
                ? '<div class="tabla-scroll"><table><thead><tr><th>ID</th><th>Nombre</th></tr></thead><tbody>' +
                  grupos.map(function (g) { return '<tr><td>#' + g.idGrupo + '</td><td>' + escaparHtml(g.nombreGrupo) + '</td></tr>'; }).join('') +
                  '</tbody></table></div>'
                : '<div class="vacio">Todavía no hay grupos técnicos.</div>';

            selectGrupoMiembro.innerHTML = grupos.map(function (g) {
                return '<option value="' + g.idGrupo + '">' + escaparHtml(g.nombreGrupo) + '</option>';
            }).join('') || '<option value="">Crea un grupo primero</option>';
        } catch (error) {
            console.error('No se pudieron cargar los grupos técnicos:', error);
        }
    }

    async function cargarTecnicosParaMiembro() {
        try {
            const tecnicos = await apiFetch('/api/tecnicos');
            selectTecnicoMiembro.innerHTML = tecnicos.map(function (t) {
                return '<option value="' + t.idUsuario + '">' + escaparHtml(t.nombreUsuario) + '</option>';
            }).join('') || '<option value="">No hay técnicos habilitados</option>';
        } catch (error) {
            console.error('No se pudieron cargar los técnicos:', error);
        }
    }

    document.getElementById('form-grupo').addEventListener('submit', async function (evento) {
        evento.preventDefault();
        ocultarMensaje(mensajeErrorGrupo);

        try {
            const nombreGrupo = document.getElementById('nombre-grupo').value.trim();
            await apiFetch('/api/grupos-tecnicos', {
                method: 'POST',
                body: JSON.stringify({ nombreGrupo: nombreGrupo })
            });
            document.getElementById('form-grupo').reset();
            cargarGrupos();
        } catch (error) {
            mostrarError(mensajeErrorGrupo, error);
        }
    });

    document.getElementById('form-miembro').addEventListener('submit', async function (evento) {
        evento.preventDefault();
        ocultarMensaje(mensajeErrorMiembro);

        try {
            const idGrupo = selectGrupoMiembro.value;
            const idTecnico = selectTecnicoMiembro.value;

            if (!idGrupo || !idTecnico) {
                throw new Error('Selecciona un grupo y un técnico.');
            }

            await apiFetch('/api/grupos-tecnicos/' + idGrupo + '/miembros', {
                method: 'POST',
                body: JSON.stringify({ idTecnico: Number(idTecnico) })
            });

            mensajeErrorMiembro.textContent = 'Técnico agregado al grupo.';
            mensajeErrorMiembro.classList.remove('mensaje-error');
            mensajeErrorMiembro.classList.add('mensaje-info');
            mensajeErrorMiembro.classList.remove('oculto');
        } catch (error) {
            mensajeErrorMiembro.classList.remove('mensaje-info');
            mensajeErrorMiembro.classList.add('mensaje-error');
            mostrarError(mensajeErrorMiembro, error);
        }
    });

    // ---------- Utilidad compartida de paginacion ----------

    function renderizarPaginacion(pagina, contenedor, alCambiar) {
        if (pagina.totalPages <= 1) {
            contenedor.innerHTML = '';
            return;
        }
        const idAnterior = 'ant-' + Math.random().toString(36).slice(2);
        const idSiguiente = 'sig-' + Math.random().toString(36).slice(2);
        contenedor.innerHTML =
            '<button class="secundario" id="' + idAnterior + '" ' + (pagina.first ? 'disabled' : '') + '>Anterior</button>' +
            '<span>Página ' + (pagina.number + 1) + ' de ' + pagina.totalPages + '</span>' +
            '<button class="secundario" id="' + idSiguiente + '" ' + (pagina.last ? 'disabled' : '') + '>Siguiente</button>';

        const btnAnterior = document.getElementById(idAnterior);
        const btnSiguiente = document.getElementById(idSiguiente);
        if (btnAnterior) btnAnterior.addEventListener('click', function () { alCambiar(pagina.number - 1); });
        if (btnSiguiente) btnSiguiente.addEventListener('click', function () { alCambiar(pagina.number + 1); });
    }

    cargarMetricas();
    cargarUsuarios();
    cargarGrupos();
    cargarTecnicosParaMiembro();
    activarNavegacionPorTabs();
})();
