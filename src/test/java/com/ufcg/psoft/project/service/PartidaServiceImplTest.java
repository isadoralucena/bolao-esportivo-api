package com.ufcg.psoft.project.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.ufcg.psoft.project.dto.partida.PartidaResponseDTO;
import com.ufcg.psoft.project.exception.PartidaSyncException;
import com.ufcg.psoft.project.model.Campeonato;
import com.ufcg.psoft.project.model.PartidaStatus;
import com.ufcg.psoft.project.repository.GrupoRepository;
import com.ufcg.psoft.project.repository.PartidaRepository;
import com.ufcg.psoft.project.service.partida.PartidaServiceImpl;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Testes de service de partida")
public class PartidaServiceImplTest {
    @Mock
    private PartidaRepository partidaRepository;

    @Mock
    private GrupoRepository grupoRepository;

    @InjectMocks
    private PartidaServiceImpl partidaService;

    private MockRestServiceServer server;

    @BeforeEach
    void setup() {
        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(partidaService, "restTemplate");
        server = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    @DisplayName("Quando sincroniza partidas, salva dados da API")
    void quandoSincronizaPartidasSalvaDadosDaApi() {
        Campeonato campeonato = Campeonato.builder()
                .id(1L)
                .nome("Campeonato Teste")
                .url("http://api.test/competitions/1")
                .codigo("TST")
                .ativo(true)
                .build();

        String resposta = """
                {
                    "matches": [
                        {
                            "id": 10,
                            "homeTeam": {"name": "Time A"},
                            "awayTeam": {"name": "Time B"},
                            "score": {"fullTime": {"home": 2, "away": 1}},
                            "utcDate": "2026-07-05T18:00:00",
                            "status": "FINISHED",
                            "matchday": 3
                        }
                    ]
                }
                """;

        when(partidaRepository.findByCampeonatoIdAndCodigoExterno(campeonato.getId(), 10L))
            .thenReturn(Optional.empty());

        server.expect(requestTo("http://api.test/competitions/1/matches"))
                .andExpect(method(GET))
                .andRespond(withSuccess(resposta, MediaType.APPLICATION_JSON));

        List<PartidaResponseDTO> resultado = partidaService.sincronizarPartidas(campeonato);

        assertEquals(1, resultado.size());

        PartidaResponseDTO partida = resultado.get(0);
        assertEquals("Time A", partida.getMandante());
        assertEquals("Time B", partida.getVisitante());
        assertEquals(2, partida.getGolsMandante());
        assertEquals(1, partida.getGolsVisitante());
        assertEquals(LocalDateTime.of(2026, 7, 5, 18, 0), partida.getData());
        assertEquals(PartidaStatus.FINALIZADO, partida.getStatus());
        assertEquals(3, partida.getRodada());
    }

    @Test
    @DisplayName("Quando resposta da API não tem campo matches, lança exceção")
    void quandoRespostaSemMatchesLancaExcecao() {
        Campeonato campeonato = Campeonato.builder()
                .id(1L)
                .nome("Campeonato Teste")
                .url("http://api.test/competitions/1")
                .codigo("TST")
                .ativo(true)
                .build();

        server.expect(requestTo("http://api.test/competitions/1/matches"))
                .andExpect(method(GET))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        assertThrows(PartidaSyncException.class, () -> partidaService.sincronizarPartidas(campeonato));
    }
}
