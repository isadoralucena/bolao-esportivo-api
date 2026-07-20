package com.ufcg.psoft.project.exception;

public class PartidaNaoExisteException extends ProjectException {
    public PartidaNaoExisteException() {
        super("Essa partida não existe!");
    }
}
