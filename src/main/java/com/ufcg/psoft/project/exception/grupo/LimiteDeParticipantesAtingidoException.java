package com.ufcg.psoft.project.exception.grupo;

import com.ufcg.psoft.project.exception.ProjectException;

public class LimiteDeParticipantesAtingidoException extends ProjectException {
    public LimiteDeParticipantesAtingidoException() {
        super("O limite de participantes para este grupo já foi atingido!");
    }
    
}
