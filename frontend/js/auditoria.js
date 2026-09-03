(function () {
    if (!exigirSesion('SUPERUSUARIO')) return;

    // ---------- Métricas ----------

    const filaMetricasAuditoria = document.getElementById('fila-metricas-auditoria');

    async function cargarMetricasAuditoria() {
        try {
            const r = await apiFetch('/api/auditoria/resumen');
            filaMetricasAuditoria.innerHTML =
                '<div class="tarjeta-metrica"><div class="valor">' + r.sesionesActivas + '</div><div class="etiqueta">Sesiones activas</div></div>' +
                '<div class="tarjeta-metrica"><div class="valor">' + r.cambiosHoy + '</div><div class="etiqueta">Cambios hoy</div></div>' +
                '<div class="tarjeta-metrica"><div class="valor">' + r.inserts + '</div><div class="etiqueta">Inserts</div></div>' +
                '<div class="tarjeta-metrica"><div class="valor">' + r.updates + '</div><div class="etiqueta">Updates</div></div>' +
                '<div class="tarjeta-metrica"><div class="valor">' + r.eliminaciones + '</div><div class="etiqueta">Eliminaciones</div></div>' +
                '<div class="tarjeta-metrica"><div class="valor">' + r.accionesSistema + '</div><div class="etiqueta">Acciones sistema</div></div>';
        } catch (error) {
            console.error('No se pudieron cargar las métricas de auditoría:', error);
        }
    }

    // ---------- Subtabs internos ----------

    const botonesSubtab = document.querySelectorAll('.subtab-btn');
    const subsecciones = {
        cambios: document.getElementById('subtab-cambios'),
        sesiones: document.getElementById('subtab-sesiones'),
        historial: document.getElementById('subtab-historial')
    };

    botonesSubtab.forEach(function (boton) {
        boton.addEventListener('click', function () {
            const objetivo = boton.getAttribute('data-subtab');
            botonesSubtab.forEach(function (b) { b.classList.toggle('activo', b === boton); });
            Object.keys(subsecciones).forEach(function (clave) {
                subsecciones[clave].classList.toggle('oculto', clave !== objetivo);
            });
        });
    });

    // ---------- Modal genérico de detalle ----------

    function mostrarModalDetalle(titulo, filasHtml) {
        const overlay = document.createElement('div');
        overlay.className = 'overlay-modal';
        overlay.innerHTML =
            '<div class="modal modal-detalle">' +
            '<h3>' + escaparHtml(titulo) + '</h3>' +
            filasHtml +
            '<div class="modal-acciones">' +
            '<button type="button" data-accion="cerrar">Cerrar</button>' +
            '</div>' +
            '</div>';

        function cerrar() {
            if (overlay.parentNode) document.body.removeChild(overlay);
            document.removeEventListener('keydown', alPresionarTecla);
        }
        function alPresionarTecla(e) { if (e.key === 'Escape') cerrar(); }

        overlay.addEventListener('click', function (e) { if (e.target === overlay) cerrar(); });
        overlay.querySelector('[data-accion="cerrar"]').addEventListener('click', cerrar);
        document.addEventListener('keydown', alPresionarTecla);
        document.body.appendChild(overlay);
    }

    function filaDetalle(etiqueta, valor) {
        return '<p><strong>' + escaparHtml(etiqueta) + ':</strong><br>' + valor + '</p>';
    }

    function bloqueJson(objeto) {
        if (!objeto) return '<em>—</em>';
        return '<pre>' + escaparHtml(JSON.stringify(objeto, null, 2)) + '</pre>';
    }

    // ---------- Paginación local ----------

    function renderizarPaginacionLocal(pagina, contenedor, alCambiar) {
        if (pagina.totalPages <= 1) { contenedor.innerHTML = ''; return; }
        const idAnt = 'ant-' + Math.random().toString(36).slice(2);
        const idSig = 'sig-' + Math.random().toString(36).slice(2);
        contenedor.innerHTML =
            '<button class="secundario" id="' + idAnt + '" ' + (pagina.first ? 'disabled' : '') + '>Anterior</button>' +
            '<span>Página ' + (pagina.number + 1) + ' de ' + pagina.totalPages + '</span>' +
            '<button class="secundario" id="' + idSig + '" ' + (pagina.last ? 'disabled' : '') + '>Siguiente</button>';
        document.getElementById(idAnt).addEventListener('click', function () { alCambiar(pagina.number - 1); });
        document.getElementById(idSig).addEventListener('click', function () { alCambiar(pagina.number + 1); });
    }

    // ---------- Autocompletar de usuario (reutilizable) ----------

    function activarAutocompletarUsuario(idInputTexto, idInputOculto, idLista, alSeleccionar) {
        const inputTexto = document.getElementById(idInputTexto);
        const inputOculto = document.getElementById(idInputOculto);
        const lista = document.getElementById(idLista);

        let temporizador = null;

        inputTexto.addEventListener('input', function () {
            inputOculto.value = '';

            const termino = inputTexto.value.trim();

            if (temporizador) clearTimeout(temporizador);

            if (termino.length < 2) {
                lista.classList.add('oculto');
                lista.innerHTML = '';
                return;
            }

            temporizador = setTimeout(async function () {
                try {
                    const resultados = await apiFetch('/api/auditoria/usuarios/buscar?nombre=' + encodeURIComponent(termino));

                    if (!resultados.length) {
                        lista.innerHTML = '<div class="autocompletar-item"><span class="detalle">Sin coincidencias</span></div>';
                        lista.classList.remove('oculto');
                        return;
                    }

                    lista.innerHTML = resultados.map(function (u) {
                        return '<div class="autocompletar-item" data-id="' + u.idUsuario + '" data-nombre="' + escaparHtml(u.nombreUsuario) + '">' +
                            '<div class="nombre">' + escaparHtml(u.nombreUsuario) + '</div>' +
                            '<div class="detalle">' + escaparHtml(u.rol) + ' — ' + escaparHtml(u.correo) + '</div>' +
                            '</div>';
                    }).join('');

                    lista.classList.remove('oculto');

                    lista.querySelectorAll('.autocompletar-item[data-id]').forEach(function (item) {
                        item.addEventListener('click', function () {
                            inputTexto.value = item.getAttribute('data-nombre');
                            inputOculto.value = item.getAttribute('data-id');
                            lista.classList.add('oculto');
                            alSeleccionar();
                        });
                    });
                } catch (error) {
                    console.error('Error buscando usuarios:', error);
                }
            }, 300);
        });

        document.addEventListener('click', function (e) {
            if (!lista.contains(e.target) && e.target !== inputTexto) {
                lista.classList.add('oculto');
            }
        });
    }

    // ---------- Cambios de datos ----------

    let paginaDatos = 0;
    const mensajeErrorDatos = document.getElementById('mensaje-error-auditoria-datos');
    const contenedorTablaDatos = document.getElementById('contenedor-tabla-auditoria-datos');
    const paginacionDatos = document.getElementById('paginacion-auditoria-datos');
    const filtroTabla = document.getElementById('filtro-tabla-auditoria');
    const filtroOperacion = document.getElementById('filtro-operacion-auditoria');
    const filtroUsuarioDatosId = document.getElementById('filtro-usuario-datos-id');

    let cacheAuditoriaDatos = [];

    function filaAuditoriaDatos(a, indice) {
        return '<tr class="fila-clic" data-indice="' + indice + '">' +
            '<td>' + formatearFecha(a.fecha) + '</td>' +
            '<td>' + (a.idUsuarioResponsable || '—') + '</td>' +
            '<td>' + escaparHtml(a.nombreUsuario || 'Sistema') + '</td>' +
            '<td>' + escaparHtml(a.rol || '—') + '</td>' +
            '<td>' + escaparHtml(a.tablaAfectada) + '</td>' +
            '<td><span class="badge">' + escaparHtml(a.operacion) + '</span></td>' +
            '</tr>';
    }

    async function cargarAuditoriaDatos() {
        ocultarMensaje(mensajeErrorDatos);
        contenedorTablaDatos.innerHTML = htmlCargando();

        try {
            let ruta = '/api/auditoria/datos?page=' + paginaDatos + '&size=15';
            if (filtroTabla.value.trim()) ruta += '&tabla=' + encodeURIComponent(filtroTabla.value.trim());
            if (filtroOperacion.value) ruta += '&operacion=' + encodeURIComponent(filtroOperacion.value);
            if (filtroUsuarioDatosId.value) ruta += '&usuario=' + encodeURIComponent(filtroUsuarioDatosId.value);

            const pagina = await apiFetch(ruta);
            cacheAuditoriaDatos = pagina.content;

            if (!pagina.content.length) {
                contenedorTablaDatos.innerHTML = '<div class="vacio">No hay registros.</div>';
                paginacionDatos.innerHTML = '';
                return;
            }

            contenedorTablaDatos.innerHTML =
                '<div class="tabla-scroll"><table><thead><tr>' +
                '<th>Fecha</th><th>ID Usuario</th><th>Usuario</th><th>Rol</th><th>Tabla</th><th>Operación</th>' +
                '</tr></thead><tbody>' +
                pagina.content.map(filaAuditoriaDatos).join('') +
                '</tbody></table></div>';

            renderizarPaginacionLocal(pagina, paginacionDatos, function (n) { paginaDatos = n; cargarAuditoriaDatos(); });

            contenedorTablaDatos.querySelectorAll('.fila-clic').forEach(function (fila) {
                fila.addEventListener('click', function () {
                    const a = cacheAuditoriaDatos[Number(fila.getAttribute('data-indice'))];
                    mostrarModalDetalle('Auditoría #' + a.idAuditoria,
                        filaDetalle('Fecha', formatearFecha(a.fecha)) +
                        filaDetalle('Usuario responsable', escaparHtml(a.nombreUsuario || 'Sistema')) +
                        filaDetalle('ID Usuario', a.idUsuarioResponsable || '—') +
                        filaDetalle('Rol', escaparHtml(a.rol || '—')) +
                        filaDetalle('Tabla afectada', escaparHtml(a.tablaAfectada)) +
                        filaDetalle('Operación', escaparHtml(a.operacion)) +
                        filaDetalle('Datos anteriores', bloqueJson(a.datosAnteriores)) +
                        filaDetalle('Datos nuevos', bloqueJson(a.datosNuevos))
                    );
                });
            });
        } catch (error) {
            contenedorTablaDatos.innerHTML = '';
            mostrarError(mensajeErrorDatos, error);
        }
    }

    filtroTabla.addEventListener('input', function () { paginaDatos = 0; cargarAuditoriaDatos(); });
    filtroOperacion.addEventListener('change', function () { paginaDatos = 0; cargarAuditoriaDatos(); });

    activarAutocompletarUsuario('filtro-usuario-datos', 'filtro-usuario-datos-id', 'lista-usuario-datos', function () {
        paginaDatos = 0;
        cargarAuditoriaDatos();
    });

    document.getElementById('btn-limpiar-usuario-datos').addEventListener('click', function () {
        document.getElementById('filtro-usuario-datos').value = '';
        filtroUsuarioDatosId.value = '';
        paginaDatos = 0;
        cargarAuditoriaDatos();
    });

    // ---------- Sesiones ----------

    let paginaSesiones = 0;
    const mensajeErrorSesiones = document.getElementById('mensaje-error-auditoria-sesiones');
    const contenedorTablaSesiones = document.getElementById('contenedor-tabla-auditoria-sesiones');
    const paginacionSesiones = document.getElementById('paginacion-auditoria-sesiones');
    const filtroActivas = document.getElementById('filtro-activas-sesion');
    const filtroUsuarioSesionesId = document.getElementById('filtro-usuario-sesiones-id');

    let cacheSesiones = [];

    function filaSesion(s, indice) {
        return '<tr class="fila-clic" data-indice="' + indice + '">' +
            '<td>' + escaparHtml(s.nombreUsuario || '—') + '</td>' +
            '<td>' + escaparHtml(s.rol || '—') + '</td>' +
            '<td>' + escaparHtml(s.ipOrigen || '—') + '</td>' +
            '<td>' + formatearFecha(s.fechaEntrada) + '</td>' +
            '<td>' + (s.activa ? '<span class="badge badge-activo">ACTIVA</span>' : formatearFecha(s.fechaSalida)) + '</td>' +
            '</tr>';
    }

    async function cargarSesiones() {
        ocultarMensaje(mensajeErrorSesiones);
        contenedorTablaSesiones.innerHTML = htmlCargando();

        try {
            let ruta = '/api/auditoria/sesiones?page=' + paginaSesiones + '&size=15';
            if (filtroActivas.checked) ruta += '&activas=true';
            if (filtroUsuarioSesionesId.value) ruta += '&usuario=' + encodeURIComponent(filtroUsuarioSesionesId.value);

            const pagina = await apiFetch(ruta);
            cacheSesiones = pagina.content;

            if (!pagina.content.length) {
                contenedorTablaSesiones.innerHTML = '<div class="vacio">No hay sesiones.</div>';
                paginacionSesiones.innerHTML = '';
                return;
            }

            contenedorTablaSesiones.innerHTML =
                '<div class="tabla-scroll"><table><thead><tr>' +
                '<th>Usuario</th><th>Rol</th><th>IP</th><th>Entrada</th><th>Salida</th>' +
                '</tr></thead><tbody>' +
                pagina.content.map(filaSesion).join('') +
                '</tbody></table></div>';

            renderizarPaginacionLocal(pagina, paginacionSesiones, function (n) { paginaSesiones = n; cargarSesiones(); });

            contenedorTablaSesiones.querySelectorAll('.fila-clic').forEach(function (fila) {
                fila.addEventListener('click', function () {
                    const s = cacheSesiones[Number(fila.getAttribute('data-indice'))];
                    mostrarModalDetalle('Sesión #' + s.idSesion,
                        filaDetalle('Usuario', escaparHtml(s.nombreUsuario || '—')) +
                        filaDetalle('ID Usuario', s.idUsuario) +
                        filaDetalle('Rol', escaparHtml(s.rol || '—')) +
                        filaDetalle('IP origen', escaparHtml(s.ipOrigen || '—')) +
                        filaDetalle('Inicio', formatearFecha(s.fechaEntrada)) +
                        filaDetalle('Última actividad', formatearFecha(s.ultimaActividad)) +
                        filaDetalle('Estado', s.activa ? '<span class="badge badge-activo">ACTIVA</span>' : formatearFecha(s.fechaSalida))
                    );
                });
            });
        } catch (error) {
            contenedorTablaSesiones.innerHTML = '';
            mostrarError(mensajeErrorSesiones, error);
        }
    }

    filtroActivas.addEventListener('change', function () { paginaSesiones = 0; cargarSesiones(); });

    activarAutocompletarUsuario('filtro-usuario-sesiones', 'filtro-usuario-sesiones-id', 'lista-usuario-sesiones', function () {
        paginaSesiones = 0;
        cargarSesiones();
    });

    document.getElementById('btn-limpiar-usuario-sesiones').addEventListener('click', function () {
        document.getElementById('filtro-usuario-sesiones').value = '';
        filtroUsuarioSesionesId.value = '';
        paginaSesiones = 0;
        cargarSesiones();
    });

    // ---------- Historial de solicitudes ----------

    const mensajeErrorHistorial = document.getElementById('mensaje-error-historial');
    const contenedorHistorial = document.getElementById('contenedor-historial');
    const inputIdSolicitud = document.getElementById('input-id-solicitud-historial');

    document.getElementById('btn-buscar-historial').addEventListener('click', async function () {
        ocultarMensaje(mensajeErrorHistorial);
        contenedorHistorial.innerHTML = '';

        const idSolicitud = inputIdSolicitud.value.trim();
        if (!idSolicitud) {
            mostrarError(mensajeErrorHistorial, new Error('Ingresa un ID de solicitud.'));
            return;
        }

        contenedorHistorial.innerHTML = htmlCargando();

        try {
            const historial = await apiFetch('/api/auditoria/historial/' + idSolicitud);

            if (!historial.length) {
                contenedorHistorial.innerHTML = '<div class="vacio">No hay historial para esta solicitud.</div>';
                return;
            }

            contenedorHistorial.innerHTML =
                '<h3>Solicitud #' + escaparHtml(idSolicitud) + '</h3>' +
                '<div class="linea-tiempo">' +
                historial.map(function (h) {
                    return '<div class="paso-tiempo">' +
                        '<div class="paso-fecha">' + formatearFecha(h.fechaCambio) + '</div>' +
                        '<div class="paso-usuario">' + escaparHtml(h.rolResponsable || 'SISTEMA') + ' — ' + escaparHtml(h.nombreUsuarioResponsable) + '</div>' +
                        '<div class="paso-cambio">' +
                        (h.estadoAnterior ? escaparHtml(h.estadoAnterior) + ' → ' : '') +
                        '<strong>' + escaparHtml(h.estadoNuevo) + '</strong>' +
                        '</div>' +
                        '</div>';
                }).join('') +
                '</div>';
        } catch (error) {
            contenedorHistorial.innerHTML = '';
            mostrarError(mensajeErrorHistorial, error);
        }
    });

    // ---------- Inicialización ----------

    cargarMetricasAuditoria();
    cargarAuditoriaDatos();
    cargarSesiones();
})();