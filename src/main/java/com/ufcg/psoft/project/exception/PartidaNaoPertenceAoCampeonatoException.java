package com.ufcg.psoft.project.exception;

public class PartidaNaoPertenceAoCampeonatoException extends ProjectException {
    public PartidaNaoPertenceAoCampeonatoException() {
        super("Essa partida não pertence ao campeonato do grupo!");
    }
}
