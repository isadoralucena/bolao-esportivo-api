package com.ufcg.psoft.project.exception;

public class PalpiteForaDoTempoException extends ProjectException {
    public PalpiteForaDoTempoException() {
        super("O palpite não pode ser editado ou removido pois a partida não está mais aberta!");
    }
}