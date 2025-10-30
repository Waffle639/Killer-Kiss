// URL base de la API (usar ruta relativa para evitar problemas CORS cuando el host cambia)
const API_URL = '/api';

// Estado global
let personas = [];
let partidasActivas = [];
let partidasFinalizadas = [];
let partidaSeleccionada = null;

// Control de caché para evitar peticiones innecesarias
let ultimaCargaPersonas = null;
let ultimaCargaPartidas = null;
let ultimaCargaRanking = null;
let tabActual = 'personas'; // Tab actual para no recargar si ya estamos ahí

// ========================================
// INICIALIZACIÓN
// ========================================

document.addEventListener('DOMContentLoaded', () => {
    console.log('🎯 Killer Kiss iniciado');
    
    // Cargar datos iniciales
    cargarPersonas();
    cargarPartidas();
    cargarEstadisticas();
    
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
    
    // Recargar datos según el tab SOLO si es necesario
    // No recargar si ya estamos en ese tab
    if (tabName === tabActual) {
        console.log(`Ya estás en la pestaña ${tabName}, no se recargan datos`);
        return;
    }
    
    // Actualizar tab actual
    tabActual = tabName;
    
    // Recargar datos según el tab
    if (tabName === 'personas') {
        cargarPersonas();
    } else if (tabName === 'partidas') {
        cargarPartidas();
    } else if (tabName === 'ranking') {
        cargarRanking();
    } else if (tabName === 'estadisticas') {
        cargarEstadisticas();
    }
}

// ========================================
// FUNCIONES PERSONAS
// ========================================

async function cargarPersonas(forzarRecarga = false) {
    try {
        // Si ya hay datos y no es recarga forzada, usar caché
        const ahora = Date.now();
        const CACHE_TIEMPO = 30000; // 30 segundos de caché
        
        if (!forzarRecarga && ultimaCargaPersonas && (ahora - ultimaCargaPersonas) < CACHE_TIEMPO && personas.length > 0) {
            console.log('📦 Usando caché de personas');
            mostrarPersonasEnTabla();
            actualizarSelectorParticipantes();
            return;
        }
        
        console.log('🌐 Cargando personas desde la API...');
        const response = await fetch(`${API_URL}/personas`);
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const data = await response.json();
        
        // Validar que sea un array
        if (Array.isArray(data)) {
            personas = data;
            ultimaCargaPersonas = Date.now(); // Actualizar timestamp de caché
        } else {
            console.error('La respuesta no es un array:', data);
            personas = [];
        }
        
        mostrarPersonasEnTabla();
        actualizarSelectorParticipantes();
        
    } catch (error) {
        console.error('Error al cargar personas:', error);
        mostrarMensaje('Error al cargar personas', 'error');
        personas = []; // Asegurar que siempre sea un array
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
            cargarPersonas(true); // Forzar recarga para obtener datos actualizados
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
            cargarPersonas(true); // Forzar recarga para obtener datos actualizados
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
            cargarPersonas(true); // Forzar recarga para obtener datos actualizados
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

async function cargarPartidas(forzarRecarga = false) {
    try {
        // Si ya hay datos y no es recarga forzada, usar caché
        const ahora = Date.now();
        const CACHE_TIEMPO = 30000; // 30 segundos de caché
        
        if (!forzarRecarga && ultimaCargaPartidas && (ahora - ultimaCargaPartidas) < CACHE_TIEMPO && 
            (partidasActivas.length > 0 || partidasFinalizadas.length > 0)) {
            console.log('📦 Usando caché de partidas');
            mostrarPartidasActivas();
            mostrarPartidasFinalizadas();
            return;
        }
        
        console.log('🌐 Cargando partidas desde la API...');
        
        // Cargar partidas activas
        const responseActivas = await fetch(`${API_URL}/partidas/activas`);
        partidasActivas = await responseActivas.json();
        mostrarPartidasActivas();
        
        // Cargar partidas finalizadas
        const responseFinalizadas = await fetch(`${API_URL}/partidas/finalizadas`);
        partidasFinalizadas = await responseFinalizadas.json();
        mostrarPartidasFinalizadas();
        
        ultimaCargaPartidas = Date.now(); // Actualizar timestamp de caché
        
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
    
    tbody.innerHTML = partidasActivas.map(partida => `
        <tr>
            <td>${partida.id}</td>
            <td><strong>${partida.nom}</strong></td>
            <td>${partida.personas.length} jugadores</td>
            <td>${formatearFecha(partida.fechaCreacion)}</td>
            <td>
                <button class="btn-action btn-finish" onclick="abrirModalFinalizar(${partida.id})">
                    🏆 Finalizar
                </button>
            </td>
        </tr>
    `).join('');
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
            cargarPartidas(true); // Forzar recarga para obtener datos actualizados
            
            // Obtener estadísticas de envío
            setTimeout(async () => {
                try {
                    const statsResponse = await fetch(`${API_URL}/partidas/${partida.id}/enviar-correos`, {
                        method: 'POST'
                    });
                    if (statsResponse.ok) {
                        const resultado = await statsResponse.json();
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
            cargarPartidas(true); // Forzar recarga para obtener datos actualizados
            cargarPersonas(true); // Forzar recarga para actualizar victorias
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
// ENVÍO DE CORREOS
// ========================================

async function enviarCorreosPartida(partidaId) {
    try {
        // Mostrar mensaje de carga
        mostrarMensaje('Enviando correos...', 'info');
        
        const response = await fetch(`${API_URL}/partidas/${partidaId}/enviar-correos`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        if (response.ok) {
            const resultado = await response.json();
            mostrarModalResultadoEnvio(resultado);
        } else {
            const error = await response.json();
            mostrarMensaje(`Error: ${error.error || 'No se pudieron enviar los correos'}`, 'error');
        }
    } catch (error) {
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
            <div class="modal-content">
                <span class="close" onclick="cerrarModalEnvio()">&times;</span>
                <h2>📧 Resultado del Envío de Correos</h2>
                
                <div class="envio-resumen">
                    <div class="envio-stat ${resultado.exitosos > 0 ? 'success' : ''}">
                        <span class="envio-icon">✅</span>
                        <div>
                            <div class="envio-numero">${resultado.exitosos}</div>
                            <div class="envio-label">Enviados</div>
                        </div>
                    </div>
                    <div class="envio-stat ${resultado.fallidos > 0 ? 'error' : ''}">
                        <span class="envio-icon">❌</span>
                        <div>
                            <div class="envio-numero">${resultado.fallidos}</div>
                            <div class="envio-label">Fallidos</div>
                        </div>
                    </div>
                    <div class="envio-stat">
                        <div>
                            <div class="envio-numero">${resultado.total}</div>
                            <div class="envio-label">Total</div>
                        </div>
                    </div>
                </div>
                
                <div class="envio-detalles">
                    <h3>Detalles por Participante:</h3>
                    <div class="envio-lista">
                        ${resultado.detalles.map(detalle => `
                            <div class="envio-item ${detalle.exitoso ? 'exitoso' : 'fallido'}">
                                <div class="envio-item-icon">${detalle.exitoso ? '✅' : '❌'}</div>
                                <div class="envio-item-info">
                                    <div class="envio-item-nombre">${detalle.nombre}</div>
                                    <div class="envio-item-email">${detalle.email}</div>
                                    <div class="envio-item-mensaje">${detalle.mensaje}</div>
                                </div>
                            </div>
                        `).join('')}
                    </div>
                </div>
                
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
