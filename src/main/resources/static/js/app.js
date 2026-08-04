const API_URL = '/api';

let personas = [];
let partidasActivas = [];
let partidasFinalizadas = [];
let partidaSeleccionada = null;
let TabActual = 'personas';

// ========================================
// TOAST NOTIFICATION SYSTEM
// ========================================

function toast(message, type = 'info', duration = 4000) {
    let container = document.querySelector('.toast-container');
    if (!container) {
        container = document.createElement('div');
        container.className = 'toast-container';
        document.body.appendChild(container);
    }

    const icons = { success: '\u2713', error: '\u2717', info: '\u2139' };
    const el = document.createElement('div');
    el.className = 'toast ' + type;
    el.innerHTML = '<span class="toast-icon">' + (icons[type] || icons.info) + '</span><span class="toast-message"></span>';
    el.querySelector('.toast-message').textContent = message;
    container.appendChild(el);

    setTimeout(() => {
        el.classList.add('leaving');
        el.addEventListener('animationend', () => el.remove());
    }, duration);
}

// ========================================
// MODAL FACTORY
// ========================================

function createModal(options) {
    const overlay = document.createElement('div');
    overlay.className = 'modal-overlay';

    const wide = options.wide ? ' wide' : '';
    overlay.innerHTML = '<div class="modal-panel' + wide + '">'
        + '<div class="modal-header">'
        + '<h2>' + (options.icon ? '<span class="icon">' + options.icon + '</span>' : '') + (options.title || '') + '</h2>'
        + '<button class="modal-close">&times;</button>'
        + '</div>'
        + '<div class="modal-body">' + (options.body || '') + '</div>'
        + (options.footer ? '<div class="modal-footer">' + options.footer + '</div>' : '')
        + '</div>';

    document.body.appendChild(overlay);

    const panel = overlay.querySelector('.modal-panel');

    function close() {
        overlay.style.opacity = '0';
        overlay.style.transition = 'opacity 0.2s ease';
        setTimeout(() => overlay.remove(), 200);
        if (options.onClose) options.onClose();
    }

    overlay.addEventListener('click', (e) => {
        if (e.target === overlay) close();
    });

    overlay.querySelector('.modal-close').addEventListener('click', close);

    document.addEventListener('keydown', function escHandler(e) {
        if (e.key === 'Escape') {
            close();
            document.removeEventListener('keydown', escHandler);
        }
    });

    if (options.onOpen) {
        requestAnimationFrame(() => options.onOpen(overlay, panel, close));
    }

    return { overlay, panel, close };
}

// ========================================
// LOGOUT
// ========================================

// ========================================
// INITIALIZATION
// ========================================

document.addEventListener('DOMContentLoaded', async () => {
    cargarPersonas();
    TabActual = 'personas';

    document.getElementById('form-persona').addEventListener('submit', crearPersona);
    document.getElementById('form-partida').addEventListener('submit', crearPartida);

    cargarEmailCounter();
});

// ========================================
// EMAIL COUNTER
// ========================================

async function cargarEmailCounter() {
    try {
        const response = await fetch(API_URL + '/partidas/estadisticas');
        // Use the daily email count if available, else keep default
        const counter = document.getElementById('email-counter');
        if (counter) {
            const countEl = counter.querySelector('.count');
            if (countEl) countEl.textContent = 'active';
        }
    } catch (e) {}
}

// ========================================
// TAB NAVIGATION
// ========================================

function showTab(tabName, event) {
    document.querySelectorAll('.tab-content').forEach(tab => tab.classList.remove('active'));
    document.querySelectorAll('.tab-button').forEach(btn => btn.classList.remove('active'));

    document.getElementById(tabName + '-tab').classList.add('active');

    if (event && event.target) {
        event.target.classList.add('active');
    } else {
        document.querySelectorAll('.tab-button').forEach(btn => {
            if (btn.getAttribute('onclick').includes(tabName)) btn.classList.add('active');
        });
    }

    if (tabName !== TabActual) {
        TabActual = tabName;
        if (tabName === 'personas') cargarPersonas();
        else if (tabName === 'partidas') cargarPartidas();
        else if (tabName === 'ranking') cargarRanking();
        else if (tabName === 'estadisticas') cargarEstadisticas();
    }
}

// ========================================
// PERSONAS
// ========================================

