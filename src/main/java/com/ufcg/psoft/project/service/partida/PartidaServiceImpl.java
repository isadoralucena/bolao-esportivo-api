package com.ufcg.psoft.project.service.partida;

import com.ufcg.psoft.project.dto.partida.PartidaResponseDTO;
import com.ufcg.psoft.project.event.PalpitesAbertosEvent;
import com.ufcg.psoft.project.event.PalpitesFechadosEvent;
import com.ufcg.psoft.project.event.PartidaFinalizadaEvent;
import com.ufcg.psoft.project.event.PartidaIniciadaEvent;
import com.ufcg.psoft.project.exception.grupo.GrupoNaoExisteException;
import com.ufcg.psoft.project.exception.partida.PartidaSyncException;
import com.ufcg.psoft.project.model.Campeonato;
import com.ufcg.psoft.project.model.Grupo;
import com.ufcg.psoft.project.model.Partida;
import com.ufcg.psoft.project.model.PartidaStatus;
import com.ufcg.psoft.project.repository.GrupoRepository;
import com.ufcg.psoft.project.repository.PartidaRepository;

import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class PartidaServiceImpl implements PartidaService {
    private final PartidaRepository partidaRepository;

    private final GrupoRepository grupoRepository;

    private final ApplicationEventPublisher eventPublisher;

    @Value("${project.football-data.api-token:}")
    private String apiToken;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public List<PartidaResponseDTO> listarPorCampeonato(Long campeonatoId) {
        return partidaRepository.findByCampeonatoId(campeonatoId).stream()
                .map(PartidaResponseDTO::new)
                .toList();
    }

    @Override
    public List<PartidaResponseDTO> listarPorGrupo(Long grupoId) {
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(GrupoNaoExisteException::new);
        LocalDateTime agora = LocalDateTime.now(ZoneOffset.UTC);
        return partidaRepository.findByCampeonatoId(grupo.getCampeonato().getId()).stream()
                .map(p -> new PartidaResponseDTO(p, grupo, agora))
                .toList();
    }

    @Override
    @Transactional
    public List<PartidaResponseDTO> sincronizarPartidas(Campeonato campeonato) {
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

        List<PartidaResponseDTO> resultado = new ArrayList<>();

        for (Map<String, Object> match : matches) {
            resultado.add(salvarOuAtualizarPartida(campeonato, match));
        }

        return resultado;
    }

    @Override
    public void deleteByCampeonatoId(Long campeonatoId) {
        partidaRepository.deleteByCampeonatoId(campeonatoId);
    }

    @Override
    public List<PartidaResponseDTO> listarPartidasFuturas() {
        return partidaRepository.findByDataAfterAndStatus(
                        LocalDateTime.now(), PartidaStatus.ABERTO)
                .stream()
                .map(PartidaResponseDTO::new)
                .toList();
    }

    private PartidaResponseDTO salvarOuAtualizarPartida(Campeonato campeonato, Map<String, Object> match) {
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

        PartidaStatus statusAnterior = partida.getStatus();
        Integer golsMandanteAnterior = partida.getGolsMandante();
        Integer golsVisitanteAnterior = partida.getGolsVisitante();

        if (fullTime != null) {
            if (fullTime.get("home") != null) {
                partida.setGolsMandante((Integer) fullTime.get("home"));
            }
            if (fullTime.get("away") != null) {
                partida.setGolsVisitante((Integer) fullTime.get("away"));
            }
        }

        String utcDate = (String) match.get("utcDate");
        PartidaStatus novoStatus = PartidaServiceImpl.converterStatus((String) match.get("status"));
        boolean mataMata = ehMataMata((String) match.get("stage"));
        boolean statusMudou = statusAnterior != novoStatus;
        boolean placarMudou = !Objects.equals(golsMandanteAnterior, partida.getGolsMandante()) ||
                            !Objects.equals(golsVisitanteAnterior, partida.getGolsVisitante());

        partida.setMandante((String) homeTeam.get("name"));
        partida.setVisitante((String) awayTeam.get("name"));

        if (utcDate != null) {
            partida.setData(LocalDateTime.ofInstant(Instant.parse(utcDate), ZoneOffset.UTC));
        }

        partida.setStatus(novoStatus);
        partida.setMataMata(mataMata);

        if (placarMudou) {
            partida.setConsolidada(false);
        }

        partida = partidaRepository.save(partida);
        boolean precisaAtualizarPontuacao = novoStatus == PartidaStatus.FINALIZADO && (statusMudou || placarMudou);

        if (statusMudou) {
            publicarEventosMudancaStatus(statusAnterior, novoStatus, partida);
        } else if (precisaAtualizarPontuacao) {
            eventPublisher.publishEvent(new PartidaFinalizadaEvent(this, partida));
        }

        return new PartidaResponseDTO(partida);
    }

    private void publicarEventosMudancaStatus(PartidaStatus statusAnterior, PartidaStatus novoStatus, Partida partida) {
        if (novoStatus == PartidaStatus.ABERTO && statusAnterior == null) {
            eventPublisher.publishEvent(new PalpitesAbertosEvent(this, partida));
        } else if (novoStatus == PartidaStatus.EM_ANDAMENTO) {
            if (statusAnterior == PartidaStatus.ABERTO) {
                eventPublisher.publishEvent(new PalpitesFechadosEvent(this, partida));
            }
            eventPublisher.publishEvent(new PartidaIniciadaEvent(this, partida));
        } else if (novoStatus == PartidaStatus.FINALIZADO) {
            eventPublisher.publishEvent(new PartidaFinalizadaEvent(this, partida));
        }
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

    private boolean ehMataMata(String stage) {
        boolean ehMataMata = false;
        
        if (stage == null) {
            return ehMataMata;
        }

        ehMataMata = switch (stage) {
            case "FINAL",
                "THIRD_PLACE",
                "SEMI_FINALS",
                "QUARTER_FINALS",
                "LAST_16",
                "LAST_32",
                "LAST_64",
                "ROUND_4",
                "ROUND_3",
                "ROUND_2",
                "ROUND_1",
                "PLAYOFF_ROUND_1",
                "PLAYOFF_ROUND_2",
                "PLAYOFFS" -> true;

            default -> false;
        };

        return ehMataMata;
    }
}
