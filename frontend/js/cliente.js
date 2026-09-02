(function () {

    if (!exigirSesion('CLIENTE')) return;

    // ============================================================
    // INFORMACIÓN DEL USUARIO
    // ============================================================

    document.getElementById('texto-usuario').textContent =
        'Cliente #' + obtenerIdUsuario();

    let paginaActual = 0;


    // ============================================================
    // ELEMENTOS DEL HTML
    // ============================================================

    const mensajeErrorCrear =
        document.getElementById('mensaje-error-crear');

    const mensajeExitoCrear =
        document.getElementById('mensaje-exito-crear');

    const mensajeErrorLista =
        document.getElementById('mensaje-error-lista');

    const contenedorTabla =
        document.getElementById('contenedor-tabla');

    const paginacion =
        document.getElementById('paginacion');

    const filtroEstado =
        document.getElementById('filtro-estado');

    const filaMetricas =
        document.getElementById('fila-metricas');

    const formCrear =
        document.getElementById('form-crear');

    const evidenciaInput =
        document.getElementById('evidencia');


    // ============================================================
    // MENSAJES
    // ============================================================

    function ocultarMensajesLocales() {

        if (mensajeErrorCrear) {
            mensajeErrorCrear.classList.add('oculto');
            mensajeErrorCrear.style.display = 'none';
            mensajeErrorCrear.textContent = '';
        }

        if (mensajeExitoCrear) {
            mensajeExitoCrear.classList.add('oculto');
            mensajeExitoCrear.style.display = 'none';
            mensajeExitoCrear.textContent = '';
        }
    }


    function mostrarExitoCrear(texto) {

        if (!mensajeExitoCrear) return;

        mensajeExitoCrear.textContent = texto;
        mensajeExitoCrear.classList.remove('oculto');
        mensajeExitoCrear.style.display = 'block';

        setTimeout(function () {

            mensajeExitoCrear.classList.add('oculto');
            mensajeExitoCrear.style.display = 'none';
            mensajeExitoCrear.textContent = '';

        }, 5000);
    }


    function mostrarErrorCrear(texto) {

        if (!mensajeErrorCrear) return;

        mensajeErrorCrear.textContent = texto;
        mensajeErrorCrear.classList.remove('oculto');
        mensajeErrorCrear.style.display = 'block';
    }


    // ============================================================
    // MÉTRICAS
    // ============================================================

    async function cargarMetricas() {

        filaMetricas.innerHTML =
            '<div class="tarjeta-metrica">' +
            '<div class="valor">—</div>' +
            '<div class="etiqueta">Pendientes</div>' +
            '</div>' +

            '<div class="tarjeta-metrica">' +
            '<div class="valor">—</div>' +
            '<div class="etiqueta">En proceso</div>' +
            '</div>' +

            '<div class="tarjeta-metrica">' +
            '<div class="valor">—</div>' +
            '<div class="etiqueta">Cerradas</div>' +
            '</div>';

        try {

            const [pendientes, enProceso, cerradas] =
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
                        encodeURIComponent('Cerrada') +
                        '&size=1'
                    )

                ]);

            filaMetricas.innerHTML =

                '<div class="tarjeta-metrica">' +
                '<div class="valor">' +
                (pendientes?.totalElements ?? 0) +
                '</div>' +
                '<div class="etiqueta">Pendientes</div>' +
                '</div>' +

                '<div class="tarjeta-metrica">' +
                '<div class="valor">' +
                (enProceso?.totalElements ?? 0) +
                '</div>' +
                '<div class="etiqueta">En proceso</div>' +
                '</div>' +

                '<div class="tarjeta-metrica">' +
                '<div class="valor">' +
                (cerradas?.totalElements ?? 0) +
                '</div>' +
                '<div class="etiqueta">Cerradas</div>' +
                '</div>';

        } catch (error) {

            console.error(
                'No se pudieron cargar las métricas:',
                error
            );

        }
    }


    // ============================================================
    // CATEGORÍAS
    // ============================================================

    async function cargarCategorias() {

        try {

            const categorias =
                await apiFetch('/api/categorias');

            const select =
                document.getElementById('categoria');

            if (!select) return;

            if (Array.isArray(categorias)) {

                categorias.forEach(function (c) {

                    const opcion =
                        document.createElement('option');

                    opcion.value =
                        c.idCategoria;

                    opcion.textContent =
                        c.nombreCategoria;

                    select.appendChild(opcion);

                });
            }

        } catch (error) {

            console.error(
                'No se pudieron cargar las categorías:',
                error
            );

        }
    }


    // ============================================================
    // ESTADOS
    // ============================================================

    async function cargarEstados() {

        try {

            const estados =
                await apiFetch('/api/estados');

            if (!filtroEstado) return;

            if (Array.isArray(estados)) {

                estados.forEach(function (e) {

                    const opcion =
                        document.createElement('option');

                    opcion.value =
                        e.nombreEstado;

                    opcion.textContent =
                        e.nombreEstado;

                    filtroEstado.appendChild(opcion);

                });
            }

        } catch (error) {

            console.error(
                'No se pudieron cargar los estados:',
                error
            );

        }
    }


    // ============================================================
    // FILA DE SOLICITUD
    // ============================================================

    function filaSolicitud(s) {

        const claseBadge =
            typeof claseBadgeEstado === 'function'
                ? claseBadgeEstado(s.estado)
                : '';

        let acciones = '';

        if (
            s.estado ===
            'Resuelta - Pendiente Confirmación del Cliente'
        ) {

            acciones =

                '<div class="acciones-fila">' +

                '<button ' +
                'data-id="' + s.idSolicitud + '" ' +
                'data-resuelto="true" ' +
                'class="btn-confirmar">' +
                'Sí, quedó resuelto' +
                '</button>' +

                '<button ' +
                'data-id="' + s.idSolicitud + '" ' +
                'data-resuelto="false" ' +
                'class="btn-confirmar secundario">' +
                'Sigue el problema' +
                '</button>' +

                '</div>';
        }

        const desc =
            typeof escaparHtml === 'function'
                ? escaparHtml(s.descripcion)
                : s.descripcion;

        const est =
            typeof escaparHtml === 'function'
                ? escaparHtml(s.estado)
                : s.estado;

        const prio =
            typeof escaparHtml === 'function'
                ? escaparHtml(s.prioridad || '—')
                : (s.prioridad || '—');

        const fecha =
            typeof formatearFecha === 'function'
                ? formatearFecha(s.fechaCreacion)
                : s.fechaCreacion;

        return (

            '<tr>' +

            '<td>#' +
            s.idSolicitud +
            '</td>' +

            '<td>' +
            desc +
            '</td>' +

            '<td>' +
            '<span class="badge ' +
            claseBadge +
            '">' +
            est +
            '</span>' +
            '</td>' +

            '<td>' +
            prio +
            '</td>' +

            '<td>' +
            fecha +
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

        if (mensajeErrorLista) {

            mensajeErrorLista.classList.add('oculto');
            mensajeErrorLista.style.display = 'none';
            mensajeErrorLista.textContent = '';

        }

        contenedorTabla.innerHTML =
            typeof htmlCargando === 'function'
                ? htmlCargando()
                : 'Cargando...';

        try {

            let ruta =
                '/api/solicitudes?page=' +
                paginaActual +
                '&size=10';

            if (filtroEstado && filtroEstado.value) {

                ruta +=
                    '&estado=' +
                    encodeURIComponent(
                        filtroEstado.value
                    );
            }

            const pagina =
                await apiFetch(ruta);

            if (
                !pagina ||
                !pagina.content ||
                pagina.content.length === 0
            ) {

                contenedorTabla.innerHTML =
                    '<div class="vacio">' +
                    'No tienes solicitudes todavía.' +
                    '</div>';

                paginacion.innerHTML = '';

                return;
            }

            const filas =
                pagina.content
                    .map(filaSolicitud)
                    .join('');

            contenedorTabla.innerHTML =

                '<div class="tabla-scroll">' +

                '<table>' +

                '<thead>' +

                '<tr>' +

                '<th>ID</th>' +
                '<th>Descripción</th>' +
                '<th>Estado</th>' +
                '<th>Prioridad</th>' +
                '<th>Creada</th>' +
                '<th></th>' +

                '</tr>' +

                '</thead>' +

                '<tbody>' +

                filas +

                '</tbody>' +

                '</table>' +

                '</div>';

            renderizarPaginacion(pagina);

            contenedorTabla
                .querySelectorAll('.btn-confirmar')
                .forEach(function (boton) {

                    boton.addEventListener(
                        'click',
                        manejarConfirmar
                    );

                });

        } catch (error) {

            contenedorTabla.innerHTML = '';

            if (mensajeErrorLista) {

                mensajeErrorLista.textContent =
                    typeof error === 'string'
                        ? error
                        : (
                            error.message ||
                            'Error al cargar las solicitudes'
                        );

                mensajeErrorLista.classList.remove('oculto');
                mensajeErrorLista.style.display = 'block';
            }
        }
    }


    // ============================================================
    // PAGINACIÓN
    // ============================================================

    function renderizarPaginacion(pagina) {

        if (
            !pagina ||
            pagina.totalPages <= 1
        ) {

            paginacion.innerHTML = '';

            return;
        }

        paginacion.innerHTML =

            '<button ' +
            'class="secundario" ' +
            'id="btn-anterior" ' +
            (pagina.first ? 'disabled' : '') +
            '>' +
            'Anterior' +
            '</button>' +

            '<span>' +
            'Página ' +
            (pagina.number + 1) +
            ' de ' +
            pagina.totalPages +
            '</span>' +

            '<button ' +
            'class="secundario" ' +
            'id="btn-siguiente" ' +
            (pagina.last ? 'disabled' : '') +
            '>' +
            'Siguiente' +
            '</button>';

        const btnAnterior =
            document.getElementById('btn-anterior');

        const btnSiguiente =
            document.getElementById('btn-siguiente');

        if (btnAnterior) {

            btnAnterior.addEventListener(
                'click',
                function () {

                    if (paginaActual > 0) {
                        paginaActual--;
                        cargarSolicitudes();
                    }

                }
            );
        }

        if (btnSiguiente) {

            btnSiguiente.addEventListener(
                'click',
                function () {

                    if (paginaActual < pagina.totalPages - 1) {
                        paginaActual++;
                        cargarSolicitudes();
                    }

                }
            );
        }
    }


    // ============================================================
    // CONFIRMAR SOLICITUD
    // ============================================================

    async function manejarConfirmar(evento) {

        const boton =
            evento.currentTarget;

        const id =
            boton.getAttribute('data-id');

        const resuelto =
            boton.getAttribute('data-resuelto') === 'true';

        boton.disabled = true;

        try {

            await apiFetch(
                '/api/solicitudes/' +
                id +
                '/confirmacion',
                {
                    method: 'POST',

                    headers: {
                        'Content-Type':
                            'application/json'
                    },

                    body: JSON.stringify({
                        problemaResuelto:
                        resuelto
                    })
                }
            );

            await cargarSolicitudes();
            await cargarMetricas();

        } catch (error) {

            if (mensajeErrorLista) {

                mensajeErrorLista.textContent =
                    typeof error === 'string'
                        ? error
                        : (
                            error.message ||
                            'Error al confirmar'
                        );

                mensajeErrorLista.classList.remove('oculto');
                mensajeErrorLista.style.display = 'block';
            }

            boton.disabled = false;
        }
    }


    // ============================================================
    // VALIDAR EVIDENCIA
    // ============================================================

    function validarEvidencia(archivo) {

        if (!archivo) {
            return null;
        }

        const nombre =
            archivo.name.toLowerCase();

        const esImagen =
            archivo.type &&
            archivo.type.startsWith('image/');

        const esPdf =
            archivo.type === 'application/pdf' ||
            nombre.endsWith('.pdf');

        if (!esImagen && !esPdf) {

            return new Error(
                'La evidencia debe ser una imagen o un archivo PDF.'
            );
        }

        const maximo =
            10 * 1024 * 1024;

        if (archivo.size > maximo) {

            return new Error(
                'La evidencia no puede superar los 10 MB.'
            );
        }

        return null;
    }


    // ============================================================
    // SUBIR EVIDENCIA
    // ============================================================

    async function subirEvidencia(idSolicitud, archivo) {

        const datos = new FormData();

        datos.append(
            'archivo',
            archivo
        );

        /*
         * IMPORTANTE:
         *
         * Este endpoint debe existir en Spring Boot:
         *
         * POST /api/solicitudes/{id}/adjuntos
         *
         * El backend debe recibir:
         *
         * @RequestParam("archivo") MultipartFile archivo
         */

        return await apiFetch(
            '/api/solicitudes/' +
            idSolicitud +
            '/adjuntos',
            {
                method: 'POST',
                body: datos
            }
        );
    }


    // ============================================================
    // CREAR SOLICITUD
    // ============================================================

    if (formCrear) {

        formCrear.addEventListener(
            'submit',
            async function (evento) {

                evento.preventDefault();

                ocultarMensajesLocales();

                const btnCrear =
                    document.getElementById('btn-crear');

                const descripcionInput =
                    document.getElementById('descripcion');

                const categoriaInput =
                    document.getElementById('categoria');

                const descripcion =
                    descripcionInput
                        ? descripcionInput.value.trim()
                        : '';

                const idCategoriaValor =
                    categoriaInput
                        ? categoriaInput.value
                        : '';

                const archivo =
                    evidenciaInput
                        ? evidenciaInput.files[0]
                        : null;


                // --------------------------------------------
                // VALIDAR DESCRIPCIÓN
                // --------------------------------------------

                if (!descripcion) {

                    mostrarErrorCrear(
                        'Por favor, ingresa una descripción.'
                    );

                    return;
                }


                // --------------------------------------------
                // VALIDAR ARCHIVO
                // --------------------------------------------

                const errorArchivo =
                    validarEvidencia(archivo);

                if (errorArchivo) {

                    mostrarErrorCrear(
                        errorArchivo.message
                    );

                    return;
                }


                // --------------------------------------------
                // DESHABILITAR BOTÓN
                // --------------------------------------------

                btnCrear.disabled = true;

                btnCrear.textContent =
                    archivo
                        ? 'Creando y adjuntando...'
                        : 'Creando...';


                try {

                    // ========================================
                    // 1. CREAR SOLICITUD
                    // ========================================

                    const solicitud =
                        await apiFetch(
                            '/api/solicitudes',
                            {
                                method: 'POST',

                                headers: {
                                    'Content-Type':
                                        'application/json'
                                },

                                body: JSON.stringify({

                                    descripcion:
                                    descripcion,

                                    idCategoria:
                                        idCategoriaValor
                                            ? Number(
                                                idCategoriaValor
                                            )
                                            : null

                                })
                            }
                        );


                    // ========================================
                    // 2. OBTENER ID DE LA SOLICITUD
                    // ========================================

                    const idSolicitud =
                        solicitud?.idSolicitud ??
                        solicitud?.id ??
                        solicitud;


                    if (!idSolicitud) {

                        throw new Error(
                            'La solicitud fue creada, pero el servidor no devolvió su ID.'
                        );
                    }


                    // ========================================
                    // 3. SUBIR EVIDENCIA
                    // ========================================

                    if (archivo) {

                        try {

                            await subirEvidencia(
                                idSolicitud,
                                archivo
                            );

                            // Si llegó aquí, el archivo sí
                            // se subió correctamente.

                            mostrarExitoCrear(
                                '¡Solicitud creada y evidencia adjuntada exitosamente!'
                            );

                        } catch (errorAdjunto) {

                            console.error(
                                'Error al subir evidencia:',
                                errorAdjunto
                            );

                            /*
                             * IMPORTANTE:
                             *
                             * NO lanzamos nuevamente el error.
                             *
                             * La solicitud YA fue creada.
                             *
                             * Por lo tanto mostramos claramente
                             * qué ocurrió.
                             */

                            mostrarExitoCrear(
                                '¡Solicitud creada exitosamente! ' +
                                'Pero la evidencia no pudo adjuntarse. ' +
                                'Puedes intentar subirla nuevamente.'
                            );
                        }

                    } else {

                        mostrarExitoCrear(
                            '¡Solicitud creada exitosamente!'
                        );
                    }


                    // ========================================
                    // 4. LIMPIAR FORMULARIO
                    // ========================================

                    if (descripcionInput) {
                        descripcionInput.value = '';
                    }

                    if (categoriaInput) {
                        categoriaInput.selectedIndex = 0;
                    }

                    if (evidenciaInput) {
                        evidenciaInput.value = '';
                    }


                    // ========================================
                    // 5. ACTUALIZAR LISTADO
                    // ========================================

                    paginaActual = 0;

                    await cargarSolicitudes();

                    await cargarMetricas();


                } catch (error) {

                    console.error(
                        'Error al crear la solicitud:',
                        error
                    );

                    mostrarErrorCrear(
                        typeof error === 'string'
                            ? error
                            : (
                                error.message ||
                                'No se pudo crear la solicitud.'
                            )
                    );

                } finally {

                    btnCrear.disabled = false;

                    btnCrear.textContent =
                        'Crear solicitud';
                }

            }
        );
    }


    // ============================================================
    // FILTRO
    // ============================================================

    if (filtroEstado) {

        filtroEstado.addEventListener(
            'change',
            function () {

                paginaActual = 0;

                cargarSolicitudes();

            }
        );
    }


    // ============================================================
    // INICIALIZACIÓN
    // ============================================================

    cargarMetricas();

    cargarCategorias();

    cargarEstados();

    cargarSolicitudes();


    if (
        typeof activarNavegacionPorTabs ===
        'function'
    ) {

        activarNavegacionPorTabs();

    }

})();