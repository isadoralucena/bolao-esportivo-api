package com.ufcg.psoft.project.exception.convite;

import com.ufcg.psoft.project.exception.ProjectException;

public class ConviteNaoExisteException extends ProjectException {
    public ConviteNaoExisteException() {
        super("O convite não existe!");
    }
}
