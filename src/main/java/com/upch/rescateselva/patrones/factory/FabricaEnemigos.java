package com.upch.rescateselva.patrones.factory;

import com.upch.rescateselva.modelo.enemigos.Cazador;
import com.upch.rescateselva.modelo.enemigos.Enemigo;
import com.upch.rescateselva.modelo.enemigos.Serpiente;

/**
 * Patrón FACTORY METHOD (GoF).
 * Centraliza la creación de enemigos: quien necesita uno (los niveles) solo
 * pide un "tipo" y no necesita conocer qué clase concreta se instancia.
 */
public class FabricaEnemigos {

    /**
     * @param tipo "cazador" o "serpiente" (no distingue mayúsculas)
     * @return un enemigo nuevo con sus valores por defecto
     */
    public Enemigo crearEnemigo(String tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de enemigo no puede ser null");
        }
        switch (tipo.toLowerCase()) {
            case "cazador":
                return new Cazador();
            case "serpiente":
                return new Serpiente();
            default:
                throw new IllegalArgumentException("Tipo de enemigo desconocido: " + tipo);
        }
    }
}
