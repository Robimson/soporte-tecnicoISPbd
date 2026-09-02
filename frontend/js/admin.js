(function () {

    if (!exigirSesion('ADMINISTRADOR')) {
        return;
    }

    document.getElementById('texto-usuario').textContent =
        'Administrador #' + obtenerIdUsuario();


    let paginaSolicitudes = 0;
    let paginaReportes = 0;

    let ultimoTotalReportesPendientes = null;


    const filaMetricas =
        document.getElementById('fila-metricas');

    const mensajeErrorSolicitudes =
        document.getElementById(
            'mensaje-error-solicitudes'
        );

    const contenedorTablaSolicitudes =
        document.getElementById(
            'contenedor-tabla-solicitudes'
        );

    const paginacionSolicitudes =
        document.getElementById(
            'paginacion-solicitudes'
        );

    const filtroEstado =
        document.getElementById(
            'filtro-estado'
        );

    const mensajeErrorReportes =
        document.getElementById(
            'mensaje-error-reportes'
        );

    const contenedorTablaReportes =
        document.getElementById(
            'contenedor-tabla-reportes'
        );

    const paginacionReportes =
        document.getElementById(
            'paginacion-reportes'
        );

    const panelAsignar =
        document.getElementById(
            'panel-asignar'
        );

    const idSolicitudAsignar =
        document.getElementById(
            'id-solicitud-asignar'
        );

    const mensajeErrorAsignar =
        document.getElementById(
            'mensaje-error-asignar'
        );

    const formAsignar =
        document.getElementById(
            'form-asignar'
        );

    const selectTecnico =
        document.getElementById(
            'select-tecnico'
        );

    const selectGrupo =
        document.getElementById(
            'select-grupo'
        );

    const selectPrioridad =
        document.getElementById(
            'select-prioridad'
        );

    const panelRechazar =
        document.getElementById(
            'panel-rechazar'
        );

    const idReporteRechazar =
        document.getElementById(
            'id-reporte-rechazar'
        );

    const mensajeErrorRechazar =
        document.getElementById(
            'mensaje-error-rechazar'
        );

    const formRechazar =
        document.getElementById(
            'form-rechazar'
        );


    // ============================================================
    // EVIDENCIAS
    // ============================================================

    const panelEvidencias =
        document.getElementById(
            'panel-evidencias'
        );

    const idSolicitudEvidencias =
        document.getElementById(
            'id-solicitud-evidencias'
        );

    const contenedorEvidencias =
        document.getElementById(
            'contenedor-evidencias'
        );

    const mensajeErrorEvidencias =
        document.getElementById(
            'mensaje-error-evidencias'
        );

    const detalleSolicitudEvidencias =
        document.getElementById(
            'detalle-solicitud-evidencias'
        );


    // ============================================================
    // FILTRO DE REPORTES
    // ============================================================

    let filtroEstadoReporte = 'pendiente';


    function crearFiltroReportes() {

        const titulo =
            document.querySelector(
                '#reportes-pendientes h2'
            );

        if (!titulo) {
            return;
        }

        if (
            document.getElementById(
                'filtro-estado-reporte'
            )
        ) {
            return;
        }

        const contenedor =
            document.createElement('div');

        contenedor.className =
            'fila-filtros';

        contenedor.style.marginBottom =
            '20px';

        contenedor.innerHTML =

            '<div class="campo">' +

            '<label for="filtro-estado-reporte">' +

            'Filtrar por estado del reporte' +

            '</label>' +

            '<select id="filtro-estado-reporte">' +

            '<option value="pendiente">' +
            'Pendientes de aprobación' +
            '</option>' +

            '<option value="">' +
            'Todos' +
            '</option>' +

            '<option value="aprobado">' +
            'Aprobados' +
            '</option>' +

            '<option value="rechazado">' +
            'Rechazados' +
            '</option>' +

            '</select>' +

            '</div>';

        titulo.insertAdjacentElement(
            'afterend',
            contenedor
        );

        const select =
            document.getElementById(
                'filtro-estado-reporte'
            );

        select.addEventListener(
            'change',
            function () {

                filtroEstadoReporte =
                    select.value;

                paginaReportes =
                    0;

                cargarReportesPendientes();
            }
        );
    }


    // ============================================================
    // PANELES FLOTANTES
    // ============================================================

    function cerrarPanelesFlotantes() {

        panelAsignar
            .classList
            .add('oculto');

        panelRechazar
            .classList
            .add('oculto');

        panelEvidencias
            .classList
            .add('oculto');

        contenedorEvidencias.innerHTML =
            '';

        detalleSolicitudEvidencias.innerHTML =
            '';
    }


    // ============================================================
    // MÉTRICAS
    // ============================================================

    async function cargarMetricas() {

        filaMetricas.innerHTML =
            '<div class="tarjeta-metrica">' +
            '<div class="valor">—</div>' +
            '<div class="etiqueta">Pendientes por asignar</div>' +
            '</div>' +

            '<div class="tarjeta-metrica">' +
            '<div class="valor">—</div>' +
            '<div class="etiqueta">En proceso</div>' +
            '</div>' +

            '<div class="tarjeta-metrica">' +
            '<div class="valor">—</div>' +
            '<div class="etiqueta">Reportes por aprobar</div>' +
            '</div>' +

            '<div class="tarjeta-metrica">' +
            '<div class="valor">—</div>' +
            '<div class="etiqueta">Esperando confirmación del cliente</div>' +
            '</div>';

        try {

            const resultados =
                await Promise.all([

                    apiFetch(
                        '/api/solicitudes?estado=' +
                        encodeURIComponent('Pendiente') +
                        '&size=1'
                    ),

                    apiFetch(
                        '/api/solicitudes?estado=' +
                        encodeURIComponent('En Proceso') +
                        '&size=1'
                    ),

                    apiFetch(
                        '/api/solicitudes?estado=' +
                        encodeURIComponent(
                            'Resuelta - Pendiente Confirmación del Cliente'
                        ) +
                        '&size=1'
                    )

                ]);

            renderizarMetricas(
                resultados[0].totalElements,
                resultados[1].totalElements,
                resultados[2].totalElements
            );

        } catch (error) {

            console.error(
                'No se pudieron cargar las métricas:',
                error
            );
        }
    }


    function renderizarMetricas(
        pendientesPorAsignar,
        enProceso,
        esperandoConfirmacion
    ) {

        const reportesPendientes =
            ultimoTotalReportesPendientes !== null
                ? ultimoTotalReportesPendientes
                : '—';

        filaMetricas.innerHTML =

            '<div class="tarjeta-metrica">' +
            '<div class="valor">' +
            pendientesPorAsignar +
            '</div>' +
            '<div class="etiqueta">Pendientes por asignar</div>' +
            '</div>' +

            '<div class="tarjeta-metrica">' +
            '<div class="valor">' +
            enProceso +
            '</div>' +
            '<div class="etiqueta">En proceso</div>' +
            '</div>' +

            '<div class="tarjeta-metrica">' +
            '<div class="valor">' +
            reportesPendientes +
            '</div>' +
            '<div class="etiqueta">Reportes por aprobar</div>' +
            '</div>' +

            '<div class="tarjeta-metrica">' +
            '<div class="valor">' +
            esperandoConfirmacion +
            '</div>' +
            '<div class="etiqueta">Esperando confirmación del cliente</div>' +
            '</div>';
    }


    // ============================================================
    // CATÁLOGOS
    // ============================================================

    async function cargarCatalogos() {

        try {

            const [
                tecnicos,
                grupos,
                prioridades,
                estados
            ] = await Promise.all([

                apiFetch('/api/tecnicos'),

                apiFetch('/api/grupos-tecnicos'),

                apiFetch('/api/prioridades'),

                apiFetch('/api/estados')

            ]);

            selectTecnico.innerHTML =
                tecnicos.map(function (t) {

                    return (
                        '<option value="' +
                        t.idUsuario +
                        '">' +
                        escaparHtml(t.nombreUsuario) +
                        ' (' +
                        escaparHtml(t.nivel) +
                        ')' +
                        '</option>'
                    );

                }).join('') ||
                '<option value="">No hay técnicos habilitados</option>';

            selectGrupo.innerHTML =
                grupos.map(function (g) {

                    return (
                        '<option value="' +
                        g.idGrupo +
                        '">' +
                        escaparHtml(g.nombreGrupo) +
                        '</option>'
                    );

                }).join('') ||
                '<option value="">No hay grupos creados</option>';

            prioridades.forEach(function (p) {

                const opcion =
                    document.createElement('option');

                opcion.value =
                    p.idPrioridad;

                opcion.textContent =
                    p.nombrePrioridad;

                selectPrioridad.appendChild(
                    opcion
                );

            });

            estados.forEach(function (e) {

                const opcion =
                    document.createElement('option');

                opcion.value =
                    e.nombreEstado;

                opcion.textContent =
                    e.nombreEstado;

                filtroEstado.appendChild(
                    opcion
                );

            });

        } catch (error) {

            console.error(
                'No se pudieron cargar los catálogos:',
                error
            );
        }
    }


    // ============================================================
    // CAMBIO TÉCNICO / GRUPO
    // ============================================================

    document
        .querySelectorAll(
            'input[name="tipo-destino"]'
        )
        .forEach(function (radio) {

            radio.addEventListener(
                'change',
                function () {

                    const seleccionado =
                        document.querySelector(
                            'input[name="tipo-destino"]:checked'
                        );

                    if (!seleccionado) {
                        return;
                    }

                    const esTecnico =
                        seleccionado.value === 'tecnico';

                    document
                        .getElementById('campo-tecnico')
                        .classList
                        .toggle(
                            'oculto',
                            !esTecnico
                        );

                    document
                        .getElementById('campo-grupo')
                        .classList
                        .toggle(
                            'oculto',
                            esTecnico
                        );
                }
            );
        });


    // ============================================================
    // FILA DE SOLICITUD
    // ============================================================

    function filaSolicitud(s) {

        const claseBadge =
            claseBadgeEstado(s.estado);

        let acciones = '—';

        if (s.estado !== 'Cerrada') {

            acciones =
                '<button ' +
                'data-id="' +
                s.idSolicitud +
                '" ' +
                'class="btn-asignar">' +
                'Asignar' +
                '</button> ';
        }

        acciones +=
            '<button ' +
            'data-id="' +
            s.idSolicitud +
            '" ' +
            'class="btn-evidencias">' +
            'Ver solicitud' +
            '</button>';

        return (

            '<tr>' +

            '<td>#' +
            s.idSolicitud +
            '</td>' +

            '<td>' +
            escaparHtml(
                s.descripcion
            ) +
            '</td>' +

            '<td>' +

            '<span class="badge ' +
            claseBadge +
            '">' +

            escaparHtml(
                s.estado
            ) +

            '</span>' +

            '</td>' +

            '<td>' +
            escaparHtml(
                s.prioridad || '—'
            ) +
            '</td>' +

            '<td>' +
            formatearFecha(
                s.fechaCreacion
            ) +
            '</td>' +

            '<td>' +
            acciones +
            '</td>' +

            '</tr>'
        );
    }


    // ============================================================
    // CARGAR SOLICITUDES
    // ============================================================

    async function cargarSolicitudes() {

        ocultarMensaje(
            mensajeErrorSolicitudes
        );

        contenedorTablaSolicitudes.innerHTML =
            htmlCargando();

        try {

            let ruta =
                '/api/solicitudes?page=' +
                paginaSolicitudes +
                '&size=10';

            if (filtroEstado.value) {

                ruta +=
                    '&estado=' +
                    encodeURIComponent(
                        filtroEstado.value
                    );
            }

            const pagina =
                await apiFetch(ruta);

            if (
                !pagina.content ||
                pagina.content.length === 0
            ) {

                contenedorTablaSolicitudes.innerHTML =
                    '<div class="vacio">' +
                    'No hay solicitudes.' +
                    '</div>';

                paginacionSolicitudes.innerHTML =
                    '';

                return;
            }

            contenedorTablaSolicitudes.innerHTML =

                '<div class="tabla-scroll">' +

                '<table>' +

                '<thead>' +

                '<tr>' +

                '<th>ID</th>' +
                '<th>Descripción</th>' +
                '<th>Estado</th>' +
                '<th>Prioridad</th>' +
                '<th>Creada</th>' +
                '<th>Acciones</th>' +

                '</tr>' +

                '</thead>' +

                '<tbody>' +

                pagina.content
                    .map(filaSolicitud)
                    .join('') +

                '</tbody>' +

                '</table>' +

                '</div>';

            renderizarPaginacion(
                pagina,
                paginacionSolicitudes,
                function (nueva) {

                    paginaSolicitudes =
                        nueva;

                    cargarSolicitudes();
                }
            );

            contenedorTablaSolicitudes
                .querySelectorAll(
                    '.btn-asignar'
                )
                .forEach(function (boton) {

                    boton.addEventListener(
                        'click',
                        function () {

                            abrirPanelAsignar(
                                boton.getAttribute(
                                    'data-id'
                                )
                            );
                        }
                    );
                });

            contenedorTablaSolicitudes
                .querySelectorAll(
                    '.btn-evidencias'
                )
                .forEach(function (boton) {

                    boton.addEventListener(
                        'click',
                        function () {

                            abrirPanelEvidencias(
                                boton.getAttribute(
                                    'data-id'
                                )
                            );
                        }
                    );
                });

        } catch (error) {

            contenedorTablaSolicitudes.innerHTML =
                '';

            mostrarError(
                mensajeErrorSolicitudes,
                error
            );
        }
    }


    // ============================================================
    // ABRIR EVIDENCIAS
    // ============================================================

    async function abrirPanelEvidencias(
        idSolicitud
    ) {

        cerrarPanelesFlotantes();

        panelEvidencias
            .classList
            .remove('oculto');

        idSolicitudEvidencias.textContent =
            '#' + idSolicitud;

        contenedorEvidencias.innerHTML =
            htmlCargando();

        detalleSolicitudEvidencias.innerHTML =
            '';

        ocultarMensaje(
            mensajeErrorEvidencias
        );

        panelEvidencias.scrollIntoView({
            behavior: 'smooth'
        });

        try {

            const solicitud =
                await apiFetch(
                    '/api/solicitudes/' +
                    idSolicitud
                );

            detalleSolicitudEvidencias.innerHTML =

                '<div class="tarjeta">' +

                '<p><strong>Descripción:</strong> ' +

                escaparHtml(
                    solicitud.descripcion || '—'
                ) +

                '</p>' +

                '<p><strong>Estado:</strong> ' +

                escaparHtml(
                    solicitud.estado || '—'
                ) +

                '</p>' +

                '<p><strong>Fecha:</strong> ' +

                formatearFecha(
                    solicitud.fechaCreacion
                ) +

                '</p>' +

                '</div>';

            const adjuntos =
                await apiFetch(
                    '/api/solicitudes/' +
                    idSolicitud +
                    '/adjuntos'
                );

            if (
                !adjuntos ||
                adjuntos.length === 0
            ) {

                contenedorEvidencias.innerHTML =

                    '<div class="vacio">' +

                    'El cliente no ha adjuntado evidencias a esta solicitud.' +

                    '</div>';

                return;
            }

            contenedorEvidencias.innerHTML =
                '<div class="evidencias-grid">' +

                adjuntos
                    .map(
                        crearTarjetaEvidencia
                    )
                    .join('') +

                '</div>';

            // ----------------------------------------------------
            // BOTONES PARA ABRIR ARCHIVOS
            // ----------------------------------------------------

            contenedorEvidencias
                .querySelectorAll(
                    '.btn-abrir-evidencia'
                )
                .forEach(function (boton) {

                    boton.addEventListener(
                        'click',
                        function () {

                            abrirEvidencia(
                                boton.getAttribute(
                                    'data-id-adjunto'
                                )
                            );

                        }
                    );

                });

        } catch (error) {

            contenedorEvidencias.innerHTML =
                '';

            mostrarError(
                mensajeErrorEvidencias,
                error
            );
        }
    }


    // ============================================================
    // ABRIR ARCHIVO CON JWT
    // ============================================================

    async function abrirEvidencia(
        idAdjunto
    ) {

        const token =
            obtenerToken();

        if (!token) {

            window.location.href =
                'login.html';

            return;
        }

        /*
         * Abrimos la pestaña inmediatamente.
         *
         * Esto evita que el navegador bloquee
         * la nueva pestaña por considerarla un
         * popup no solicitado.
         */

        const nuevaPestana =
            window.open(
                '',
                '_blank'
            );

        if (!nuevaPestana) {

            alert(
                'El navegador bloqueó la nueva pestaña. ' +
                'Permite ventanas emergentes para este sitio.'
            );

            return;
        }

        nuevaPestana.document.write(
            '<!DOCTYPE html>' +
            '<html lang="es">' +
            '<head>' +
            '<meta charset="UTF-8">' +
            '<title>Abriendo evidencia...</title>' +
            '</head>' +
            '<body style="font-family:Arial,sans-serif;text-align:center;padding:40px;">' +
            '<h2>Abriendo evidencia...</h2>' +
            '<p>Por favor espera.</p>' +
            '</body>' +
            '</html>'
        );

        try {

            const respuesta =
                await fetch(
                    API_BASE +
                    '/api/solicitudes/adjuntos/' +
                    idAdjunto +
                    '/archivo',
                    {
                        method: 'GET',

                        headers: {
                            'Authorization':
                                'Bearer ' + token
                        }
                    }
                );

            if (
                respuesta.status === 401
            ) {

                nuevaPestana.close();

                limpiarSesion();

                window.location.href =
                    'login.html';

                return;
            }

            if (!respuesta.ok) {

                let mensaje =
                    'No se pudo abrir la evidencia.';

                try {

                    const texto =
                        await respuesta.text();

                    if (texto) {

                        const cuerpo =
                            JSON.parse(texto);

                        mensaje =
                            cuerpo.mensaje ||
                            cuerpo.error ||
                            cuerpo.message ||
                            mensaje;
                    }

                } catch (error) {
                    // No hacer nada.
                }

                throw new Error(
                    mensaje
                );
            }

            /*
             * Convertimos la respuesta del servidor
             * en Blob.
             */

            const blob =
                await respuesta.blob();

            /*
             * Creamos una URL temporal.
             */

            const urlBlob =
                URL.createObjectURL(
                    blob
                );

            /*
             * La nueva pestaña navega al Blob.
             *
             * Si es PDF:
             * Chrome mostrará el visor PDF.
             *
             * Si es imagen:
             * Chrome mostrará la imagen.
             */

            nuevaPestana.location.href =
                urlBlob;

            /*
             * Liberamos la URL después de un tiempo.
             */

            setTimeout(
                function () {

                    URL.revokeObjectURL(
                        urlBlob
                    );

                },
                60000
            );

        } catch (error) {

            console.error(
                'Error al abrir evidencia:',
                error
            );

            nuevaPestana.document.body.innerHTML =

                '<div style="font-family:Arial,sans-serif;padding:40px;text-align:center;">' +

                '<h2>No se pudo abrir la evidencia</h2>' +

                '<p>' +

                escaparHtml(
                    error.message ||
                    'Error desconocido.'
                ) +

                '</p>' +

                '</div>';
        }
    }


    // ============================================================
    // TARJETA DE EVIDENCIA
    // ============================================================

    function crearTarjetaEvidencia(
        adjunto
    ) {

        const nombre =
            adjunto.nombreArchivo ||
            adjunto.nombre ||
            'Archivo';

        const tipo =
            adjunto.tipoArchivo ||
            adjunto.tipo ||
            '';

        const idAdjunto =
            adjunto.idAdjunto;

        const esImagen =
            tipo.toLowerCase()
                .startsWith('image/');

        const esPdf =
            tipo.toLowerCase()
            === 'application/pdf';

        let contenido = '';

        /*
         * Para las imágenes también usamos
         * el endpoint autenticado.
         *
         * No usamos directamente urlAlmacenamiento.
         */

        if (esImagen) {

            contenido =

                '<div class="evidencia-preview">' +

                '<div style="font-size:48px;">🖼️</div>' +

                '<p>Imagen</p>' +

                '</div>';

        } else if (esPdf) {

            contenido =

                '<div class="evidencia-preview">' +

                '<div style="font-size:48px;">📄</div>' +

                '<p>Archivo PDF</p>' +

                '</div>';

        } else {

            contenido =

                '<div class="evidencia-preview">' +

                '<div style="font-size:48px;">📎</div>' +

                '<p>Archivo adjunto</p>' +

                '</div>';
        }

        return (

            '<div class="tarjeta evidencia-card">' +

            contenido +

            '<div class="evidencia-info">' +

            '<strong>' +

            escaparHtml(nombre) +

            '</strong>' +

            '<br>' +

            '<small>' +

            escaparHtml(tipo) +

            '</small>' +

            '</div>' +

            '<br>' +

            '<button ' +

            'type="button" ' +

            'class="btn-abrir-evidencia" ' +

            'data-id-adjunto="' +
            idAdjunto +
            '">' +

            (
                esPdf
                    ? 'Abrir PDF'
                    : esImagen
                        ? 'Ver imagen'
                        : 'Abrir archivo'
            ) +

            '</button>' +

            '</div>'
        );
    }


    // ============================================================
    // CERRAR EVIDENCIAS
    // ============================================================

    document
        .getElementById(
            'btn-cerrar-evidencias'
        )
        .addEventListener(
            'click',
            function () {

                cerrarPanelesFlotantes();
            }
        );


    // ============================================================
    // ASIGNACIÓN
    // ============================================================

    function abrirPanelAsignar(
        idSolicitud
    ) {

        cerrarPanelesFlotantes();

        panelAsignar
            .classList
            .remove('oculto');

        idSolicitudAsignar.textContent =
            '#' + idSolicitud;

        formAsignar.dataset.idSolicitud =
            idSolicitud;

        document
            .getElementById(
                'motivo-reasignacion'
            )
            .value = '';

        selectPrioridad.value =
            '';

        ocultarMensaje(
            mensajeErrorAsignar
        );

        panelAsignar.scrollIntoView({
            behavior: 'smooth'
        });
    }


    document
        .getElementById(
            'btn-cancelar-asignar'
        )
        .addEventListener(
            'click',
            function () {

                cerrarPanelesFlotantes();
            }
        );


    formAsignar.addEventListener(
        'submit',
        async function (evento) {

            evento.preventDefault();

            ocultarMensaje(
                mensajeErrorAsignar
            );

            const btnConfirmar =
                document.getElementById(
                    'btn-confirmar-asignar'
                );

            btnConfirmar.disabled =
                true;

            btnConfirmar.textContent =
                'Asignando...';

            try {

                const idSolicitud =
                    formAsignar.dataset
                        .idSolicitud;

                const seleccionado =
                    document.querySelector(
                        'input[name="tipo-destino"]:checked'
                    );

                if (!seleccionado) {

                    throw new Error(
                        'Selecciona un técnico o un grupo.'
                    );
                }

                const esTecnico =
                    seleccionado.value === 'tecnico';

                const motivo =
                    document
                        .getElementById(
                            'motivo-reasignacion'
                        )
                        .value
                        .trim();

                const prioridad =
                    selectPrioridad.value;

                await apiFetch(
                    '/api/solicitudes/' +
                    idSolicitud +
                    '/asignaciones',
                    {
                        method: 'POST',

                        body:
                            JSON.stringify({
                                idTecnico:
                                    esTecnico
                                        ? Number(
                                            selectTecnico.value
                                        )
                                        : null,

                                idGrupo:
                                    esTecnico
                                        ? null
                                        : Number(
                                            selectGrupo.value
                                        ),

                                idPrioridad:
                                    prioridad
                                        ? Number(
                                            prioridad
                                        )
                                        : null,

                                motivoReasignacion:
                                    motivo ||
                                    null
                            })
                    }
                );

                cerrarPanelesFlotantes();

                cargarSolicitudes();
                cargarMetricas();

            } catch (error) {

                mostrarError(
                    mensajeErrorAsignar,
                    error
                );

            } finally {

                btnConfirmar.disabled =
                    false;

                btnConfirmar.textContent =
                    'Asignar';
            }
        }
    );


    filtroEstado.addEventListener(
        'change',
        function () {

            paginaSolicitudes =
                0;

            cargarSolicitudes();
        }
    );


    // ============================================================
    // REPORTES
    // ============================================================

    function filaReporte(r) {

        return (

            '<tr>' +

            '<td>#' +
            r.idReporte +
            '</td>' +

            '<td>Solicitud #' +
            r.idSolicitud +
            '</td>' +

            '<td>Técnico #' +
            r.idTecnico +
            '</td>' +

            '<td>' +
            escaparHtml(
                r.detalleReporte
            ) +
            '</td>' +

            '<td>' +
            formatearFecha(
                r.fechaEnvio
            ) +
            '</td>' +

            '<td>' +

            '<div class="acciones-fila">' +

            (
                r.estadoAprobacion === 'pendiente'
                    ? (
                        '<button data-id="' +
                        r.idReporte +
                        '" class="btn-aprobar">' +
                        'Aprobar' +
                        '</button>' +

                        '<button data-id="' +
                        r.idReporte +
                        '" class="btn-rechazar secundario">' +
                        'Rechazar' +
                        '</button>'
                    )
                    : '—'
            ) +

            '</div>' +

            '</td>' +

            '</tr>'
        );
    }


    async function cargarReportesPendientes() {

        ocultarMensaje(
            mensajeErrorReportes
        );

        contenedorTablaReportes.innerHTML =
            htmlCargando();

        try {

            let ruta =
                '/api/reportes?page=' +
                paginaReportes +
                '&size=10';

            if (filtroEstadoReporte) {

                ruta +=
                    '&estado=' +
                    encodeURIComponent(
                        filtroEstadoReporte
                    );
            }

            const pagina =
                await apiFetch(ruta);

            if (
                filtroEstadoReporte === 'pendiente'
            ) {

                ultimoTotalReportesPendientes =
                    pagina.totalElements;

                actualizarTarjetaReportesPendientes();
            }

            if (
                !pagina.content ||
                pagina.content.length === 0
            ) {

                let mensaje =
                    'No hay reportes.';

                if (
                    filtroEstadoReporte ===
                    'pendiente'
                ) {

                    mensaje =
                        'No hay reportes pendientes de aprobación.';

                } else if (
                    filtroEstadoReporte ===
                    'aprobado'
                ) {

                    mensaje =
                        'No hay reportes aprobados.';

                } else if (
                    filtroEstadoReporte ===
                    'rechazado'
                ) {

                    mensaje =
                        'No hay reportes rechazados.';
                }

                contenedorTablaReportes.innerHTML =
                    '<div class="vacio">' +
                    mensaje +
                    '</div>';

                paginacionReportes.innerHTML =
                    '';

                return;
            }

            contenedorTablaReportes.innerHTML =

                '<div class="tabla-scroll">' +

                '<table>' +

                '<thead>' +

                '<tr>' +

                '<th>ID</th>' +
                '<th>Solicitud</th>' +
                '<th>Técnico</th>' +
                '<th>Detalle</th>' +
                '<th>Enviado</th>' +
                '<th>Acciones</th>' +

                '</tr>' +

                '</thead>' +

                '<tbody>' +

                pagina.content
                    .map(filaReporte)
                    .join('') +

                '</tbody>' +

                '</table>' +

                '</div>';

            renderizarPaginacion(
                pagina,
                paginacionReportes,
                function (nueva) {

                    paginaReportes =
                        nueva;

                    cargarReportesPendientes();
                }
            );

            contenedorTablaReportes
                .querySelectorAll(
                    '.btn-aprobar'
                )
                .forEach(function (boton) {

                    boton.addEventListener(
                        'click',
                        function () {

                            manejarAprobar(
                                boton
                            );
                        }
                    );
                });

            contenedorTablaReportes
                .querySelectorAll(
                    '.btn-rechazar'
                )
                .forEach(function (boton) {

                    boton.addEventListener(
                        'click',
                        function () {

                            abrirPanelRechazar(
                                boton.getAttribute(
                                    'data-id'
                                )
                            );
                        }
                    );
                });

        } catch (error) {

            contenedorTablaReportes.innerHTML =
                '';

            mostrarError(
                mensajeErrorReportes,
                error
            );
        }
    }


    function actualizarTarjetaReportesPendientes() {

        const tarjetas =
            filaMetricas
                .querySelectorAll(
                    '.tarjeta-metrica'
                );

        if (
            tarjetas[2] &&
            ultimoTotalReportesPendientes !== null
        ) {

            tarjetas[2]
                .querySelector(
                    '.valor'
                )
                .textContent =
                ultimoTotalReportesPendientes;
        }
    }


    async function manejarAprobar(
        boton
    ) {

        const idReporte =
            boton.getAttribute(
                'data-id'
            );

        const confirmado =
            await confirmarAccion(

                'Aprobar reporte #' +
                idReporte,

                'El cliente será notificado para confirmar si el problema quedó resuelto.',

                'Aprobar'
            );

        if (!confirmado) {
            return;
        }

        boton.disabled =
            true;

        try {

            await apiFetch(
                '/api/reportes/' +
                idReporte +
                '/aprobacion',
                {
                    method: 'POST',

                    body:
                        JSON.stringify({})
                }
            );

            cargarReportesPendientes();

            cargarSolicitudes();

            cargarMetricas();

        } catch (error) {

            mostrarError(
                mensajeErrorReportes,
                error
            );

            boton.disabled =
                false;
        }
    }


    // ============================================================
    // RECHAZAR REPORTE
    // ============================================================

    function abrirPanelRechazar(
        idReporte
    ) {

        cerrarPanelesFlotantes();

        panelRechazar
            .classList
            .remove('oculto');

        idReporteRechazar.textContent =
            '#' + idReporte;

        formRechazar.dataset.idReporte =
            idReporte;

        document
            .getElementById(
                'comentario-rechazo'
            )
            .value = '';

        ocultarMensaje(
            mensajeErrorRechazar
        );

        panelRechazar.scrollIntoView({
            behavior: 'smooth'
        });
    }


    document
        .getElementById(
            'btn-cancelar-rechazar'
        )
        .addEventListener(
            'click',
            function () {

                cerrarPanelesFlotantes();
            }
        );


    formRechazar.addEventListener(
        'submit',
        async function (evento) {

            evento.preventDefault();

            ocultarMensaje(
                mensajeErrorRechazar
            );

            const btnConfirmar =
                document.getElementById(
                    'btn-confirmar-rechazar'
                );

            btnConfirmar.disabled =
                true;

            btnConfirmar.textContent =
                'Rechazando...';

            try {

                const idReporte =
                    formRechazar.dataset
                        .idReporte;

                const comentario =
                    document
                        .getElementById(
                            'comentario-rechazo'
                        )
                        .value
                        .trim();

                await apiFetch(
                    '/api/reportes/' +
                    idReporte +
                    '/rechazo',
                    {
                        method: 'POST',

                        body:
                            JSON.stringify({
                                comentarioRechazo:
                                comentario
                            })
                    }
                );

                cerrarPanelesFlotantes();

                cargarReportesPendientes();

                cargarSolicitudes();

                cargarMetricas();

            } catch (error) {

                mostrarError(
                    mensajeErrorRechazar,
                    error
                );

            } finally {

                btnConfirmar.disabled =
                    false;

                btnConfirmar.textContent =
                    'Rechazar reporte';
            }
        }
    );


    // ============================================================
    // PAGINACIÓN
    // ============================================================

    function renderizarPaginacion(
        pagina,
        contenedor,
        alCambiar
    ) {

        if (pagina.totalPages <= 1) {

            contenedor.innerHTML =
                '';

            return;
        }

        const idAnterior =
            'ant-' +
            Math.random()
                .toString(36)
                .slice(2);

        const idSiguiente =
            'sig-' +
            Math.random()
                .toString(36)
                .slice(2);

        contenedor.innerHTML =

            '<button class="secundario" id="' +
            idAnterior +
            '" ' +
            (pagina.first
                ? 'disabled'
                : '') +
            '>' +

            'Anterior' +

            '</button>' +

            '<span>' +

            'Página ' +
            (pagina.number + 1) +
            ' de ' +
            pagina.totalPages +

            '</span>' +

            '<button class="secundario" id="' +
            idSiguiente +
            '" ' +
            (pagina.last
                ? 'disabled'
                : '') +
            '>' +

            'Siguiente' +

            '</button>';

        const btnAnterior =
            document.getElementById(
                idAnterior
            );

        const btnSiguiente =
            document.getElementById(
                idSiguiente
            );

        if (btnAnterior) {

            btnAnterior.addEventListener(
                'click',
                function () {

                    alCambiar(
                        pagina.number - 1
                    );
                }
            );
        }

        if (btnSiguiente) {

            btnSiguiente.addEventListener(
                'click',
                function () {

                    alCambiar(
                        pagina.number + 1
                    );
                }
            );
        }
    }


    // ============================================================
    // NAVEGACIÓN POR PESTAÑAS
    // ============================================================

    function activarNavegacionPorTabs() {

        const enlaces =
            document.querySelectorAll(
                '.sidebar-nav a'
            );

        const secciones =
            document.querySelectorAll(
                '.contenido-principal > .seccion'
            );

        function mostrarSeccion(
            idObjetivo
        ) {

            cerrarPanelesFlotantes();

            secciones.forEach(
                function (seccion) {

                    seccion
                        .classList
                        .toggle(
                            'oculto',
                            seccion.id !== idObjetivo
                        );
                }
            );

            enlaces.forEach(
                function (enlace) {

                    const esActivo =
                        enlace.getAttribute('href') ===
                        '#' + idObjetivo;

                    enlace
                        .classList
                        .toggle(
                            'activo',
                            esActivo
                        );
                }
            );
        }

        enlaces.forEach(function (enlace) {

            enlace.addEventListener(
                'click',
                function (evento) {

                    const href =
                        enlace.getAttribute('href') || '';

                    if (href.indexOf('#') !== 0) {
                        return;
                    }

                    evento.preventDefault();

                    mostrarSeccion(
                        href.slice(1)
                    );
                }
            );
        });

        const idInicial =
            (secciones[0] && secciones[0].id) ||
            'resumen';

        mostrarSeccion(idInicial);
    }


    // ============================================================
    // INICIO
    // ============================================================

    crearFiltroReportes();

    cargarMetricas();

    cargarCatalogos();

    cargarSolicitudes();

    cargarReportesPendientes();

    activarNavegacionPorTabs();

})();