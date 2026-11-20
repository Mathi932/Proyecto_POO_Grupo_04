package com.upch.rescateselva.core;

import com.upch.rescateselva.servicios.APIClient;
import com.upch.rescateselva.modelo.enemigos.Serpiente;
import com.upch.rescateselva.modelo.personajes.AnimalAtrapado;
import com.upch.rescateselva.modelo.personajes.Jugador;
import com.upch.rescateselva.niveles.DisenoNiveles;
import com.upch.rescateselva.niveles.Nivel;
import com.upch.rescateselva.recursos.Fuente;
import com.upch.rescateselva.recursos.Musica;
import com.upch.rescateselva.ui.HUD;
import com.upch.rescateselva.ui.Pantallas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JPanel;

public class GamePanel extends JPanel implements Runnable {
    private static final long serialVersionUID = 1L;

    public static final int ANCHO = Nivel.ANCHO_PANTALLA;
    public static final int ALTO = Nivel.ALTO_PANTALLA;
    private static final int FPS = Jugador.FPS;
    private static final int INTENTOS_INICIALES = 3;

    private enum Pantalla { MENU, CONTROLES, HISTORIA, JUGANDO, PAUSA, NIVEL_COMPLETO, GAME_OVER, VICTORIA }

    private static final String[] OPCIONES_MENU = {"JUGAR", "CONTROLES", "SALIR"};
    private static final String[] HISTORIA = {
        "Amaru, guardaparques de la Amazonía peruana, encuentra jaulas, trampas y huellas de botas en la reserva.",
        "Son Los Extractores, una banda de tráfico de fauna. Quieren llevarse a los animales antes del amanecer.",
        "Libera al Tucán, al Mono Bebé y al Jaguar Cachorro. Al final te espera el líder: una gran serpiente."
    };

    private Thread hiloJuego;
    private volatile boolean corriendo;
    private final Object lock = new Object();
    private final BufferedImage bufferMundo = new BufferedImage(ANCHO / 2, ALTO / 2, BufferedImage.TYPE_INT_RGB);

