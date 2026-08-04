package com.ufcg.psoft.project.service.consolidacao;


import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.annotation.Transactional;

import com.ufcg.psoft.project.event.PartidaConsolidadaEvent;
import com.ufcg.psoft.project.event.PartidaFinalizadaEvent;
import com.ufcg.psoft.project.exception.partida.PartidaSyncException;
import com.ufcg.psoft.project.model.Partida;
import com.ufcg.psoft.project.model.PartidaStatus;
import com.ufcg.psoft.project.repository.PartidaRepository;
import com.ufcg.psoft.project.service.pontuacao.PontuacaoService;

@Service
@RequiredArgsConstructor
public class ConsolidacaoPartidaServiceImpl implements ConsolidacaoPartidaService {

    private final PontuacaoService pontuacaoService;

    private final PartidaRepository partidaRepository;

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void consolidar(Partida partida) {
        if (partida.getStatus() != PartidaStatus.FINALIZADO || partida.isConsolidada()) {
            return; 
        }

        if (!resultadoValido(partida)) {
            throw new PartidaSyncException("Resultado inválido para consolidação da partida " + partida.getId());
        }

        pontuacaoService.calcularPontuacoesAssociadasAPartida(partida.getId());
        partida.setConsolidada(true);
        partidaRepository.save(partida);
        eventPublisher.publishEvent(new PartidaConsolidadaEvent(this, partida));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void aoFinalizarPartida(PartidaFinalizadaEvent event) {
        consolidar(event.getPartida());
    }

    private boolean resultadoValido(Partida partida) {
        return partida.getGolsMandante() != null && partida.getGolsMandante() >= 0
                && partida.getGolsVisitante() != null && partida.getGolsVisitante() >= 0;
    }
}
