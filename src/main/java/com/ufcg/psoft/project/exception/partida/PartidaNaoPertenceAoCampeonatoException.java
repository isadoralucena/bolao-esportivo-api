package com.ufcg.psoft.project.exception.partida;

import com.ufcg.psoft.project.exception.ProjectException;

public class PartidaNaoPertenceAoCampeonatoException extends ProjectException {
    public PartidaNaoPertenceAoCampeonatoException() {
        super("Essa partida não pertence ao campeonato do grupo!");
    }
}
