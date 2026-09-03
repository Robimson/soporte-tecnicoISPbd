(function () {
    if (!exigirSesion('TECNICO')) return;

    document.getElementById('texto-usuario').textContent = 'Técnico #' + obtenerIdUsuario();

    const MAX_EVIDENCIAS = 5;
    const TIPOS_PERMITIDOS = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp', 'application/pdf'];

    let paginaActual = 0;
    let archivosSeleccionados = [];

    const mensajeErrorLista = document.getElementById('mensaje-error-lista');
    const contenedorTabla = document.getElementById('contenedor-tabla');
    const paginacion = document.getElementById('paginacion');

    const panelReportar = document.getElementById('panel-reportar');
    const idSolicitudReportar = document.getElementById('id-solicitud-reportar');
    const mensajeErrorReportar = document.getElementById('mensaje-error-reportar');
    const formReportar = document.getElementById('form-reportar');
    const filaMetricas = document.getElementById('fila-metricas');
    const inputEvidencias = document.getElementById('input-evidencias');
    const listaEvidencias = document.getElementById('lista-evidencias');

    // ---------- Métricas ----------

    async function cargarMetricas() {
        filaMetricas.innerHTML =
            '<div class="tarjeta-metrica"><div class="valor">—</div><div class="etiqueta">En proceso</div></div>' +
            '<div class="tarjeta-metrica"><div class="valor">—</div><div class="etiqueta">Pendientes</div></div>' +
            '<div class="tarjeta-metrica"><div class="valor">—</div><div class="etiqueta">Resueltas hoy</div></div>' +
            '<div class="tarjeta-metrica"><div class="valor">—</div><div class="etiqueta">Total cerradas</div></div>';

        try {
            const r = await apiFetch('/api/solicitudes/mis-tareas/resumen');
            filaMetricas.innerHTML =
                '<div class="tarjeta-metrica"><div class="valor">' + r.enProceso + '</div><div class="etiqueta">En proceso</div></div>' +
                '<div class="tarjeta-metrica"><div class="valor">' + r.pendientes + '</div><div class="etiqueta">Pendientes</div></div>' +
                '<div class="tarjeta-metrica"><div class="valor">' + r.resueltasHoy + '</div><div class="etiqueta">Resueltas hoy</div></div>' +
                '<div class="tarjeta-metrica"><div class="valor">' + r.totalCerradas + '</div><div class="etiqueta">Total cerradas</div></div>';
        } catch (error) {
            console.error('No se pudieron cargar las métricas:', error);
        }
    }

    // ---------- Listado de tareas ----------

    function filaSolicitud(s) {
        const claseBadge = claseBadgeEstado(s.estado);
        let acciones = '—';
        if (s.estado === 'En Proceso') {
            acciones = '<button data-id="' + s.idSolicitud + '" class="btn-reportar">Reportar solución</button>';
        }
        return '<tr>' +
            '<td>#' + s.idSolicitud + '</td>' +
            '<td>' + escaparHtml(s.descripcion) + '</td>' +
            '<td><span class="badge ' + claseBadge + '">' + escaparHtml(s.estado) + '</span></td>' +
            '<td>' + escaparHtml(s.prioridad || '—') + '</td>' +
            '<td>' + formatearFecha(s.fechaCreacion) + '</td>' +
            '<td>' + acciones + '</td>' +
            '</tr>';
    }

    async function cargarMisTareas() {
        ocultarMensaje(mensajeErrorLista);
        contenedorTabla.innerHTML = htmlCargando();

        try {
            const pagina = await apiFetch('/api/solicitudes/mis-tareas?page=' + paginaActual + '&size=10');

            if (!pagina.content || pagina.content.length === 0) {
                contenedorTabla.innerHTML = '<div class="vacio">No tienes tareas asignadas.</div>';
                paginacion.innerHTML = '';
                return;
            }

            const filas = pagina.content.map(filaSolicitud).join('');
            contenedorTabla.innerHTML =
                '<div class="tabla-scroll"><table><thead><tr>' +
                '<th>ID</th><th>Descripción</th><th>Estado</th><th>Prioridad</th><th>Creada</th><th></th>' +
                '</tr></thead><tbody>' + filas + '</tbody></table></div>';

            renderizarPaginacion(pagina);
            contenedorTabla.querySelectorAll('.btn-reportar').forEach(function (boton) {
                boton.addEventListener('click', function () { abrirPanelReportar(boton.getAttribute('data-id')); });
            });
        } catch (error) {
            contenedorTabla.innerHTML = '';
            mostrarError(mensajeErrorLista, error);
        }
    }

    function renderizarPaginacion(pagina) {
        if (pagina.totalPages <= 1) {
            paginacion.innerHTML = '';
            return;
        }
        paginacion.innerHTML =
            '<button class="secundario" id="btn-anterior" ' + (pagina.first ? 'disabled' : '') + '>Anterior</button>' +
            '<span>Página ' + (pagina.number + 1) + ' de ' + pagina.totalPages + '</span>' +
            '<button class="secundario" id="btn-siguiente" ' + (pagina.last ? 'disabled' : '') + '>Siguiente</button>';

        const btnAnterior = document.getElementById('btn-anterior');
        const btnSiguiente = document.getElementById('btn-siguiente');
        if (btnAnterior) btnAnterior.addEventListener('click', function () { paginaActual--; cargarMisTareas(); });
        if (btnSiguiente) btnSiguiente.addEventListener('click', function () { paginaActual++; cargarMisTareas(); });
    }

    // ---------- Panel de reportar (con evidencias) ----------

    function abrirPanelReportar(idSolicitud) {
        panelReportar.classList.remove('oculto');
        idSolicitudReportar.textContent = '#' + idSolicitud;
        formReportar.dataset.idSolicitud = idSolicitud;
        document.getElementById('detalle-reporte').value = '';
        archivosSeleccionados = [];
        inputEvidencias.value = '';
        renderizarListaEvidencias();
        ocultarMensaje(mensajeErrorReportar);
        panelReportar.scrollIntoView({ behavior: 'smooth' });
    }

    function renderizarListaEvidencias() {
        if (!archivosSeleccionados.length) {
            listaEvidencias.innerHTML = '';
            return;
        }
        listaEvidencias.innerHTML = archivosSeleccionados.map(function (archivo, indice) {
            return '<div class="archivo-item">' +
                '<span>' + escaparHtml(archivo.name) + '</span>' +
                '<button type="button" class="quitar-archivo" data-indice="' + indice + '">Quitar</button>' +
                '</div>';
        }).join('');

        listaEvidencias.querySelectorAll('.quitar-archivo').forEach(function (boton) {
            boton.addEventListener('click', function () {
                archivosSeleccionados.splice(Number(boton.getAttribute('data-indice')), 1);
                renderizarListaEvidencias();
            });
        });
    }

    inputEvidencias.addEventListener('change', function () {
        const nuevos = Array.from(inputEvidencias.files);

        for (const archivo of nuevos) {
            if (archivosSeleccionados.length >= MAX_EVIDENCIAS) {
                mostrarError(mensajeErrorReportar, new Error('Máximo ' + MAX_EVIDENCIAS + ' archivos.'));
                break;
            }
            if (!TIPOS_PERMITIDOS.includes(archivo.type)) {
                mostrarError(mensajeErrorReportar, new Error('"' + archivo.name + '" no es una imagen ni un PDF válido.'));
                continue;
            }
            if (archivo.size > 10 * 1024 * 1024) {
                mostrarError(mensajeErrorReportar, new Error('"' + archivo.name + '" supera los 10 MB.'));
                continue;
            }
            archivosSeleccionados.push(archivo);
        }

        inputEvidencias.value = '';
        renderizarListaEvidencias();
    });

    document.getElementById('btn-cancelar-reporte').addEventListener('click', function () {
        panelReportar.classList.add('oculto');
    });

    formReportar.addEventListener('submit', async function (evento) {
        evento.preventDefault();
        ocultarMensaje(mensajeErrorReportar);

        const btnEnviar = document.getElementById('btn-enviar-reporte');
        btnEnviar.disabled = true;
        btnEnviar.textContent = 'Enviando...';

        try {
            const idSolicitud = formReportar.dataset.idSolicitud;
            const detalleReporte = document.getElementById('detalle-reporte').value.trim();

            await apiFetch('/api/solicitudes/' + idSolicitud + '/reportes', {
                method: 'POST',
                body: JSON.stringify({ detalleReporte: detalleReporte })
            });

            // Subir evidencias, una por una, sobre la solicitud
            for (const archivo of archivosSeleccionados) {
                const formData = new FormData();
                formData.append('archivo', archivo);

                await apiFetch('/api/solicitudes/' + idSolicitud + '/adjuntos', {
                    method: 'POST',
                    body: formData
                });
            }

            panelReportar.classList.add('oculto');
            cargarMisTareas();
            cargarMetricas();
        } catch (error) {
            mostrarError(mensajeErrorReportar, error);
        } finally {
            btnEnviar.disabled = false;
            btnEnviar.textContent = 'Enviar reporte';
        }
    });

    cargarMetricas();
    cargarMisTareas();
    activarNavegacionPorTabs();
})();