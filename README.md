# Kawsay: Rescate en la Selva 🐾🌲

Videojuego de plataformas 2D desarrollado en Java SE para el curso de **Programación Orientada a Objetos** de la **Universidad Peruana Cayetano Heredia**.

---

## 👥 Integrantes (Grupo 04)
* **Mathias Veramendi**
* **Ríos Villar Juan de Dios**
* **Varsola Sebastián**
* **Elmer David**

## 👨‍🏫 Docentes
* **MSc. Wilder Nina Choquehuayta** (Profesor Adjunto Principal)
* **MSc. Percy Wilianson Lovón Ramos** (Profesor Adjunto Auxiliar 1)
* **Dr. Emanuel Ortiz Ortiz** (Profesor Adjunto Auxiliar 2)

---

## 🎮 Descripción del Proyecto
El jugador controla a **Amaru**, un explorador comprometido con el rescate y preservación de la fauna amazónica frente a cazadores clandestinos y trampas en la selva. El proyecto implementa los pilares de la POO, modularidad y patrones de diseño.

### 🛠️ Características Técnicas
* **Arquitectura:** Game Loop a 60 FPS desacoplado (`GamePanel`), renderizado con Swing y AWT. 22 clases organizadas en paquetes (`core`, `modelo`, `niveles`, `patrones`, `ui`).
* **Pilares de la POO:** abstracción (`Personaje`, `AnimalAtrapado`, `Enemigo`, `Obstaculo`), herencia, polimorfismo (`update()`, `dibujar()`, `liberar()`, `atacar()`, `afectar()`), encapsulamiento, y las relaciones de composición, agregación, asociación y dependencia.
* **Patrón de diseño:** **Factory** — `FabricaEnemigos` crea los enemigos (`Cazador`, `Serpiente`) a partir de un tipo, sin que el nivel conozca la clase concreta.
* **Reglas del juego:** el tiempo total de la partida se muestra en el HUD; tras liberar a los animales hay una fase de huida contra reloj hasta la salida.

---

## ⌨️ Controles
* **Moverse:** Flechas direccionales izquierda / derecha (o `A` / `D`)
* **Saltar:** Flecha arriba / Barra espaciadora / `W`
* **Liberar al animal atrapado:** mantener `E`
* **Lanzar una piedra (Distractor):** Tecla `F`
* **Pausa:** `P` / `Esc`

---

## 🚀 Compilación y Ejecución
Requiere **Java 17 (o superior)** y **Apache Maven**:

```bash
# Compilar el proyecto
mvn compile

# Ejecutar el videojuego
java -cp target/classes com.upch.rescateselva.Main
