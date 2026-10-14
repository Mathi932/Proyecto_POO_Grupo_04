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
* **Arquitectura:** Game Loop a 60 FPS desacoplado (`GamePanel`), renderizado con Swing y AWT.
* **Patrones GoF:**
  * **Factory Method:** `FabricaEnemigos` para la instanciación limpia de adversarios (`Cazador`, `Serpiente`).
  * **Observer:** `PuntuacionSubject` y `HUD` para actualización desacoplada de estadísticas y vida.
  * **State:** `EstadoNivel` (`Explorando`, `Huyendo`) para el control de fases por nivel.
* **Integración API REST:** Cliente `APIClient` asíncrono y tolerante a fallos para el registro remoto de puntuaciones.

---

## ⌨️ Controles
* **Moverse:** Flechas direccionales izquierda / derecha (o `A` / `D`)
* **Saltar:** Flecha arriba / Barra espaciadora / `W`
* **Lanzar Distractor:** Tecla `F`

---

## 🚀 Compilación y Ejecución
Requiere **Java 21 LTS** y **Apache Maven**:

```bash
# Compilar el proyecto
mvn compile

# Ejecutar el videojuego
java -cp target/classes com.upch.rescateselva.Main
