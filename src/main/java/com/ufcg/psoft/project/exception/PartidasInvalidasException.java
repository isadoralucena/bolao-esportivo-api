package com.ufcg.psoft.project.exception;

public class PartidasInvalidasException extends ProjectException {
    public PartidasInvalidasException() {
        super("O campeonato não possui partida válidas!");
    }
}