package com.upch.rescateselva.niveles.estado;

import com.upch.rescateselva.niveles.Nivel;

/**
 * Patrón STATE (GoF) - interfaz del estado.
 * Un nivel pasa por fases (Explorando -> Huyendo) y en cada una se comporta
 * distinto. En vez de llenar el Nivel de "if (fase == ...)", cada fase es una
 * clase que sabe qué hacer en cada frame.
 */
public interface EstadoNivel {

    /** Lo que ocurre en cada frame mientras el nivel está en esta fase. */
    void manejar(Nivel n);
}
