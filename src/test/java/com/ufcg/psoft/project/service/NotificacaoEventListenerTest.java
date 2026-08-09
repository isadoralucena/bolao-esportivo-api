package com.ufcg.psoft.project.service;

import com.ufcg.psoft.project.event.PalpitesAbertosEvent;
import com.ufcg.psoft.project.event.PalpitesFechadosEvent;
import com.ufcg.psoft.project.event.PartidaFinalizadaEvent;
import com.ufcg.psoft.project.event.PartidaIniciadaEvent;
import com.ufcg.psoft.project.model.Partida;
import com.ufcg.psoft.project.repository.PartidaRepository;
import com.ufcg.psoft.project.service.notificacao.NotificacaoEventListener;
import com.ufcg.psoft.project.service.notificacao.NotificacaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do listener de notificacoes")
class NotificacaoEventListenerTest {

    @Mock
    private NotificacaoService notificacaoService;

    @Mock
    private PartidaRepository partidaRepository;

    @InjectMocks
    private NotificacaoEventListener listener;

    private Partida partida;

    @BeforeEach
    void setUp() {
        partida = Partida.builder().id(1L).build();
        when(partidaRepository.findById(1L)).thenReturn(Optional.of(partida));
    }

    @Test
    void deveNotificarAberturaUsandoPartidaDoRepositorio() {
        listener.aoAbrirPalpites(new PalpitesAbertosEvent(this, 1L));

        verify(notificacaoService).notificarAberturaPalpites(partida);
    }

    @Test
    void deveNotificarFechamentoUsandoPartidaDoRepositorio() {
        listener.aoFecharPalpites(new PalpitesFechadosEvent(this, 1L));

        verify(notificacaoService).notificarFechamentoPalpites(partida);
    }

    @Test
    void deveNotificarInicioUsandoPartidaDoRepositorio() {
        listener.aoIniciarPartida(new PartidaIniciadaEvent(this, 1L));

        verify(notificacaoService).notificarInicioPartida(partida);
    }

    @Test
    void deveNotificarFinalizacaoUsandoPartidaDoRepositorio() {
        listener.aoFinalizarPartida(new PartidaFinalizadaEvent(this, 1L));

        verify(notificacaoService).notificarPartidaFinalizada(partida);
    }
}
