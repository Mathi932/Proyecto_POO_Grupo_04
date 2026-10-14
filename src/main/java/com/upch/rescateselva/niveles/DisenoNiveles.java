package com.upch.rescateselva.niveles;

import com.upch.rescateselva.modelo.enemigos.Cazador;
import com.upch.rescateselva.modelo.enemigos.Serpiente;
import com.upch.rescateselva.modelo.obstaculos.Distractor;
import com.upch.rescateselva.modelo.obstaculos.Rio;
import com.upch.rescateselva.modelo.obstaculos.Trampa;
import com.upch.rescateselva.modelo.personajes.AnimalAtrapado;
import com.upch.rescateselva.modelo.personajes.JaguarCachorro;
import com.upch.rescateselva.modelo.personajes.Jugador;
import com.upch.rescateselva.modelo.personajes.MonoBebe;
import com.upch.rescateselva.modelo.personajes.Tucan;
import com.upch.rescateselva.niveles.Plataforma.Tipo;
import com.upch.rescateselva.patrones.factory.FabricaEnemigos;
import java.util.ArrayList;
import java.util.List;

/**
 * Aquí está el "mapa" de los tres niveles (dónde va cada plataforma, trampa,
 * animal y enemigo). Separarlo de Nivel permite ajustar el diseño sin tocar la
 * lógica del nivel. Los enemigos se piden a FabricaEnemigos (Factory Method).
 *
 * Referencias de física (para diseñar): el jugador sube hasta 120 px de alto
 * saltando y salta unos 120 px de distancia; sube sin saltar escalones de 24 px.
 */
public final class DisenoNiveles {

    private static final FabricaEnemigos FABRICA = new FabricaEnemigos();
    private static final int S = Nivel.SUELO_Y;

    private DisenoNiveles() {
    }

    /**
     * @param numero         1, 2 o 3
     * @param jugador        el jugador (se deja listo en la posición inicial)
     * @param aliadosTipos   animales ya rescatados en niveles previos ("tucan", "mono")
     */
    public static Nivel construir(int numero, Jugador jugador, List<String> aliadosTipos) {
        Nivel n;
        switch (numero) {
            case 1:
                n = nivel1(jugador);
                break;
            case 2:
                n = nivel2(jugador);
                break;
            default:
                n = nivel3(jugador);
                break;
        }
        n.setAliadosTipos(aliadosTipos);
        jugador.reiniciarParaNivel(100, S - jugador.getAlto());
        return n;
    }

    // ------------------------------------------------------------------
    //  NIVEL 1 - El Claro (tutorial: plataformas y trampas, sin enemigos)
    // ------------------------------------------------------------------
    private static Nivel nivel1(Jugador j) {
        Nivel n = new Nivel(1, j, 3200, "Nivel 1 — El Claro");
        n.setTema(1);
        n.agregarPlataforma(new Plataforma(0, S, 3200, 100, Tipo.FIRME));

        int[][] altas = {{420, 380, 120}, {640, 320, 120}, {1000, 370, 140}, {1500, 380, 120},
            {1720, 320, 120}, {2100, 380, 140}, {2500, 360, 120}, {2760, 300, 100}};
        for (int[] p : altas) {
            n.agregarPlataforma(new Plataforma(p[0], p[1], p[2], 24, Tipo.FIRME));
        }
        int[] trampas = {520, 880, 1250, 1620, 2000, 2350, 2650};
        for (int x : trampas) {
            n.agregarObstaculo(new Trampa(x, S - 24));
        }
        n.agregarAnimal(new Tucan(2980, S - 30));
        // piedras que aparecen al empezar la vuelta (para aprender a lanzarlas)
        for (int x : new int[] {2700, 2050, 1400, 800}) {
            n.agregarPuntoDistractor(x);
        }
        return n;
    }

    // ------------------------------------------------------------------
    //  NIVEL 2 - El Río Turbio (troncos que se hunden + Cazador)
    // ------------------------------------------------------------------
    private static Nivel nivel2(Jugador j) {
        Nivel n = new Nivel(2, j, 3700, "Nivel 2 — El Río Turbio");
        n.setTema(2);
        n.agregarPlataforma(new Plataforma(0, S, 700, 100, Tipo.FIRME));
        n.agregarPlataforma(new Plataforma(700, 520, 1500, 40, Tipo.LECHO));
        n.agregarPlataforma(new Plataforma(2200, S, 1500, 100, Tipo.FIRME));

        int[][] troncos = {{760, 440}, {940, 420}, {1120, 400}, {1300, 420}, {1480, 440},
            {1660, 410}, {1840, 430}, {2020, 440}};
        for (int[] t : troncos) {
            n.agregarPlataforma(new Plataforma(t[0], t[1], 110, 24, Tipo.TRONCO));
        }
        n.agregarObstaculo(new Rio(700, 456, 1500, 104, 1));
        for (int x : new int[] {300, 520, 2400, 2750, 3050}) {
            n.agregarObstaculo(new Trampa(x, S - 24));
        }
        n.agregarAnimal(new MonoBebe(3250, S - 34));

        Cazador c = (Cazador) FABRICA.crearEnemigo("cazador");
        c.setPosicion(3450, S - c.getAlto());
        c.configurarPatrulla(3380, 3640);
        c.setLanzaPiedras(true);
        n.agregarEnemigo(c);

        for (int x : new int[] {2450, 2900, 3150, 650, 300}) {
            n.agregarPuntoDistractor(x);
        }
        return n;
    }

