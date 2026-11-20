package com.upch.rescateselva.ui;

import com.upch.rescateselva.patrones.observer.Observador;
import com.upch.rescateselva.recursos.Fuente;
import com.upch.rescateselva.recursos.Sprites;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

// PATRÓN OBSERVER: observador concreto
public class HUD extends JPanel implements Observador {
    private static final long serialVersionUID = 1L;

    private int tiempoTotal;
    private int vida = 100;
    private int intentos = 3;
    private int piedras;
    private int segundos = -1;
    private boolean agotado;
    private String titulo = "";
    private String objetivo = "";
    private String mensaje;
    private int mensajeFrames;
    private int bannerFrames;
    private int tick;

    private static final Color CLARO = new Color(0xf4ead0);
    private static final Color AMARILLO = new Color(0xffd96b);

    public HUD() {
        setOpaque(false);
    }

    @Override
    public void actualizar(int segundos, int vida) {
        this.tiempoTotal = segundos;
        this.vida = vida;
    }


    public void setIntentos(int intentos) {
        this.intentos = intentos;
    }

    public void setPiedras(int piedras) {
        this.piedras = piedras;
    }

    public void setCronometro(int segundos, boolean agotado) {
        this.segundos = segundos;
        this.agotado = agotado;
    }

    public void setObjetivo(String objetivo) {
        this.objetivo = objetivo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
        this.bannerFrames = 170;
    }

    public void mostrarMensaje(String texto, int frames) {
        this.mensaje = texto;
        this.mensajeFrames = frames;
    }

    public void avanzar() {
        tick++;
        if (mensajeFrames > 0) {
            mensajeFrames--;
        }
        if (bannerFrames > 0) {
            bannerFrames--;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        dibujar((Graphics2D) g, getWidth(), getHeight());
    }

    public void dibujar(Graphics2D g, int w, int h) {
        Fuente.nitido(g);
        Pantallas.caja(g, 12, 12, 290, 100, false);
        Fuente.texto(g, String.format("TIEMPO %02d:%02d", tiempoTotal / 60, tiempoTotal % 60), 28, 44, 16, CLARO);

        int bx = 28;
        int by = 56;
        int bw = 170;
        g.setColor(new Color(0x2a1414));
        g.fillRect(bx - 2, by - 2, bw + 4, 20);
        Color colorVida = vida > 50 ? new Color(0x4ec27a) : (vida > 25 ? new Color(0xe5b93c) : new Color(0xe5513c));
        g.setColor(colorVida);
        g.fillRect(bx, by, (int) (bw * Math.max(0, vida) / 100.0), 16);
        Fuente.texto(g, String.valueOf(Math.max(0, vida)), bx + bw + 12, by + 15, 16, CLARO);

        BufferedImage lleno = Sprites.cargar("corazon.png");
        BufferedImage vacio = Sprites.cargar("corazon_vacio.png");
        for (int i = 0; i < 3; i++) {
            BufferedImage c = i < intentos ? lleno : vacio;
            if (c != null) {
                Sprites.dibujar(g, c, 28 + i * 28, 82);
            }
        }
        BufferedImage roca = Sprites.cargar("piedra.png");
        if (roca != null) {
            Sprites.dibujar(g, roca, 130, 86);
        }
        Fuente.texto(g, "x" + piedras + " [F]", 158, 98, 16, CLARO);

        String linea = "Objetivo: " + objetivo;
        int ow = Fuente.ancho(g, linea, 16) + 32;
        Pantallas.caja(g, w - ow - 12, 12, ow, 40, false);
        Fuente.texto(g, linea, w - ow + 4, 40, 16, AMARILLO);

        if (agotado) {
            if ((tick / 20) % 2 == 0) {
                Fuente.centrado(g, "¡AGOTADO!", w / 2 + 10, 100, 32, new Color(0xff6a5a));
            }
        } else if (segundos >= 0) {
            Color c = segundos <= 10 ? new Color(0xff6a5a) : Color.WHITE;
            Fuente.centrado(g, String.format("HUIDA %02d:%02d", segundos / 60, segundos % 60), w / 2 + 10, 100, 32, c);
        }

        if (bannerFrames > 0) {
            int alfa = Math.min(255, bannerFrames * 4);
            g.setColor(new Color(0, 0, 0, Math.min(170, alfa)));
            g.fillRect(0, h / 2 - 90, w, 130);
            Fuente.centrado(g, titulo, w / 2, h / 2 - 30, 24, new Color(244, 234, 208, alfa));
            Fuente.centrado(g, objetivo, w / 2, h / 2 + 10, 16, new Color(255, 217, 107, alfa));
        }

        if (mensajeFrames > 0 && mensaje != null) {
            java.util.List<String> lineas = Fuente.lineas(g, mensaje, 780, 16);
            int alto = 28 + lineas.size() * 28;
            int ancho = 0;
            for (String l : lineas) {
                ancho = Math.max(ancho, Fuente.ancho(g, l, 16));
            }
            Pantallas.caja(g, w / 2 - ancho / 2 - 24, 126, ancho + 48, alto, true);
            for (int i = 0; i < lineas.size(); i++) {
                Fuente.centrado(g, lineas.get(i), w / 2, 126 + 36 + i * 28, 16, CLARO);
            }
        }
    }
}
