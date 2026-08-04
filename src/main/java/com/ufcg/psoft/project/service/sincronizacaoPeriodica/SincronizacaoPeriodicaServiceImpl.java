package com.ufcg.psoft.project.service.sincronizacaoPeriodica;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import com.ufcg.psoft.project.model.Campeonato;
import com.ufcg.psoft.project.repository.CampeonatoRepository;
import com.ufcg.psoft.project.service.campeonato.CampeonatoService;

@Service
@RequiredArgsConstructor
public class SincronizacaoPeriodicaServiceImpl implements SincronizacaoPeriodicaService {

    private final CampeonatoService campeonatoService;

    private final CampeonatoRepository campeonatoRepository;

    @Value("${project.sync.max-sincronizacoes-por-ciclo}")
    private int maxSincronizacoesPorCiclo;

    @Scheduled(fixedDelayString = "${project.sync.scheduler-delay-ms}")
    public void sincronizarCampeonatosAtivos() {
        List<Campeonato> campeonatos = campeonatoRepository.findByAtivoTrue();

        campeonatos.sort(Comparator.comparing(Campeonato::getUltimaSincronizacao, Comparator.nullsFirst(LocalDateTime::compareTo)));
        
        int sincronizacoesChamadas = 0;

        for (Campeonato campeonato : campeonatos) {
            if (sincronizacoesChamadas >= maxSincronizacoesPorCiclo) {
                break;
            }

            try {
                campeonatoService.sincronizarCampeonato(campeonato);
            } catch (Exception e) {
                System.err.println("Warning: Erro ao sincronizar campeonato " + campeonato.getUrl() + " - " + e.getMessage());
            }

            sincronizacoesChamadas++;
        }
    }
}
