#!/usr/bin/env bash
# Script de build para Render.com

echo "🚀 Iniciando build de Killer-Kiss..."

# Instalar dependencias y compilar
./mvnw clean install -DskipTests

echo "✅ Build completado exitosamente!"
