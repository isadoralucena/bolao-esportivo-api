package com.ufcg.psoft.project.exception.convite;

import com.ufcg.psoft.project.exception.ProjectException;

public class ConviteJaProcessadoException extends ProjectException {
    public ConviteJaProcessadoException() {
        super("O convite já foi processado e não pode ser modificado!");
    }
}
