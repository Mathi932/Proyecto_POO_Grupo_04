package com.upch.rescateselva.patrones.factory;

import com.upch.rescateselva.modelo.enemigos.Cazador;
import com.upch.rescateselva.modelo.enemigos.Enemigo;
import com.upch.rescateselva.modelo.enemigos.Serpiente;

// PATRÓN FACTORY (Simple Factory): único lugar donde se crean los enemigos
public class FabricaEnemigos {
    public Enemigo crearEnemigo(String tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de enemigo no puede ser null");
        }
        switch (tipo.toLowerCase()) {
            case "cazador":
                return crearCazador();
            case "serpiente":
                return crearSerpiente();
            default:
                throw new IllegalArgumentException("Tipo de enemigo desconocido: " + tipo);
        }
    }

    public Cazador crearCazador() {
        return new Cazador();
    }

    public Serpiente crearSerpiente() {
        return new Serpiente();
    }
}
