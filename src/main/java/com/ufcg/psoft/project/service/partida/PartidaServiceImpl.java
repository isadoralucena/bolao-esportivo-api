package com.ufcg.psoft.project.service.partida;

import com.ufcg.psoft.project.dto.partida.PartidaResponseDTO;
import com.ufcg.psoft.project.exception.GrupoNaoExisteException;
import com.ufcg.psoft.project.exception.PartidaSyncException;
import com.ufcg.psoft.project.model.Campeonato;
import com.ufcg.psoft.project.model.Grupo;
import com.ufcg.psoft.project.model.Partida;
import com.ufcg.psoft.project.model.PartidaStatus;
import com.ufcg.psoft.project.repository.CampeonatoRepository;
import com.ufcg.psoft.project.repository.GrupoRepository;
import com.ufcg.psoft.project.repository.PartidaRepository;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PartidaServiceImpl implements PartidaService {

    @Autowired
    private PartidaRepository partidaRepository;

    @Autowired
    private CampeonatoRepository campeonatoRepository;

    @Autowired
    private GrupoRepository grupoRepository;

    @Value("${project.football-data.api-token:}")
    private String apiToken;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public List<PartidaResponseDTO> listarPorCampeonato(Long campeonatoId) {
        return partidaRepository.findByCampeonatoId(campeonatoId).stream()
                .map(PartidaResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<PartidaResponseDTO> listarPorGrupo(Long grupoId) {
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(GrupoNaoExisteException::new);
        return partidaRepository.findByCampeonatoId(grupo.getCampeonato().getId()).stream()
                .map(PartidaResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void sincronizarPartidas(Campeonato campeonato) {
        HttpHeaders headers = new HttpHeaders();
        
        if (apiToken != null && !apiToken.isEmpty()) {
            headers.set("X-Auth-Token", apiToken);
        }
        
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        String matchesUrl = campeonato.getUrl() + "/matches";


        ResponseEntity<Map> response = restTemplate.exchange(
                matchesUrl,
                HttpMethod.GET,
                entity,
                Map.class
        );

        Map<String, Object> body = response.getBody();

        if (body == null) {
            throw new PartidaSyncException("Resposta da API sem corpo.");
        }

        List<Map<String, Object>> matches = (List<Map<String, Object>>) body.get("matches");

        if (matches == null) {
            throw new PartidaSyncException("Resposta da API sem campo matches.");
        }

        for (Map<String, Object> match : matches) {
            salvarOuAtualizarPartida(campeonato, match);
        }

        campeonato.setUltimaSincronizacao(LocalDateTime.now());
        campeonatoRepository.save(campeonato);
    }

    private void salvarOuAtualizarPartida(Campeonato campeonato, Map<String, Object> match) {
        Long codigoExterno = Long.valueOf(match.get("id").toString());
        Map<String, Object> homeTeam = (Map<String, Object>) match.get("homeTeam");
        Map<String, Object> awayTeam = (Map<String, Object>) match.get("awayTeam");
        Map<String, Object> score = (Map<String, Object>) match.get("score");
        Map<String, Object> fullTime = score != null ? (Map<String, Object>) score.get("fullTime") : null;

        Partida partida = partidaRepository
                .findByCampeonatoIdAndCodigoExterno(campeonato.getId(), codigoExterno)
                .orElse(Partida.builder()
                        .campeonato(campeonato)
                        .codigoExterno(codigoExterno)
                        .build());

        partida.setMandante((String) homeTeam.get("name"));
        partida.setVisitante((String) awayTeam.get("name"));

        if (fullTime != null) {
            if (fullTime.get("home") != null) {
                partida.setGolsMandante((Integer) fullTime.get("home"));
            }
            if (fullTime.get("away") != null) {
                partida.setGolsVisitante((Integer) fullTime.get("away"));
            }
        }

        String utcDate = (String) match.get("utcDate");
        if (utcDate != null) {
            partida.setData(LocalDateTime.parse(utcDate, DateTimeFormatter.ISO_DATE_TIME));
        }

        partida.setStatus(PartidaServiceImpl.converterStatus((String) match.get("status")));

        if (match.get("matchday") != null) {
            partida.setRodada((Integer) match.get("matchday"));
        }

        partidaRepository.save(partida);
    }

    private static PartidaStatus converterStatus(String statusApi) {
        if (statusApi == null) return PartidaStatus.ABERTO;
        return switch (statusApi) {
            case "SCHEDULED", "TIMED", "POSTPONED" -> PartidaStatus.ABERTO;
            case "LIVE", "IN_PLAY", "PAUSED", "SUSPENDED" -> PartidaStatus.EM_ANDAMENTO;
            case "FINISHED", "AWARDED" -> PartidaStatus.FINALIZADO;
            case "CANCELLED" -> PartidaStatus.CANCELADO;
            default -> PartidaStatus.ABERTO;
        };
    }
}
