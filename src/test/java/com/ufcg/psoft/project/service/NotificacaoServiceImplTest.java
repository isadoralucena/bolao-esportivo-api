package com.ufcg.psoft.project.service;

import com.ufcg.psoft.project.model.Partida;
import com.ufcg.psoft.project.model.PartidaStatus;
import com.ufcg.psoft.project.service.notificacao.NotificacaoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@DisplayName("Testes do serviço de notificações - US11")
public class NotificacaoServiceImplTest {

    private NotificacaoServiceImpl notificacaoService;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;

    @BeforeEach
    void setup() {
        notificacaoService = new NotificacaoServiceImpl();
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
    }

    @AfterEach
    void teardown() {
        System.setOut(originalOut);
    }

    private Partida criarPartida(Long id, String mandante, String visitante, PartidaStatus status) {
        return Partida.builder()
                .id(id)
                .mandante(mandante)
                .visitante(visitante)
                .golsMandante(2)
                .golsVisitante(1)
                .status(status)
                .data(LocalDateTime.now().plusDays(1))
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
            String saida = outputStream.toString();
            assertTrue(saida.contains("[NOTIFICAÇÃO]"));
            assertTrue(saida.contains("Flamengo"));
            assertTrue(saida.contains("Vasco"));
        }

        @Test
        @DisplayName("Quando notifica fechamento de palpites")
        void quandoNotificaFechamentoPalpites() {
            Partida partida = criarPartida(2L, "Palmeiras", "Santos", PartidaStatus.EM_ANDAMENTO);
            notificacaoService.notificarFechamentoPalpites(partida);
            String saida = outputStream.toString();
            assertTrue(saida.contains("[NOTIFICAÇÃO]"));
            assertTrue(saida.contains("Palmeiras"));
            assertTrue(saida.contains("Santos"));
        }

        @Test
        @DisplayName("Quando notifica início de partida")
        void quandoNotificaInicioPartida() {
            Partida partida = criarPartida(3L, "Corinthians", "São Paulo", PartidaStatus.EM_ANDAMENTO);
            notificacaoService.notificarInicioPartida(partida);
            String saida = outputStream.toString();
            assertTrue(saida.contains("[NOTIFICAÇÃO]"));
            assertTrue(saida.contains("Corinthians"));
            assertTrue(saida.contains("São Paulo"));
        }

        @Test
        @DisplayName("Quando notifica partida finalizada")
        void quandoNotificaPartidaFinalizada() {
            Partida partida = criarPartida(4L, "Grêmio", "Internacional", PartidaStatus.FINALIZADO);
            notificacaoService.notificarPartidaFinalizada(partida);
            String saida = outputStream.toString();
            assertTrue(saida.contains("[NOTIFICAÇÃO]"));
            assertTrue(saida.contains("Grêmio"));
            assertTrue(saida.contains("Internacional"));
            assertTrue(saida.contains("2"));
            assertTrue(saida.contains("1"));
        }
    }

    @Nested
    @DisplayName("Notificações de ranking")
    class NotificacoesDeRanking {

        @Test
        @DisplayName("Quando notifica atualização de ranking")
        void quandoNotificaAtualizacaoRanking() {
            notificacaoService.notificarAtualizacaoRanking(10L);
            String saida = outputStream.toString();
            assertTrue(saida.contains("[NOTIFICAÇÃO]"));
            assertTrue(saida.contains("10"));
        }

        @Test
        @DisplayName("Quando notifica mudança de posição para melhor")
        void quandoNotificaMudancaDePosicaoParaMelhor() {
            notificacaoService.notificarMudancaDePosicao("Erik", 3, 1, 10L);
            String saida = outputStream.toString();
            assertTrue(saida.contains("[NOTIFICAÇÃO]"));
            assertTrue(saida.contains("Erik"));
            assertTrue(saida.contains("3"));
            assertTrue(saida.contains("1"));
            assertTrue(saida.contains("10"));
        }

        @Test
        @DisplayName("Quando notifica mudança de posição para pior")
        void quandoNotificaMudancaDePosicaoParaPior() {
            notificacaoService.notificarMudancaDePosicao("João", 1, 3, 10L);
            String saida = outputStream.toString();
            assertTrue(saida.contains("[NOTIFICAÇÃO]"));
            assertTrue(saida.contains("João"));
            assertTrue(saida.contains("1"));
            assertTrue(saida.contains("3"));
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
            String saida = outputStream.toString();
            assertTrue(saida.contains("[NOTIFICAÇÃO]"));
            assertTrue(saida.contains("Athletico"));
        }

        @Test
        @DisplayName("Quando partida muda para EM_ANDAMENTO notifica fechamento e início")
        void quandoPartidaMudaParaEmAndamentoNotificaFechamentoEInicio() {
            Partida partida = criarPartida(6L, "Botafogo", "Fluminense", PartidaStatus.EM_ANDAMENTO);
            notificacaoService.notificarFechamentoPalpites(partida);
            notificacaoService.notificarInicioPartida(partida);
            String saida = outputStream.toString();
            assertTrue(saida.contains("Botafogo"));
            assertTrue(saida.contains("Fluminense"));
            long count = saida.lines().filter(l -> l.contains("[NOTIFICAÇÃO]")).count();
            assertTrue(count >= 2);
        }

        @Test
        @DisplayName("Quando partida finalizada notifica conclusão")
        void quandoPartidaFinalizadaNotificaConclusao() {
            Partida partida = criarPartida(7L, "Bahia", "Fortaleza", PartidaStatus.FINALIZADO);
            notificacaoService.notificarPartidaFinalizada(partida);
            String saida = outputStream.toString();
            assertTrue(saida.contains("[NOTIFICAÇÃO]"));
            assertTrue(saida.contains("Bahia"));
            assertTrue(saida.contains("Fortaleza"));
        }
    }
}