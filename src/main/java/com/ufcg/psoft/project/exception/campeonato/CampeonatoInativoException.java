package com.ufcg.psoft.project.exception.campeonato;

import com.ufcg.psoft.project.exception.ProjectException;

public class CampeonatoInativoException extends ProjectException {
    public CampeonatoInativoException() {
        super("O campeonato associado a este grupo não está ativo!");
    }
}