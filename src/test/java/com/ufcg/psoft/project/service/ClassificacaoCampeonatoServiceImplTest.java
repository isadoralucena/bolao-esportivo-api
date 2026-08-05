package com.ufcg.psoft.project.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

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

import com.ufcg.psoft.project.dto.campeonato.ClassificacaoCampeonatoResponseDTO;
import com.ufcg.psoft.project.exception.campeonato.ClassificacaoCampeonatoSyncException;
import com.ufcg.psoft.project.model.Campeonato;
import com.ufcg.psoft.project.repository.CampeonatoRepository;
import com.ufcg.psoft.project.repository.ClassificacaoCampeonatoRepository;
import com.ufcg.psoft.project.service.campeonato.ClassificacaoCampeonatoServiceImpl;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Testes da service de classificação de campeonato")
class ClassificacaoCampeonatoServiceImplTest {
    @Mock
    private ClassificacaoCampeonatoRepository classificacaoCampeonatoRepository;

    @Mock
    private CampeonatoRepository campeonatoRepository;

    @InjectMocks
    private ClassificacaoCampeonatoServiceImpl classificacaoCampeonatoService;

    private MockRestServiceServer server;
    private Campeonato campeonato;

    @BeforeEach
    void setup() {
        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(classificacaoCampeonatoService, "restTemplate");
        server = MockRestServiceServer.createServer(restTemplate);

        campeonato = Campeonato.builder()
                .id(1L)
                .nome("Campeonato Teste")
                .url("http://api.test/competitions/1")
                .codigo("TST")
                .ativo(true)
                .build();
    }

    @Test
    @DisplayName("Quando sincroniza classificação, substitui tabela antiga pela nova")
    void quandoSincronizaClassificacaoSubstituiTabelaAntigaPelaNova() {
        String respostaAPI = """
            {
                "standings": [
                    {
                        "table": [
                            {"position": 1, "team": {"name": "Time A"}},
                            {"position": 2, "team": {"name": "Time B"}}
                        ]
                    }
                ]
            }
        """;

        when(campeonatoRepository.findById(campeonato.getId())).thenReturn(Optional.of(campeonato));

        server.expect(requestTo("http://api.test/competitions/1/standings"))
                .andExpect(method(GET))
                .andRespond(withSuccess(respostaAPI, MediaType.APPLICATION_JSON));

        List<ClassificacaoCampeonatoResponseDTO> resultado = classificacaoCampeonatoService.sincronizarClassificacao(campeonato.getId());

        assertEquals(2, resultado.size());
        assertEquals(1, resultado.get(0).getPosicao());
        assertEquals("Time A", resultado.get(0).getNomeTime());
        assertEquals(2, resultado.get(1).getPosicao());
        assertEquals("Time B", resultado.get(1).getNomeTime());
    }

    @Test
    @DisplayName("Quando resposta da API não possui standings, lança exceção")
    void quandoRespostaSemStandingsLancaExcecao() {
        String respostaAPI = """
            {}
        """;

        when(campeonatoRepository.findById(campeonato.getId())).thenReturn(Optional.of(campeonato));

        server.expect(requestTo("http://api.test/competitions/1/standings"))
                .andExpect(method(GET))
                .andRespond(withSuccess(respostaAPI, MediaType.APPLICATION_JSON));
        Long campeonatoId = campeonato.getId();

        assertThrows(ClassificacaoCampeonatoSyncException.class,
                () -> classificacaoCampeonatoService.sincronizarClassificacao(campeonatoId)
        );
    }

    @Test
    @DisplayName("Quando sincroniza classificação com standings vazio, retorna lista vazia")
    void quandoSincronizaClassificacaoComTokenEStandingsVazio() {
        String respostaAPI = """
            {
                "standings": []
            }
        """;
        ReflectionTestUtils.setField(classificacaoCampeonatoService, "apiToken", "TOKEN");

        when(campeonatoRepository.findById(campeonato.getId())).thenReturn(Optional.of(campeonato));

        server.expect(requestTo("http://api.test/competitions/1/standings"))
                .andExpect(method(GET))
                .andRespond(withSuccess(respostaAPI, MediaType.APPLICATION_JSON));

        List<ClassificacaoCampeonatoResponseDTO> resultado = classificacaoCampeonatoService.sincronizarClassificacao(campeonato.getId());

        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("Quando resposta da API não tem corpo, lança exceção")
    void quandoRespostaSemCorpoLancaExcecao() {
        when(campeonatoRepository.findById(campeonato.getId())).thenReturn(Optional.of(campeonato));

        server.expect(requestTo("http://api.test/competitions/1/standings"))
                .andExpect(method(GET))
                .andRespond(withSuccess("", MediaType.APPLICATION_JSON));
        Long campeonatoId = campeonato.getId();

        assertThrows(ClassificacaoCampeonatoSyncException.class,
                () -> classificacaoCampeonatoService.sincronizarClassificacao(campeonatoId)
        );
    }

    @Test
    @DisplayName("Quando resposta da API tem tabela vazia, lança exceção")
    void quandoRespostaComTabelaVaziaLancaExcecao() {
        String respostaAPI = """
            {
                "standings": [
                    {}
                ]
            }
        """;

        when(campeonatoRepository.findById(campeonato.getId())).thenReturn(Optional.of(campeonato));

        server.expect(requestTo("http://api.test/competitions/1/standings"))
                .andExpect(method(GET))
                .andRespond(withSuccess(respostaAPI, MediaType.APPLICATION_JSON));
        Long campeonatoId = campeonato.getId();

        assertThrows(ClassificacaoCampeonatoSyncException.class,
                () -> classificacaoCampeonatoService.sincronizarClassificacao(campeonatoId)
        );
    }

    @Test
    @DisplayName("Quando linha da classificação não tem time, lança exceção")
    void quandoLinhaDaClassificacaoNaoTemTimeLancaExcecao() {
        String respostaAPI = """
            {
                "standings": [
                    {
                        "table": [
                            {"position": 1}
                        ]
                    }
                ]
            }
        """;

        when(campeonatoRepository.findById(campeonato.getId())).thenReturn(Optional.of(campeonato));

        server.expect(requestTo("http://api.test/competitions/1/standings"))
                .andExpect(method(GET))
                .andRespond(withSuccess(respostaAPI, MediaType.APPLICATION_JSON));
        Long campeonatoId = campeonato.getId();

        assertThrows(ClassificacaoCampeonatoSyncException.class,
                () -> classificacaoCampeonatoService.sincronizarClassificacao(campeonatoId)
        );
    }

}
