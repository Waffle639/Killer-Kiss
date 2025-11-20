// Sistema de internacionalización simple
let currentLang = localStorage.getItem('language') || 'es';
let translations = {};

// Cargar traducciones
async function loadTranslations(lang) {
    try {
        const response = await fetch(`/i18n/${lang}.json`);
        translations = await response.json();
        currentLang = lang;
        localStorage.setItem('language', lang);
        applyTranslations();
        updateLanguageSwitch();
    } catch (error) {
        console.error('Error cargando traducciones:', error);
    }
}

// Aplicar traducciones a elementos con data-i18n
function applyTranslations() {
    document.querySelectorAll('[data-i18n]').forEach(element => {
        const key = element.getAttribute('data-i18n');
        const translation = getTranslation(key);
        
        if (translation) {
            if (element.tagName === 'INPUT' && element.placeholder !== undefined) {
                element.placeholder = translation;
            } else {
                element.textContent = translation;
            }
        }
    });
}

// Obtener traducción por clave (ej: "header.title")
function getTranslation(key) {
    const keys = key.split('.');
    let value = translations;
    
    for (const k of keys) {
        if (value && value[k]) {
            value = value[k];
        } else {
            return key; // Si no encuentra, devuelve la clave
        }
    }
    
    return value;
}

// Actualizar el switch visual de idioma
function updateLanguageSwitch() {
    const langEs = document.getElementById('lang-es');
    const langCa = document.getElementById('lang-ca');
    
    if (langEs && langCa) {
        if (currentLang === 'es') {
            langEs.classList.add('active');
            langCa.classList.remove('active');
        } else {
            langCa.classList.add('active');
            langEs.classList.remove('active');
        }
    }
}

// Cambiar idioma
function changeLanguage(lang) {
    if (lang !== currentLang) {
        loadTranslations(lang);
    }
}

// Obtener idioma actual
function getCurrentLanguage() {
    return currentLang;
}

// Inicializar al cargar la página
document.addEventListener('DOMContentLoaded', () => {
    loadTranslations(currentLang);
});
