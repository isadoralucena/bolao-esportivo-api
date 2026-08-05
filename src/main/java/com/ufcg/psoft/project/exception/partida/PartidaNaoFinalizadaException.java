package com.ufcg.psoft.project.exception.partida;

import com.ufcg.psoft.project.exception.ProjectException;

public class PartidaNaoFinalizadaException extends ProjectException {

    public PartidaNaoFinalizadaException() {
        super("A partida precisa estar finalizada para ser consolidada!");
    }
}