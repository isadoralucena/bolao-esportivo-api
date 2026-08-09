package com.ufcg.psoft.project.service.ranking;

import com.ufcg.psoft.project.event.RankingAtualizadoEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class RankingSnapshotListener {

    private final RankingHistoricoService rankingHistoricoService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void aoAtualizarRanking(RankingAtualizadoEvent event) {
        rankingHistoricoService.gerarSnapshot(event.getGrupoId(), event.getPartidaId());
    }
}