async function cargarPersonas() {
    try {
        const response = await fetch(API_URL + '/personas');
        if (!response.ok) throw new Error('HTTP ' + response.status);
        const data = await response.json();
        personas = Array.isArray(data) ? data : [];
        mostrarPersonasEnTabla();
        actualizarSelectorParticipantes();
    } catch (error) {
        console.error('Error al cargar personas:', error);
        toast('Error al cargar personas', 'error');
        personas = [];
    }
}

function mostrarPersonasEnTabla() {
    const tbody = document.querySelector('#tabla-personas tbody');
    if (!Array.isArray(personas)) {
        tbody.innerHTML = '<tr><td colspan="5" class="empty-state">Error al cargar personas</td></tr>';
        return;
    }
    if (personas.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="empty-state">No hay personas registradas</td></tr>';
        return;
    }

    tbody.innerHTML = personas.map(p => '<tr>'
        + '<td>' + p.id + '</td>'
        + '<td><strong>' + escapeHtml(p.nom) + '</strong></td>'
        + '<td>' + (p.mail ? p.mail : '<span class="text-error">Sin email</span>') + '</td>'
        + '<td><strong>' + p.victories + '</strong></td>'
        + '<td><button class="btn-action btn-edit" onclick="abrirModalEditarPersona(' + p.id + ')">Editar</button> '
        + '<button class="btn-action btn-delete" onclick="eliminarPersona(' + p.id + ')">Eliminar</button></td>'
        + '</tr>').join('');
}

async function crearPersona(event) {
    event.preventDefault();
    const nombre = document.getElementById('persona-nombre').value.trim();
    const email = document.getElementById('persona-email').value.trim();

    try {
        const response = await fetch(API_URL + '/personas', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ nom: nombre, mail: email, victories: 0 })
        });

        if (response.ok) {
            toast('Persona creada correctamente', 'success');
            document.getElementById('form-persona').reset();
            cargarPersonas();
        } else {
            const error = await response.json();
            toast('Error: ' + (error.error || 'No se pudo crear la persona'), 'error');
        }
    } catch (error) {
        console.error('Error:', error);
        toast('Error al crear persona', 'error');
    }
}

function abrirModalEditarPersona(id) {
    const persona = personas.find(p => p.id === id);
    if (!persona) {
        toast('Persona no encontrada', 'error');
        return;
    }

    const bodyHtml = '<form id="form-editar-persona">'
        + '<div class="form-group"><label for="editar-nombre">Nombre</label>'
        + '<input type="text" id="editar-nombre" value="' + escapeHtml(persona.nom) + '" required></div>'
        + '<div class="form-group"><label for="editar-email">Email</label>'
        + '<input type="email" id="editar-email" value="' + (persona.mail || '') + '" placeholder="email@ejemplo.com"></div>'
        + '<div class="modal-actions">'
        + '<button type="submit" class="btn-primary">Guardar</button>'
        + '<button type="button" class="btn-cancel" data-close="true">Cancelar</button>'
        + '</div></form>';

    const modal = createModal({
        title: 'Editar Persona',
        body: bodyHtml,
        onOpen: function(overlay, panel, close) {
            panel.querySelector('#form-editar-persona').addEventListener('submit', function(e) {
                e.preventDefault();
                editarPersona(id, close);
            });
            panel.querySelector('[data-close]').addEventListener('click', close);
        }
    });
}

async function editarPersona(id, closeModal) {
    const nombre = document.getElementById('editar-nombre').value.trim();
    const email = document.getElementById('editar-email').value.trim();

    try {
        const response = await fetch(API_URL + '/personas/' + id, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ nom: nombre, mail: email || null })
        });

        if (response.ok) {
            toast('Persona actualizada correctamente', 'success');
            closeModal();
            cargarPersonas();
        } else {
            const error = await response.json();
            toast('Error: ' + (error.error || 'No se pudo actualizar'), 'error');
        }
    } catch (error) {
        console.error('Error:', error);
        toast('Error al actualizar persona', 'error');
    }
}

async function eliminarPersona(id) {
    if (!confirm('Estas seguro de que quieres eliminar esta persona?')) return;

    try {
        const response = await fetch(API_URL + '/personas/' + id, { method: 'DELETE' });

        if (response.ok) {
            toast('Persona eliminada correctamente', 'success');
            cargarPersonas();
        } else {
            const errorData = await response.json();
            toast('Error: ' + (errorData.error || 'Error al eliminar persona'), 'error');
        }
    } catch (error) {
        console.error('Error:', error);
        toast('Error al eliminar persona', 'error');
    }
}

// ========================================
// RANKING
// ========================================

