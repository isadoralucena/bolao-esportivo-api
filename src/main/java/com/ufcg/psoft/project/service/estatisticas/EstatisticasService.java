package com.ufcg.psoft.project.service.estatisticas;

import java.util.List;

import com.ufcg.psoft.project.dto.estatisticas.EstatisticasResponseDTO;
public interface EstatisticasService {
    List<EstatisticasResponseDTO> calcularEstatisticasAssociadasAPartida(Long partidaId);
    EstatisticasResponseDTO obterEstatisticaMaisRecente(Long usuarioId, String codigoAcesso);
    List<EstatisticasResponseDTO> obterEvolucaoEstatisticas(Long usuarioId, String codigoAcesso);
}
