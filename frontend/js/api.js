// ============================================================
// API.JS - SOPORTENET
// ============================================================
//
// Capa de presentación:
// - Se comunica con el backend.
// - Guarda y recupera la sesión.
// - Envía automáticamente el JWT.
// - Maneja respuestas y errores.
// - Permite enviar JSON y archivos mediante FormData.
//
// IMPORTANTE:
// Cuando se envía un archivo mediante FormData,
// NO se establece manualmente Content-Type.
// El navegador lo configura automáticamente.
// ============================================================


const API_BASE = 'http://localhost:8080';


// ============================================================
// SESIÓN
// ============================================================

function guardarSesion(token, idUsuario, rol) {

    localStorage.setItem(
        'token',
        token
    );

    localStorage.setItem(
        'idUsuario',
        idUsuario
    );

    localStorage.setItem(
        'rol',
        rol
    );
}


function limpiarSesion() {

    localStorage.removeItem('token');
    localStorage.removeItem('idUsuario');
    localStorage.removeItem('rol');
}


function obtenerToken() {

    return localStorage.getItem('token');

}


function obtenerRol() {

    return localStorage.getItem('rol');

}


function obtenerIdUsuario() {

    return localStorage.getItem('idUsuario');

}


// ============================================================
// PÁGINAS SEGÚN ROL
// ============================================================

const PAGINA_POR_ROL = {

    CLIENTE: 'cliente.html',

    TECNICO: 'tecnico.html',

    ADMINISTRADOR: 'admin.html',

    SUPERUSUARIO: 'superusuario.html'

};


// ============================================================
// EXIGIR SESIÓN
// ============================================================

function exigirSesion(rolEsperado) {

    const token =
        obtenerToken();


    if (!token) {

        window.location.href =
            'login.html';

        return false;
    }


    if (
        rolEsperado &&
        obtenerRol() !== rolEsperado
    ) {

        window.location.href =
            'login.html';

        return false;
    }


    return true;

}


// ============================================================
// CERRAR SESIÓN
// ============================================================

function cerrarSesion() {

    limpiarSesion();

    window.location.href =
        'login.html';

}


// ============================================================
// API FETCH
// ============================================================
//
// Esta función es especialmente importante para los adjuntos.
//
// JSON:
//     Content-Type: application/json
//
// FormData:
//     NO se establece Content-Type.
//     El navegador crea:
//     multipart/form-data; boundary=...
//
// ============================================================

async function apiFetch(
    path,
    options = {}
) {

    const token =
        obtenerToken();


    // --------------------------------------------------------
    // COPIAR HEADERS
    // --------------------------------------------------------

    const headers =
        Object.assign(
            {},
            options.headers || {}
        );


    // --------------------------------------------------------
    // DETECTAR SI SE ENVÍA ARCHIVO
    // --------------------------------------------------------

    const esFormData =
        options.body instanceof FormData;


    // --------------------------------------------------------
    // CONTENT-TYPE
    // --------------------------------------------------------

    if (esFormData) {

        /*
         * MUY IMPORTANTE:
         *
         * No colocar:
         *
         * Content-Type: application/json
         *
         * cuando enviamos FormData.
         *
         * El navegador necesita colocar automáticamente:
         *
         * multipart/form-data; boundary=...
         */

        delete headers['Content-Type'];

        delete headers['content-type'];

    } else {

        /*
         * Las demás peticiones utilizan JSON.
         */

        if (
            !headers['Content-Type'] &&
            !headers['content-type']
        ) {

            headers['Content-Type'] =
                'application/json';

        }

    }


    // --------------------------------------------------------
    // JWT
    // --------------------------------------------------------

    if (token) {

        headers['Authorization'] =
            'Bearer ' + token;

    }


    // --------------------------------------------------------
    // PETICIÓN
    // --------------------------------------------------------

    let respuesta;


    try {

        respuesta =
            await fetch(
                API_BASE + path,
                Object.assign(
                    {},
                    options,
                    {
                        headers: headers
                    }
                )
            );

    } catch (error) {

        console.error(
            'Error de conexión:',
            error
        );


        throw new Error(
            'No se pudo conectar con el servidor. ' +
            '¿Está corriendo el backend en ' +
            API_BASE +
            '?'
        );

    }


    // ========================================================
    // SESIÓN VENCIDA
    // ========================================================

    if (
        respuesta.status === 401 &&
        token
    ) {

        limpiarSesion();

        window.location.href =
            'login.html';


        throw new Error(
            'Sesión vencida o inválida.'
        );

    }


    // ========================================================
    // SIN CONTENIDO
    // ========================================================

    if (
        respuesta.status === 204
    ) {

        return null;

    }


    // ========================================================
    // LEER RESPUESTA
    // ========================================================

    const texto =
        await respuesta.text();


    let cuerpo =
        null;


    if (texto) {

        try {

            cuerpo =
                JSON.parse(texto);

        } catch (error) {

            cuerpo =
                texto;

        }

    }


    // ========================================================
    // ERROR HTTP
    // ========================================================

    if (!respuesta.ok) {

        let mensaje =
            'Error ' +
            respuesta.status +
            ' del servidor.';


        /*
         * Spring puede devolver:
         *
         * {
         *     "error": "..."
         * }
         *
         * o:
         *
         * {
         *     "mensaje": "..."
         * }
         */

        if (
            cuerpo &&
            typeof cuerpo === 'object'
        ) {

            mensaje =
                cuerpo.error ||
                cuerpo.mensaje ||
                cuerpo.message ||
                cuerpo.detail ||
                mensaje;

        } else if (
            typeof cuerpo === 'string' &&
            cuerpo.trim() !== ''
        ) {

            mensaje =
                cuerpo;

        }


        console.error(
            'Error HTTP:',
            respuesta.status,
            mensaje
        );


        throw new Error(
            mensaje
        );

    }


    // ========================================================
    // RESPUESTA CORRECTA
    // ========================================================

    return cuerpo;

}


