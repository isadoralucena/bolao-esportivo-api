package com.ufcg.psoft.project.exception.partida;

import com.ufcg.psoft.project.exception.ProjectException;

public class PartidasInvalidasException extends ProjectException {
    public PartidasInvalidasException() {
        super("O campeonato não possui partida válidas!");
    }
}