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
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class DisenoNiveles {
    private static final FabricaEnemigos FABRICA = new FabricaEnemigos(); // FACTORY: el nivel es cliente
    private static final int S = Nivel.SUELO_Y;
    private static final int FONDO_RIO = 520;

    private DisenoNiveles() {
    }

    public static Nivel construir(int numero, Jugador jugador, List<String> aliadosTipos) {
        Nivel n = leerNivel(Math.max(1, Math.min(3, numero)), jugador);
        n.setAliadosTipos(aliadosTipos);
        jugador.reiniciarParaNivel(100, S - jugador.getAlto());
        return n;
    }

    private static List<String> leerLineas(int numero) {
        String archivo = "nivel" + numero + ".txt";
        List<String> lineas = new ArrayList<>();
        try {
            Path externo = Path.of("niveles", archivo);
            if (!Files.exists(externo)) {
                externo = Path.of("src", "main", "resources", "niveles", archivo);
            }
            if (Files.exists(externo)) {
                lineas = Files.readAllLines(externo, StandardCharsets.UTF_8);
            } else {
                try (InputStream in = DisenoNiveles.class.getResourceAsStream("/niveles/" + archivo)) {
                    if (in == null) {
                        throw new IllegalStateException("No se encontró el archivo de nivel " + archivo);
                    }
                    lineas = List.of(new String(in.readAllBytes(), StandardCharsets.UTF_8).split("\\R"));
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer " + archivo, e);
        }
        return lineas;
    }

    private static Nivel leerNivel(int numero, Jugador jugador) {
        List<String[]> filas = new ArrayList<>();
        String nombre = "Nivel " + numero;
        int ancho = 4800;
        int tema = numero;
        for (String linea : leerLineas(numero)) {
            int comentario = linea.indexOf('#');
            String limpia = (comentario >= 0 ? linea.substring(0, comentario) : linea).trim();
            if (limpia.isEmpty()) {
                continue;
            }
            String[] p = limpia.split("\\s+", 2);
            if (p[0].equals("nombre")) {
                nombre = p[1];
            } else if (p[0].equals("ancho")) {
                ancho = Integer.parseInt(p[1].trim());
            } else if (p[0].equals("tema")) {
                tema = Integer.parseInt(p[1].trim());
            } else {
                filas.add(limpia.split("\\s+"));
            }
        }
        Nivel n = new Nivel(numero, jugador, ancho, nombre);
        n.setTema(tema);
        for (String[] f : filas) {
            try {
                agregar(n, f);
            } catch (RuntimeException e) {
                throw new IllegalArgumentException("Nivel " + numero + ": línea incorrecta «" + String.join(" ", f) + "»", e);
            }
        }
        return n;
    }

    private static int num(String[] f, int i, int porDefecto) {
        return f.length > i ? Integer.parseInt(f[i]) : porDefecto;
    }

    private static void agregar(Nivel n, String[] f) {
        switch (f[0]) {
            case "piso":
                n.agregarPlataforma(new Plataforma(num(f, 1, 0), S, num(f, 2, 0), 100, Tipo.FIRME));
                break;
            case "bloque":
                int top = num(f, 2, S);
                n.agregarPlataforma(new Plataforma(num(f, 1, 0), top, num(f, 3, 0), num(f, 4, S) - top, Tipo.FIRME));
                break;
            case "isla":
                n.agregarPlataforma(new Plataforma(num(f, 1, 0), num(f, 2, S), num(f, 3, 0), 24, Tipo.FIRME));
                break;
            case "tronco":
                n.agregarPlataforma(new Plataforma(num(f, 1, 0), num(f, 2, S), num(f, 3, 0), 24, Tipo.TRONCO));
                break;
            case "fragil":
                n.agregarPlataforma(new Plataforma(num(f, 1, 0), num(f, 2, S), num(f, 3, 0), 24, Tipo.FRAGIL));
                break;
            case "lecho":
                n.agregarPlataforma(new Plataforma(num(f, 1, 0), FONDO_RIO, num(f, 2, 0), 40, Tipo.LECHO));
                break;
            case "rio":
                n.agregarObstaculo(new Rio(num(f, 1, 0), 456, num(f, 2, 0), 104, 1));
                break;
            case "trampa":
                n.agregarObstaculo(new Trampa(num(f, 1, 0), num(f, 2, S) - 24));
                break;
            case "piedra":
                n.agregarPuntoDistractor(num(f, 1, 0), num(f, 2, S));
                break;
            case "animal":
                n.agregarAnimal(crearAnimal(f[1], num(f, 2, 0)));
                break;
            case "cazador":
                n.agregarEnemigo(crearCazador(f));
                break;
            case "serpiente":
                Serpiente s = FABRICA.crearSerpiente();
                s.setPosicion(num(f, 1, 0), S - s.getAlto());
                s.configurarPatrulla(num(f, 2, 0), num(f, 3, 0));
                n.agregarEnemigo(s);
                break;
            default:
                throw new IllegalArgumentException("palabra desconocida: " + f[0]);
        }
    }

    private static AnimalAtrapado crearAnimal(String tipo, int x) {
        switch (tipo) {
            case "tucan":
                return new Tucan(x, S - 30);
            case "mono":
                return new MonoBebe(x, S - 34);
            case "jaguar":
                return new JaguarCachorro(x, S - 32);
            default:
                throw new IllegalArgumentException("animal desconocido: " + tipo);
        }
    }

    private static Cazador crearCazador(String[] f) {
        Cazador c = FABRICA.crearCazador();
        c.setPosicion(num(f, 1, 0), S - c.getAlto());
        c.configurarPatrulla(num(f, 2, 0), num(f, 3, 0));
        List<String> opciones = List.of(f).subList(Math.min(4, f.length), f.length);
        c.setLanzaPiedras(opciones.contains("piedras"));
        c.setPersigueAlDetectar(opciones.contains("persigue"));
        return c;
    }


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

        Serpiente jefe = FABRICA.crearSerpiente();
        jefe.convertirEnJefe();
        jefe.setPosicion(1450, S - jefe.getAlto());
        jefe.configurarPatrulla(1000, 1500);
        n.agregarJefe(jefe);

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
        j.agregarDistractor(new Distractor(0, 0));
        j.agregarDistractor(new Distractor(0, 0));
        n.entrarEnCampamento();
        n.setMensaje("¡Campamento de Los Extractores! ¡Espanta a la Serpiente!");
    }
}
