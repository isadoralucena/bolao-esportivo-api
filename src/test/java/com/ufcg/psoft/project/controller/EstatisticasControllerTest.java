package com.ufcg.psoft.project.controller;

import com.ufcg.psoft.project.dto.estatisticas.EstatisticasResponseDTO;
import com.ufcg.psoft.project.service.estatisticas.EstatisticasService;
import com.ufcg.psoft.project.service.premium.RequisicaoAutenticadaEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitarios do EstatisticasController")
class EstatisticasControllerTest {

    @Mock
    private EstatisticasService estatisticasService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private EstatisticasController controller;

    @BeforeEach
    void setUp() {
        controller = new EstatisticasController(estatisticasService, eventPublisher);
    }

    @Test
    @DisplayName("Deve retornar a estatistica mais recente e publicar requisicao autenticada")
    void deveObterEstatisticaMaisRecente() {
        EstatisticasResponseDTO estatistica = EstatisticasResponseDTO.builder()
                .usuarioId(1L)
                .taxaAcerto(0.5f)
                .build();
        when(estatisticasService.obterEstatisticaMaisRecente(1L, "123456"))
                .thenReturn(estatistica);

        var resposta = controller.obterEstatisticas(1L, "123456");

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertSame(estatistica, resposta.getBody());
        verify(estatisticasService).obterEstatisticaMaisRecente(1L, "123456");
        verify(eventPublisher).publishEvent(new RequisicaoAutenticadaEvent(1L));
    }

    @Test
    @DisplayName("Deve retornar a evolucao e publicar requisicao autenticada")
    void deveObterEvolucaoDasEstatisticas() {
        List<EstatisticasResponseDTO> evolucao = List.of(
                EstatisticasResponseDTO.builder().usuarioId(1L).taxaAcerto(0.25f).build(),
                EstatisticasResponseDTO.builder().usuarioId(1L).taxaAcerto(0.5f).build()
        );
        when(estatisticasService.obterEvolucaoEstatisticas(1L, "123456"))
                .thenReturn(evolucao);

        var resposta = controller.obterEvolucao(1L, "123456");

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertSame(evolucao, resposta.getBody());
        verify(estatisticasService).obterEvolucaoEstatisticas(1L, "123456");
        verify(eventPublisher).publishEvent(new RequisicaoAutenticadaEvent(1L));
    }
}
