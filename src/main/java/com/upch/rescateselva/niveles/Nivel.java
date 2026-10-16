package com.upch.rescateselva.niveles;

import com.upch.rescateselva.modelo.enemigos.Cazador;
import com.upch.rescateselva.modelo.enemigos.Enemigo;
import com.upch.rescateselva.modelo.enemigos.Serpiente;
import com.upch.rescateselva.modelo.obstaculos.Distractor;
import com.upch.rescateselva.modelo.obstaculos.Obstaculo;
import com.upch.rescateselva.modelo.personajes.AnimalAtrapado;
import com.upch.rescateselva.modelo.personajes.Jugador;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Un nivel del juego. Reúne (AGREGACIÓN) sus enemigos, animales y obstáculos, que le
 * entrega DisenoNiveles ya creados. Pasa por dos fases: ida (explorar hasta liberar al
 * animal) y huida (volver a la salida).
 *
 * Qué hace en cada frame (actualizar): mueve plataformas, resuelve las colisiones
 * del jugador, actualiza animales/enemigos/obstáculos/distractores y finalmente
 * cambia de fase (ida → huida) cuando corresponde.
 */
public class Nivel {

    public static final int ANCHO_PANTALLA = 960;
    public static final int ALTO_PANTALLA = 540;
    public static final int SUELO_Y = 460;
    private static final int PASO = 24; // desnivel que el jugador sube sin saltar

    private final int numero;
    private final String titulo;
    private final Jugador jugador;
    private int anchoMundo;
    private int salidaX = 40;
    private int tema;

    private final List<Enemigo> enemigos = new ArrayList<>();
    private final List<AnimalAtrapado> animales = new ArrayList<>();
    private final List<Obstaculo> obstaculos = new ArrayList<>();
    private final List<Plataforma> plataformas = new ArrayList<>();
    private final List<Distractor> distractores = new ArrayList<>();
    private final List<Integer> puntosDistractores = new ArrayList<>();
    private List<String> aliadosTipos = new ArrayList<>();

    private boolean huyendo;        // false = fase de ida, true = fase de huida
    private boolean huidaIniciada;
    private int tick;
    private boolean cronometroActivo;
    private int cronometroFrames;
    private boolean campamento;
    private Serpiente jefe;
    private String mensaje;

    public Nivel(int numero, Jugador jugador, int anchoMundo, String titulo) {
        this.numero = numero;
        this.jugador = jugador;
        this.anchoMundo = anchoMundo;
        this.titulo = titulo;
    }

    // ------------------------------------------------------------------
    //  Construcción (la usa DisenoNiveles)
    // ------------------------------------------------------------------

    public void agregarPlataforma(Plataforma p) {
        plataformas.add(p);
    }

    public void agregarObstaculo(Obstaculo o) {
        obstaculos.add(o);
    }

    public void agregarAnimal(AnimalAtrapado a) {
        a.setSeguirA(jugador);
        animales.add(a);
    }

    public void agregarEnemigo(Enemigo e) {
        e.setObjetivo(jugador);
        if (e instanceof Serpiente && ((Serpiente) e).isJefe()) {
            jefe = (Serpiente) e;
        }
        enemigos.add(e);
    }

    public void agregarPuntoDistractor(int x) {
        puntosDistractores.add(x);
    }

    public void limpiarContenido() {
        enemigos.clear();
        animales.clear();
        obstaculos.clear();
        plataformas.clear();
        distractores.clear();
        puntosDistractores.clear();
        jefe = null;
    }

    public void setAnchoMundo(int ancho) {
        this.anchoMundo = ancho;
    }

    public void setSalidaX(int x) {
        this.salidaX = x;
    }

    public void setTema(int tema) {
        this.tema = tema;
    }

    public void setAliadosTipos(List<String> tipos) {
        this.aliadosTipos = new ArrayList<>(tipos);
    }

    // ------------------------------------------------------------------
    //  Lógica por frame
    // ------------------------------------------------------------------

