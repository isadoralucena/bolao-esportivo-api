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
import com.ufcg.psoft.project.service.campeonato.ClassificacaoCampeonatoService;

@Service
public class SincronizacaoPartidas {

    @Autowired
    private CampeonatoRepository campeonatoRepository;

    @Autowired
    private PartidaService partidaService;

    @Autowired
    private ClassificacaoCampeonatoService classificacaoCampeonatoService;

    @Value("${project.sync.max-sincronizacoes-por-ciclo}")
    private int maxSincronizacoesPorCiclo;

    @Scheduled(fixedDelayString = "${project.sync.scheduler-delay-ms}")
    public void sincronizarPartidas() {
        List<Campeonato> campeonatos = campeonatoRepository.findByAtivoTrue();

        campeonatos.sort(Comparator.comparing(Campeonato::getUltimaSincronizacao, Comparator.nullsFirst(LocalDateTime::compareTo)));
        
        int sincronizacoesChamadas = 0;

        for (Campeonato campeonato : campeonatos) {
            if (sincronizacoesChamadas >= maxSincronizacoesPorCiclo) {
                break;
            }

            partidaService.sincronizarPartidas(campeonato);

            try {
                partidaService.sincronizarPartidas(campeonato);
                classificacaoCampeonatoService.sincronizarClassificacao(campeonato.getId());
            } catch (Exception e) {
                System.err.println("Warning: Erro ao sincronizar campeonato " + campeonato.getUrl() + " - " + e.getMessage());
            }

            sincronizacoesChamadas++;
        }
    }
}