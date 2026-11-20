@echo off
chcp 65001 >nul
if not exist target\classes (
  echo Primero compila el proyecto con: mvn compile
  pause
  exit /b 1
)
if not exist herramientas\out mkdir herramientas\out
javac -encoding UTF-8 -cp target\classes -d herramientas\out herramientas\ValidadorNiveles.java
for %%N in (1 2 3) do java -Dfile.encoding=UTF-8 -cp target\classes;herramientas\out ValidadorNiveles %%N
pause
