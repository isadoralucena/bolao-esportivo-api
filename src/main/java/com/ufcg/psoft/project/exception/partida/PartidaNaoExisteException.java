package com.ufcg.psoft.project.exception.partida;

import com.ufcg.psoft.project.exception.ProjectException;

public class PartidaNaoExisteException extends ProjectException {
    public PartidaNaoExisteException() {
        super("Essa partida não existe!");
    }
}
