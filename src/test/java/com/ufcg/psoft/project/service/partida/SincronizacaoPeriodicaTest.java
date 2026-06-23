package com.ufcg.psoft.project.service.partida;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.ufcg.psoft.project.model.Campeonato;
import com.ufcg.psoft.project.repository.CampeonatoRepository;
import com.ufcg.psoft.project.service.campeonato.ClassificacaoCampeonatoService;
import com.ufcg.psoft.project.service.sincronizacaoPeriodica.SincronizacaoPeriodicaServiceImpl;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes da sincronização automática de partidas")
class SincronizacaoPeriodicaTest {

    @Mock
    private CampeonatoRepository campeonatoRepository;

    @Mock
    private PartidaService partidaService;

    @Mock
    private ClassificacaoCampeonatoService classificacaoCampeonatoService;

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
        InOrder inOrder = Mockito.inOrder(partidaService);

        inOrder.verify(partidaService).sincronizarPartidas(nuncaSincronizado);
        inOrder.verify(partidaService).sincronizarPartidas(sincronizadoMaisAntigo);

        verify(partidaService, never()).sincronizarPartidas(sincronizadoMaisRecente);
    }

    @Test
    @DisplayName("Quando não há campeonatos ativos, não sincroniza partidas")
    void quandoNaoHaCampeonatosAtivosNaoSincronizaPartidas() {
        // Arrange
        when(campeonatoRepository.findByAtivoTrue()).thenReturn(new ArrayList<>());

        // Act
        sincronizacaoPeriodicaService.sincronizarCampeonatosAtivos();

        // Assert
        verify(partidaService, never()).sincronizarPartidas(any(Campeonato.class));
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
        verify(partidaService, never()).sincronizarPartidas(any(Campeonato.class));
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
        verify(partidaService).sincronizarPartidas(campeonato1);
        verify(partidaService).sincronizarPartidas(campeonato2);
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