    // ------------------------------------------------------------------
    //  NIVEL 3 - Las Ruinas Escondidas (+ Campamento al liberar al Jaguar)
    // ------------------------------------------------------------------
    private static Nivel nivel3(Jugador j) {
        Nivel n = new Nivel(3, j, 3600, "Nivel 3 — Las Ruinas Escondidas");
        n.setTema(3);
        int[][] suelo = {{0, 600}, {850, 500}, {1700, 500}, {2600, 1000}};
        for (int[] s : suelo) {
            n.agregarPlataforma(new Plataforma(s[0], S, s[1], 100, Tipo.FIRME));
        }
        int[][] fragiles = {{640, 440}, {760, 420}, {1380, 440}, {1520, 420}, {1620, 440},
            {2230, 440}, {2370, 420}, {2490, 440}};
        for (int[] f : fragiles) {
            n.agregarPlataforma(new Plataforma(f[0], f[1], 80, 24, Tipo.FRAGIL));
        }
        int[][] altas = {{950, 360, 120}, {1120, 300, 120}, {2760, 360, 140}};
        for (int[] a : altas) {
            n.agregarPlataforma(new Plataforma(a[0], a[1], a[2], 24, Tipo.FIRME));
        }
        for (int x : new int[] {350, 1050, 1900, 2800}) {
            n.agregarObstaculo(new Trampa(x, S - 24));
        }

        Cazador c = (Cazador) FABRICA.crearEnemigo("cazador");
        c.setPosicion(1000, S - c.getAlto());
        c.configurarPatrulla(900, 1300);
        c.setPersigueAlDetectar(true);
        n.agregarEnemigo(c);

        Serpiente s = (Serpiente) FABRICA.crearEnemigo("serpiente");
        s.setPosicion(1800, S - s.getAlto());
        s.configurarPatrulla(1750, 2130);
        n.agregarEnemigo(s);

        n.agregarAnimal(new JaguarCachorro(3430, S - 30));
        return n;
    }

    // ------------------------------------------------------------------
    //  CAMPAMENTO - pasillo nuevo y separado donde se pelea con la Serpiente
    // ------------------------------------------------------------------

    /**
     * Reemplaza todo el contenido del nivel 3 por el corredor del campamento:
     * suelo largo, tres plataformas para esquivar, la Serpiente como jefe y
     * los animales ya rescatados como aliados que ayudan a atacar.
     */
    public static void prepararCampamento(Nivel n) {
        Jugador j = n.getJugador();
        List<AnimalAtrapado> previos = new ArrayList<>(n.getAnimales());

        n.limpiarContenido();
        n.setAnchoMundo(1800);
        n.setTema(4);
        n.setSalidaX(-1);
        n.agregarPlataforma(new Plataforma(0, S, 1800, 100, Tipo.FIRME));
        for (int[] p : new int[][] {{420, 370, 150}, {900, 340, 150}, {1250, 370, 150}}) {
            n.agregarPlataforma(new Plataforma(p[0], p[1], p[2], 24, Tipo.FIRME));
        }

        Serpiente jefe = (Serpiente) FABRICA.crearEnemigo("serpiente");
        jefe.convertirEnJefe();
        jefe.setPosicion(1450, S - jefe.getAlto());
        jefe.configurarPatrulla(1000, 1500);
        n.agregarEnemigo(jefe);

        // el Jaguar recién liberado + los que se rescataron antes
        for (AnimalAtrapado a : previos) {
            a.setPosicion(60, S - a.getAlto());
            a.setObjetivoCombate(jefe);
            n.agregarAnimal(a);
        }
        int pos = 20;
        for (String tipo : n.getAliadosTipos()) {
            AnimalAtrapado aliado = "tucan".equals(tipo) ? new Tucan(pos, S - 60) : new MonoBebe(pos, S - 34);
            aliado.marcarLiberado();
            aliado.setObjetivoCombate(jefe);
            n.agregarAnimal(aliado);
            pos += 20;
        }

        j.teletransportar(100, S - j.getAlto());
        j.agregarDistractor(new Distractor(0, 0)); // municion inicial
        j.agregarDistractor(new Distractor(0, 0));
        n.entrarEnCampamento();
        n.setMensaje("¡Campamento de Los Extractores! ¡Espanta a la Serpiente!");
    }
}
