package com.ufcg.psoft.project.service.partida;

import com.ufcg.psoft.project.dto.partida.PartidaResponseDTO;
import com.ufcg.psoft.project.model.Campeonato;

import java.util.List;

public interface PartidaService {
    List<PartidaResponseDTO> listarPorCampeonato(Long campeonatoId);

    List<PartidaResponseDTO> listarPorGrupo(Long grupoId);
    
    List<PartidaResponseDTO> sincronizarPartidas(Campeonato campeonato);
    
    void deleteByCampeonatoId(Long campeonatoId);
    
    List<PartidaResponseDTO> listarPartidasFuturas();
}