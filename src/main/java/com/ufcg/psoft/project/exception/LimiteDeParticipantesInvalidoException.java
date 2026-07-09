package com.ufcg.psoft.project.exception;

public class LimiteDeParticipantesInvalidoException extends ProjectException {
    public LimiteDeParticipantesInvalidoException() {
        super("Não é possivel definir um limite inferior a quantidade de participantes atual.");
    }
    
}
