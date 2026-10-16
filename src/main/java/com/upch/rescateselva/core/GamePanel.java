package com.upch.rescateselva.core;

import com.upch.rescateselva.modelo.enemigos.Serpiente;
import com.upch.rescateselva.modelo.personajes.AnimalAtrapado;
import com.upch.rescateselva.modelo.personajes.Jugador;
import com.upch.rescateselva.niveles.DisenoNiveles;
import com.upch.rescateselva.niveles.Nivel;
import com.upch.rescateselva.ui.HUD;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JPanel;

/**
 * El corazón del juego: contiene el bucle principal (Runnable) que actualiza y
 * dibuja a 60 FPS, maneja las pantallas (menú, historia, juego, pausa, final) y
 * pasa de un nivel a otro. Es la clase que integra el trabajo de todos los módulos.
 *
 * Hereda de JPanel (para poder dibujar) e implementa Runnable (para correr en un
 * hilo propio): en Java una clase solo puede extender UNA clase, pero puede
 * implementar varias interfaces.
 */
public class GamePanel extends JPanel implements Runnable {

    private static final long serialVersionUID = 1L;

    public static final int ANCHO = Nivel.ANCHO_PANTALLA;
    public static final int ALTO = Nivel.ALTO_PANTALLA;
    private static final int FPS = Jugador.FPS;
    private static final int INTENTOS_INICIALES = 3;

    private enum Pantalla { MENU, CONTROLES, HISTORIA, JUGANDO, PAUSA, NIVEL_COMPLETO, GAME_OVER, VICTORIA }

    private static final String[] OPCIONES_MENU = {"JUGAR", "CONTROLES", "SALIR"};
    private static final String[] HISTORIA = {
        "Amaru, guardaparques de la Amazonía peruana, recorre la reserva cuando encuentra "
            + "jaulas, trampas y huellas de botas por todas partes.",
        "Son Los Extractores, una banda de tráfico de fauna. Han atrapado a los animales "
            + "del bosque y planean llevárselos antes del amanecer.",
        "Libera al Tucán, al Mono Bebé y al Jaguar Cachorro. Cada rescate despierta a los "
            + "cazadores... y al final, su líder te espera en el campamento: una gran serpiente."
    };

    private Thread hiloJuego;
    private volatile boolean corriendo;
    private final Object lock = new Object();

    private final Entrada entrada = new Entrada();
    private final HUD hud = new HUD();
    private Jugador jugador;
    private Nivel nivelActual;
    private List<Nivel> niveles = new ArrayList<>();
    private int indiceNivel;
    private boolean pausado;

    private Pantalla pantalla = Pantalla.MENU;
    private int opcionMenu;
    private int paginaHistoria;
    private int vidasRestantes = INTENTOS_INICIALES;
    private final List<String> aliadosTipos = new ArrayList<>();
    private int contadorNivelCompleto;
    private final List<String> lineasNivelCompleto = new ArrayList<>();
    private int ticks;
    private String tituloMostrado = "";
    private final Nivel fondoMenu = new Nivel(1, null, 3200, "");

    public GamePanel() {
        setPreferredSize(new Dimension(ANCHO, ALTO));
        setFocusable(true);
        setDoubleBuffered(true);
        addKeyListener(entrada);
        fondoMenu.setTema(1);
    }

    // ------------------------------------------------------------------
    //  Bucle principal
    // ------------------------------------------------------------------

    /** Arranca el hilo del juego (lo llama VentanaJuego). */
    public void iniciarJuego() {
        if (hiloJuego != null) {
            return;
        }
        corriendo = true;
        hiloJuego = new Thread(this, "bucle-juego");
        hiloJuego.setDaemon(true);
        hiloJuego.start();
    }

