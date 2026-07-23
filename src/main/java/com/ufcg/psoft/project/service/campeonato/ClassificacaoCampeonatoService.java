package com.ufcg.psoft.project.service.campeonato;

import java.util.List;

import com.ufcg.psoft.project.dto.campeonato.ClassificacaoCampeonatoResponseDTO;

public interface ClassificacaoCampeonatoService {
    List<ClassificacaoCampeonatoResponseDTO> sincronizarClassificacao(Long campeonatoId);
    List<ClassificacaoCampeonatoResponseDTO> listarPorCampeonato(Long campeonatoId);
    void deleteByCampeonatoId(Long campeonatoId);
}
