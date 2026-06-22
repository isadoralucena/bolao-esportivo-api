package com.ufcg.psoft.project.exception;

public class PalpiteForaDoTempoException extends ProjectException {
    public PalpiteForaDoTempoException() {
        super("O palpite não pode ser editado ou removido pois o tempo de criação do palpite expirou!");
    }
}