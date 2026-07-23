package com.ufcg.psoft.project.service.estatisticas;

import java.util.List;

import com.ufcg.psoft.project.dto.estatisticas.EstatisticasResponseDTO;
import com.ufcg.psoft.project.event.PartidaConsolidadaEvent;

public interface EstatisticasService {
    List<EstatisticasResponseDTO> calcularEstatisticasAssociadasAConsolidacaoDePartida(PartidaConsolidadaEvent event);
    EstatisticasResponseDTO obterEstatisticaMaisRecente(Long usuarioId, String codigoAcesso);
    List<EstatisticasResponseDTO> obterEvolucaoEstatisticas(Long usuarioId, String codigoAcesso);
}
