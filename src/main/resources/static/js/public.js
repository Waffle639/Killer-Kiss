const API_URL = '/api';

// ========================================
// TOAST NOTIFICATION SYSTEM
// ========================================

function toast(message, type, duration) {
    type = type || 'info';
    duration = duration || 4000;

    var container = document.querySelector('.toast-container');
    if (!container) {
        container = document.createElement('div');
        container.className = 'toast-container';
        document.body.appendChild(container);
    }

    var icons = { success: '\u2713', error: '\u2717', info: '\u2139' };
    var el = document.createElement('div');
    el.className = 'toast ' + type;
    el.innerHTML = '<span class="toast-icon">' + (icons[type] || icons.info) + '</span><span class="toast-message"></span>';
    el.querySelector('.toast-message').textContent = message;
    container.appendChild(el);

    setTimeout(function() {
        el.classList.add('leaving');
        el.addEventListener('animationend', function() { el.remove(); });
    }, duration);
}

// ========================================
// INITIALIZATION
// ========================================

document.addEventListener('DOMContentLoaded', function() {
    cargarRanking();
    cargarPartidasActivas();

    document.getElementById('form-login').addEventListener('submit', handleLogin);
});

// ========================================
// TAB NAVIGATION
// ========================================

function showTab(tabName, event) {
    document.querySelectorAll('.tab-content').forEach(function(tab) {
        tab.classList.remove('active');
    });
    document.querySelectorAll('.tab-button').forEach(function(btn) {
        btn.classList.remove('active');
    });

    document.getElementById(tabName + '-tab').classList.add('active');
    if (event) event.target.classList.add('active');

    if (tabName === 'estadisticas') {
        cargarEstadisticas();
    }
}

// ========================================
// RANKING
// ========================================

function cargarRanking() {
    fetch(API_URL + '/personas/ranking')
        .then(function(r) { return r.json(); })
        .then(function(ranking) { mostrarRanking(ranking); })
        .catch(function(e) { console.error('Error al cargar ranking:', e); });
}

function mostrarRanking(ranking) {
    var container = document.getElementById('ranking-personas');

    if (!Array.isArray(ranking) || ranking.length === 0) {
        container.innerHTML = '<p class="empty-state">No hay datos para mostrar</p>';
        return;
    }

    var medals = ['\uD83E\uDD47', '\uD83E\uDD48', '\uD83E\uDD49'];

    container.innerHTML = ranking.map(function(persona, index) {
        var isTop3 = index < 3;
        return '<div class="ranking-item' + (isTop3 ? ' top-three' : '') + '">' +
            '<div class="ranking-position">' + (medals[index] || (index + 1)) + '</div>' +
            '<span class="ranking-name">' + escapeHtml(persona.nom) + '</span>' +
            '<span class="ranking-victories">' + persona.victories + ' ' + (persona.victories === 1 ? 'victoria' : 'victorias') + '</span>' +
            '</div>';
    }).join('');
}

function escapeHtml(text) {
    var div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// ========================================
// PARTIDAS ACTIVAS
// ========================================

function cargarPartidasActivas() {
    fetch(API_URL + '/partidas/activas')
        .then(function(r) { return r.json(); })
        .then(function(partidas) {
            var tbody = document.querySelector('#tabla-partidas-activas tbody');

            if (partidas.length === 0) {
                tbody.innerHTML = '<tr><td colspan="2" class="empty-state">No hay partidas activas</td></tr>';
                return;
            }

            tbody.innerHTML = partidas.map(function(p) {
                return '<tr><td><strong>' + escapeHtml(p.nom) + '</strong></td><td>' + (p.personas ? p.personas.length : 0) + '</td></tr>';
            }).join('');
        })
        .catch(function(e) { console.error('Error cargando partidas:', e); });
}

// ========================================
// ESTADISTICAS
// ========================================

function cargarEstadisticas() {
    fetch(API_URL + '/partidas/estadisticas')
        .then(function(r) { return r.json(); })
        .then(function(stats) {
            document.getElementById('stat-total-partidas').textContent = stats.totalPartidas;
            document.getElementById('stat-partidas-activas').textContent = stats.partidasActivas;
            document.getElementById('stat-partidas-finalizadas').textContent = stats.partidasFinalizadas;
            document.getElementById('stat-total-jugadores').textContent = stats.totalJugadores;
        })
        .catch(function(e) { console.error('Error al cargar estadisticas:', e); });
}

// ========================================
// LOGIN
// ========================================

function showLogin() {
    var modal = document.getElementById('login-modal');
    modal.style.display = 'block';
    document.getElementById('login-error').textContent = '';
    document.getElementById('login-error').style.display = 'none';
    setTimeout(function() {
        document.getElementById('username').focus();
    }, 150);
}

function closeLogin() {
    document.getElementById('login-modal').style.display = 'none';
}

function handleLogin(e) {
    e.preventDefault();

    var username = document.getElementById('username').value;
    var password = document.getElementById('password').value;
    var errorEl = document.getElementById('login-error');

    fetch(API_URL + '/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username: username, password: password })
    })
    .then(function(r) { return r.json(); })
    .then(function(data) {
        if (data.success) {
            localStorage.setItem('authenticated', 'true');
            window.location.href = 'index.html';
        } else {
            errorEl.textContent = data.message || 'Credenciales incorrectas';
            errorEl.style.display = 'block';
        }
    })
    .catch(function() {
        errorEl.textContent = 'Error de conexion';
        errorEl.style.display = 'block';
    });
}

// Click outside login modal to close
document.getElementById('login-modal').addEventListener('click', function(e) {
    if (e.target === this) {
        closeLogin();
    }
});

// Escape key to close login
document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
        var modal = document.getElementById('login-modal');
        if (modal.style.display === 'block') {
            closeLogin();
        }
    }
});
