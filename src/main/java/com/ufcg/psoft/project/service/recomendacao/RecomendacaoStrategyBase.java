package com.ufcg.psoft.project.service.recomendacao;

import com.ufcg.psoft.project.dto.recomendacao.RecomendacaoResponseDTO;
import com.ufcg.psoft.project.model.Partida;
import com.ufcg.psoft.project.model.PartidaStatus;
import com.ufcg.psoft.project.repository.PartidaRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public abstract class RecomendacaoStrategyBase implements RecomendacaoStrategy {

    @Autowired
    protected PartidaRepository partidaRepository;

    protected List<Partida> buscarPartidasFinalizadas(Long campeonatoId) {
        return partidaRepository.findByCampeonatoId(campeonatoId).stream()
                .filter(p -> p.getStatus() == PartidaStatus.FINALIZADO
                        && p.getGolsMandante() != null
                        && p.getGolsVisitante() != null)
                .toList();
    }

    protected RecomendacaoResponseDTO semHistorico() {
        return RecomendacaoResponseDTO.builder()
                .golsMandanteRecomendado(0)
                .golsVisitanteRecomendado(0)
                .estrategia(getNome())
                .temHistorico(false)
                .build();
    }
}