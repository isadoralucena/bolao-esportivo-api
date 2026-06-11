package com.ufcg.psoft.project.exception;

public class LimiteDeParticipantesAtingidoException extends ProjectException {
    public LimiteDeParticipantesAtingidoException() {
        super("O limite de participantes para este grupo já foi atingido!");
    }
    
}