    /** Delega update()/afectar() a cada personaje y obstáculo y luego a la fase actual. */
    public void actualizar() {
        tick++;
        for (Plataforma p : plataformas) {
            p.update();
        }
        if (jugador.getY() > ALTO_PANTALLA + 120) {
            jugador.caer();
        }
        resolverJugador();
        limitarJugador();

        for (AnimalAtrapado a : animales) {
            a.update();
        }
        for (Enemigo e : enemigos) {
            int xAntes = e.getX();
            e.update();
            if (!ajustarASuelo(e)) {
                e.setPosicion(xAntes, e.getY()); // no camina hacia el vacío
            }
            distractores.addAll(e.retirarLanzados());
            if (e.isActivo() && e.getBounds().intersects(jugador.getBounds())) {
                e.atacar(jugador);
            }
        }
        enemigos.removeIf(e -> !e.isActivo());

        for (Obstaculo o : obstaculos) {
            o.update();
            o.afectar(jugador);
        }
        distractores.addAll(jugador.retirarLanzados());
        actualizarDistractores();

        if (!huyendo) {
            // Fase de ida: en cuanto todos los animales quedan libres, empieza la huida.
            if (todosLiberados()) {
                anunciarLiberacion();
                huyendo = true;
            }
        } else {
            // Fase de huida / campamento.
            if (!huidaIniciada) {
                huidaIniciada = true;
                iniciarHuida();
            }
            avanzarCronometro();
        }
    }

    private void resolverJugador() {
        int prevPies = jugador.getPrevY() + jugador.getAlto();
        for (Plataforma p : plataformas) {
            if (!p.isActiva()) {
                continue;
            }
            Rectangle jb = jugador.getBounds();
            if (!jb.intersects(p.getBounds())) {
                continue;
            }
            int top = p.getTop();
            int bottom = top + p.getAlto();
            if (jugador.getVy() >= 0 && prevPies <= top + PASO) {
                jugador.aterrizar(top);
                p.pisar();
                if (p.getTipo() == Plataforma.Tipo.FIRME) {
                    jugador.marcarSeguro();
                }
            } else if (jugador.getVy() < 0 && jugador.getPrevY() >= bottom - 2) {
                jugador.golpearTecho(bottom);
            } else if (jugador.getPrevX() + jugador.getAncho() <= p.getX() + 4) {
                jugador.empujarX(p.getX() - jugador.getAncho());
            } else if (jugador.getPrevX() >= p.getX() + p.getAncho() - 4) {
                jugador.empujarX(p.getX() + p.getAncho());
            } else {
                jugador.aterrizar(top); // atascado dentro: lo sacamos hacia arriba
            }
        }
    }

    private void limitarJugador() {
        if (jugador.getX() < 0) {
            jugador.empujarX(0);
        } else if (jugador.getX() + jugador.getAncho() > anchoMundo) {
            jugador.empujarX(anchoMundo - jugador.getAncho());
        }
    }

    /**
     * Los enemigos "caminan" sobre la plataforma más alta que tengan a su alcance
     * y saltan huecos de hasta ~45 px a cada lado (así cruzan de tronco en tronco).
     * Devuelve false si no hay nada donde pararse (no debe avanzar hacia el vacío).
     */
    private boolean ajustarASuelo(Enemigo e) {
        if (e instanceof Serpiente && ((Serpiente) e).estaAsustada()) {
            return true;
        }
        final int salto = 45;
        int pies = e.getY() + e.getAlto();
        int cx = e.getX() + e.getAncho() / 2;
        Plataforma mejor = null;
        for (Plataforma p : plataformas) {
            if (!p.isActiva() || cx < p.getX() - salto || cx > p.getX() + p.getAncho() + salto) {
                continue;
            }
            int top = p.getTop();
            if (top >= pies - 70 && (mejor == null || top < mejor.getTop())) {
                mejor = p;
            }
        }
        if (mejor == null) {
            return false;
        }
        e.setPosicion(e.getX(), mejor.getTop() - e.getAlto());
        return true;
    }

