package com.upch.rescateselva.recursos;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

public final class Sprites {
    public static final int ESCALA = 2;

    private static final Map<String, BufferedImage> CACHE = new HashMap<>();

    private Sprites() {
    }

    public static synchronized BufferedImage cargar(String nombre) {
        if (CACHE.containsKey(nombre)) {
            return CACHE.get(nombre);
        }
        BufferedImage img = null;
        URL url = Sprites.class.getResource("/sprites/" + nombre);
        if (url != null) {
            try {
                img = ImageIO.read(url);
            } catch (IOException e) {
                img = null;
            }
        }
        CACHE.put(nombre, img);
        return img;
    }

    public static BufferedImage[] cargarCuadros(String nombre, int cuadros) {
        BufferedImage hoja = cargar(nombre);
        if (hoja == null || cuadros < 1) {
            return null;
        }
        int w = hoja.getWidth() / cuadros;
        BufferedImage[] res = new BufferedImage[cuadros];
        for (int i = 0; i < cuadros; i++) {
            res[i] = hoja.getSubimage(i * w, 0, w, hoja.getHeight());
        }
        return res;
    }

    public static void dibujar(Graphics2D g, BufferedImage img, int x, int y) {
        g.drawImage(img, x, y, img.getWidth() * ESCALA, img.getHeight() * ESCALA, null);
    }

    public static void dibujar(Graphics2D g, BufferedImage img, int x, int y, int escala) {
        g.drawImage(img, x, y, img.getWidth() * escala, img.getHeight() * escala, null);
    }

    public static void mosaico(Graphics2D g, BufferedImage img, int x, int y, int w, int h) {
        int tw = img.getWidth() * ESCALA;
        int th = img.getHeight() * ESCALA;
        for (int dy = 0; dy < h; dy += th) {
            int alto = Math.min(th, h - dy);
            for (int dx = 0; dx < w; dx += tw) {
                int ancho = Math.min(tw, w - dx);
                g.drawImage(img, x + dx, y + dy, x + dx + ancho, y + dy + alto,
                        0, 0, ancho / ESCALA, alto / ESCALA, null);
            }
        }
    }
}