// ============================================================
// MOSTRAR ERROR
// ============================================================

function mostrarError(
    elementoMensaje,
    error
) {

    if (!elementoMensaje) {
        return;
    }


    elementoMensaje.textContent =
        error.message ||
        String(error);


    elementoMensaje.classList.remove(
        'oculto'
    );

}


// ============================================================
// OCULTAR MENSAJE
// ============================================================

function ocultarMensaje(
    elementoMensaje
) {

    if (!elementoMensaje) {
        return;
    }


    elementoMensaje.textContent =
        '';


    elementoMensaje.classList.add(
        'oculto'
    );

}


// ============================================================
// BADGE DE ESTADO DE SOLICITUD
// ============================================================

function claseBadgeEstado(
    nombreEstado
) {

    const mapa = {

        'Pendiente':
            'badge-pendiente',

        'En Proceso':
            'badge-en-proceso',

        'Pendiente Aprobación':
            'badge-pendiente-aprobacion',

        'Resuelta - Pendiente Confirmación del Cliente':
            'badge-resuelta',

        'Cerrada':
            'badge-cerrada'

    };


    return mapa[nombreEstado] || '';

}


// ============================================================
// BADGE DE ESTADO DE CUENTA
// ============================================================

function claseBadgeEstadoCuenta(
    estadoCuenta
) {

    const mapa = {

        'activo':
            'badge-activo',

        'suspendido':
            'badge-suspendido',

        'inactivo':
            'badge-inactivo'

    };


    return mapa[estadoCuenta] || '';

}


// ============================================================
// ESCAPAR HTML
// ============================================================

function escaparHtml(
    texto
) {

    if (
        texto === null ||
        texto === undefined
    ) {

        return '';

    }


    const div =
        document.createElement(
            'div'
        );


    div.textContent =
        String(texto);


    return div.innerHTML;

}


// ============================================================
// FORMATEAR FECHA
// ============================================================

function formatearFecha(
    fechaIso
) {

    if (!fechaIso) {

        return '—';

    }


    const fecha =
        new Date(fechaIso);


    if (
        isNaN(
            fecha.getTime()
        )
    ) {

        return fechaIso;

    }


    return fecha.toLocaleString(
        'es-EC',
        {
            dateStyle: 'medium',
            timeStyle: 'short'
        }
    );

}


// ============================================================
// NAVEGACIÓN POR PESTAÑAS
// ============================================================

function activarNavegacionPorTabs() {

    const enlaces =
        document.querySelectorAll(
            '.sidebar-nav a[href^="#"]'
        );


    const secciones =
        Array.from(enlaces)
            .map(
                function (enlace) {

                    return document.querySelector(
                        enlace.getAttribute(
                            'href'
                        )
                    );

                }
            )
            .filter(Boolean);


    if (
        secciones.length === 0
    ) {

        return;

    }


    function mostrarSeccion(
        idObjetivo
    ) {

        enlaces.forEach(
            function (enlace) {

                enlace.classList.toggle(
                    'activo',
                    enlace.getAttribute(
                        'href'
                    ) ===
                    '#' + idObjetivo
                );

            }
        );


        secciones.forEach(
            function (seccion) {

                seccion.classList.toggle(
                    'oculto',
                    seccion.id !== idObjetivo
                );

            }
        );

    }


    enlaces.forEach(
        function (enlace) {

            enlace.addEventListener(
                'click',
                function (evento) {

                    evento.preventDefault();


                    mostrarSeccion(
                        enlace
                            .getAttribute('href')
                            .slice(1)
                    );

                }
            );

        }
    );


    mostrarSeccion(
        secciones[0].id
    );

}