    private void actualizarDistractores() {
        Iterator<Distractor> it = distractores.iterator();
        while (it.hasNext()) {
            Distractor d = it.next();
            if (d.isRecogido()) {
                it.remove();
                continue;
            }
            d.update();
            Rectangle db = d.getBounds();
            if (!d.isEnSuelo()) {
                for (Plataforma p : plataformas) {
                    if (p.isActiva() && d.getVy() > 0 && db.intersects(p.getBounds())
                            && d.getYAnteriorPies() <= p.getTop() + 6) {
                        d.aterrizarEn(p.getTop());
                        break;
                    }
                }
            }
            if (!d.isEnSuelo()) {
                if (d.isLanzadoPorJugador()) {
                    for (Enemigo e : enemigos) {
                        boolean intocable = e instanceof Serpiente && ((Serpiente) e).estaAsustada();
                        if (e.isActivo() && !intocable && db.intersects(e.getBounds())) {
                            d.aplicarEfecto(e);
                            d.recoger(); // se "gasta" al impactar
                            break;
                        }
                    }
                } else if (db.intersects(jugador.getBounds())) {
                    jugador.recibirDanio(d.getDanioAJugador());
                    d.recoger();
                }
            } else if (db.intersects(jugador.getBounds()) && jugador.agregarDistractor(d)) {
                d.recoger(); // recogida automática, sin tecla
            }
            if (d.getY() > 1000 || d.getX() < -200 || d.getX() > anchoMundo + 200) {
                d.recoger();
            }
        }
    }

    // ------------------------------------------------------------------
    //  Fases del nivel
    // ------------------------------------------------------------------

    public boolean todosLiberados() {
        if (animales.isEmpty()) {
            return false;
        }
        for (AnimalAtrapado a : animales) {
            if (!a.isLiberado()) {
                return false;
            }
        }
        return true;
    }

    public void anunciarLiberacion() {
        String nombre = animales.get(0).getNombre();
        mensaje = numero < 3
                ? "¡" + nombre + " liberado! ¡Corre de vuelta a la salida!"
                : "¡" + nombre + " liberado! ¡Entra al campamento!";
    }

    /** Se llama una vez al empezar la fase de huida. */
    public void iniciarHuida() {
        switch (numero) {
            case 1:
                cronometroActivo = true;
                cronometroFrames = 60 * 60;
                soltarDistractores();
                mensaje = "¡Regresa! Recoge piedras y lánzalas con [F]";
                break;
            case 2:
                cronometroActivo = true;
                cronometroFrames = 35 * 60;
                for (Enemigo e : enemigos) {
                    if (e instanceof Cazador) {
                        ((Cazador) e).activarPersecucion();
                    }
                }
                soltarDistractores();
                mensaje = "¡El Cazador te persigue! ¡Corre!";
                break;
            default:
                DisenoNiveles.prepararCampamento(this);
                break;
        }
    }

    public void soltarDistractores() {
        for (int x : puntosDistractores) {
            distractores.add(Distractor.enElSuelo(x, SUELO_Y));
        }
    }

    /** Se acaba el cronómetro => el jugador se agota (casi no puede moverse). */
    public void avanzarCronometro() {
        if (!cronometroActivo) {
            return;
        }
        if (jugador.estaAgotado()) {
            cronometroActivo = false;
            return;
        }
        cronometroFrames--;
        if (cronometroFrames <= 0) {
            jugador.activarCansancio();
            cronometroActivo = false;
            mensaje = "¡Se acabó el tiempo! Amaru está agotado... ¡llega a la salida!";
        }
    }

    /** Usado al reintentar el nivel 3 tras morir en la pelea final. */
    public void saltarACampamento() {
        for (AnimalAtrapado a : animales) {
            a.marcarLiberado();
        }
        huyendo = true;
        huidaIniciada = false;
    }

    public void entrarEnCampamento() {
        this.campamento = true;
    }

    // ------------------------------------------------------------------
    //  Consultas
    // ------------------------------------------------------------------

