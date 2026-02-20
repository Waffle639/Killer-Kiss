// URL base de la API (usar ruta relativa para evitar problemas CORS cuando el host cambia)
const API_URL = '/api';

// Configuración de EmailJS - se carga desde el servidor en tiempo de ejecución
// (los valores reales viven en las variables de entorno del servidor, no aquí)
let emailjsConfig = null;

// Estado global
let personas = [];
let partidasActivas = [];
let partidasFinalizadas = [];
let partidaSeleccionada = null;

// Logout
function logout() {
    localStorage.removeItem('authenticated');
    window.location.href = 'public.html';
}

// ========================================
// INICIALIZACIÓN
// ========================================

document.addEventListener('DOMContentLoaded', async () => {
    // Configurar selector de idioma
    const langSelector = document.getElementById('language-selector');
    if (langSelector) {
        langSelector.value = getCurrentLanguage();
    }
    
    // Cargar configuración de EmailJS desde el servidor
    try {
        const res = await fetch(`${API_URL}/config`);
        emailjsConfig = await res.json();
        emailjs.init(emailjsConfig.emailjsPublicKey);
    } catch (e) {
        console.error('No se pudo cargar la configuración de EmailJS:', e);
    }
    
    // Cargar solo la primera pestaña (personas)
    cargarPersonas();
    TabActual = 'personas';
    
    // Event listeners de formularios
    document.getElementById('form-persona').addEventListener('submit', crearPersona);
    document.getElementById('form-partida').addEventListener('submit', crearPartida);
});

// ========================================
// NAVEGACIÓN ENTRE TABS
// ========================================

function showTab(tabName, event) {
    // Ocultar todos los tabs
    document.querySelectorAll('.tab-content').forEach(tab => {
        tab.classList.remove('active');
    });
    
    // Desactivar todos los botones
    document.querySelectorAll('.tab-button').forEach(button => {
        button.classList.remove('active');
    });
    
    // Mostrar el tab seleccionado
    document.getElementById(`${tabName}-tab`).classList.add('active');
    
    // Activar el botón correspondiente
    if (event && event.target) {
        event.target.classList.add('active');
    } else {
        // Si no hay event, buscar el botón manualmente
        document.querySelectorAll('.tab-button').forEach(button => {
            if (button.getAttribute('onclick').includes(tabName)) {
                button.classList.add('active');
            }
        });
    }
    if (tabName !== TabActual) {
        // Cargar datos según el tab seleccionado
        if (tabName === 'personas') {
            cargarPersonas();
            TabActual = 'personas';
        } else if (tabName === 'partidas') {
            cargarPartidas();
            TabActual = 'partidas';
        } else if (tabName === 'ranking') {
            cargarRanking();
            TabActual = 'ranking';
        } else if (tabName === 'estadisticas') {
            cargarEstadisticas();
            TabActual = 'estadisticas';
        }
    }
}

// ========================================
// FUNCIONES PERSONAS
// ========================================

async function cargarPersonas() {
    try {
        const response = await fetch(`${API_URL}/personas`);
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const data = await response.json();
        
        // Validar que sea un array
        if (Array.isArray(data)) {
            personas = data;
        } else {
            console.error('La respuesta no es un array:', data);
            personas = [];
        }
        
        mostrarPersonasEnTabla();
        actualizarSelectorParticipantes();
        
    } catch (error) {
        console.error('Error al cargar personas:', error);
        mostrarMensaje('Error al cargar personas', 'error');
        personas = [];
    }
}

