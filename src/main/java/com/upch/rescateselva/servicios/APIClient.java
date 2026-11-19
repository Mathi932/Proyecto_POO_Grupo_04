package com.upch.rescateselva.servicios;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class APIClient {
    private static final String API_URL = "https://jsonplaceholder.typicode.com/posts";
    private static final int TIMEOUT_MS = 3000;

    private APIClient() {
    }

    public static void enviarTiempo(String jugador, int segundos) {
        new Thread(() -> { // API: hilo secundario, no bloquea los 60 FPS
            HttpURLConnection conn = null;
            try {
                URL url = URI.create(API_URL).toURL();
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setConnectTimeout(TIMEOUT_MS);
                conn.setReadTimeout(TIMEOUT_MS);
                conn.setDoOutput(true);

                String payload = String.format("{\"jugador\":\"%s\",\"tiempoSegundos\":%d}", jugador, segundos);

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = payload.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int codigoRespuesta = conn.getResponseCode();
                if (codigoRespuesta == HttpURLConnection.HTTP_CREATED || codigoRespuesta == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader br = new BufferedReader(
                            new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                        StringBuilder respuesta = new StringBuilder();
                        String linea;
                        while ((linea = br.readLine()) != null) {
                            respuesta.append(linea.trim());
                        }
                        System.out.println("[APIClient] Tiempo guardado con éxito en la nube: " + respuesta);
                    }
                } else {
                    System.err.println("[APIClient] Error HTTP del servidor. Código: " + codigoRespuesta);
                }
            } catch (Exception e) { // API: modo offline / timeout
                System.err.println("[APIClient] No se pudo conectar a la API (offline o timeout): " + e.getMessage());
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }, "hilo-api-tiempo").start();
    }
}
