const API_URL = '/api';

// Cargar datos al inicio
document.addEventListener('DOMContentLoaded', () => {
    cargarRanking();
    cargarPartidasActivas();
});

// Tabs
function showTab(tabName, event) {
    const tabs = document.querySelectorAll('.tab-content');
    tabs.forEach(tab => tab.classList.remove('active'));
    
    const buttons = document.querySelectorAll('.tab-button');
    buttons.forEach(btn => btn.classList.remove('active'));
    
    document.getElementById(`${tabName}-tab`).classList.add('active');
    if (event) event.target.classList.add('active');
    
    // Cargar datos según el tab
    if (tabName === 'estadisticas') {
        cargarEstadisticas();
    }
}

// Ranking (igual que admin)
async function cargarRanking() {
    try {
        const response = await fetch(`${API_URL}/personas/ranking`);
        const ranking = await response.json();
        mostrarRanking(ranking);
    } catch (error) {
        console.error('Error al cargar ranking:', error);
    }
}

function mostrarRanking(ranking) {
    const container = document.getElementById('ranking-personas');
    
    if (!Array.isArray(ranking) || ranking.length === 0) {
        container.innerHTML = '<p class="empty-state">No hay datos para mostrar</p>';
        return;
    }
    
    container.innerHTML = ranking.map((persona, index) => {
        let medalEmoji = '';
        let borderColor = '';
        
        if (index === 0) {
            medalEmoji = '🥇';
            borderColor = 'border-left: 4px solid gold;';
        } else if (index === 1) {
            medalEmoji = '🥈';
            borderColor = 'border-left: 4px solid silver;';
        } else if (index === 2) {
            medalEmoji = '🥉';
            borderColor = 'border-left: 4px solid #cd7f32;';
        } else {
            borderColor = 'border-left: 4px solid #667eea;';
        }
        
        return `
            <div class="ranking-item" style="${borderColor}">
                <span class="ranking-position">${medalEmoji || `#${index + 1}`}</span>
                <span class="ranking-name">${escapeHtml(persona.nom)}</span>
                <span class="ranking-victories">${persona.victories} ${persona.victories === 1 ? 'victoria' : 'victorias'}</span>
            </div>
        `;
    }).join('');
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// Partidas activas
async function cargarPartidasActivas() {
    try {
        const response = await fetch(`${API_URL}/partidas/activas`);
        const partidas = await response.json();
        
        const tbody = document.querySelector('#tabla-partidas-activas tbody');
        
        if (partidas.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4">No hay partidas activas</td></tr>';
            return;
        }
        
        tbody.innerHTML = partidas.map(p => `
            <tr>
                <td>${p.nom}</td>
                <td>${p.personas ? p.personas.length : 0}</td>
            </tr>
        `).join('');
    } catch (error) {
        console.error('Error cargando partidas:', error);
    }
}

// Estadísticas
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

// Login
function showLogin() {
    document.getElementById('login-modal').style.display = 'flex';
}

function closeLogin() {
    document.getElementById('login-modal').style.display = 'none';
}

document.getElementById('form-login').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    
    try {
        const response = await fetch(`${API_URL}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });
        
        const data = await response.json();
        
        if (data.success) {
            localStorage.setItem('authenticated', 'true');
            window.location.href = 'index.html';
        } else {
            document.getElementById('login-error').textContent = data.message;
            document.getElementById('login-error').style.display = 'block';
        }
    } catch (error) {
        document.getElementById('login-error').textContent = 'Error de conexión';
        document.getElementById('login-error').style.display = 'block';
    }
});
