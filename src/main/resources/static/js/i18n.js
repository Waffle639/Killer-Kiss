let currentLang = localStorage.getItem('language') || 'es';
let translations = {};

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

function applyTranslations() {
    document.querySelectorAll('[data-i18n]').forEach(element => {
        const key = element.getAttribute('data-i18n');
        const translation = getTranslation(key);

        if (translation) {
            if (element.tagName === 'INPUT' && element.placeholder !== undefined) {
                element.placeholder = translation;
            } else if (element.tagName === 'INPUT' || element.tagName === 'TEXTAREA') {
                // Skip value changes for inputs
            } else {
                element.textContent = translation;
            }
        }
    });

    document.querySelectorAll('[data-i18n-placeholder]').forEach(element => {
        const key = element.getAttribute('data-i18n-placeholder');
        const translation = getTranslation(key);
        if (translation && element.placeholder !== undefined) {
            element.placeholder = translation;
        }
    });
}

function getTranslation(key) {
    const keys = key.split('.');
    let value = translations;

    for (const k of keys) {
        if (value && value[k]) {
            value = value[k];
        } else {
            return key;
        }
    }

    return value;
}

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

function changeLanguage(lang) {
    if (lang !== currentLang) {
        loadTranslations(lang);
    }
}

function getCurrentLanguage() {
    return currentLang;
}

document.addEventListener('DOMContentLoaded', () => {
    loadTranslations(currentLang);
});