    /**
     * Niveles 1 y 2: completado al volver a la salida con el animal liberado.
     * Nivel 3: completado cuando la Serpiente ya huyó de la pantalla.
     */
    public boolean estaCompleto() {
        if (numero < 3) {
            return huyendo && jugador.getVida() > 0
                    && jugador.getX() <= salidaX + 80;
        }
        return campamento && jefe != null && jefe.huidaCompleta();
    }

    public String getObjetivo() {
        if (campamento) {
            return jefe != null && jefe.estaAsustada() ? "¡La Serpiente huye!" : "Espanta a la Serpiente";
        }
        if (huyendo) {
            return "¡Regresa a la salida!";
        }
        return animales.isEmpty() ? "" : "Rescata: " + animales.get(0).getNombre();
    }

    /** Segundos que quedan en el cronómetro, o -1 si no hay cronómetro activo. */
    public int getSegundosRestantes() {
        return cronometroActivo ? (int) Math.ceil(cronometroFrames / 60.0) : -1;
    }

    public String consumirMensaje() {
        String m = mensaje;
        mensaje = null;
        return m;
    }

    public void setMensaje(String m) {
        this.mensaje = m;
    }

    public int getNumero() {
        return numero;
    }

    public String getTitulo() {
        return campamento ? "Nivel 3 — El Campamento" : titulo;
    }

    public int getAnchoMundo() {
        return anchoMundo;
    }

    public Jugador getJugador() {
        return jugador;
    }

    public List<Enemigo> getEnemigos() {
        return enemigos;
    }

    public List<AnimalAtrapado> getAnimales() {
        return animales;
    }

    public List<Obstaculo> getObstaculos() {
        return obstaculos;
    }

    public List<Plataforma> getPlataformas() {
        return plataformas;
    }

    public List<Distractor> getDistractores() {
        return distractores;
    }

    public int getSalidaX() {
        return salidaX;
    }

    public List<String> getAliadosTipos() {
        return aliadosTipos;
    }

    public Serpiente getJefe() {
        return jefe;
    }

    public boolean estaEnCampamento() {
        return campamento;
    }

    // ------------------------------------------------------------------
    //  Dibujo
    // ------------------------------------------------------------------

    /** Fondo con parallax (se dibuja sin la cámara). */
    public void dibujarFondo(Graphics2D g, int camX) {
        Color[] c = paleta();
        g.setPaint(new GradientPaint(0, 0, c[0], 0, ALTO_PANTALLA, c[1]));
        g.fillRect(0, 0, ANCHO_PANTALLA, ALTO_PANTALLA);

        // sol o luna
        g.setColor(c[5]);
        g.fillOval(700, 50, 80, 80);
        g.setColor(new Color(c[5].getRed(), c[5].getGreen(), c[5].getBlue(), 50));
        g.fillOval(680, 30, 120, 120);

        // capa lejana: colinas (o ruinas)
        int lejos = (int) (camX * 0.12);
        int ancho = 280;
        for (int i = -1; i < ANCHO_PANTALLA / ancho + 3; i++) {
            int idx = i + lejos / ancho;
            int bx = i * ancho - (lejos % ancho);
            int h = 150 + (Math.abs(idx * 37) % 60);
            g.setColor(c[2]);
            if (tema == 3 || tema == 4) {
                int colH = 120 + (Math.abs(idx * 53) % 120);
                g.fillRect(bx + 60, 460 - colH, 46, colH);
                g.fillRect(bx + 50, 460 - colH - 8, 66, 10);
                if (idx % 2 == 0) {
                    g.fillRect(bx + 150, 460 - colH / 2, 40, colH / 2);
                }
            } else {
                g.fillOval(bx - 40, 470 - h, ancho + 90, h * 2);
            }
        }

        // capa media: árboles
        int medio = (int) (camX * 0.35);
        int sep = 170;
        for (int i = -1; i < ANCHO_PANTALLA / sep + 3; i++) {
            int idx = i + medio / sep;
            int bx = i * sep - (medio % sep) + (Math.abs(idx * 29) % 50);
            int altoTronco = 110 + (Math.abs(idx * 41) % 70);
            g.setColor(c[4]);
            g.fillRect(bx, 470 - altoTronco, 14, altoTronco);
            g.setColor(c[3]);
            g.fillOval(bx - 38, 470 - altoTronco - 46, 90, 70);
            g.fillOval(bx - 14, 470 - altoTronco - 70, 70, 60);
        }

        if (tema == 2) { // neblina del río
            g.setPaint(new GradientPaint(0, 300, new Color(255, 235, 190, 0), 0, 470, new Color(255, 235, 190, 110)));
            g.fillRect(0, 300, ANCHO_PANTALLA, 200);
        } else if (tema == 4) { // fogata y tiendas
            int fx = 480 - (int) (camX * 0.5) % 1400 + 700;
            for (int k = -1; k <= 1; k++) {
                int tx = ((fx + k * 1400) % 2800);
                g.setColor(new Color(255, 140, 40, 40));
                g.fillOval(tx - 120, 340, 240, 200);
            }
        }
    }

