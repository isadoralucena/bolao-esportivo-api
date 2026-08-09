package com.ufcg.psoft.project.service.recomendacao;

import com.ufcg.psoft.project.dto.recomendacao.RecomendacaoResponseDTO;
import com.ufcg.psoft.project.model.Partida;
import com.ufcg.psoft.project.model.PartidaStatus;
import com.ufcg.psoft.project.repository.PartidaRepository;
import java.util.List;
import java.util.Optional;

public abstract class RecomendacaoStrategyBase implements RecomendacaoStrategy {

    protected final PartidaRepository partidaRepository;

    protected RecomendacaoStrategyBase(PartidaRepository partidaRepository) {
        this.partidaRepository = partidaRepository;
    }

    protected List<Partida> buscarPartidasFinalizadas(Long campeonatoId) {
        return partidaRepository.findByCampeonatoId(campeonatoId).stream()
                .filter(p -> p.getStatus() == PartidaStatus.FINALIZADO
                        && p.getGolsMandante() != null
                        && p.getGolsVisitante() != null)
                .toList();
    }

    protected Optional<RecomendacaoResponseDTO> semHistorico() {
        return Optional.empty();
    }
}
