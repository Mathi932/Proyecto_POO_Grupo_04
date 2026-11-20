package com.upch.rescateselva.recursos;

import java.io.BufferedInputStream;
import java.io.InputStream;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

public final class Musica {
    public enum Pista {
        PRINCIPAL("pantalla_principal"), NIVEL1("level1"), NIVEL2("level2"), NIVEL3("level3"), JEFE("jefe");

        private final String archivo;

        Pista(String archivo) {
            this.archivo = archivo;
        }
    }

    public static final int VOLUMEN_MAX = 5;
    private static final float[] DECIBELES = {-80f, -30f, -22f, -15f, -9f, -4f};

    private static Clip clip;
    private static Pista solicitada;
    private static int volumen = 3;
    private static boolean silenciada;
    private static boolean disponible = true;

    private Musica() {
    }

    public static synchronized void reproducir(Pista pista) {
        if (pista == solicitada || !disponible) {
            return;
        }
        solicitada = pista;
        detener();
        if (pista == null) {
            return;
        }
        Thread carga = new Thread(() -> cargar(pista), "musica-" + pista.archivo);
        carga.setDaemon(true);
        carga.start();
    }

    private static void cargar(Pista pista) {
        Clip nuevo = null;
        try (InputStream raw = Musica.class.getResourceAsStream("/sonidos/" + pista.archivo + ".wav")) {
            if (raw == null) {
                return;
            }
            try (AudioInputStream in = AudioSystem.getAudioInputStream(new BufferedInputStream(raw))) {
                nuevo = AudioSystem.getClip();
                nuevo.open(in);
            }
        } catch (Exception | LinkageError e) {
            disponible = false;
            if (nuevo != null) {
                nuevo.close();
            }
            return;
        }
        synchronized (Musica.class) {
            if (solicitada != pista) {
                nuevo.close();
                return;
            }
            clip = nuevo;
            aplicarVolumen();
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    private static void detener() {
        if (clip != null) {
            clip.stop();
            clip.close();
            clip = null;
        }
    }

    public static synchronized void subirVolumen() {
        silenciada = false;
        volumen = Math.min(VOLUMEN_MAX, volumen + 1);
        aplicarVolumen();
    }

    public static synchronized void bajarVolumen() {
        volumen = Math.max(0, volumen - 1);
        aplicarVolumen();
    }

    public static synchronized void alternarSilencio() {
        silenciada = !silenciada;
        aplicarVolumen();
    }

    public static synchronized String descripcion() {
        return silenciada || volumen == 0 ? "Música: apagada" : "Música: volumen " + volumen + " de " + VOLUMEN_MAX;
    }

    private static void aplicarVolumen() {
        if (clip == null || !clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            return;
        }
        FloatControl control = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        float db = silenciada ? DECIBELES[0] : DECIBELES[volumen];
        control.setValue(Math.max(control.getMinimum(), Math.min(control.getMaximum(), db)));
    }
}