    /** Todo lo que está dentro del mundo (el llamador ya aplicó la cámara). */
    public void dibujarMundo(Graphics2D g) {
        for (Plataforma p : plataformas) {
            p.dibujar(g);
        }
        for (Obstaculo o : obstaculos) {
            if (!o.esFrontal()) {
                o.dibujar(g);
            }
        }
        dibujarSalida(g);
        for (AnimalAtrapado a : animales) {
            a.dibujar(g);
        }
        for (Enemigo e : enemigos) {
            e.dibujar(g);
        }
        for (Distractor d : distractores) {
            d.dibujar(g);
        }
    }

    /** Lo que va por delante del jugador (el agua). */
    public void dibujarFrente(Graphics2D g) {
        for (Obstaculo o : obstaculos) {
            if (o.esFrontal()) {
                o.dibujar(g);
            }
        }
    }

    private void dibujarSalida(Graphics2D g) {
        if (campamento || salidaX < 0) {
            return;
        }
        boolean huyendo = this.huyendo;
        int px = salidaX;
        g.setColor(new Color(0x5a3f24));
        g.fillRect(px, SUELO_Y - 90, 8, 90);
        Color bandera = huyendo ? new Color(0x2fb36b) : new Color(0x8a8a82);
        g.setColor(bandera);
        int ondula = (int) (3 * Math.sin(tick * 0.1));
        g.fillRect(px + 8, SUELO_Y - 88, 54, 28 + ondula);
        g.setColor(new Color(0, 0, 0, 120));
        g.drawRect(px + 8, SUELO_Y - 88, 54, 28 + ondula);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.drawString("SALIDA", px + 12, SUELO_Y - 69);
        if (huyendo) {
            int a = 90 + (int) (80 * Math.sin(tick * 0.15));
            g.setColor(new Color(120, 255, 170, Math.max(10, a)));
            g.fillOval(px - 30, SUELO_Y - 120, 120, 120);
        }
    }

    /** {cieloArriba, cieloAbajo, colinas, copas, troncos, sol} según el tema. */
    private Color[] paleta() {
        switch (tema) {
            case 2:
                return new Color[] {new Color(0xe0a15f), new Color(0xf6dca6), new Color(0x8aa07a),
                    new Color(0x3f6f4d), new Color(0x4a3524), new Color(0xfff0c0)};
            case 3:
                return new Color[] {new Color(0x35335a), new Color(0x8c7088), new Color(0x57536f),
                    new Color(0x2b4a3c), new Color(0x2e2a36), new Color(0xe8e0ff)};
            case 4:
                return new Color[] {new Color(0x24151f), new Color(0x8a3d2b), new Color(0x4a2a30),
                    new Color(0x1f2f28), new Color(0x2a1d1a), new Color(0xffb870)};
            default:
                return new Color[] {new Color(0x76c6e8), new Color(0xdcf3cf), new Color(0x93cba3),
                    new Color(0x2e7d4f), new Color(0x6b4a2b), new Color(0xfff6b0)};
        }
    }
}
