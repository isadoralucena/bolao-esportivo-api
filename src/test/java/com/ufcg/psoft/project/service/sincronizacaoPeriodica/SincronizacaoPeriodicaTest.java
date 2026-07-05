package com.ufcg.psoft.project.service.sincronizacaoPeriodica;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import com.ufcg.psoft.project.model.Campeonato;
import com.ufcg.psoft.project.repository.CampeonatoRepository;
import com.ufcg.psoft.project.service.campeonato.CampeonatoService;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Testes da sincronização periódica")
class SincronizacaoPeriodicaTest {

    @Mock
    private CampeonatoRepository campeonatoRepository;

    @Mock
    private CampeonatoService campeonatoService;

    @InjectMocks
    private SincronizacaoPeriodicaServiceImpl sincronizacaoPeriodicaService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(sincronizacaoPeriodicaService, "maxSincronizacoesPorCiclo", 2);
    }

    @Test
    @DisplayName("Quando sincroniza, ordena por ultimaSincronizacao e respeita a quota do ciclo")
    void quandoSincronizaOrdenaPorUltimaSincronizacaoERespeitaQuota() {
        // Arrange
        Campeonato nuncaSincronizado = campeonato(
            1L,
             "Nunca sincronizado",
              null);

        Campeonato sincronizadoMaisAntigo = campeonato(
                2L,
                "Sincronizado mais antigo",
                LocalDateTime.of(2026, 1, 1, 1, 1));

        Campeonato sincronizadoMaisRecente = campeonato(
                3L,
                "Sincronizado mais recente",
                LocalDateTime.of(2026, 6, 1, 1, 1));

        when(campeonatoRepository.findByAtivoTrue()).thenReturn(new ArrayList<>(List.of(
                sincronizadoMaisRecente,
                sincronizadoMaisAntigo,
                nuncaSincronizado
        )));

        // Act
        sincronizacaoPeriodicaService.sincronizarCampeonatosAtivos();

        // Assert
        InOrder inOrder = Mockito.inOrder(campeonatoService);

        inOrder.verify(campeonatoService).sincronizarCampeonato(nuncaSincronizado);
        inOrder.verify(campeonatoService).sincronizarCampeonato(sincronizadoMaisAntigo);

        verify(campeonatoService, never()).sincronizarCampeonato(sincronizadoMaisRecente);
    }

    @Test
    @DisplayName("Quando não há campeonatos ativos, não sincroniza nenhum campeonato")
    void quandoNaoHaCampeonatosAtivosNaoSincronizaNenhumCampeonato() {
        // Arrange
        when(campeonatoRepository.findByAtivoTrue()).thenReturn(new ArrayList<>());

        // Act
        sincronizacaoPeriodicaService.sincronizarCampeonatosAtivos();
        
        // Assert
        verify(campeonatoService, never()).sincronizarCampeonato(any(Campeonato.class));
    }

    @Test
    @DisplayName("Quando quota do ciclo é zero, não sincroniza nenhum campeonato")
    void quandoQuotaDoCicloEZeroNaoSincronizaNenhumCampeonato() {
        // Arrange
        ReflectionTestUtils.setField(sincronizacaoPeriodicaService, "maxSincronizacoesPorCiclo", 0);

        Campeonato campeonato = campeonato(
                1L,
                "Brasileirão",
                null);

        when(campeonatoRepository.findByAtivoTrue()).thenReturn(new ArrayList<>(List.of(campeonato)));

        
        // Act
        sincronizacaoPeriodicaService.sincronizarCampeonatosAtivos();

        // Assert
        verify(campeonatoService, never()).sincronizarCampeonato(any(Campeonato.class));
    }

    @Test
    @DisplayName("Quando quota é maior que a quantidade de campeonatos, sincroniza todos")
    void quandoQuotaMaiorQueQuantidadeDeCampeonatosSincronizaTodos() {
        // Arrange
        ReflectionTestUtils.setField(sincronizacaoPeriodicaService, "maxSincronizacoesPorCiclo", 10);

        Campeonato campeonato1 = campeonato(1L, "Campeonato 1", null);
        Campeonato campeonato2 = campeonato(2L, "Campeonato 2", LocalDateTime.of(2026, 1, 1, 1, 1));

        when(campeonatoRepository.findByAtivoTrue()).thenReturn(new ArrayList<>(List.of(campeonato1, campeonato2)));

        // Act
        sincronizacaoPeriodicaService.sincronizarCampeonatosAtivos();

        // Assert
        verify(campeonatoService).sincronizarCampeonato(campeonato1);
        verify(campeonatoService).sincronizarCampeonato(campeonato2);
    }


    @Test
    @DisplayName("Quando sincronização de campeonato falha, continua o ciclo")
    void quandoSincronizacaoDeCampeonatoFalhaContinuaOCiclo() {
        // Arrange
        Campeonato campeonatoComErro = campeonato(1L, "Campeonato com erro", null);
        campeonatoComErro.setUrl("http://api.test/competitions/1");

        Campeonato campeonatoSeguinte = campeonato(2L, "Campeonato seguinte", LocalDateTime.of(2026, 1, 1, 1, 1));
        campeonatoSeguinte.setUrl("http://api.test/competitions/2");

        when(campeonatoRepository.findByAtivoTrue()).thenReturn(new ArrayList<>(List.of(campeonatoComErro, campeonatoSeguinte)));
        doThrow(new RuntimeException("erro"))
                .when(campeonatoService)
                .sincronizarCampeonato(campeonatoComErro);

        // Act
        sincronizacaoPeriodicaService.sincronizarCampeonatosAtivos();

        // Assert
        verify(campeonatoService).sincronizarCampeonato(campeonatoComErro);
        verify(campeonatoService).sincronizarCampeonato(campeonatoSeguinte);
    }

    private Campeonato campeonato(Long id, String nome, LocalDateTime ultimaSincronizacao) {
        return Campeonato.builder()
                .id(id)
                .nome(nome)
                .ativo(true)
                .ultimaSincronizacao(ultimaSincronizacao)
                .build();
    }
}