async function cargarRanking() {
    try {
        const response = await fetch(API_URL + '/personas/ranking');
        const ranking = await response.json();
        mostrarRanking(ranking);
    } catch (error) {
        console.error('Error al cargar ranking:', error);
        toast('Error al cargar ranking', 'error');
    }
}

function mostrarRanking(ranking) {
    const container = document.getElementById('ranking-personas');

    if (!Array.isArray(ranking)) {
        container.innerHTML = '<p class="empty-state">Error al cargar ranking</p>';
        return;
    }

    if (ranking.length === 0) {
        container.innerHTML = '<p class="empty-state">No hay datos para mostrar</p>';
        return;
    }

    const medals = ['\uD83E\uDD47', '\uD83E\uDD48', '\uD83E\uDD49'];

    container.innerHTML = ranking.map((p, i) => {
        const isTop3 = i < 3;
        return '<div class="ranking-item' + (isTop3 ? ' top-three' : '') + '">'
            + '<div class="ranking-position">' + (medals[i] || (i + 1)) + '</div>'
            + '<div class="ranking-info"><div class="ranking-name">' + escapeHtml(p.nom) + '</div></div>'
            + '<div class="ranking-victories">' + p.victories + ' victorias</div>'
            + '</div>';
    }).join('');
}

// ========================================
// PARTIDAS
// ========================================

async function cargarPartidas() {
    try {
        const [resActivas, resFinalizadas] = await Promise.all([
            fetch(API_URL + '/partidas/activas'),
            fetch(API_URL + '/partidas/finalizadas')
        ]);
        partidasActivas = await resActivas.json();
        partidasFinalizadas = await resFinalizadas.json();
        mostrarPartidasActivas();
        mostrarPartidasFinalizadas();
    } catch (error) {
        console.error('Error al cargar partidas:', error);
        toast('Error al cargar partidas', 'error');
    }
}

function mostrarPartidasActivas() {
    const tbody = document.querySelector('#tabla-partidas-activas tbody');

    if (partidasActivas.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="empty-state">No hay partidas activas</td></tr>';
        return;
    }

    tbody.innerHTML = partidasActivas.map(partida => {
        const tieneFallidos = partida.asignaciones && Object.keys(partida.asignaciones).length > 0;
        return '<tr>'
            + '<td>' + partida.id + '</td>'
            + '<td><strong>' + escapeHtml(partida.nom) + '</strong></td>'
            + '<td>' + partida.personas.length + ' jugadores</td>'
            + '<td>' + formatearFecha(partida.fechaCreacion) + '</td>'
            + '<td>'
            + (tieneFallidos ? '<button class="btn-action btn-reenviar" onclick="mostrarCorreosFallidos(' + partida.id + ')">Reenviar (' + Object.keys(partida.asignaciones).length + ')</button> ' : '')
            + '<button class="btn-action btn-finish" onclick="abrirModalFinalizar(' + partida.id + ')">Finalizar</button>'
            + '</td></tr>';
    }).join('');
}

function mostrarPartidasFinalizadas() {
    const tbody = document.querySelector('#tabla-partidas-finalizadas tbody');

    if (partidasFinalizadas.length === 0) {
        tbody.innerHTML = '<tr><td colspan="4" class="empty-state">No hay partidas finalizadas</td></tr>';
        return;
    }

    tbody.innerHTML = partidasFinalizadas.map(partida => '<tr>'
        + '<td>' + partida.id + '</td>'
        + '<td>' + escapeHtml(partida.nom) + '</td>'
        + '<td><strong>' + (partida.ganador ? escapeHtml(partida.ganador.nom) : 'N/A') + '</strong></td>'
        + '<td>' + formatearFecha(partida.fechaFinalizacion) + '</td>'
        + '</tr>').join('');
}

function actualizarSelectorParticipantes() {
    const container = document.getElementById('lista-participantes');

    if (!Array.isArray(personas)) {
        container.innerHTML = '<p class="empty-state">Error al cargar personas</p>';
        return;
    }

    if (personas.length === 0) {
        container.innerHTML = '<p class="empty-state">Primero debes anadir personas</p>';
        return;
    }

    container.innerHTML = personas.map(p => '<label class="participante-checkbox">'
        + '<input type="checkbox" name="participante" value="' + p.id + '" onchange="actualizarCheckboxSelectAll()">'
        + escapeHtml(p.nom) + '</label>').join('');
}

function toggleSeleccionarTodos() {
    const selectAll = document.getElementById('select-all-checkbox');
    document.querySelectorAll('input[name="participante"]').forEach(cb => {
        cb.checked = selectAll.checked;
    });
}

