package com.ufcg.psoft.project.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

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
import com.ufcg.psoft.project.service.partida.PartidaServiceImpl;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes de service de partida")
public class PartidaServiceImplTest {
    @Mock
    private PartidaRepository partidaRepository;

    @Mock
    private GrupoRepository grupoRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private PartidaServiceImpl partidaService;

    private MockRestServiceServer server;

    @BeforeEach
    void setup() {
        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(partidaService, "restTemplate");
        server = MockRestServiceServer.createServer(restTemplate);
        lenient().when(partidaRepository.save(any(Partida.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
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
                            "utcDate": "2026-07-05T18:00:00Z",
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

    @Test
    @DisplayName("Quando sincroniza partidas com token configurado")
    void quandoSincronizaPartidasComTokenConfigurado() {

        String resposta = """
                {
                    "matches": [
                    ]
                }
                """;
                
        ReflectionTestUtils.setField(partidaService, "apiToken", "TOKEN");

        Campeonato campeonato = Campeonato.builder()
                .id(1L)
                .nome("Campeonato Teste")
                .url("http://api.test/competitions/1")
                .codigo("TST")
                .ativo(true)
                .build();

        server.expect(requestTo("http://api.test/competitions/1/matches"))
                .andExpect(method(GET))
                .andRespond(withSuccess(resposta, MediaType.APPLICATION_JSON));

        List<PartidaResponseDTO> resultado = partidaService.sincronizarPartidas(campeonato);

        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("Quando resposta da API não tem corpo, lança exceção")
    void quandoRespostaSemCorpoLancaExcecao() {
        Campeonato campeonato = Campeonato.builder()
                .id(1L)
                .nome("Campeonato Teste")
                .url("http://api.test/competitions/1")
                .codigo("TST")
                .ativo(true)
                .build();

        server.expect(requestTo("http://api.test/competitions/1/matches"))
                .andExpect(method(GET))
                .andRespond(withSuccess("", MediaType.APPLICATION_JSON));

        assertThrows(PartidaSyncException.class, () -> partidaService.sincronizarPartidas(campeonato));
    }

    @Test
    @DisplayName("Quando sincroniza partidas com diferentes status da API")
    void quandoSincronizaPartidasComDiferentesStatusDaApi() {
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
                        {"id": 20, "homeTeam": {"name": "A"}, "awayTeam": {"name": "B"}, "status": "SCHEDULED"},
                        {"id": 21, "homeTeam": {"name": "C"}, "awayTeam": {"name": "D"}, "status": "LIVE"},
                        {"id": 22, "homeTeam": {"name": "E"}, "awayTeam": {"name": "F"}, "status": "CANCELLED"},
                        {"id": 23, "homeTeam": {"name": "G"}, "awayTeam": {"name": "H"}, "status": "UNKNOWN"},
                        {"id": 24, "homeTeam": {"name": "I"}, "awayTeam": {"name": "J"}, "status": null}
                    ]
                }
                """;

        when(partidaRepository.findByCampeonatoIdAndCodigoExterno(campeonato.getId(), 20L)).thenReturn(Optional.empty());
        when(partidaRepository.findByCampeonatoIdAndCodigoExterno(campeonato.getId(), 21L)).thenReturn(Optional.empty());
        when(partidaRepository.findByCampeonatoIdAndCodigoExterno(campeonato.getId(), 22L)).thenReturn(Optional.empty());
        when(partidaRepository.findByCampeonatoIdAndCodigoExterno(campeonato.getId(), 23L)).thenReturn(Optional.empty());
        when(partidaRepository.findByCampeonatoIdAndCodigoExterno(campeonato.getId(), 24L)).thenReturn(Optional.empty());

        server.expect(requestTo("http://api.test/competitions/1/matches"))
                .andExpect(method(GET))
                .andRespond(withSuccess(resposta, MediaType.APPLICATION_JSON));

        List<PartidaResponseDTO> resultado = partidaService.sincronizarPartidas(campeonato);

        assertEquals(PartidaStatus.ABERTO, resultado.get(0).getStatus());
        assertEquals(PartidaStatus.EM_ANDAMENTO, resultado.get(1).getStatus());
        assertEquals(PartidaStatus.CANCELADO, resultado.get(2).getStatus());
        assertEquals(PartidaStatus.ABERTO, resultado.get(3).getStatus());
        assertEquals(PartidaStatus.ABERTO, resultado.get(4).getStatus());
    }

    @Test
    @DisplayName("deleteByCampeonatoId delega ao repositorio")
    void quandoDeleteByCampeonatoIdChamaRepositorio() {
        partidaService.deleteByCampeonatoId(1L);
        verify(partidaRepository).deleteByCampeonatoId(1L);
    }

    @Test
    @DisplayName("listarPorCampeonato retorna DTOs do repositorio")
    void quandoListarPorCampeonatoRetornaDTOs() {
        Campeonato campeonato = Campeonato.builder().id(1L).build();
        Partida partida = Partida.builder()
                .id(10L).campeonato(campeonato)
                .mandante("A").visitante("B")
                .golsMandante(2).golsVisitante(1)
                .data(LocalDateTime.of(2026, 7, 5, 18, 0))
                .status(PartidaStatus.FINALIZADO)
                .codigoExterno(100L)
                .mataMata(false)
                .build();

        when(partidaRepository.findByCampeonatoId(1L)).thenReturn(List.of(partida));

        List<PartidaResponseDTO> resultado = partidaService.listarPorCampeonato(1L);

        assertEquals(1, resultado.size());
        assertEquals("A", resultado.get(0).getMandante());
        assertEquals("B", resultado.get(0).getVisitante());
        assertEquals(PartidaStatus.FINALIZADO, resultado.get(0).getStatus());
    }

    @Test
    @DisplayName("listarPorGrupo quando grupo nao existe lanca GrupoNaoExisteException")
    void quandoListarPorGrupoGrupoNaoExiste() {
        when(grupoRepository.findById(999L)).thenReturn(java.util.Optional.empty());
        assertThrows(GrupoNaoExisteException.class, () -> partidaService.listarPorGrupo(999L));
    }

    @Test
    @DisplayName("listaPorGrupo retorna DTOs quando grupo existe")
    void quandoListarPorGrupoRetornaDTOs() {
        Campeonato campeonato = Campeonato.builder().id(1L).build();
        Grupo grupo = Grupo.builder().id(1L).campeonato(campeonato).build();

        when(grupoRepository.findById(1L)).thenReturn(java.util.Optional.of(grupo));
        when(partidaRepository.findByCampeonatoId(1L)).thenReturn(List.of());

        List<PartidaResponseDTO> resultado = partidaService.listarPorGrupo(1L);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("Nova partida SCHEDULED publica evento de abertura de palpites")
    void quandoNovaPartidaScheduledPublicaEventoDeAbertura() {
        Campeonato campeonato = Campeonato.builder()
                .id(1L).nome("Teste")
                .url("http://api.test/competitions/1")
                .codigo("TST").ativo(true).build();

        String resposta = """
                {
                    "matches": [
                        {"id": 30, "homeTeam": {"name": "A"}, "awayTeam": {"name": "B"},
                         "status": "SCHEDULED", "utcDate": "2026-07-06T18:00:00Z"}
                    ]
                }
                """;

        when(partidaRepository.findByCampeonatoIdAndCodigoExterno(1L, 30L))
                .thenReturn(java.util.Optional.empty());

        server.expect(requestTo("http://api.test/competitions/1/matches"))
                .andExpect(method(GET))
                .andRespond(withSuccess(resposta, MediaType.APPLICATION_JSON));

        partidaService.sincronizarPartidas(campeonato);

        ArgumentCaptor<PalpitesAbertosEvent> eventoCaptor =
                ArgumentCaptor.forClass(PalpitesAbertosEvent.class);

        verify(eventPublisher).publishEvent(eventoCaptor.capture());
        assertEquals(30L, eventoCaptor.getValue().getPartida().getCodigoExterno());
        assertEquals(PartidaStatus.ABERTO, eventoCaptor.getValue().getPartida().getStatus());
    }

    @Test
    @DisplayName("Partida transita de ABERTO para EM_ANDAMENTO e publica eventos")
    void quandoPartidaTransitaDeAbertoParaEmAndamentoPublicaEventos() {
        Campeonato campeonato = Campeonato.builder()
                .id(1L).nome("Teste")
                .url("http://api.test/competitions/1")
                .codigo("TST").ativo(true).build();

        String resposta = """
                {
                    "matches": [
                        {"id": 40, "homeTeam": {"name": "C"}, "awayTeam": {"name": "D"},
                         "status": "LIVE", "utcDate": "2026-07-06T18:00:00Z"}
                    ]
                }
                """;

        Partida existente = Partida.builder()
                .id(100L).campeonato(campeonato).codigoExterno(40L)
                .mandante("C").visitante("D")
                .status(PartidaStatus.ABERTO)
                .data(LocalDateTime.of(2026, 7, 6, 18, 0))
                .build();

        when(partidaRepository.findByCampeonatoIdAndCodigoExterno(1L, 40L))
                .thenReturn(java.util.Optional.of(existente));

        server.expect(requestTo("http://api.test/competitions/1/matches"))
                .andExpect(method(GET))
                .andRespond(withSuccess(resposta, MediaType.APPLICATION_JSON));

        partidaService.sincronizarPartidas(campeonato);

        ArgumentCaptor<ApplicationEvent> eventosCaptor =
                ArgumentCaptor.forClass(ApplicationEvent.class);

        verify(eventPublisher, times(2))
                .publishEvent(eventosCaptor.capture());

        List<ApplicationEvent> eventosPublicados =
                eventosCaptor.getAllValues();

        PalpitesFechadosEvent eventoFechamento = eventosPublicados.stream()
                .filter(PalpitesFechadosEvent.class::isInstance)
                .map(PalpitesFechadosEvent.class::cast)
                .findFirst()
                .orElseThrow();

        PartidaIniciadaEvent eventoInicio = eventosPublicados.stream()
                .filter(PartidaIniciadaEvent.class::isInstance)
                .map(PartidaIniciadaEvent.class::cast)
                .findFirst()
                .orElseThrow();

        assertAll(
                () -> assertEquals(
                        100L,
                        eventoFechamento.getPartida().getId()
                ),
                () -> assertEquals(
                        100L,
                        eventoInicio.getPartida().getId()
                )
        );
    }

    @Test
    @DisplayName("Partida transita para FINALIZADO publica evento de finalização")
    void quandoPartidaTransitaParaFinalizadoPublicaEvento() {
        Campeonato campeonato = Campeonato.builder()
                .id(1L).nome("Teste")
                .url("http://api.test/competitions/1")
                .codigo("TST").ativo(true).build();

        String resposta = """
                {
                    "matches": [
                        {"id": 50, "homeTeam": {"name": "E"}, "awayTeam": {"name": "F"},
                         "score": {"fullTime": {"home": 2, "away": 1}},
                         "status": "FINISHED", "utcDate": "2026-07-06T18:00:00Z"}
                    ]
                }
                """;

        Partida existente = Partida.builder()
                .id(101L).campeonato(campeonato).codigoExterno(50L)
                .mandante("E").visitante("F")
                .status(PartidaStatus.EM_ANDAMENTO)
                .data(LocalDateTime.of(2026, 7, 6, 18, 0))
                .build();

        when(partidaRepository.findByCampeonatoIdAndCodigoExterno(1L, 50L))
                .thenReturn(java.util.Optional.of(existente));

        server.expect(requestTo("http://api.test/competitions/1/matches"))
                .andExpect(method(GET))
                .andRespond(withSuccess(resposta, MediaType.APPLICATION_JSON));

        partidaService.sincronizarPartidas(campeonato);

        ArgumentCaptor<PartidaFinalizadaEvent> eventoCaptor =
                ArgumentCaptor.forClass(PartidaFinalizadaEvent.class);

        verify(eventPublisher).publishEvent(eventoCaptor.capture());
        assertEquals(101L, eventoCaptor.getValue().getPartida().getId());
        assertEquals(PartidaStatus.FINALIZADO, eventoCaptor.getValue().getPartida().getStatus());
    }

    @Test
    @DisplayName("Partida com status e placar inalterados não publica eventos")
    void quandoPartidaStatusEPlacarInalteradosNaoPublicaEventos() {
        Campeonato campeonato = Campeonato.builder()
                .id(1L).nome("Teste")
                .url("http://api.test/competitions/1")
                .codigo("TST").ativo(true).build();

        String resposta = """
                {
                    "matches": [
                        {"id": 60, "homeTeam": {"name": "G"}, "awayTeam": {"name": "H"},
                         "score": {"fullTime": {"home": 1, "away": 0}},
                         "status": "FINISHED", "utcDate": "2026-07-06T18:00:00Z"}
                    ]
                }
                """;

        Partida existente = Partida.builder()
                .id(102L).campeonato(campeonato).codigoExterno(60L)
                .mandante("G").visitante("H")
                .golsMandante(1).golsVisitante(0)
                .status(PartidaStatus.FINALIZADO)
                .data(LocalDateTime.of(2026, 7, 6, 18, 0))
                .build();

        when(partidaRepository.findByCampeonatoIdAndCodigoExterno(1L, 60L))
                .thenReturn(java.util.Optional.of(existente));

        server.expect(requestTo("http://api.test/competitions/1/matches"))
                .andExpect(method(GET))
                .andRespond(withSuccess(resposta, MediaType.APPLICATION_JSON));

        partidaService.sincronizarPartidas(campeonato);

        verifyNoInteractions(eventPublisher);
    }

    @Test
	@DisplayName("Partida com status AWARDED é finalizada e publica evento")
	void quandoStatusAwardedFinalizaPartidaEPublicaEvento() {
		Campeonato campeonato = Campeonato.builder()
				.id(1L)
				.nome("Teste")
				.url("http://api.test/competitions/1")
				.codigo("TST")
				.ativo(true)
				.build();

		String resposta = """
				{
						"matches": [
						{
								"id": 100,
								"homeTeam": {"name": "Time A"},
								"awayTeam": {"name": "Time B"},
								"score": {
								"fullTime": {
										"home": 3,
										"away": 0
								}
								},
								"status": "AWARDED",
								"utcDate": "2026-07-06T18:00:00Z"
						}
						]
				}
				""";

		Partida existente = Partida.builder()
				.id(200L)
				.campeonato(campeonato)
				.codigoExterno(100L)
				.mandante("Time A")
				.visitante("Time B")
				.status(PartidaStatus.EM_ANDAMENTO)
				.consolidada(false)
				.data(LocalDateTime.of(2026, 7, 6, 18, 0))
				.build();

		when(partidaRepository.findByCampeonatoIdAndCodigoExterno(1L, 100L))
				.thenReturn(Optional.of(existente));

		when(partidaRepository.save(any(Partida.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		server.expect(requestTo("http://api.test/competitions/1/matches"))
				.andExpect(method(GET))
				.andRespond(withSuccess(resposta, MediaType.APPLICATION_JSON));

		List<PartidaResponseDTO> resultado =
				partidaService.sincronizarPartidas(campeonato);

		ArgumentCaptor<PartidaFinalizadaEvent> eventoCaptor =
                ArgumentCaptor.forClass(PartidaFinalizadaEvent.class);

		verify(eventPublisher).publishEvent(eventoCaptor.capture());

		Partida partidaPublicada = eventoCaptor.getValue().getPartida();

		assertEquals(1, resultado.size());
		assertEquals(PartidaStatus.FINALIZADO, resultado.get(0).getStatus());
		assertEquals(3, resultado.get(0).getGolsMandante());
		assertEquals(0, resultado.get(0).getGolsVisitante());

		assertEquals(200L, partidaPublicada.getId());
		assertEquals(PartidaStatus.FINALIZADO, partidaPublicada.getStatus());
		assertEquals(3, partidaPublicada.getGolsMandante());
		assertEquals(0, partidaPublicada.getGolsVisitante());
		assertFalse(partidaPublicada.isConsolidada());

		verify(partidaRepository).save(existente);
	}

    @Test
    @DisplayName("Placar alterado em partida finalizada publica novo evento de finalização")
    void quandoPlacarDePartidaFinalizadaMudaPublicaNovoEvento() {
        Campeonato campeonato = Campeonato.builder()
                .id(1L).nome("Teste")
                .url("http://api.test/competitions/1")
                .codigo("TST").ativo(true).build();

        String resposta = """
                {
                    "matches": [
                        {"id": 61, "homeTeam": {"name": "G"}, "awayTeam": {"name": "H"},
                         "score": {"fullTime": {"home": 2, "away": 0}},
                         "status": "FINISHED", "utcDate": "2026-07-06T18:00:00Z"}
                    ]
                }
                """;

        Partida existente = Partida.builder()
                .id(103L).campeonato(campeonato).codigoExterno(61L)
                .mandante("G").visitante("H")
                .golsMandante(1).golsVisitante(0)
                .status(PartidaStatus.FINALIZADO)
                .consolidada(true)
                .data(LocalDateTime.of(2026, 7, 6, 18, 0))
                .build();

        when(partidaRepository.findByCampeonatoIdAndCodigoExterno(1L, 61L))
                .thenReturn(Optional.of(existente));

        server.expect(requestTo("http://api.test/competitions/1/matches"))
                .andExpect(method(GET))
                .andRespond(withSuccess(resposta, MediaType.APPLICATION_JSON));

        partidaService.sincronizarPartidas(campeonato);

        ArgumentCaptor<PartidaFinalizadaEvent> eventoCaptor =
                ArgumentCaptor.forClass(PartidaFinalizadaEvent.class);
        verify(eventPublisher).publishEvent(eventoCaptor.capture());

        Partida partidaAtualizada = eventoCaptor.getValue().getPartida();
        assertEquals(2, partidaAtualizada.getGolsMandante());
        assertEquals(0, partidaAtualizada.getGolsVisitante());
        assertFalse(partidaAtualizada.isConsolidada());
    }

    @Test
    @DisplayName("converterStatus para todos os valores da API")
    void quandoConverteStatusParaTodosValoresApi() {
        Campeonato campeonato = Campeonato.builder()
                .id(1L).nome("Teste")
                .url("http://api.test/competitions/1")
                .codigo("TST").ativo(true).build();

        String resposta = """
                {
                    "matches": [
                        {"id": 1, "homeTeam": {"name": "A"}, "awayTeam": {"name": "B"}, "status": "SCHEDULED"},
                        {"id": 2, "homeTeam": {"name": "C"}, "awayTeam": {"name": "D"}, "status": "TIMED"},
                        {"id": 3, "homeTeam": {"name": "E"}, "awayTeam": {"name": "F"}, "status": "POSTPONED"},
                        {"id": 4, "homeTeam": {"name": "G"}, "awayTeam": {"name": "H"}, "status": "LIVE"},
                        {"id": 5, "homeTeam": {"name": "I"}, "awayTeam": {"name": "J"}, "status": "IN_PLAY"},
                        {"id": 6, "homeTeam": {"name": "K"}, "awayTeam": {"name": "L"}, "status": "PAUSED"},
                        {"id": 7, "homeTeam": {"name": "M"}, "awayTeam": {"name": "N"}, "status": "SUSPENDED"},
                        {"id": 8, "homeTeam": {"name": "O"}, "awayTeam": {"name": "P"}, "status": "FINISHED"},
                        {"id": 9, "homeTeam": {"name": "Q"}, "awayTeam": {"name": "R"}, "status": "AWARDED"},
                        {"id": 10, "homeTeam": {"name": "S"}, "awayTeam": {"name": "T"}, "status": "CANCELLED"}
                    ]
                }
                """;

        for (long id = 1; id <= 10; id++) {
            when(partidaRepository.findByCampeonatoIdAndCodigoExterno(1L, id))
                    .thenReturn(java.util.Optional.empty());
        }

        server.expect(requestTo("http://api.test/competitions/1/matches"))
                .andExpect(method(GET))
                .andRespond(withSuccess(resposta, MediaType.APPLICATION_JSON));

        List<PartidaResponseDTO> resultado = partidaService.sincronizarPartidas(campeonato);

        assertEquals(10, resultado.size());
        assertEquals(PartidaStatus.ABERTO, resultado.get(0).getStatus());       // SCHEDULED
        assertEquals(PartidaStatus.ABERTO, resultado.get(1).getStatus());       // TIMED
        assertEquals(PartidaStatus.ABERTO, resultado.get(2).getStatus());       // POSTPONED
        assertEquals(PartidaStatus.EM_ANDAMENTO, resultado.get(3).getStatus()); // LIVE
        assertEquals(PartidaStatus.EM_ANDAMENTO, resultado.get(4).getStatus()); // IN_PLAY
        assertEquals(PartidaStatus.EM_ANDAMENTO, resultado.get(5).getStatus()); // PAUSED
        assertEquals(PartidaStatus.EM_ANDAMENTO, resultado.get(6).getStatus()); // SUSPENDED
        assertEquals(PartidaStatus.FINALIZADO, resultado.get(7).getStatus());   // FINISHED
        assertEquals(PartidaStatus.FINALIZADO, resultado.get(8).getStatus());   // AWARDED
        assertEquals(PartidaStatus.CANCELADO, resultado.get(9).getStatus());    // CANCELLED
    }

    @Test
    @DisplayName("ehMataMata com stage FINAL retorna true no DTO")
    void quandoStageFINALMataMataTrue() {
        Campeonato campeonato = Campeonato.builder()
                .id(1L).nome("Teste")
                .url("http://api.test/competitions/1")
                .codigo("TST").ativo(true).build();

        String resposta = """
                {
                    "matches": [
                        {"id": 70, "homeTeam": {"name": "X"}, "awayTeam": {"name": "Y"},
                         "score": {"fullTime": {"home": 2, "away": 1}},
                         "status": "FINISHED", "utcDate": "2026-07-06T18:00:00Z",
                         "stage": "FINAL"}
                    ]
                }
                """;

        when(partidaRepository.findByCampeonatoIdAndCodigoExterno(1L, 70L))
                .thenReturn(java.util.Optional.empty());

        server.expect(requestTo("http://api.test/competitions/1/matches"))
                .andExpect(method(GET))
                .andRespond(withSuccess(resposta, MediaType.APPLICATION_JSON));

        List<PartidaResponseDTO> resultado = partidaService.sincronizarPartidas(campeonato);
        assertTrue(resultado.get(0).isMataMata());
    }

    @Test
    @DisplayName("ehMataMata com stage null retorna false no DTO")
    void quandoStageNullMataMataFalse() {
        Campeonato campeonato = Campeonato.builder()
                .id(1L).nome("Teste")
                .url("http://api.test/competitions/1")
                .codigo("TST").ativo(true).build();

        String resposta = """
                {
                    "matches": [
                        {"id": 80, "homeTeam": {"name": "Z"}, "awayTeam": {"name": "W"},
                         "score": {"fullTime": {"home": 1, "away": 0}},
                         "status": "FINISHED", "utcDate": "2026-07-06T18:00:00Z"}
                    ]
                }
                """;

        when(partidaRepository.findByCampeonatoIdAndCodigoExterno(1L, 80L))
                .thenReturn(java.util.Optional.empty());

        server.expect(requestTo("http://api.test/competitions/1/matches"))
                .andExpect(method(GET))
                .andRespond(withSuccess(resposta, MediaType.APPLICATION_JSON));

        List<PartidaResponseDTO> resultado = partidaService.sincronizarPartidas(campeonato);
        assertFalse(resultado.get(0).isMataMata());
    }

    @Test
    @DisplayName("fullTime null em partida SCHEDULED nao quebra")
    void quandoFullTimeNullNaoQuebra() {
        Campeonato campeonato = Campeonato.builder()
                .id(1L).nome("Teste")
                .url("http://api.test/competitions/1")
                .codigo("TST").ativo(true).build();

        String resposta = """
                {
                    "matches": [
                        {"id": 90, "homeTeam": {"name": "M"}, "awayTeam": {"name": "N"},
                         "status": "SCHEDULED", "utcDate": "2026-07-07T18:00:00Z"}
                    ]
                }
                """;

        when(partidaRepository.findByCampeonatoIdAndCodigoExterno(1L, 90L))
                .thenReturn(java.util.Optional.empty());

        server.expect(requestTo("http://api.test/competitions/1/matches"))
                .andExpect(method(GET))
                .andRespond(withSuccess(resposta, MediaType.APPLICATION_JSON));

        List<PartidaResponseDTO> resultado = partidaService.sincronizarPartidas(campeonato);

        assertEquals(1, resultado.size());
        assertNull(resultado.get(0).getGolsMandante());
        assertNull(resultado.get(0).getGolsVisitante());
        assertEquals(PartidaStatus.ABERTO, resultado.get(0).getStatus());
    }

    @Test
    @DisplayName("ehMataMata com GROUP_STAGE retorna false")
    void quandoStageGROUP_STAGEMataMataFalse() {
        Campeonato campeonato = Campeonato.builder()
                .id(1L).nome("Teste")
                .url("http://api.test/competitions/1")
                .codigo("TST").ativo(true).build();

        String resposta = """
                {
                    "matches": [
                        {"id": 95, "homeTeam": {"name": "O"}, "awayTeam": {"name": "P"},
                         "score": {"fullTime": {"home": 1, "away": 0}},
                         "status": "FINISHED", "utcDate": "2026-07-06T18:00:00Z",
                         "stage": "GROUP_STAGE"}
                    ]
                }
                """;

        when(partidaRepository.findByCampeonatoIdAndCodigoExterno(1L, 95L))
                .thenReturn(java.util.Optional.empty());

        server.expect(requestTo("http://api.test/competitions/1/matches"))
                .andExpect(method(GET))
                .andRespond(withSuccess(resposta, MediaType.APPLICATION_JSON));

        List<PartidaResponseDTO> resultado = partidaService.sincronizarPartidas(campeonato);
        assertFalse(resultado.get(0).isMataMata());
    }

    @Nested
    @DisplayName("Status efetivo por grupo")
    class StatusEfetivoPorGrupo {

        private Grupo grupo;
        private Partida.PartidaBuilder partidaBuilder;
        private LocalDateTime dataPartida;

        @BeforeEach
        void setup() {
            grupo = Grupo.builder()
                    .id(1L).nome("Grupo Teste")
                    .minutosAberturaPalpites(120)
                    .minutosFechamentoPalpites(30)
                    .build();
            dataPartida = LocalDateTime.of(2026, 7, 15, 18, 0);
            partidaBuilder = Partida.builder()
                    .id(100L).codigoExterno(1L)
                    .mandante("A").visitante("B")
                    .data(dataPartida);
        }

        @Test
        @DisplayName("EM_ANDAMENTO retorna EM_ANDAMENTO independente da janela")
        void quandoStatusEmAndamentoIgnoraJanela() {
            Partida partida = partidaBuilder.status(PartidaStatus.EM_ANDAMENTO).build();
            assertEquals(PartidaStatus.EM_ANDAMENTO,
                    partida.statusEfetivoParaGrupo(grupo, dataPartida));
        }

        @Test
        @DisplayName("FINALIZADO retorna FINALIZADO independente da janela")
        void quandoStatusFinalizadoIgnoraJanela() {
            Partida partida = partidaBuilder.status(PartidaStatus.FINALIZADO).build();
            assertEquals(PartidaStatus.FINALIZADO,
                    partida.statusEfetivoParaGrupo(grupo, dataPartida));
        }

        @Test
        @DisplayName("CANCELADO retorna CANCELADO independente da janela")
        void quandoStatusCanceladoIgnoraJanela() {
            Partida partida = partidaBuilder.status(PartidaStatus.CANCELADO).build();
            assertEquals(PartidaStatus.CANCELADO,
                    partida.statusEfetivoParaGrupo(grupo, dataPartida));
        }

        @Test
        @DisplayName("ABERTO dentro da janela retorna ABERTO")
        void quandoAbertoDentroDaJanela() {
            Partida partida = partidaBuilder.status(PartidaStatus.ABERTO).build();
            LocalDateTime agora = LocalDateTime.of(2026, 7, 15, 17, 0);
            assertEquals(PartidaStatus.ABERTO,
                    partida.statusEfetivoParaGrupo(grupo, agora));
        }

        @Test
        @DisplayName("ABERTO antes da abertura retorna EM_ANDAMENTO")
        void quandoAbertoAntesDaAbertura() {
            Partida partida = partidaBuilder.status(PartidaStatus.ABERTO).build();
            LocalDateTime agora = LocalDateTime.of(2026, 7, 15, 15, 0);
            assertEquals(PartidaStatus.EM_ANDAMENTO,
                    partida.statusEfetivoParaGrupo(grupo, agora));
        }

        @Test
        @DisplayName("ABERTO depois do fechamento retorna EM_ANDAMENTO")
        void quandoAbertoDepoisDoFechamento() {
            Partida partida = partidaBuilder.status(PartidaStatus.ABERTO).build();
            LocalDateTime agora = LocalDateTime.of(2026, 7, 15, 17, 45);
            assertEquals(PartidaStatus.EM_ANDAMENTO,
                    partida.statusEfetivoParaGrupo(grupo, agora));
        }
    }
}