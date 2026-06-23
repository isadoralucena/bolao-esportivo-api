package com.ufcg.psoft.project.service.campeonato;

import java.util.List;

import com.ufcg.psoft.project.dto.campeonato.ClassificacaoCampeonatoResponseDTO;
import com.ufcg.psoft.project.model.Campeonato;

public interface ClassificacaoCampeonatoService {
    void sincronizarClassificacao(Long campeonatoId);
    List<ClassificacaoCampeonatoResponseDTO> listarPorCampeonato(Long campeonatoId);
}
