// URL base de la API
const API_URL = 'http://localhost:8080/api';

// Estado global
let personas = [];
let partidasActivas = [];
let partidasFinalizadas = [];
let partidaSeleccionada = null;

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

async function cargarPersonas() {
    try {
        const response = await fetch(`${API_URL}/personas`);
        personas = await response.json();
        
        mostrarPersonasEnTabla();
        actualizarSelectorParticipantes();
        
    } catch (error) {
        console.error('Error al cargar personas:', error);
        mostrarMensaje('Error al cargar personas', 'error');
    }
}

function mostrarPersonasEnTabla() {
    const tbody = document.querySelector('#tabla-personas tbody');
    
    if (personas.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="empty-state">No hay personas registradas</td></tr>';
        return;
    }
    
    tbody.innerHTML = personas.map(persona => `
        <tr>
            <td>${persona.id}</td>
            <td>${persona.nom}</td>
            <td>${persona.mail}</td>
            <td><strong>${persona.victories}</strong></td>
            <td>
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
        ranking = [...personas].sort((a, b) => b.victories - a.victories);
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
            cargarPersonas();
        } else {
            const error = await response.json();
            mostrarMensaje(`Error: ${error.error || 'No se pudo crear la persona'}`, 'error');
        }
    } catch (error) {
        console.error('Error:', error);
        mostrarMensaje('Error al crear persona', 'error');
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
            cargarPersonas();
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
            mostrarMensaje('Partida creada correctamente. Emails enviados a los participantes!', 'success');
            document.getElementById('form-partida').reset();
            cargarPartidas();
        } else {
            const error = await response.json();
            mostrarMensaje(`Error: ${error.error || 'No se pudo crear la partida'}`, 'error');
        }
    } catch (error) {
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
            cargarPartidas();
            cargarPersonas(); // Para actualizar victorias
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

// Cerrar modal al hacer click fuera de él
window.onclick = function(event) {
    const modal = document.getElementById('modal-finalizar');
    if (event.target === modal) {
        cerrarModal();
    }
}
