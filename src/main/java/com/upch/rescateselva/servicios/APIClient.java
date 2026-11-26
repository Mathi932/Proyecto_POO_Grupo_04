package com.upch.rescateselva.servicios;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class APIClient {

    private static final String API_URL =
            "https://gtnbyjqpqbvvrklsrtko.supabase.co/rest/v1/";

    // Coloca aquí tu clave publishable de Supabase.
    private static final String API_KEY =
            "sb_publishable_kJMmEyfi0pAnClWrHiVKVQ_jK5T2IKB";

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private APIClient() {
    }

    // Crear una partida y obtener su ID.
    public static CompletableFuture<Integer> registrarPartida(
            String jugador) {

        return CompletableFuture.supplyAsync(() -> {

            try {
                String json = "{"
                        + "\"jugador\":\"" + escaparJSON(jugador) + "\","
                        + "\"puntuacion\":0,"
                        + "\"nivel\":1,"
                        + "\"resultado\":\"EN_CURSO\","
                        + "\"tiempo_segundos\":0,"
                        + "\"vidas_restantes\":3,"
                        + "\"animales_rescatados\":0"
                        + "}";

                String respuesta = enviar(
                        "POST",
                        "partidas",
                        json,
                        "return=representation"
                );

                Matcher matcher = Pattern.compile(
                        "\"id\"\\s*:\\s*(\\d+)"
                ).matcher(respuesta);

                if (!matcher.find()) {
                    throw new RuntimeException(
                            "No se recibió el ID de la partida: "
                                    + respuesta
                    );
                }

                int id = Integer.parseInt(matcher.group(1));

                System.out.println(
                        "[Supabase] Partida creada. ID: " + id
                );

                return id;

            } catch (Exception e) {

                System.err.println(
                        "[Supabase] Error al crear partida: "
                                + e.getMessage()
                );

                e.printStackTrace();

                throw new CompletionException(e);
            }
        });
    }

    // Registrar un animal rescatado.
    public static CompletableFuture<Void> registrarRescate(
            CompletableFuture<Integer> partidaId,
            String animal,
            int nivel,
            int tiempoSegundos) {

        return partidaId.thenAcceptAsync(id -> {

            try {
                String json = "{"
                        + "\"partida_id\":" + id + ","
                        + "\"animal\":\"" + escaparJSON(animal) + "\","
                        + "\"nivel\":" + nivel + ","
                        + "\"tiempo_segundos\":" + tiempoSegundos
                        + "}";

                enviar(
                        "POST",
                        "rescates",
                        json,
                        "return=minimal"
                );

                System.out.println(
                        "[Supabase] Rescate guardado: " + animal
                );

            } catch (Exception e) {

                System.err.println(
                        "[Supabase] Error al registrar rescate: "
                                + e.getMessage()
                );

                e.printStackTrace();

                throw new CompletionException(e);
            }

        }).exceptionally(error -> {

            System.err.println(
                    "[Supabase] No se pudo completar el rescate: "
                            + error.getMessage()
            );

            return null;
        });
    }

    // Actualizar el resumen de la partida al finalizar.
    public static CompletableFuture<Void> actualizarPartida(
            CompletableFuture<Integer> partidaId,
            int puntuacion,
            int nivel,
            String resultado,
            int tiempoSegundos,
            int vidasRestantes,
            int animalesRescatados) {

        return partidaId.thenAcceptAsync(id -> {

            try {
                String json = "{"
                        + "\"puntuacion\":" + puntuacion + ","
                        + "\"nivel\":" + nivel + ","
                        + "\"resultado\":\"" + escaparJSON(resultado) + "\","
                        + "\"tiempo_segundos\":" + tiempoSegundos + ","
                        + "\"vidas_restantes\":" + vidasRestantes + ","
                        + "\"animales_rescatados\":" + animalesRescatados
                        + "}";

                enviar(
                        "PATCH",
                        "partidas?id=eq." + id,
                        json,
                        "return=minimal"
                );

                System.out.println(
                        "[Supabase] Partida actualizada. ID: " + id
                );

            } catch (Exception e) {

                System.err.println(
                        "[Supabase] Error al actualizar partida: "
                                + e.getMessage()
                );

                e.printStackTrace();

                throw new CompletionException(e);
            }

        }).exceptionally(error -> {

            System.err.println(
                    "[Supabase] No se pudo completar la actualización: "
                            + error.getMessage()
            );

            return null;
        });
    }

    // Enviar solicitudes HTTP a Supabase.
    private static String enviar(
            String metodo,
            String endpoint,
            String json,
            String prefer) throws Exception {

        HttpRequest.BodyPublisher cuerpo;

        if (json != null) {
            cuerpo = HttpRequest.BodyPublishers.ofString(
                    json,
                    StandardCharsets.UTF_8
            );
        } else {
            cuerpo = HttpRequest.BodyPublishers.noBody();
        }

        HttpRequest solicitud = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + endpoint))
                .timeout(Duration.ofSeconds(10))
                .header("apikey", API_KEY)
                .header(
                        "Content-Type",
                        "application/json; charset=UTF-8"
                )
                .header("Accept", "application/json")
                .header("Prefer", prefer)
                .method(metodo, cuerpo)
                .build();

        HttpResponse<String> respuesta = CLIENT.send(
                solicitud,
                HttpResponse.BodyHandlers.ofString(
                        StandardCharsets.UTF_8
                )
        );

        int codigo = respuesta.statusCode();

        if (codigo < 200 || codigo >= 300) {
            throw new RuntimeException(
                    "HTTP " + codigo + ": " + respuesta.body()
            );
        }

        return respuesta.body();
    }

    // Escapar caracteres especiales para JSON.
    private static String escaparJSON(String texto) {

        if (texto == null) {
            return "";
        }

        return texto
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}