// ============================================================
// ICONOS DE CONTRASEÑA
// ============================================================

const ICONO_OJO =
    '<svg viewBox="0 0 24 24" ' +
    'fill="none" ' +
    'stroke-width="2" ' +
    'stroke-linecap="round" ' +
    'stroke-linejoin="round">' +
    '<path d="M1 12s4-7 11-7 11 7 11 7-4 7-11 7-11-7-11-7z"/>' +
    '<circle cx="12" cy="12" r="3"/>' +
    '</svg>';


const ICONO_OJO_TACHADO =
    '<svg viewBox="0 0 24 24" ' +
    'fill="none" ' +
    'stroke-width="2" ' +
    'stroke-linecap="round" ' +
    'stroke-linejoin="round">' +
    '<path d="M17.94 17.94A10.94 10.94 0 0112 19c-7 0-11-7-11-7a20.3 20.3 0 015.06-5.94M9.9 4.24A10.4 10.4 0 0112 4c7 0 11 7 11 7a20.3 20.3 0 01-3.22 4.06M14.12 14.12a3 3 0 11-4.24-4.24"/>' +
    '<path d="M1 1l22 22"/>' +
    '</svg>';


// ============================================================
// ALTERNAR CONTRASEÑA
// ============================================================

function activarAlternarContrasena() {

    document
        .querySelectorAll(
            '.alternar-contrasena'
        )
        .forEach(
            function (boton) {

                boton.innerHTML =
                    ICONO_OJO;


                boton.addEventListener(
                    'click',
                    function () {

                        const input =
                            document.getElementById(
                                boton.getAttribute(
                                    'data-target'
                                )
                            );


                        if (!input) {
                            return;
                        }


                        const mostrando =
                            input.type === 'text';


                        input.type =
                            mostrando
                                ? 'password'
                                : 'text';


                        boton.innerHTML =
                            mostrando
                                ? ICONO_OJO
                                : ICONO_OJO_TACHADO;


                        boton.setAttribute(
                            'aria-label',
                            mostrando
                                ? 'Mostrar contraseña'
                                : 'Ocultar contraseña'
                        );

                    }
                );

            }
        );

}


// ============================================================
// HTML CARGANDO
// ============================================================

function htmlCargando(
    texto
) {

    return (

        '<div class="estado-cargando">' +

        '<span class="spinner"></span>' +

        escaparHtml(
            texto ||
            'Cargando...'
        ) +

        '</div>'

    );

}


// ============================================================
// MODAL DE CONFIRMACIÓN
// ============================================================

function confirmarAccion(
    titulo,
    mensaje,
    textoConfirmar
) {

    return new Promise(
        function (resolve) {

            const overlay =
                document.createElement(
                    'div'
                );


            overlay.className =
                'overlay-modal';


            overlay.innerHTML =

                '<div class="modal">' +

                '<h3>' +
                escaparHtml(titulo) +
                '</h3>' +

                '<p>' +
                escaparHtml(mensaje) +
                '</p>' +

                '<div class="modal-acciones">' +

                '<button ' +
                'type="button" ' +
                'class="secundario" ' +
                'data-accion="cancelar">' +

                'Cancelar' +

                '</button>' +

                '<button ' +
                'type="button" ' +
                'data-accion="confirmar">' +

                escaparHtml(
                    textoConfirmar ||
                    'Confirmar'
                ) +

                '</button>' +

                '</div>' +

                '</div>';


            function cerrar(
                resultado
            ) {

                if (
                    overlay.parentNode
                ) {

                    document.body.removeChild(
                        overlay
                    );

                }


                document.removeEventListener(
                    'keydown',
                    alPresionarTecla
                );


                resolve(
                    resultado
                );

            }


            function alPresionarTecla(
                evento
            ) {

                if (
                    evento.key ===
                    'Escape'
                ) {

                    cerrar(false);

                }

            }


            overlay.addEventListener(
                'click',
                function (evento) {

                    if (
                        evento.target ===
                        overlay
                    ) {

                        cerrar(false);

                    }

                }
            );


            const botonCancelar =
                overlay.querySelector(
                    '[data-accion="cancelar"]'
                );


            const botonConfirmar =
                overlay.querySelector(
                    '[data-accion="confirmar"]'
                );


            if (botonCancelar) {

                botonCancelar.addEventListener(
                    'click',
                    function () {

                        cerrar(false);

                    }
                );

            }


            if (botonConfirmar) {

                botonConfirmar.addEventListener(
                    'click',
                    function () {

                        cerrar(true);

                    }
                );

            }


            document.addEventListener(
                'keydown',
                alPresionarTecla
            );


            document.body.appendChild(
                overlay
            );

        }
    );

}