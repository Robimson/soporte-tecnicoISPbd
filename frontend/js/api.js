// Capa de presentacion: este archivo SOLO habla con el backend y guarda la
// sesion en el navegador. No valida reglas de negocio ni decide estados de
// ticket - eso vive en los procedimientos de PostgreSQL y en el backend
// Java, tal como en el resto del sistema.

const API_BASE = 'http://localhost:8080';

function guardarSesion(token, idUsuario, rol) {
    localStorage.setItem('token', token);
    localStorage.setItem('idUsuario', idUsuario);
    localStorage.setItem('rol', rol);
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

const PAGINA_POR_ROL = {
    CLIENTE: 'cliente.html',
    TECNICO: 'tecnico.html',
    ADMINISTRADOR: 'admin.html',
    SUPERUSUARIO: 'superusuario.html'
};

/**
 * Exige sesion activa; si no hay token, manda a login. Si se pasa un rol
 * esperado y no coincide, tambien manda a login (proteccion de UI: la
 * proteccion real ya la hace el backend con el JWT en cada endpoint).
 */
function exigirSesion(rolEsperado) {
    const token = obtenerToken();
    if (!token) {
        window.location.href = 'login.html';
        return false;
    }
    if (rolEsperado && obtenerRol() !== rolEsperado) {
        window.location.href = 'login.html';
        return false;
    }
    return true;
}

function cerrarSesion() {
    limpiarSesion();
    window.location.href = 'login.html';
}

/**
 * Wrapper central de fetch: agrega el token si existe, arma el body en
 * JSON, y convierte una respuesta no exitosa en una excepcion con el
 * mensaje real que manda el backend (GlobalExceptionHandler siempre
 * responde {"error": "..."}).
 */
async function apiFetch(path, options = {}) {
    const headers = Object.assign({ 'Content-Type': 'application/json' }, options.headers || {});
    const token = obtenerToken();
    if (token) {
        headers['Authorization'] = 'Bearer ' + token;
    }

    let respuesta;
    try {
        respuesta = await fetch(API_BASE + path, Object.assign({}, options, { headers }));
    } catch (error) {
        throw new Error('No se pudo conectar con el servidor. ¿Esta corriendo el backend en ' + API_BASE + '?');
    }

    // Solo se trata como "sesion vencida" si la llamada llevaba un token:
    // el propio /api/auth/login tambien responde 401 cuando la contrasena
    // es incorrecta, y ese caso NO lleva token - debe mostrarse como error
    // normal en el formulario, no mandar de vuelta a login.html (eso hacia
    // que la pantalla de login "no cargara" al fallar el intento).
    if (respuesta.status === 401 && token) {
        limpiarSesion();
        window.location.href = 'login.html';
        throw new Error('Sesion vencida o invalida.');
    }

    if (respuesta.status === 204) {
        return null;
    }

    const texto = await respuesta.text();
    let cuerpo = null;
    if (texto) {
        try {
            cuerpo = JSON.parse(texto);
        } catch (error) {
            cuerpo = texto;
        }
    }

    if (!respuesta.ok) {
        const mensaje = (cuerpo && cuerpo.error) ? cuerpo.error : ('Error ' + respuesta.status + ' del servidor.');
        throw new Error(mensaje);
    }

    return cuerpo;
}

function mostrarError(elementoMensaje, error) {
    elementoMensaje.textContent = error.message || String(error);
    elementoMensaje.classList.remove('oculto');
}

function ocultarMensaje(elementoMensaje) {
    elementoMensaje.textContent = '';
    elementoMensaje.classList.add('oculto');
}

function claseBadgeEstado(nombreEstado) {
    const mapa = {
        'Pendiente': 'badge-pendiente',
        'En Proceso': 'badge-en-proceso',
        'Pendiente Aprobación': 'badge-pendiente-aprobacion',
        'Resuelta - Pendiente Confirmación del Cliente': 'badge-resuelta',
        'Cerrada': 'badge-cerrada'
    };
    return mapa[nombreEstado] || '';
}

function claseBadgeEstadoCuenta(estadoCuenta) {
    const mapa = {
        'activo': 'badge-activo',
        'suspendido': 'badge-suspendido',
        'inactivo': 'badge-inactivo'
    };
    return mapa[estadoCuenta] || '';
}

function escaparHtml(texto) {
    if (texto === null || texto === undefined) return '';
    const div = document.createElement('div');
    div.textContent = String(texto);
    return div.innerHTML;
}

function formatearFecha(fechaIso) {
    if (!fechaIso) return '—';
    const fecha = new Date(fechaIso);
    if (isNaN(fecha.getTime())) return fechaIso;
    return fecha.toLocaleString('es-EC', { dateStyle: 'medium', timeStyle: 'short' });
}

/**
 * Convierte la barra lateral en pestañas reales: al hacer clic en un link
 * se muestra SOLO esa seccion (#id) y se ocultan las demas, en vez de
 * simplemente saltar con scroll. Por defecto queda visible la primera
 * seccion (el resumen de metricas de cada panel). Se llama una vez al
 * cargar cada panel; no hace nada si la pagina no tiene barra lateral.
 */
function activarNavegacionPorTabs() {
    const enlaces = document.querySelectorAll('.sidebar-nav a[href^="#"]');
    const secciones = Array.from(enlaces)
        .map(function (enlace) { return document.querySelector(enlace.getAttribute('href')); })
        .filter(Boolean);

    if (secciones.length === 0) return;

    function mostrarSeccion(idObjetivo) {
        enlaces.forEach(function (enlace) {
            enlace.classList.toggle('activo', enlace.getAttribute('href') === '#' + idObjetivo);
        });
        secciones.forEach(function (seccion) {
            seccion.classList.toggle('oculto', seccion.id !== idObjetivo);
        });
    }

    enlaces.forEach(function (enlace) {
        enlace.addEventListener('click', function (evento) {
            evento.preventDefault();
            mostrarSeccion(enlace.getAttribute('href').slice(1));
        });
    });

    mostrarSeccion(secciones[0].id);
}

const ICONO_OJO = '<svg viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-7 11-7 11 7 11 7-4 7-11 7-11-7-11-7z"/><circle cx="12" cy="12" r="3"/></svg>';
const ICONO_OJO_TACHADO = '<svg viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17.94 17.94A10.94 10.94 0 0112 19c-7 0-11-7-11-7a20.3 20.3 0 015.06-5.94M9.9 4.24A10.4 10.4 0 0112 4c7 0 11 7 11 7a20.3 20.3 0 01-3.22 4.06M14.12 14.12a3 3 0 11-4.24-4.24"/><path d="M1 1l22 22"/></svg>';

/**
 * Agrega el boton de "mostrar/ocultar contraseña" a cada campo marcado con
 * la clase campo-con-icono. Cambia el type del input entre password/text.
 */
function activarAlternarContrasena() {
    document.querySelectorAll('.alternar-contrasena').forEach(function (boton) {
        boton.innerHTML = ICONO_OJO;
        boton.addEventListener('click', function () {
            const input = document.getElementById(boton.getAttribute('data-target'));
            const mostrando = input.type === 'text';
            input.type = mostrando ? 'password' : 'text';
            boton.innerHTML = mostrando ? ICONO_OJO : ICONO_OJO_TACHADO;
            boton.setAttribute('aria-label', mostrando ? 'Mostrar contraseña' : 'Ocultar contraseña');
        });
    });
}

/** HTML de un estado "cargando" con spinner, para usar en listas mientras llega la respuesta. */
function htmlCargando(texto) {
    return '<div class="estado-cargando"><span class="spinner"></span>' + escaparHtml(texto || 'Cargando...') + '</div>';
}

/**
 * Modal de confirmacion propio (reemplaza el confirm() nativo del
 * navegador, que no se puede estilizar). Devuelve una Promise<boolean>:
 * true si el usuario confirma, false si cancela o cierra.
 */
function confirmarAccion(titulo, mensaje, textoConfirmar) {
    return new Promise(function (resolve) {
        const overlay = document.createElement('div');
        overlay.className = 'overlay-modal';
        overlay.innerHTML =
            '<div class="modal">' +
            '<h3>' + escaparHtml(titulo) + '</h3>' +
            '<p>' + escaparHtml(mensaje) + '</p>' +
            '<div class="modal-acciones">' +
            '<button type="button" class="secundario" data-accion="cancelar">Cancelar</button>' +
            '<button type="button" data-accion="confirmar">' + escaparHtml(textoConfirmar || 'Confirmar') + '</button>' +
            '</div></div>';

        function cerrar(resultado) {
            document.body.removeChild(overlay);
            document.removeEventListener('keydown', alPresionarTecla);
            resolve(resultado);
        }

        function alPresionarTecla(evento) {
            if (evento.key === 'Escape') cerrar(false);
        }

        overlay.addEventListener('click', function (evento) {
            if (evento.target === overlay) cerrar(false);
        });
        overlay.querySelector('[data-accion="cancelar"]').addEventListener('click', function () { cerrar(false); });
        overlay.querySelector('[data-accion="confirmar"]').addEventListener('click', function () { cerrar(true); });
        document.addEventListener('keydown', alPresionarTecla);

        document.body.appendChild(overlay);
    });
}
