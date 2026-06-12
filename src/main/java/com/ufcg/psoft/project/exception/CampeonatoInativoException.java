package com.ufcg.psoft.project.exception;

public class CampeonatoInativoException extends ProjectException {
    public CampeonatoInativoException() {
        super("O campeonato associado a este grupo não está ativo!");
    }
}