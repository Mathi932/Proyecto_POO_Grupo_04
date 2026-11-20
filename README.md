# Kawsay: Rescate en la Selva 

Videojuego de plataformas 2D desarrollado en Java SE para el curso de **Programación Orientada a Objetos** de la **Universidad Peruana Cayetano Heredia**.

---

## Integrantes (Grupo 04)
* **Mathias Veramendi**
* **Ríos Villar Juan de Dios**
* **Barzola Sebastián**
* **Elmer David**

## Docentes
* **MSc. Wilder Nina Choquehuayta** (Profesor Adjunto Principal)
* **MSc. Percy Wilianson Lovón Ramos** (Profesor Adjunto Auxiliar 1)
* **Dr. Emanuel Ortiz Ortiz** (Profesor Adjunto Auxiliar 2)

---

## Descripción del Proyecto
El jugador controla a **Amaru**, un explorador comprometido con el rescate y preservación de la fauna amazónica frente a cazadores clandestinos y trampas en la selva. El proyecto implementa los pilares de la POO, modularidad y patrones de diseño.

### Características Técnicas
* **Arquitectura:** Game Loop a 60 FPS desacoplado (`GamePanel`), renderizado con Swing y Java2D. 32 clases organizadas en paquetes (`core`, `modelo`, `niveles`, `niveles.estado`, `patrones`, `recursos`, `servicios`, `ui`).
* **Estilo visual (pixel art):** los sprites PNG están en `src/main/resources/sprites` y los carga la clase `Sprites` (caché, recorte de cuadros de animación y dibujado ×2). El mundo se dibuja en un buffer de 480×270 que se amplía al doble sin suavizado; el HUD y los menús se dibujan encima a resolución completa. Si falta un PNG, cada clase usa su dibujo de respaldo con formas de Java2D.
* **Tipografía:** fuente pixel *Press Start 2P* (licencia OFL, en `src/main/resources/fonts`), cargada por `Fuente`; los menús, controles, pausa y pantallas finales los dibuja `Pantallas`.
* **Música:** `Musica` reproduce en bucle una pista por pantalla (`src/main/resources/sonidos`: `pantalla_principal.wav`, `level1.wav`, `level2.wav`, `level3.wav`, `jefe.wav`) con `javax.sound.sampled`; volumen de 0 a 5 (por defecto 3) y opción de silenciar; si no hay audio, el juego sigue en silencio.
* **Niveles:** tres escenarios de 4800, 5550 y 5400 px de largo (≈50 % más largos que en el parcial), con fondos de tres capas y parallax. Con colinas, islas, pilares y fosos para que no se repita el mismo tipo de plataforma. Todas las plataformas son alcanzables, las trampas se pueden esquivar y el animal está al final del recorrido (verificado con una búsqueda automática de caminos sobre la física del juego).
* **Pilares de la POO:** abstracción (`Personaje`, `AnimalAtrapado`, `Enemigo`, `Obstaculo`), herencia, polimorfismo (`update()`, `dibujar()`, `liberar()`, `atacar()`, `afectar()`), encapsulamiento, y las relaciones de composición, agregación, asociación y dependencia.
* **Patrones de diseño:**
  * **Factory** — `FabricaEnemigos` crea los enemigos (`Cazador`, `Serpiente`) a partir de un tipo, sin que el nivel conozca la clase concreta.
  * **Observer** — `EstadisticasSubject` notifica el tiempo y la vida del jugador a los `Observador` registrados; el `HUD` es uno de ellos.
  * **State** — `Nivel` delega su comportamiento en un `EstadoNivel` (`Explorando` y `Huyendo`).
* **API externa:** `APIClient` envía el tiempo total de la partida (POST JSON a JSONPlaceholder) en un hilo secundario, con timeouts de 3 s y modo offline.
* **Reglas del juego:** el tiempo total de la partida se muestra en el HUD; tras liberar a los animales hay una fase de huida contra reloj hasta la salida.

---

## Controles
* **Moverse:** Flechas direccionales izquierda / derecha (o `A` / `D`)
* **Saltar:** Flecha arriba / Barra espaciadora / `W`
* **Liberar al animal atrapado:** mantener `E`
* **Lanzar una piedra (Distractor):** Tecla `F`
* **Pausa:** `P` / `Esc`
* **Música:** `N` silencia o reactiva; `-` y `+` bajan o suben el volumen

---

## Editar los niveles
Los niveles son archivos de texto: `src/main/resources/niveles/nivel1.txt`, `nivel2.txt` y `nivel3.txt` (una línea = un elemento: `piso`, `bloque`, `isla`, `tronco`, `fragil`, `trampa`, `piedra`, `animal`, `cazador`, `serpiente`…). Las palabras y las reglas para que el nivel se pueda pasar están en `src/main/resources/niveles/LEEME.txt`.

```bash
# Ir directo a un nivel y a una posición, sin pasar por el menú
java -cp target/classes com.upch.rescateselva.Main --nivel 2 --x 3000

# Revisar que todas las plataformas se alcancen y que haya un camino sin daño
herramientas\validar-niveles.bat      # Windows
./herramientas/validar-niveles.sh     # Linux / Mac
```

---

## Compilación y Ejecución
Requiere **Java 17 (o superior)** y **Apache Maven**:

```bash
# Compilar el proyecto
mvn compile

# Ejecutar el videojuego
java -cp target/classes com.upch.rescateselva.Main
