package com.ufcg.psoft.project.exception.estatistica;

import com.ufcg.psoft.project.exception.ProjectException;

public class EstatisticaNaoExisteExpcetion extends ProjectException {
    public EstatisticaNaoExisteExpcetion() {
        super("Não existe estatística associada ao usuário!");
    }
}
