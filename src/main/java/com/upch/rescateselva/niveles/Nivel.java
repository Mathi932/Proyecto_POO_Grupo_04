package com.upch.rescateselva.niveles;

import com.upch.rescateselva.modelo.enemigos.Enemigo;
import com.upch.rescateselva.modelo.enemigos.Serpiente;
import com.upch.rescateselva.modelo.obstaculos.Distractor;
import com.upch.rescateselva.modelo.obstaculos.Obstaculo;
import com.upch.rescateselva.modelo.personajes.AnimalAtrapado;
import com.upch.rescateselva.modelo.personajes.Jugador;
import com.upch.rescateselva.niveles.estado.EstadoNivel;
import com.upch.rescateselva.niveles.estado.Explorando;
import com.upch.rescateselva.niveles.estado.Huyendo;
import com.upch.rescateselva.recursos.Fuente;
import com.upch.rescateselva.recursos.Sprites;
import com.upch.rescateselva.ui.Pantallas;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Nivel {
    public static final int ANCHO_PANTALLA = 960;
    public static final int ALTO_PANTALLA = 540;
    public static final int SUELO_Y = 460;
    private static final int PASO = 24;

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
    private final List<int[]> puntosDistractores = new ArrayList<>();
    private List<String> aliadosTipos = new ArrayList<>();

    private EstadoNivel estado; // STATE: contexto
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
        this.estado = new Explorando();
    }



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
        enemigos.add(e);
    }

    public void agregarJefe(Serpiente s) {
        jefe = s;
        agregarEnemigo(s);
    }

    public void agregarPuntoDistractor(int x) {
        puntosDistractores.add(new int[] {x, SUELO_Y});
    }

    public void agregarPuntoDistractor(int x, int yPies) {
        puntosDistractores.add(new int[] {x, yPies});
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
                e.setPosicion(xAntes, e.getY());
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

        if (estado != null) {
            estado.manejar(this); // STATE: delega en la fase actual
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
                jugador.aterrizar(top);
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

    private boolean ajustarASuelo(Enemigo e) {
        if (e.esIntocable()) {
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
                        boolean intocable = e.esIntocable();
                        if (e.isActivo() && !intocable && db.intersects(e.getBounds())) {
                            d.aplicarEfecto(e);
                            d.recoger();
                            break;
                        }
                    }
                } else if (db.intersects(jugador.getBounds())) {
                    jugador.recibirDanio(d.getDanioAJugador());
                    d.recoger();
                }
            } else if (db.intersects(jugador.getBounds()) && jugador.agregarDistractor(d)) {
                d.recoger();
            }
            if (d.getY() > 1000 || d.getX() < -200 || d.getX() > anchoMundo + 200) {
                d.recoger();
            }
        }
    }



    public void cambiarEstado(EstadoNivel e) {
        this.estado = e;
    }

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

    public void iniciarHuida() {
        switch (numero) {
            case 1:
                cronometroActivo = true;
                cronometroFrames = 80 * 60;
                soltarDistractores();
                mensaje = "¡Regresa! Recoge piedras y lánzalas con [F]";
                break;
            case 2:
                cronometroActivo = true;
                cronometroFrames = 50 * 60;
                for (Enemigo e : enemigos) {
                    e.activarPersecucion();
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
        for (int[] punto : puntosDistractores) {
            distractores.add(Distractor.enElSuelo(punto[0], punto[1]));
        }
    }

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

    public void saltarACampamento() {
        for (AnimalAtrapado a : animales) {
            a.marcarLiberado();
        }
        cambiarEstado(new Huyendo());
    }

    public void entrarEnCampamento() {
        this.campamento = true;
    }



    public boolean estaCompleto() {
        if (numero < 3) {
            return estado.enHuida() && jugador.getVida() > 0
                    && jugador.getX() <= salidaX + 80;
        }
        return campamento && jefe != null && jefe.huidaCompleta();
    }

    public String getObjetivo() {
        if (campamento) {
            return jefe != null && jefe.estaAsustada() ? "¡La Serpiente huye!" : "Espanta a la Serpiente";
        }
        if (estado.enHuida()) {
            return "¡Regresa a la salida!";
        }
        return animales.isEmpty() ? "" : "Rescata: " + animales.get(0).getNombre();
    }

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

    public EstadoNivel getEstado() {
        return estado;
    }



    public void dibujarFondo(Graphics2D g, int camX) {
        BufferedImage cielo = Sprites.cargar("fondo" + tema + "_cielo.png");
        if (cielo == null) {
            Color[] c = paleta();
            g.setPaint(new GradientPaint(0, 0, c[0], 0, ALTO_PANTALLA, c[1]));
            g.fillRect(0, 0, ANCHO_PANTALLA, ALTO_PANTALLA);
            return;
        }
        g.drawImage(cielo, 0, 0, ANCHO_PANTALLA, ALTO_PANTALLA, null);
        capaParalaje(g, Sprites.cargar("fondo" + tema + "_lejos.png"), camX, 0.12);
        capaParalaje(g, Sprites.cargar("fondo" + tema + "_medio.png"), camX, 0.35);
    }

    private void capaParalaje(Graphics2D g, BufferedImage capa, int camX, double factor) {
        if (capa == null) {
            return;
        }
        int periodo = capa.getWidth() * Sprites.ESCALA;
        int desplazo = ((int) (camX * factor) / Sprites.ESCALA) * Sprites.ESCALA % periodo;
        for (int bx = -desplazo; bx < ANCHO_PANTALLA; bx += periodo) {
            g.drawImage(capa, bx, 0, periodo, capa.getHeight() * Sprites.ESCALA, null);
        }
    }

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

    public void dibujarFrente(Graphics2D g) {
        for (Obstaculo o : obstaculos) {
            if (o.esFrontal()) {
                o.dibujar(g);
            }
        }
    }

    public void dibujarIndicadores(Graphics2D g, int camX) {
        for (AnimalAtrapado a : animales) {
            if (a.isLiberado()) {
                continue;
            }
            int cx = a.getX() + a.getAncho() / 2 - camX;
            int arriba = a.getY() + a.getAlto() - 74 - 56;
            if (a.getFraccionProgreso() > 0) {
                int w = 130;
                int bx = Math.max(24, Math.min(ANCHO_PANTALLA - 24 - w, cx - w / 2));
                Pantallas.caja(g, bx - 10, arriba + 8, w + 20, 34, true);
                g.setColor(new Color(0x2a1418));
                g.fillRect(bx, arriba + 17, w, 16);
                g.setColor(new Color(0x74d1a0));
                g.fillRect(bx, arriba + 17, (int) (w * a.getFraccionProgreso()), 16);
            } else if (a.pideAccion()) {
                String txt = "Mantén [E] para " + a.getTipoTrampa();
                int tw = Fuente.ancho(g, txt, 16);
                int bx = Math.max(8 + tw / 2 + 16, Math.min(ANCHO_PANTALLA - 8 - tw / 2 - 16, cx));
                Pantallas.caja(g, bx - tw / 2 - 16, arriba, tw + 32, 42, true);
                Fuente.centrado(g, txt, bx, arriba + 28, 16, new Color(0xf4ead0));
            }
        }
    }

    private void dibujarSalida(Graphics2D g) {
        if (campamento || salidaX < 0) {
            return;
        }
        boolean huyendo = estado.enHuida();
        BufferedImage bandera = Sprites.cargar(huyendo ? "salida_verde.png" : "salida.png");
        if (huyendo) {
            int a = 90 + (int) (80 * Math.sin(tick * 0.15));
            g.setColor(new Color(120, 255, 170, Math.max(10, a)));
            g.fillOval(salidaX - 40, SUELO_Y - 100, 120, 120);
        }
        if (bandera != null) {
            Sprites.dibujar(g, bandera, salidaX, SUELO_Y - bandera.getHeight() * Sprites.ESCALA);
        } else {
            g.setColor(huyendo ? new Color(0x2fb36b) : new Color(0x8a8a82));
            g.fillRect(salidaX, SUELO_Y - 90, 8, 90);
        }
    }

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