    @Override
    public void run() {
        final double nsPorFrame = 1_000_000_000.0 / FPS;
        long ultimo = System.nanoTime();
        double acumulado = 0;
        while (corriendo) {
            long ahora = System.nanoTime();
            acumulado += (ahora - ultimo) / nsPorFrame;
            ultimo = ahora;
            int guardia = 0;
            while (acumulado >= 1 && guardia++ < 5) {
                try {
                    actualizar();
                } catch (RuntimeException ex) { // un error en un frame no debe congelar el juego
                    ex.printStackTrace();
                }
                acumulado -= 1;
            }
            if (guardia >= 5) {
                acumulado = 0; // si el equipo se atrasó mucho, no intentamos "recuperar"
            }
            repaint();
            try {
                Thread.sleep(3);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    // ------------------------------------------------------------------
    //  Lógica por frame
    // ------------------------------------------------------------------

    private void actualizar() {
        synchronized (lock) {
            ticks++;
            switch (pantalla) {
                case MENU:
                    actualizarMenu();
                    break;
                case CONTROLES:
                    if (entrada.consumir(KeyEvent.VK_ENTER) || entrada.consumir(KeyEvent.VK_ESCAPE)) {
                        pantalla = Pantalla.MENU;
                    }
                    break;
                case HISTORIA:
                    if (entrada.consumir(KeyEvent.VK_ENTER) || entrada.consumir(KeyEvent.VK_SPACE)) {
                        paginaHistoria++;
                        if (paginaHistoria >= HISTORIA.length) {
                            comenzarPartida();
                        }
                    }
                    break;
                case JUGANDO:
                    actualizarJuego();
                    break;
                case PAUSA:
                    if (entrada.consumir(KeyEvent.VK_P) || entrada.consumir(KeyEvent.VK_ESCAPE)) {
                        pausado = false;
                        pantalla = Pantalla.JUGANDO;
                    } else if (entrada.consumir(KeyEvent.VK_M)) {
                        pausado = false;
                        pantalla = Pantalla.MENU;
                    }
                    break;
                case NIVEL_COMPLETO:
                    if (--contadorNivelCompleto <= 0) {
                        if (indiceNivel >= 2) {
                            terminarPartida(Pantalla.VICTORIA);
                        } else {
                            siguienteNivel();
                        }
                    }
                    break;
                case GAME_OVER:
                case VICTORIA:
                    if (entrada.consumir(KeyEvent.VK_ENTER)) {
                        pantalla = Pantalla.MENU;
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void actualizarMenu() {
        if (entrada.consumir(KeyEvent.VK_UP) || entrada.consumir(KeyEvent.VK_W)) {
            opcionMenu = (opcionMenu + OPCIONES_MENU.length - 1) % OPCIONES_MENU.length;
        }
        if (entrada.consumir(KeyEvent.VK_DOWN) || entrada.consumir(KeyEvent.VK_S)) {
            opcionMenu = (opcionMenu + 1) % OPCIONES_MENU.length;
        }
        if (entrada.consumir(KeyEvent.VK_ENTER) || entrada.consumir(KeyEvent.VK_SPACE)) {
            switch (opcionMenu) {
                case 0:
                    paginaHistoria = 0;
                    pantalla = Pantalla.HISTORIA;
                    break;
                case 1:
                    pantalla = Pantalla.CONTROLES;
                    break;
                default:
                    System.exit(0);
                    break;
            }
        }
    }

    private void actualizarJuego() {
        if (entrada.consumir(KeyEvent.VK_P) || entrada.consumir(KeyEvent.VK_ESCAPE)) {
            pausado = true;
            pantalla = Pantalla.PAUSA;
            return;
        }
        int dx = 0;
        if (entrada.izquierda()) {
            dx -= 1;
        }
        if (entrada.derecha()) {
            dx += 1;
        }
        jugador.mover(dx, entrada.salto() ? -1 : 0);

        if (entrada.accion()) {
            for (AnimalAtrapado a : nivelActual.getAnimales()) {
                jugador.rescatar(a);
            }
        }
        if (entrada.consumir(KeyEvent.VK_F)) {
            jugador.lanzarDistractor();
        }

        jugador.update();
        nivelActual.actualizar();

        String mensaje = nivelActual.consumirMensaje();
        if (mensaje != null) {
            hud.mostrarMensaje(mensaje, 200);
        }
        hud.avanzar();
        if (!nivelActual.getTitulo().equals(tituloMostrado)) { // p. ej. al entrar al campamento
            tituloMostrado = nivelActual.getTitulo();
            hud.setTitulo(tituloMostrado);
        }
        hud.setObjetivo(nivelActual.getObjetivo());
        hud.setEstadoJugador(jugador.getSegundosJugados(), jugador.getVida());
        hud.setPiedras(jugador.getCantidadDistractores());
        hud.setIntentos(vidasRestantes);
        hud.setCronometro(nivelActual.getSegundosRestantes(), jugador.estaAgotado());

        if (jugador.getVida() <= 0) {
            perderIntento();
        } else if (nivelActual.estaCompleto()) {
            completarNivel();
        }
    }

    // ------------------------------------------------------------------
    //  Flujo de la partida
    // ------------------------------------------------------------------

    private void comenzarPartida() {
        jugador = new Jugador(100, Nivel.SUELO_Y - 44);
        vidasRestantes = INTENTOS_INICIALES;
        aliadosTipos.clear();
        niveles = new ArrayList<>(Collections.nCopies(3, (Nivel) null));
        indiceNivel = 0;
        cargarNivel(0, false);
        pantalla = Pantalla.JUGANDO;
        entrada.limpiar();
    }

    private void cargarNivel(int indice, boolean desdeCampamento) {
        indiceNivel = indice;
        Nivel nuevo = DisenoNiveles.construir(indice + 1, jugador, aliadosTipos);
        niveles.set(indice, nuevo);
        nivelActual = nuevo;
        if (desdeCampamento) {
            nivelActual.saltarACampamento();
        }
        tituloMostrado = nivelActual.getTitulo();
        hud.setTitulo(tituloMostrado);
        hud.setObjetivo(nivelActual.getObjetivo());
        hud.setIntentos(vidasRestantes);
        entrada.limpiar();
    }

    /** Pasa al nivel siguiente (el nivel anterior queda guardado en la lista). */
    public void siguienteNivel() {
        if (indiceNivel + 1 >= niveles.size()) {
            terminarPartida(Pantalla.VICTORIA);
            return;
        }
        cargarNivel(indiceNivel + 1, false);
        pantalla = Pantalla.JUGANDO;
    }

    /** Cierra la partida (victoria o game over). */
    private void terminarPartida(Pantalla pantallaFinal) {
        pantalla = pantallaFinal;
        entrada.limpiar();
    }

    private void perderIntento() {
        vidasRestantes--;
        if (vidasRestantes <= 0) {
            terminarPartida(Pantalla.GAME_OVER);
            return;
        }
        boolean enCampamento = nivelActual.estaEnCampamento();
        cargarNivel(indiceNivel, enCampamento);
        hud.mostrarMensaje(jugador.isAtrapado() ? "¡Te atraparon! Intenta de nuevo" : "Perdiste un intento. ¡Otra vez!", 150);
    }

    private void completarNivel() {
        lineasNivelCompleto.clear();
        int numero = nivelActual.getNumero();
        if (numero == 1) {
            aliadosTipos.add("tucan");
            lineasNivelCompleto.add("El Tucán se une a tu equipo");
        } else if (numero == 2) {
            aliadosTipos.add("mono");
            lineasNivelCompleto.add("El Mono Bebé se une a tu equipo");
        } else {
            lineasNivelCompleto.add("Los tres animales están a salvo");
        }
        contadorNivelCompleto = 190;
        pantalla = Pantalla.NIVEL_COMPLETO;
        entrada.limpiar();
    }

    // ------------------------------------------------------------------
    //  Dibujo
    // ------------------------------------------------------------------

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        synchronized (lock) {
            switch (pantalla) {
                case MENU:
                    dibujarMenu(g2);
                    break;
                case CONTROLES:
                    dibujarControles(g2);
                    break;
                case HISTORIA:
                    dibujarHistoria(g2);
                    break;
                case JUGANDO:
                    dibujarJuego(g2);
                    break;
                case PAUSA:
                    dibujarJuego(g2);
                    velo(g2, "PAUSA", "[P] continuar     [M] menú principal");
                    break;
                case NIVEL_COMPLETO:
                    dibujarJuego(g2);
                    dibujarNivelCompleto(g2);
                    break;
                case GAME_OVER:
                    dibujarFinal(g2, false);
                    break;
                case VICTORIA:
                    dibujarFinal(g2, true);
                    break;
                default:
                    break;
            }
        }
        g2.dispose();
    }

    private int calcularCamara() {
        int centro = jugador.getX() + jugador.getAncho() / 2;
        int cam = centro - ANCHO / 2;
        return Math.max(0, Math.min(cam, nivelActual.getAnchoMundo() - ANCHO));
    }

    private void dibujarJuego(Graphics2D g) {
        if (nivelActual == null || jugador == null) {
            return;
        }
        int cam = calcularCamara();
        nivelActual.dibujarFondo(g, cam);
        g.translate(-cam, 0);
        nivelActual.dibujarMundo(g);
        jugador.dibujar(g);
        nivelActual.dibujarFrente(g);
        g.translate(cam, 0);

        hud.dibujar(g, ANCHO, ALTO);
        dibujarBarraJefe(g);
    }

    /** La barra de vida de la Serpiente se lee directo de jefe.getVida(). */
    private void dibujarBarraJefe(Graphics2D g) {
        Serpiente jefe = nivelActual.getJefe();
        if (jefe == null || !nivelActual.estaEnCampamento()) {
            return;
        }
        int w = 440;
        int x = ANCHO / 2 - w / 2;
        int y = ALTO - 46;
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRoundRect(x - 10, y - 24, w + 20, 46, 14, 14);
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.setColor(new Color(0xffd27a));
        g.drawString("LA SERPIENTE", x, y - 8);
        g.setColor(new Color(70, 20, 30));
        g.fillRoundRect(x, y, w, 14, 8, 8);
        g.setColor(new Color(0xc73a5a));
        g.fillRoundRect(x, y, (int) (w * Math.max(0, jefe.getVida()) / (double) jefe.getVidaMaxima()), 14, 8, 8);
        g.setColor(Color.WHITE);
        g.drawRoundRect(x, y, w, 14, 8, 8);
    }

    private void dibujarMenu(Graphics2D g) {
        fondoMenu.dibujarFondo(g, ticks / 2);
        g.setColor(new Color(0, 0, 0, 110));
        g.fillRect(0, 0, ANCHO, ALTO);
        texto(g, "RESCATE EN LA SELVA", ANCHO / 2, 150, 54, new Color(0xffe9a8));
        texto(g, "Un juego de rescate y escape", ANCHO / 2, 190, 20, new Color(0xdfeede));
        for (int i = 0; i < OPCIONES_MENU.length; i++) {
            boolean sel = i == opcionMenu;
            texto(g, (sel ? "▶  " : "") + OPCIONES_MENU[i], ANCHO / 2, 280 + i * 52, sel ? 32 : 26,
                    sel ? Color.WHITE : new Color(0xb9cdb9));
        }
        texto(g, "↑ ↓ para elegir · Enter para aceptar", ANCHO / 2, ALTO - 30, 15, new Color(0xcfdccf));
    }

    private void dibujarControles(Graphics2D g) {
        fondoMenu.dibujarFondo(g, ticks / 2);
        g.setColor(new Color(0, 0, 0, 160));
        g.fillRect(0, 0, ANCHO, ALTO);
        texto(g, "CONTROLES", ANCHO / 2, 100, 40, new Color(0xffe9a8));
        String[][] filas = {
            {"← →  /  A D", "Moverse"},
            {"↑  /  W  /  Espacio", "Saltar"},
            {"E (mantener)", "Liberar al animal atrapado"},
            {"F", "Lanzar una piedra"},
            {"P  /  Esc", "Pausa"}
        };
        for (int i = 0; i < filas.length; i++) {
            g.setFont(new Font("SansSerif", Font.BOLD, 22));
            g.setColor(new Color(0xffe27a));
            g.drawString(filas[i][0], 220, 190 + i * 50);
            g.setColor(Color.WHITE);
            g.drawString(filas[i][1], 520, 190 + i * 50);
        }
        texto(g, "Las piedras se recogen solas al pasar encima.", ANCHO / 2, 450, 17, new Color(0xcfdccf));
        texto(g, "Enter para volver", ANCHO / 2, ALTO - 30, 16, new Color(0xcfdccf));
    }

    private void dibujarHistoria(Graphics2D g) {
        fondoMenu.dibujarFondo(g, ticks / 3);
        g.setColor(new Color(0, 0, 0, 170));
        g.fillRect(0, 0, ANCHO, ALTO);
        g.setFont(new Font("Serif", Font.PLAIN, 26));
        g.setColor(Color.WHITE);
        parrafo(g, HISTORIA[Math.min(paginaHistoria, HISTORIA.length - 1)], 120, 190, ANCHO - 240, 40);
        texto(g, "Enter para continuar  (" + (paginaHistoria + 1) + "/" + HISTORIA.length + ")",
                ANCHO / 2, ALTO - 40, 16, new Color(0xcfdccf));
    }

    private void dibujarNivelCompleto(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, ALTO / 2 - 100, ANCHO, 200);
        boolean ultimo = nivelActual.getNumero() >= 3;
        texto(g, ultimo ? "¡LA SERPIENTE HUYE!" : "¡NIVEL COMPLETADO!", ANCHO / 2, ALTO / 2 - 40, 42, new Color(0xffe9a8));
        int y = ALTO / 2 + 5;
        for (String linea : lineasNivelCompleto) {
            texto(g, linea, ANCHO / 2, y, 20, Color.WHITE);
            y += 28;
        }
        texto(g, "Tiempo: " + formatoTiempo(jugador.getSegundosJugados()), ANCHO / 2, y + 6, 22, new Color(0xb9f0cc));
    }

    private void dibujarFinal(Graphics2D g, boolean victoria) {
        Color arriba = victoria ? new Color(0x2a6b4a) : new Color(0x2b1a1a);
        Color abajo = victoria ? new Color(0xe8d48a) : new Color(0x6a2a2a);
        g.setPaint(new GradientPaint(0, 0, arriba, 0, ALTO, abajo));
        g.fillRect(0, 0, ANCHO, ALTO);
        if (victoria) {
            texto(g, "¡VICTORIA!", ANCHO / 2, 150, 60, Color.WHITE);
            g.setFont(new Font("Serif", Font.PLAIN, 24));
            g.setColor(Color.WHITE);
            parrafo(g, "La Serpiente huye asustada y Los Extractores pierden su guarida. "
                    + "Tucán, Mono Bebé y Jaguar Cachorro vuelven a casa gracias a Amaru.", 140, 220, ANCHO - 280, 36);
        } else {
            texto(g, "GAME OVER", ANCHO / 2, 170, 60, new Color(0xffd0c0));
            texto(g, "Los animales siguen necesitando ayuda...", ANCHO / 2, 230, 24, Color.WHITE);
        }
        int segundos = jugador == null ? 0 : jugador.getSegundosJugados();
        texto(g, "Tiempo total: " + formatoTiempo(segundos), ANCHO / 2, 380, 30, new Color(0xfff2b0));
        texto(g, "Enter para volver al menú", ANCHO / 2, ALTO - 40, 18, new Color(0xf3f3f3));
    }

    private static String formatoTiempo(int segundos) {
        return String.format("%02d:%02d", segundos / 60, segundos % 60);
    }

    private void velo(Graphics2D g, String titulo, String subtitulo) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, ANCHO, ALTO);
        texto(g, titulo, ANCHO / 2, ALTO / 2 - 10, 56, Color.WHITE);
        texto(g, subtitulo, ANCHO / 2, ALTO / 2 + 36, 20, new Color(0xdfeede));
    }

    // ---- utilidades de texto ----

    private void texto(Graphics2D g, String s, int cx, int baseY, int tamano, Color color) {
        g.setFont(new Font("SansSerif", Font.BOLD, tamano));
        FontMetrics fm = g.getFontMetrics();
        int x = cx - fm.stringWidth(s) / 2;
        g.setColor(new Color(0, 0, 0, 170));
        g.drawString(s, x + 2, baseY + 2);
        g.setColor(color);
        g.drawString(s, x, baseY);
        g.setStroke(new BasicStroke(1f));
    }

    /** Escribe un párrafo partiéndolo en líneas según el ancho disponible. */
    private void parrafo(Graphics2D g, String texto, int x, int y, int ancho, int interlineado) {
        FontMetrics fm = g.getFontMetrics();
        StringBuilder linea = new StringBuilder();
        int yy = y;
        for (String palabra : texto.split(" ")) {
            String prueba = linea.length() == 0 ? palabra : linea + " " + palabra;
            if (fm.stringWidth(prueba) > ancho) {
                g.drawString(linea.toString(), x, yy);
                yy += interlineado;
                linea = new StringBuilder(palabra);
            } else {
                linea = new StringBuilder(prueba);
            }
        }
        g.drawString(linea.toString(), x, yy);
    }
}