function mostrarPersonasEnTabla() {
    const tbody = document.querySelector('#tabla-personas tbody');
    
    // Validar que personas sea un array
    if (!Array.isArray(personas)) {
        console.error('personas no es un array:', personas);
        tbody.innerHTML = '<tr><td colspan="5" class="empty-state">Error al cargar personas</td></tr>';
        return;
    }
    
    if (personas.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="empty-state">No hay personas registradas</td></tr>';
        return;
    }
    
    tbody.innerHTML = personas.map(persona => `
        <tr>
            <td>${persona.id}</td>
            <td>${persona.nom}</td>
            <td>${persona.mail ? persona.mail : '<span style="color: red;">❌ Sin email</span>'}</td>
            <td><strong>${persona.victories}</strong></td>
            <td>
                <button class="btn-action btn-edit" onclick="abrirModalEditarPersona(${persona.id})">
                    Editar
                </button>
                <button class="btn-action btn-delete" onclick="eliminarPersona(${persona.id})">
                    Eliminar
                </button>
            </td>
        </tr>
    `).join('');
}

async function cargarRanking() {
    try {
        const response = await fetch(`${API_URL}/personas/ranking`);
        const ranking = await response.json();
        mostrarRanking(ranking);
    } catch (error) {
        console.error('Error al cargar ranking:', error);
        mostrarMensaje('Error al cargar ranking', 'error');
    }
}

function mostrarRanking(ranking = null) {
    const container = document.getElementById('ranking-personas');
    
    // Si no se proporciona ranking, usar personas ordenadas
    if (!ranking) {
        // Validar que personas sea un array
        if (!Array.isArray(personas)) {
            console.error('personas no es un array:', personas);
            container.innerHTML = '<p class="empty-state">Error al cargar ranking</p>';
            return;
        }
        ranking = [...personas].sort((a, b) => b.victories - a.victories);
    }
    
    // Validar que ranking sea un array
    if (!Array.isArray(ranking)) {
        console.error('ranking no es un array:', ranking);
        container.innerHTML = '<p class="empty-state">Error al cargar ranking</p>';
        return;
    }
    
    if (ranking.length === 0) {
        container.innerHTML = '<p class="empty-state">No hay datos para mostrar</p>';
        return;
    }
    
    container.innerHTML = ranking.map((persona, index) => {
        let medalEmoji = '';
        if (index === 0) medalEmoji = '🥇';
        else if (index === 1) medalEmoji = '🥈';
        else if (index === 2) medalEmoji = '🥉';
        
        return `
            <div class="ranking-item ${index < 3 ? 'top-three' : ''}">
                <div class="ranking-position">${medalEmoji || (index + 1)}</div>
                <div class="ranking-info">
                    <strong>${persona.nom}</strong><br>
                    <small>${persona.mail}</small>
                </div>
                <div class="ranking-victories">${persona.victories} victorias</div>
            </div>
        `;
    }).join('');
}

async function crearPersona(event) {
    event.preventDefault();
    
    const nombre = document.getElementById('persona-nombre').value;
    const email = document.getElementById('persona-email').value;
    
    try {
        const response = await fetch(`${API_URL}/personas`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                nom: nombre,
                mail: email,
                victories: 0
            })
        });
        
        if (response.ok) {
            mostrarMensaje('Persona creada correctamente', 'success');
            document.getElementById('form-persona').reset();
            cargarPersonas(); // Forzar recarga para obtener datos actualizados
        } else {
            const error = await response.json();
            mostrarMensaje(`Error: ${error.error || 'No se pudo crear la persona'}`, 'error');
        }
    } catch (error) {
        console.error('Error:', error);
        mostrarMensaje('Error al crear persona', 'error');
    }
}

function abrirModalEditarPersona(id) {
    const persona = personas.find(p => p.id === id);
    if (!persona) {
        mostrarMensaje('Persona no encontrada', 'error');
        return;
    }
    
    // Crear modal
    const modal = document.createElement('div');
    modal.id = 'modal-editar-persona';
    modal.className = 'modal';
    modal.style.display = 'block'; // Mostrar el modal
    modal.innerHTML = `
        <div class="modal-content">
            <h2>Editar Persona</h2>
            <form id="form-editar-persona">
                <div class="form-group">
                    <label for="editar-nombre">Nombre:</label>
                    <input type="text" id="editar-nombre" value="${persona.nom}" required>
                </div>
                <div class="form-group">
                    <label for="editar-email">Email:</label>
                    <input type="email" id="editar-email" value="${persona.mail || ''}" placeholder="email@ejemplo.com">
                </div>
                <div class="modal-actions">
                    <button type="submit" class="btn-action">Guardar</button>
                    <button type="button" class="btn-action btn-delete" onclick="cerrarModalEditarPersona()">Cancelar</button>
                </div>
            </form>
        </div>
    `;
    
    document.body.appendChild(modal);
    
    // Agregar evento al formulario
    document.getElementById('form-editar-persona').addEventListener('submit', (e) => {
        e.preventDefault();
        editarPersona(id);
    });
    
    // Cerrar modal al hacer clic fuera del contenido
    modal.addEventListener('click', (e) => {
        if (e.target === modal) {
            cerrarModalEditarPersona();
        }
    });
}

