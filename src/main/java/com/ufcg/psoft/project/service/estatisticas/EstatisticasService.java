package com.ufcg.psoft.project.service.estatisticas;

import java.util.List;

import com.ufcg.psoft.project.dto.estatisticas.EstatisticasResponseDTO;
import com.ufcg.psoft.project.model.Partida;

public interface EstatisticasService {
    List<EstatisticasResponseDTO> calcularEstatisticasAssociadasAPartida(Partida p);
    EstatisticasResponseDTO obterEstatisticaMaisRecente(Long usuarioId, String codigoAcesso);
    List<EstatisticasResponseDTO> obterEvolucaoEstatisticas(Long usuarioId, String codigoAcesso);
}