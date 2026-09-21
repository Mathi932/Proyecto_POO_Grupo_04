#!/bin/bash
# Crea la estructura de carpetas del proyecto "Rescate en la Selva".
#
# Uso:
#   1. Coloca este archivo en la carpeta donde quieren crear el proyecto.
#   2. Ejecuta:  bash crear-estructura.sh
#   3. Se crea la carpeta "rescate-en-la-selva" con todo el árbol adentro.
#
# Funciona en Mac, Linux, o Windows con Git Bash (el que ya usan para Git).

BASE="rescate-en-la-selva"
PKG="$BASE/src/main/java/com/upch/rescateselva"

mkdir -p "$BASE/docs/uml"
mkdir -p "$PKG/core"
mkdir -p "$PKG/modelo/personajes"
mkdir -p "$PKG/modelo/enemigos"
mkdir -p "$PKG/modelo/obstaculos"
mkdir -p "$PKG/niveles/estado"
mkdir -p "$PKG/patrones/observer"
mkdir -p "$PKG/patrones/factory"
mkdir -p "$PKG/ui"
mkdir -p "$BASE/src/main/resources/sprites"
mkdir -p "$BASE/src/main/resources/sonidos"
mkdir -p "$BASE/src/test/java/com/upch/rescateselva"

touch "$BASE/README.md"
touch "$BASE/.gitignore"
touch "$BASE/pom.xml"
touch "$BASE/docs/decisiones-diseno.md"

# .gitkeep para que Git no ignore las carpetas de recursos vacías
touch "$BASE/src/main/resources/sprites/.gitkeep"
touch "$BASE/src/main/resources/sonidos/.gitkeep"

echo "Listo. Estructura creada en ./$BASE"
