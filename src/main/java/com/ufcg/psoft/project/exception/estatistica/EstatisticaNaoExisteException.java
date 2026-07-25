package com.ufcg.psoft.project.exception.estatistica;

import com.ufcg.psoft.project.exception.ProjectException;

public class EstatisticaNaoExisteException extends ProjectException {
    public EstatisticaNaoExisteException() {
        super("Não existe estatística associada ao usuário!");
    }
}