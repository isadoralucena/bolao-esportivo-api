package com.ufcg.psoft.project.service;

import static com.ufcg.psoft.project.config.TestClockConfig.FIXED_CLOCK;

import com.ufcg.psoft.project.model.Partida;
import com.ufcg.psoft.project.model.PartidaStatus;
import com.ufcg.psoft.project.service.notificacao.NotificacaoService;
import com.ufcg.psoft.project.service.notificacao.NotificacaoServiceImpl;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes do serviço de notificações - US11")
class NotificacaoServiceImplTest {

    private static final String PREFIXO_NOTIFICACAO = "[NOTIFICAÇÃO]";

    private final NotificacaoService notificacaoService = new NotificacaoServiceImpl();
    private Logger logbackLogger;
    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setup() {
        logbackLogger = (Logger) LoggerFactory.getLogger(NotificacaoServiceImpl.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logbackLogger.addAppender(listAppender);
    }

    @AfterEach
    void teardown() {
        logbackLogger.detachAppender(listAppender);
        listAppender.stop();
    }

    private String logsCapturados() {
        return listAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .collect(Collectors.joining(System.lineSeparator()));
    }

    private Partida criarPartida(Long id, String mandante, String visitante, PartidaStatus status) {
        return Partida.builder()
                .id(id)
                .mandante(mandante)
                .visitante(visitante)
                .golsMandante(2)
                .golsVisitante(1)
                .status(status)
                .data(LocalDateTime.now(FIXED_CLOCK).plusDays(1))
                .codigoExterno(id)
                .build();
    }

    @Nested
    @DisplayName("Notificações de eventos de partida")
    class NotificacoesDePartida {

        @Test
        @DisplayName("Quando notifica abertura de palpites")
        void quandoNotificaAberturaPalpites() {
            Partida partida = criarPartida(1L, "Flamengo", "Vasco", PartidaStatus.ABERTO);
            notificacaoService.notificarAberturaPalpites(partida);
            assertThat(logsCapturados())
                    .contains(PREFIXO_NOTIFICACAO, "Flamengo", "Vasco");
        }

        @Test
        @DisplayName("Quando notifica fechamento de palpites")
        void quandoNotificaFechamentoPalpites() {
            Partida partida = criarPartida(2L, "Palmeiras", "Santos", PartidaStatus.EM_ANDAMENTO);
            notificacaoService.notificarFechamentoPalpites(partida);
            assertThat(logsCapturados())
                    .contains(PREFIXO_NOTIFICACAO, "Palmeiras", "Santos");
        }

        @Test
        @DisplayName("Quando notifica início de partida")
        void quandoNotificaInicioPartida() {
            Partida partida = criarPartida(3L, "Corinthians", "São Paulo", PartidaStatus.EM_ANDAMENTO);
            notificacaoService.notificarInicioPartida(partida);
            assertThat(logsCapturados())
                    .contains(PREFIXO_NOTIFICACAO, "Corinthians", "São Paulo");
        }

        @Test
        @DisplayName("Quando notifica partida finalizada")
        void quandoNotificaPartidaFinalizada() {
            Partida partida = criarPartida(4L, "Grêmio", "Internacional", PartidaStatus.FINALIZADO);
            notificacaoService.notificarPartidaFinalizada(partida);
            assertThat(logsCapturados())
                    .contains(PREFIXO_NOTIFICACAO, "Grêmio", "Internacional", "2", "1");
        }
    }

    @Nested
    @DisplayName("Notificações de ranking")
    class NotificacoesDeRanking {

        @Test
        @DisplayName("Quando notifica atualização de ranking")
        void quandoNotificaAtualizacaoRanking() {
            notificacaoService.notificarAtualizacaoRanking(10L);
            assertThat(logsCapturados())
                    .contains(PREFIXO_NOTIFICACAO, "10");
        }

        @Test
        @DisplayName("Quando notifica mudança de posição para melhor")
        void quandoNotificaMudancaDePosicaoParaMelhor() {
            notificacaoService.notificarMudancaDePosicao("Erik", 3, 1, 10L);
            assertThat(logsCapturados())
                    .contains(PREFIXO_NOTIFICACAO, "Erik", "3", "1", "10");
        }

        @Test
        @DisplayName("Quando notifica mudança de posição para pior")
        void quandoNotificaMudancaDePosicaoParaPior() {
            notificacaoService.notificarMudancaDePosicao("João", 1, 3, 10L);
            assertThat(logsCapturados())
                    .contains(PREFIXO_NOTIFICACAO, "João", "1", "3", "10");
        }
    }

    @Nested
    @DisplayName("Notificações disparadas durante sincronização de partidas")
    class NotificacoesDeSincronizacao {

        @Test
        @DisplayName("Quando partida nova é criada com status ABERTO notifica abertura de palpites")
        void quandoPartidaNovaAbertaNotificaAbertura() {
            Partida partida = criarPartida(5L, "Athletico", "Atletico MG", PartidaStatus.ABERTO);
            notificacaoService.notificarAberturaPalpites(partida);
            assertThat(logsCapturados())
                    .contains(PREFIXO_NOTIFICACAO, "Athletico", "Atletico MG");
        }

        @Test
        @DisplayName("Quando partida muda para EM_ANDAMENTO notifica fechamento e início")
        void quandoPartidaMudaParaEmAndamentoNotificaFechamentoEInicio() {
            Partida partida = criarPartida(6L, "Botafogo", "Fluminense", PartidaStatus.EM_ANDAMENTO);
            notificacaoService.notificarFechamentoPalpites(partida);
            notificacaoService.notificarInicioPartida(partida);
            assertThat(logsCapturados()).contains("Botafogo", "Fluminense");
            long quantidadeDeNotificacoes = listAppender.list.stream()
                    .map(ILoggingEvent::getFormattedMessage)
                    .filter(mensagem -> mensagem.contains(PREFIXO_NOTIFICACAO))
                    .count();
            assertThat(quantidadeDeNotificacoes).isEqualTo(2);
        }

        @Test
        @DisplayName("Quando partida finalizada notifica conclusão")
        void quandoPartidaFinalizadaNotificaConclusao() {
            Partida partida = criarPartida(7L, "Bahia", "Fortaleza", PartidaStatus.FINALIZADO);
            notificacaoService.notificarPartidaFinalizada(partida);
            assertThat(logsCapturados())
                    .contains(PREFIXO_NOTIFICACAO, "Bahia", "Fortaleza");
        }
    }
}