function actualizarCheckboxSelectAll() {
    const selectAll = document.getElementById('select-all-checkbox');
    const checkboxes = document.querySelectorAll('input[name="participante"]');
    if (selectAll) {
        selectAll.checked = Array.from(checkboxes).every(cb => cb.checked);
    }
}

async function crearPartida(event) {
    event.preventDefault();

    const nombre = document.getElementById('partida-nombre').value.trim();
    const checkboxes = document.querySelectorAll('input[name="participante"]:checked');

    if (checkboxes.length < 2) {
        toast('Debes seleccionar al menos 2 participantes', 'error');
        return;
    }

    const participantes = Array.from(checkboxes).map(cb => ({ id: parseInt(cb.value) }));

    let loadingModal = null;
    try {
        loadingModal = createModal({
            title: 'Enviando Correos',
            body: '<div class="loading-modal-content"><div class="spinner"></div><p>Por favor espera mientras se envian los correos a todos los participantes...</p></div>'
        });

        const response = await fetch(API_URL + '/partidas', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ nom: nombre, personas: participantes })
        });

        if (response.ok) {
            const partida = await response.json();
            document.getElementById('form-partida').reset();
            cargarPartidas();

            setTimeout(async () => {
                try {
                    const statsResponse = await fetch(API_URL + '/partidas/' + partida.id + '/enviar-correos', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ idioma: getCurrentLanguage() })
                    });
                    loadingModal.close();

                    if (statsResponse.ok) {
                        const body = await statsResponse.json();
                        const resultado = body.resultado || body;
                        mostrarModalResultadoEnvio(resultado);
                    } else {
                        toast('Partida creada correctamente', 'success');
                    }
                } catch (e) {
                    loadingModal.close();
                    toast('Partida creada correctamente', 'success');
                }
            }, 500);
        } else {
            loadingModal.close();
            const error = await response.json();
            toast('Error: ' + (error.error || 'No se pudo crear la partida'), 'error');
        }
    } catch (error) {
        if (loadingModal) loadingModal.close();
        console.error('Error:', error);
        toast('Error al crear partida', 'error');
    }
}

// ========================================
// FINALIZAR PARTIDA
// ========================================

function abrirModalFinalizar(partidaId) {
    const partida = partidasActivas.find(p => p.id === partidaId);
    if (!partida) return;
    partidaSeleccionada = partida;

    const container = document.getElementById('modal-ganadores-lista');
    container.innerHTML = partida.personas.map(persona => '<div class="ganador-option" onclick="finalizarPartida(' + partida.id + ', ' + persona.id + ')">'
        + escapeHtml(persona.nom) + '</div>').join('');

    document.getElementById('modal-finalizar').style.display = 'block';
}

function cerrarModal() {
    document.getElementById('modal-finalizar').style.display = 'none';
    partidaSeleccionada = null;
}

async function finalizarPartida(partidaId, ganadorId) {
    try {
        const response = await fetch(API_URL + '/partidas/' + partidaId + '/finalizar', {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ ganadorId: ganadorId })
        });

        if (response.ok) {
            toast('Partida finalizada correctamente', 'success');
            cerrarModal();
            cargarPartidas();
            cargarPersonas();
            cargarEstadisticas();
        } else {
            const error = await response.json();
            toast('Error: ' + (error.error || 'No se pudo finalizar la partida'), 'error');
        }
    } catch (error) {
        console.error('Error:', error);
        toast('Error al finalizar partida', 'error');
    }
}

// ========================================
// ESTADISTICAS
// ========================================

async function cargarEstadisticas() {
    try {
        const response = await fetch(API_URL + '/partidas/estadisticas');
        const stats = await response.json();

        animateStat('stat-total-partidas', stats.totalPartidas);
        animateStat('stat-partidas-activas', stats.partidasActivas);
        animateStat('stat-partidas-finalizadas', stats.partidasFinalizadas);
        animateStat('stat-total-jugadores', stats.totalJugadores);
    } catch (error) {
        console.error('Error al cargar estadisticas:', error);
    }
}

function animateStat(id, value) {
    const el = document.getElementById(id);
    if (!el) return;
    el.textContent = value;
    el.classList.remove('animate');
    void el.offsetWidth;
    el.classList.add('animate');
}

// ========================================
// EMAIL SENDING MODAL
// ========================================

