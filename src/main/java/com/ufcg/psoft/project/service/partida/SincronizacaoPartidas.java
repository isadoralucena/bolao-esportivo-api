package com.ufcg.psoft.project.service.partida;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import com.ufcg.psoft.project.model.Campeonato;
import com.ufcg.psoft.project.repository.CampeonatoRepository;

@Service
public class SincronizacaoPartidas {

    @Autowired
    private CampeonatoRepository campeonatoRepository;

    @Autowired
    private PartidaService partidaService;

    @Value("${project.sync.max-requisicoes-por-ciclo}")
    private int maxRequisicoesPorCiclo;

    @Scheduled(fixedDelayString = "${project.sync.scheduler-delay-ms}")
    public void sincronizarPartidas() {
        List<Campeonato> campeonatos = campeonatoRepository.findByAtivoTrue();

        campeonatos.sort(Comparator.comparing(Campeonato::getUltimaSincronizacao, Comparator.nullsFirst(LocalDateTime::compareTo)));
        
        int requisicoesFeitas = 0;

        for (Campeonato campeonato : campeonatos) {
            if (requisicoesFeitas >= maxRequisicoesPorCiclo) {
                break;
            }

            partidaService.sincronizarPartidas(campeonato);
            requisicoesFeitas++;
        }
    }
}