function cerrarModalEditarPersona() {
    const modal = document.getElementById('modal-editar-persona');
    if (modal) {
        modal.remove();
    }
}

async function editarPersona(id) {
    const nombre = document.getElementById('editar-nombre').value;
    const email = document.getElementById('editar-email').value;
    
    try {
        const response = await fetch(`${API_URL}/personas/${id}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                nom: nombre,
                mail: email || null
            })
        });
        
        if (response.ok) {
            mostrarMensaje('Persona actualizada correctamente', 'success');
            cerrarModalEditarPersona();
            cargarPersonas(); // Forzar recarga para obtener datos actualizados
        } else {
            const error = await response.json();
            mostrarMensaje(`Error: ${error.error || 'No se pudo actualizar la persona'}`, 'error');
        }
    } catch (error) {
        console.error('Error:', error);
        mostrarMensaje('Error al actualizar persona', 'error');
    }
}

async function eliminarPersona(id) {
    if (!confirm('¿Estás seguro de que quieres eliminar esta persona?')) {
        return;
    }
    
    try {
        const response = await fetch(`${API_URL}/personas/${id}`, {
            method: 'DELETE'
        });
        
        if (response.ok) {
            mostrarMensaje('Persona eliminada correctamente', 'success');
            cargarPersonas(); // Forzar recarga para obtener datos actualizados
        } else {
            // Intentar obtener el mensaje de error del servidor
            const errorData = await response.json();
            const errorMsg = errorData.error || 'Error al eliminar persona';
            mostrarMensaje(errorMsg, 'error');
        }
    } catch (error) {
        console.error('Error:', error);
        mostrarMensaje('Error al eliminar persona', 'error');
    }
}

// ========================================
// FUNCIONES PARTIDAS
// ========================================

async function cargarPartidas() {
    try {
        // Cargar partidas activas
        const responseActivas = await fetch(`${API_URL}/partidas/activas`);
        partidasActivas = await responseActivas.json();
        mostrarPartidasActivas();
        
        // Cargar partidas finalizadas
        const responseFinalizadas = await fetch(`${API_URL}/partidas/finalizadas`);
        partidasFinalizadas = await responseFinalizadas.json();
        mostrarPartidasFinalizadas();
        
    } catch (error) {
        console.error('Error al cargar partidas:', error);
        mostrarMensaje('Error al cargar partidas', 'error');
    }
}

function mostrarPartidasActivas() {
    const tbody = document.querySelector('#tabla-partidas-activas tbody');
    
    if (partidasActivas.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="empty-state">No hay partidas activas</td></tr>';
        return;
    }
    
    tbody.innerHTML = partidasActivas.map(partida => {
        const tieneCorreosFallidos = partida.asignaciones && Object.keys(partida.asignaciones).length > 0;
        
        return `
            <tr>
                <td>${partida.id}</td>
                <td><strong>${partida.nom}</strong></td>
                <td>${partida.personas.length} jugadores</td>
                <td>${formatearFecha(partida.fechaCreacion)}</td>
                <td>
                    ${tieneCorreosFallidos ? `
                        <button class="btn-action btn-reenviar" onclick="mostrarCorreosFallidos(${partida.id})">
                            📧 Reenviar (${Object.keys(partida.asignaciones).length})
                        </button>
                    ` : ''}
                    <button class="btn-action btn-finish" onclick="abrirModalFinalizar(${partida.id})">
                        🏆 Finalizar
                    </button>
                </td>
            </tr>
        `;
    }).join('');
}

function mostrarPartidasFinalizadas() {
    const tbody = document.querySelector('#tabla-partidas-finalizadas tbody');
    
    if (partidasFinalizadas.length === 0) {
        tbody.innerHTML = '<tr><td colspan="4" class="empty-state">No hay partidas finalizadas</td></tr>';
        return;
    }
    
    tbody.innerHTML = partidasFinalizadas.map(partida => `
        <tr>
            <td>${partida.id}</td>
            <td>${partida.nom}</td>
            <td><strong>${partida.ganador ? partida.ganador.nom : 'N/A'}</strong></td>
            <td>${formatearFecha(partida.fechaFinalizacion)}</td>
        </tr>
    `).join('');
}

function actualizarSelectorParticipantes() {
    const container = document.getElementById('lista-participantes');
    
    // Validar que personas sea un array
    if (!Array.isArray(personas)) {
        console.error('personas no es un array:', personas);
        container.innerHTML = '<p class="empty-state">Error al cargar personas</p>';
        return;
    }
    
    if (personas.length === 0) {
        container.innerHTML = '<p class="empty-state">Primero debes añadir personas</p>';
        return;
    }
    
    const checkboxes = personas.map(persona => `
        <label class="participante-checkbox">
            <input type="checkbox" name="participante" value="${persona.id}" onchange="actualizarCheckboxSelectAll()">
            ${persona.nom}
        </label>
    `).join('');
    
    container.innerHTML = checkboxes;
}

function toggleSeleccionarTodos() {
    const selectAllCheckbox = document.getElementById('select-all-checkbox');
    const checkboxes = document.querySelectorAll('input[name="participante"]');
    
    checkboxes.forEach(cb => {
        cb.checked = selectAllCheckbox.checked;
    });
}

function actualizarCheckboxSelectAll() {
    const selectAllCheckbox = document.getElementById('select-all-checkbox');
    const checkboxes = document.querySelectorAll('input[name="participante"]');
    const allChecked = Array.from(checkboxes).every(cb => cb.checked);
    
    if (selectAllCheckbox) {
        selectAllCheckbox.checked = allChecked;
    }
}

async function crearPartida(event) {
    event.preventDefault();
    
    const nombre = document.getElementById('partida-nombre').value;
    const checkboxes = document.querySelectorAll('input[name="participante"]:checked');
    
    if (checkboxes.length < 2) {
        mostrarMensaje('Debes seleccionar al menos 2 participantes', 'error');
        return;
    }
    
    // Crear array de personas con solo el ID
    const participantes = Array.from(checkboxes).map(checkbox => ({
        id: parseInt(checkbox.value)
    }));
    
    try {
        // Mostrar modal de carga
        mostrarModalCargaCorreos();
        
        const response = await fetch(`${API_URL}/partidas`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                nom: nombre,
                personas: participantes
            })
        });
        
        if (response.ok) {
            const partida = await response.json();
            document.getElementById('form-partida').reset();
            cargarPartidas(); // Forzar recarga para obtener datos actualizados
            
            // Obtener asignaciones y enviar via EmailJS
            setTimeout(async () => {
                try {
                    const statsResponse = await fetch(`${API_URL}/partidas/${partida.id}/enviar-correos`, {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ idioma: getCurrentLanguage() })
                    });
                    if (statsResponse.ok) {
                        const body = await statsResponse.json();
                        const resultado = body.resultado || body;
                        // Enviar emails via EmailJS desde el navegador
                        await enviarEmailsConEmailJS(resultado.detalles);
                        resultado.exitosos = resultado.detalles.filter(d => d.exitoso).length;
                        resultado.fallidos = resultado.detalles.filter(d => !d.exitoso).length;
                        mostrarModalResultadoEnvio(resultado);
                    } else {
                        cerrarModalCargaCorreos();
                        mostrarMensaje('✅ Partida creada correctamente!', 'success');
                    }
                } catch (e) {
                    cerrarModalCargaCorreos();
                    mostrarMensaje('✅ Partida creada correctamente!', 'success');
                }
            }, 1000);
        } else {
            cerrarModalCargaCorreos();
            const error = await response.json();
            mostrarMensaje(`Error: ${error.error || 'No se pudo crear la partida'}`, 'error');
        }
    } catch (error) {
        cerrarModalCargaCorreos();
        console.error('Error:', error);
        mostrarMensaje('Error al crear partida', 'error');
    }
}

function abrirModalFinalizar(partidaId) {
    const partida = partidasActivas.find(p => p.id === partidaId);
    if (!partida) return;
    
    partidaSeleccionada = partida;
    
    const container = document.getElementById('modal-ganadores-lista');
    container.innerHTML = partida.personas.map(persona => `
        <div class="ganador-option" onclick="finalizarPartida(${partida.id}, ${persona.id})">
            ${persona.nom}
        </div>
    `).join('');
    
    document.getElementById('modal-finalizar').style.display = 'block';
}

function cerrarModal() {
    document.getElementById('modal-finalizar').style.display = 'none';
    partidaSeleccionada = null;
}

async function finalizarPartida(partidaId, ganadorId) {
    try {
        const response = await fetch(`${API_URL}/partidas/${partidaId}/finalizar`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                ganadorId: ganadorId
            })
        });
        
        if (response.ok) {
            mostrarMensaje('Partida finalizada correctamente!', 'success');
            cerrarModal();
            cargarPartidas(); // Forzar recarga para obtener datos actualizados
            cargarPersonas(); // Forzar recarga para actualizar victorias
            cargarEstadisticas();
        } else {
            const error = await response.json();
            mostrarMensaje(`Error: ${error.error || 'No se pudo finalizar la partida'}`, 'error');
        }
    } catch (error) {
        console.error('Error:', error);
        mostrarMensaje('Error al finalizar partida', 'error');
    }
}

// ========================================
// ESTADÍSTICAS
// ========================================

async function cargarEstadisticas() {
    try {
        const response = await fetch(`${API_URL}/partidas/estadisticas`);
        const stats = await response.json();
        
        document.getElementById('stat-total-partidas').textContent = stats.totalPartidas;
        document.getElementById('stat-partidas-activas').textContent = stats.partidasActivas;
        document.getElementById('stat-partidas-finalizadas').textContent = stats.partidasFinalizadas;
        document.getElementById('stat-total-jugadores').textContent = stats.totalJugadores;
        
    } catch (error) {
        console.error('Error al cargar estadísticas:', error);
    }
}

// ========================================
// UTILIDADES
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

function mostrarMensaje(texto, tipo = 'info') {
    // Crear elemento de mensaje
    const mensaje = document.createElement('div');
    mensaje.className = `message message-${tipo}`;
    mensaje.textContent = texto;
    
    // Insertar al principio del container
    const container = document.querySelector('.container');
    container.insertBefore(mensaje, container.firstChild);
    
    // Eliminar después de 5 segundos
    setTimeout(() => {
        mensaje.style.animation = 'fadeOut 0.3s ease';
        setTimeout(() => mensaje.remove(), 300);
    }, 5000);
}

// ========================================
// ENVÍO DE CORREOS - EmailJS
// ========================================

/**
 * Envía emails a los participantes usando EmailJS desde el navegador.
 * @param {Array} detalles - Lista de { nombre, email, exitoso, victima } del backend
 */
async function enviarEmailsConEmailJS(detalles) {
    if (!emailjsConfig) {
        console.error('EmailJS no configurado');
        return;
    }
    for (const detalle of detalles) {
        if (!detalle.exitoso || !detalle.email || detalle.email === 'Sin email') {
            continue;
        }
        try {
            const params = {
                email: detalle.email,
                to_name: detalle.nombre,
                name: detalle.nombre,
                target_name: detalle.victima
            };
            await emailjs.send(emailjsConfig.emailjsServiceId, emailjsConfig.emailjsTemplateId, params);
            detalle.exitoso = true;
            detalle.mensaje = 'Enviado correctamente';
        } catch (err) {
            detalle.exitoso = false;
            detalle.mensaje = 'Error EmailJS: ' + (err.text || err.message || JSON.stringify(err));
            console.error('EmailJS error para', detalle.email, err);
        }
    }
}

async function enviarCorreosPartida(partidaId) {
    try {
        mostrarMensaje('Obteniendo asignaciones...', 'info');
        mostrarModalCargaCorreos();
        
        const response = await fetch(`${API_URL}/partidas/${partidaId}/enviar-correos`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ idioma: getCurrentLanguage() })
        });
        
        if (response.ok) {
            const body = await response.json();
            const resultado = body.resultado || body;
            await enviarEmailsConEmailJS(resultado.detalles);
            resultado.exitosos = resultado.detalles.filter(d => d.exitoso).length;
            resultado.fallidos = resultado.detalles.filter(d => !d.exitoso).length;
            mostrarModalResultadoEnvio(resultado);
        } else {
            cerrarModalCargaCorreos();
            const error = await response.json();
            mostrarMensaje(`Error: ${error.error || 'No se pudieron enviar los correos'}`, 'error');
        }
    } catch (error) {
        cerrarModalCargaCorreos();
        console.error('Error:', error);
        mostrarMensaje('Error al enviar correos', 'error');
    }
}

function mostrarModalResultadoEnvio(resultado) {
    // Cerrar modal de carga primero
    cerrarModalCargaCorreos();
    
    // Crear modal dinámicamente
    const modalHtml = `
        <div id="modal-envio" class="modal" style="display: block;">
            <div class="modal-content" style="padding: 20px 30px 30px 30px;">
                <button class="close-btn" onclick="cerrarModalEnvio()" title="Cerrar">✕</button>
                <h2 style="margin-top: 5px;">📧 Resultado del Envío de Correos</h2>
                
                <div class="envio-resumen">
                    <div class="envio-stat-card success">
                        <div class="envio-icon-large">✅</div>
                        <div class="envio-numero">${resultado.exitosos}</div>
                        <div class="envio-label">Enviados</div>
                    </div>
                    <div class="envio-stat-card ${resultado.fallidos > 0 ? 'error' : ''}">
                        <div class="envio-icon-large">❌</div>
                        <div class="envio-numero">${resultado.fallidos}</div>
                        <div class="envio-label">Fallidos</div>
                    </div>
                    <div class="envio-stat-card info">
                        <div class="envio-icon-large">📊</div>
                        <div class="envio-numero">${resultado.total}</div>
                        <div class="envio-label">Total</div>
                    </div>
                </div>
                
                ${resultado.fallidos > 0 ? `
                    <div class="envio-detalles">
                        <h3>❌ Correos Fallidos:</h3>
                        <div class="envio-lista">
                            ${resultado.detalles.filter(d => !d.exitoso).map(detalle => `
                                <div class="envio-item fallido">
                                    <div class="envio-item-icon">❌</div>
                                    <div class="envio-item-info">
                                        <div class="envio-item-nombre">${detalle.nombre}</div>
                                        <div class="envio-item-email">${detalle.email}</div>
                                        <div class="envio-item-mensaje">${detalle.mensaje}</div>
                                    </div>
                                    ${detalle.email !== 'Sin email' ? `
                                        <button class="btn-reenviar" onclick="reenviarCorreo(${resultado.partidaId}, '${detalle.email}', '${detalle.nombre}')">
                                            📧 Reenviar
                                        </button>
                                    ` : ''}
                                </div>
                            `).join('')}
                        </div>
                    </div>
                ` : ''}
                
                <div class="modal-buttons">
                    <button class="btn-cancel" onclick="cerrarModalEnvio()">Cerrar</button>
                </div>
            </div>
        </div>
    `;
    
    // Agregar modal al body
    const modalContainer = document.createElement('div');
    modalContainer.innerHTML = modalHtml;
    document.body.appendChild(modalContainer.firstElementChild);
}

function mostrarModalCargaCorreos() {
    const modalHtml = `
        <div id="modal-carga-correos" class="modal" style="display: block;">
            <div class="modal-content" style="text-align: center;">
                <h2>Enviando Correos</h2>
                <div style="margin: 40px 0;">
                    <div class="spinner"></div>
                    <p style="margin-top: 20px; font-size: 1.1em; color: #667eea;">
                        Por favor espera mientras se envían los correos a todos los participantes...
                    </p>
                </div>
            </div>
        </div>
    `;
    
    const modalContainer = document.createElement('div');
    modalContainer.innerHTML = modalHtml;
    document.body.appendChild(modalContainer.firstElementChild);
}

function cerrarModalCargaCorreos() {
    const modal = document.getElementById('modal-carga-correos');
    if (modal) {
        modal.remove();
    }
}

function cerrarModalEnvio() {
    const modal = document.getElementById('modal-envio');
    if (modal) {
        modal.remove();
    }
}

// Cerrar modal al hacer click fuera de él
window.onclick = function(event) {
    const modal = document.getElementById('modal-finalizar');
    const modalEnvio = document.getElementById('modal-envio');
    
    if (event.target === modal) {
        cerrarModal();
    }
    
    if (event.target === modalEnvio) {
        cerrarModalEnvio();
    }
}

// cargarContadorEmails y actualizarContadorEnDOM eliminados - ahora se usa EmailJS (client-side)

/**
 * Muestra modal con correos fallidos de una partida
 */
async function mostrarCorreosFallidos(partidaId) {
    try {
        const partida = partidasActivas.find(p => p.id === partidaId);
        if (!partida || !partida.asignaciones) {
            mostrarMensaje('No hay correos pendientes', 'info');
            return;
        }
        
        const asignaciones = partida.asignaciones;
        const personas = partida.personas;
        
        // asignaciones: { emailJugador -> nombreVictima }
        const detalles = Object.entries(asignaciones).map(([emailCazador, nombreVictima]) => {
            const cazador = personas.find(p => p.mail === emailCazador);
            return {
                nombre: cazador ? cazador.nom : 'Desconocido',
                email: emailCazador,
                exitoso: false,
                victima: nombreVictima,
                mensaje: 'Correo pendiente de envío'
            };
        });
        
        const resultado = {
            partidaId: partidaId,
            exitosos: 0,
            fallidos: detalles.length,
            total: detalles.length,
            detalles: detalles
        };
        
        mostrarModalResultadoEnvio(resultado);
    } catch (error) {
        console.error('Error:', error);
        mostrarMensaje('Error al cargar correos fallidos', 'error');
    }
}

/**
 * Reenvía el correo a un jugador específico usando EmailJS
 */
async function reenviarCorreo(partidaId, email, nombre) {
    if (!confirm(`¿Reenviar correo a ${nombre} (${email})?`)) {
        return;
    }
    
    // Obtener nombre de la víctima desde las asignaciones de la partida
    const partida = partidasActivas.find(p => p.id === partidaId);
    const victimaNombre = partida?.asignaciones?.[email];
    
    if (!victimaNombre) {
        mostrarMensaje('❌ No se encontró la asignación para este jugador', 'error');
        return;
    }
    
    try {
        await emailjs.send(emailjsConfig.emailjsServiceId, emailjsConfig.emailjsTemplateId, {
            email: email,
            to_name: nombre,
            name: nombre,
            target_name: victimaNombre
        });
        mostrarMensaje(`✅ Correo reenviado correctamente a ${nombre}`, 'success');
        await cargarPartidas();
        cerrarModalEnvio();
    } catch (err) {
        console.error('EmailJS reenvio error:', err);
        mostrarMensaje(`❌ Error EmailJS: ${err.text || err.message || 'Error desconocido'}`, 'error');
    }
}