    private final Entrada entrada = new Entrada();
    private final HUD hud = new HUD();
    private Jugador jugador;
    private Nivel nivelActual;
    private List<Nivel> niveles = new ArrayList<>();
    private int indiceNivel;

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
                } catch (RuntimeException ex) {
                    ex.printStackTrace();
                }
                acumulado -= 1;
            }
            if (guardia >= 5) {
                acumulado = 0;
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



    private void actualizar() {
        synchronized (lock) {
            ticks++;
            ajustarMusica();
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
                        pantalla = Pantalla.JUGANDO;
                    } else if (entrada.consumir(KeyEvent.VK_M)) {
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
            Musica.reproducir(pistaActual());
        }
    }

    private void ajustarMusica() {
        boolean cambio = false;
        if (entrada.consumir(KeyEvent.VK_N)) {
            Musica.alternarSilencio();
            cambio = true;
        }
        if (entrada.consumir(KeyEvent.VK_MINUS) || entrada.consumir(KeyEvent.VK_SUBTRACT)) {
            Musica.bajarVolumen();
            cambio = true;
        }
        if (entrada.consumir(KeyEvent.VK_PLUS) || entrada.consumir(KeyEvent.VK_ADD)
                || entrada.consumir(KeyEvent.VK_EQUALS)) {
            Musica.subirVolumen();
            cambio = true;
        }
        if (cambio && pantalla == Pantalla.JUGANDO) {
            hud.mostrarMensaje(Musica.descripcion(), 90);
        }
    }

    private Musica.Pista pistaActual() {
        switch (pantalla) {
            case JUGANDO:
            case PAUSA:
            case NIVEL_COMPLETO:
                if (nivelActual == null) {
                    return Musica.Pista.PRINCIPAL;
                }
                if (nivelActual.estaEnCampamento()) {
                    return Musica.Pista.JEFE;
                }
                return Musica.Pista.values()[1 + Math.min(2, indiceNivel)];
            default:
                return Musica.Pista.PRINCIPAL;
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
        if (!nivelActual.getTitulo().equals(tituloMostrado)) {
            tituloMostrado = nivelActual.getTitulo();
            hud.setTitulo(tituloMostrado);
        }
        hud.setObjetivo(nivelActual.getObjetivo());
        hud.setPiedras(jugador.getCantidadDistractores());
        hud.setIntentos(vidasRestantes);
        hud.setCronometro(nivelActual.getSegundosRestantes(), jugador.estaAgotado());

        if (jugador.getVida() <= 0) {
            perderIntento();
        } else if (nivelActual.estaCompleto()) {
            completarNivel();
        }
    }



    public void iniciarEnNivel(int numero, int x) {
        synchronized (lock) {
            comenzarPartida();
            int indice = Math.max(0, Math.min(2, numero - 1));
            if (indice >= 1) {
                aliadosTipos.add("tucan");
            }
            if (indice >= 2) {
                aliadosTipos.add("mono");
            }
            cargarNivel(indice, false);
            jugador.teletransportar(x, Nivel.SUELO_Y - jugador.getAlto());
        }
    }

    private void comenzarPartida() {
        jugador = new Jugador(100, Nivel.SUELO_Y - 44);
        jugador.getStats().agregarObservador(hud); // OBSERVER: registro del HUD
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

    public void siguienteNivel() {
        if (indiceNivel + 1 >= niveles.size()) {
            terminarPartida(Pantalla.VICTORIA);
            return;
        }
        cargarNivel(indiceNivel + 1, false);
        pantalla = Pantalla.JUGANDO;
    }

    private void terminarPartida(Pantalla pantallaFinal) {
        pantalla = pantallaFinal;
        entrada.limpiar();
        APIClient.enviarTiempo("Amaru", jugador.getSegundosJugados());
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



    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        Fuente.nitido(g2);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        synchronized (lock) {
            switch (pantalla) {
                case MENU:
                    fondoMenu.dibujarFondo(g2, ticks / 2);
                    Pantallas.menu(g2, opcionMenu, OPCIONES_MENU);
                    break;
                case CONTROLES:
                    fondoMenu.dibujarFondo(g2, ticks / 2);
                    Pantallas.controles(g2);
                    break;
                case HISTORIA:
                    fondoMenu.dibujarFondo(g2, ticks / 3);
                    Pantallas.historia(g2, HISTORIA[Math.min(paginaHistoria, HISTORIA.length - 1)],
                            paginaHistoria, HISTORIA.length);
                    break;
                case JUGANDO:
                    dibujarJuego(g2);
                    break;
                case PAUSA:
                    dibujarJuego(g2);
                    Pantallas.pausa(g2, Musica.descripcion());
                    break;
                case NIVEL_COMPLETO:
                    dibujarJuego(g2);
                    Pantallas.nivelCompleto(g2, nivelActual.getNumero() >= 3, lineasNivelCompleto,
                            formatoTiempo(jugador.getSegundosJugados()));
                    break;
                case GAME_OVER:
                    Pantallas.finPartida(g2, false, formatoTiempo(segundosTotales()));
                    break;
                case VICTORIA:
                    Pantallas.finPartida(g2, true, formatoTiempo(segundosTotales()));
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
        int cam = (calcularCamara() / 2) * 2;
        Graphics2D b = bufferMundo.createGraphics();
        b.scale(0.5, 0.5);
        nivelActual.dibujarFondo(b, cam);
        b.translate(-cam, 0);
        nivelActual.dibujarMundo(b);
        jugador.dibujar(b);
        nivelActual.dibujarFrente(b);
        b.dispose();
        g.drawImage(bufferMundo, 0, 0, ANCHO, ALTO, null);
        nivelActual.dibujarIndicadores(g, cam);

        hud.dibujar(g, ANCHO, ALTO);
        dibujarBarraJefe(g);
    }

    private void dibujarBarraJefe(Graphics2D g) {
        Serpiente jefe = nivelActual.getJefe();
        if (jefe == null || !nivelActual.estaEnCampamento()) {
            return;
        }
        int w = 460;
        int x = ANCHO / 2 - w / 2;
        int y0 = ALTO - 76;
        Pantallas.caja(g, x - 16, y0, w + 32, 68, false);
        Fuente.texto(g, "LA SERPIENTE", x, y0 + 28, 16, new Color(0xffd96b));
        g.setColor(new Color(0x2a1418));
        g.fillRect(x, y0 + 38, w, 18);
        g.setColor(new Color(0xc73a5a));
        g.fillRect(x, y0 + 38, (int) (w * Math.max(0, jefe.getVida()) / (double) jefe.getVidaMaxima()), 18);
    }

    private int segundosTotales() {
        return jugador == null ? 0 : jugador.getSegundosJugados();
    }

    private static String formatoTiempo(int segundos) {
        return String.format("%02d:%02d", segundos / 60, segundos % 60);
    }
}
