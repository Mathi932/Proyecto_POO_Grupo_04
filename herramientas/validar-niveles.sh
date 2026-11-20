#!/bin/sh
# Revisa los tres niveles. Ejecutar desde la carpeta del proyecto, después de "mvn compile".
[ -d target/classes ] || { echo "Primero compila el proyecto con: mvn compile"; exit 1; }
mkdir -p herramientas/out
javac -encoding UTF-8 -cp target/classes -d herramientas/out herramientas/ValidadorNiveles.java
for n in 1 2 3; do java -Dfile.encoding=UTF-8 -cp target/classes:herramientas/out ValidadorNiveles $n; done
