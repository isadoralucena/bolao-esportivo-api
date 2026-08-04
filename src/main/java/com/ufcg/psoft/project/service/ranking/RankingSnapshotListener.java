package com.ufcg.psoft.project.service.ranking;

import com.ufcg.psoft.project.event.PartidaFinalizadaEvent;
import com.ufcg.psoft.project.model.Grupo;
import com.ufcg.psoft.project.repository.GrupoRepository;
import com.ufcg.psoft.project.repository.PalpiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RankingSnapshotListener {

    private final RankingHistoricoService rankingHistoricoService;

    private final PalpiteRepository palpiteRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void aoFinalizarPartida(PartidaFinalizadaEvent event) {
        Long partidaId = event.getPartida().getId();

        List<Long> gruposAfetados = palpiteRepository.findByPartidaId(partidaId)
                .stream()
                .map(p -> p.getGrupo().getId())
                .distinct()
                .toList();

        for (Long grupoId : gruposAfetados) {
            rankingHistoricoService.gerarSnapshot(grupoId, partidaId);
        }
    }
}