function mostrarModalResultadoEnvio(resultado) {
    let statsHtml = '<div class="envio-resumen">'
        + renderStatCard('\u2714', resultado.exitosos, 'Enviados', 'success')
        + renderStatCard('\u2718', resultado.fallidos, 'Fallidos', resultado.fallidos > 0 ? 'error' : '')
        + renderStatCard('\u2261', resultado.total, 'Total', 'info')
        + '</div>';

    let failedHtml = '';
    if (resultado.fallidos > 0) {
        failedHtml = '<div class="envio-detalles"><h3>Correos Fallidos</h3><div class="envio-lista">'
            + resultado.detalles.filter(d => !d.exitoso).map(detalle => '<div class="envio-item fallido">'
                + '<div class="envio-item-icon">\u2718</div>'
                + '<div class="envio-item-info">'
                + '<div class="envio-item-nombre">' + escapeHtml(detalle.nombre) + '</div>'
                + '<div class="envio-item-email">' + escapeHtml(detalle.email) + '</div>'
                + '<div class="envio-item-mensaje">' + escapeHtml(detalle.mensaje) + '</div>'
                + '</div>'
                + (detalle.email !== 'Sin email' ? '<button class="btn-reenviar-sm" onclick="reenviarCorreo(' + resultado.partidaId + ', \'' + escapeAttr(detalle.email) + '\', \'' + escapeAttr(detalle.nombre) + '\')">Reenviar</button>' : '')
                + '</div>').join('')
            + '</div></div>';
    }

    createModal({
        title: 'Resultado del Envio de Correos',
        wide: true,
        body: statsHtml + failedHtml,
        footer: '<button class="btn-cancel" data-close="true">Cerrar</button>',
        onOpen: function(overlay, panel, close) {
            panel.querySelector('[data-close]').addEventListener('click', close);
        }
    });
}

function renderStatCard(icon, num, label, extraClass) {
    return '<div class="envio-stat-card' + (extraClass ? ' ' + extraClass : '') + '">'
        + '<span class="envio-icon-large">' + icon + '</span>'
        + '<div class="envio-numero">' + num + '</div>'
        + '<div class="envio-label">' + label + '</div>'
        + '</div>';
}

// ========================================
// REENVIO DE CORREOS
// ========================================

async function mostrarCorreosFallidos(partidaId) {
    try {
        const partida = partidasActivas.find(p => p.id === partidaId);
        if (!partida || !partida.asignaciones) {
            toast('No hay correos pendientes', 'info');
            return;
        }

        const asignaciones = partida.asignaciones;
        const personasList = partida.personas;

        const detalles = Object.entries(asignaciones).map(([emailCazador, nombreVictima]) => {
            const cazador = personasList.find(p => p.mail === emailCazador);
            return {
                nombre: cazador ? cazador.nom : 'Desconocido',
                email: emailCazador,
                exitoso: false,
                victima: nombreVictima,
                mensaje: 'Correo pendiente de envio'
            };
        });

        mostrarModalResultadoEnvio({
            partidaId: partidaId,
            exitosos: 0,
            fallidos: detalles.length,
            total: detalles.length,
            detalles: detalles
        });
    } catch (error) {
        console.error('Error:', error);
        toast('Error al cargar correos fallidos', 'error');
    }
}

async function reenviarCorreo(partidaId, email, nombre) {
    if (!confirm('Reenviar correo a ' + nombre + ' (' + email + ')?')) return;

    try {
        const response = await fetch(API_URL + '/partidas/' + partidaId + '/reenviar-correo', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email: email, idioma: getCurrentLanguage() })
        });

        if (response.ok) {
            toast('Correo reenviado correctamente a ' + nombre, 'success');
            await cargarPartidas();
            // Close any open email result modal
            document.querySelectorAll('.modal-overlay').forEach(o => o.remove());
        } else {
            const err = await response.json();
            toast('Error: ' + (err.error || 'No se pudo reenviar'), 'error');
        }
    } catch (err) {
        console.error('Reenvio error:', err);
        toast('Error de conexion al reenviar correo', 'error');
    }
}

// ========================================
// UTILITIES
// ========================================

function formatearFecha(fecha) {
    if (!fecha) return 'N/A';
    const date = new Date(fecha);
    return date.toLocaleDateString('es-ES', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function escapeAttr(text) {
    if (!text) return '';
    return text.replace(/'/g, "\\'").replace(/"/g, '\\"').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}

// Click outside modal-finalizar to close
window.addEventListener('click', function(event) {
    const modal = document.getElementById('modal-finalizar');
    if (event.target === modal) {
        cerrarModal();
    }
});
