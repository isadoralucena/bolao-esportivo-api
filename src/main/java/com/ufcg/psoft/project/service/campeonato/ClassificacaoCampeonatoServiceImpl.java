package com.ufcg.psoft.project.service.campeonato;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.ufcg.psoft.project.dto.campeonato.ClassificacaoCampeonatoResponseDTO;
import com.ufcg.psoft.project.exception.campeonato.CampeonatoNaoExisteException;
import com.ufcg.psoft.project.exception.campeonato.ClassificacaoCampeonatoSyncException;
import com.ufcg.psoft.project.model.Campeonato;
import com.ufcg.psoft.project.model.ClassificacaoCampeonato;
import com.ufcg.psoft.project.repository.CampeonatoRepository;
import com.ufcg.psoft.project.repository.ClassificacaoCampeonatoRepository;

import jakarta.transaction.Transactional;

@Service
@RequiredArgsConstructor
public class ClassificacaoCampeonatoServiceImpl implements ClassificacaoCampeonatoService {

    private final ClassificacaoCampeonatoRepository classificacaoCampeonatoRepository;

    private final CampeonatoRepository campeonatoRepository;

    @Value("${project.football-data.api-token:}")
    private String apiToken;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    @Transactional
    public List<ClassificacaoCampeonatoResponseDTO> sincronizarClassificacao(Long campeonatoId) {
        Campeonato campeonato = campeonatoRepository.findById(campeonatoId).orElseThrow(CampeonatoNaoExisteException::new);

        HttpHeaders headers = new HttpHeaders();

        if (apiToken != null && !apiToken.isBlank()) {
            headers.set("X-Auth-Token", apiToken);
        }

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        String standingsUrl = campeonato.getUrl() + "/standings";

        ResponseEntity<Map> response = restTemplate.exchange(
                standingsUrl,
                HttpMethod.GET,
                entity,
                Map.class);

        Map<String, Object> body = response.getBody();

        if (body == null) {
            throw new ClassificacaoCampeonatoSyncException("Resposta da API sem corpo.");
        }

        List<Map<String, Object>> standings = (List<Map<String, Object>>) body.get("standings");

        if (standings == null) {
            throw new ClassificacaoCampeonatoSyncException("Resposta da API sem campo standings.");
        }

        if (standings.isEmpty()) {
            return List.of(); // aceitável um campeonato ainda não ter classificações.
        }

        Map<String, Object> standing = standings.get(0);
        List<Map<String, Object>> table = (List<Map<String, Object>>) standing.get("table");

        if (table == null) {
            throw new ClassificacaoCampeonatoSyncException("Resposta da API com tabela nula.");
        }

        classificacaoCampeonatoRepository.deleteByCampeonatoId(campeonato.getId());

        List<ClassificacaoCampeonatoResponseDTO> resultado = new ArrayList<>();

        for (Map<String, Object> linha : table) {
            resultado.add(salvarLinhaClassificacao(campeonato, linha));
        }

        return resultado;
    }

    @Override
    public List<ClassificacaoCampeonatoResponseDTO> listarPorCampeonato(Long campeonatoId) {
        if (!campeonatoRepository.existsById(campeonatoId)) {
            throw new CampeonatoNaoExisteException();
        }

        return classificacaoCampeonatoRepository.findByCampeonatoIdOrderByPosicaoAsc(campeonatoId)
                .stream()
                .map(ClassificacaoCampeonatoResponseDTO::new)
                .toList();
    }

    @Override
    public void deleteByCampeonatoId(Long campeonatoId) {
        classificacaoCampeonatoRepository.deleteByCampeonatoId(campeonatoId);
    }

    private ClassificacaoCampeonatoResponseDTO salvarLinhaClassificacao(Campeonato campeonato, Map<String, Object> linha) {
        Map<String, Object> team = (Map<String, Object>) linha.get("team");

        if (team == null) {
            throw new ClassificacaoCampeonatoSyncException("Linha inválida na tabela.");
        }

        ClassificacaoCampeonato classificacao = ClassificacaoCampeonato.builder()
                .campeonato(campeonato)
                .posicao((Integer) linha.get("position"))
                .nomeTime((String) team.get("name"))
                .build();

        classificacaoCampeonatoRepository.save(classificacao);

        return new ClassificacaoCampeonatoResponseDTO(classificacao);
    }
}
