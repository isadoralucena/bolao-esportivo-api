package com.ufcg.psoft.project.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEvent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@DisplayName("Serializacao dos eventos da aplicacao")
class EventosSerializacaoTest {

    @Test
    @DisplayName("Todos os eventos possuem somente estado serializavel")
    void deveSerializarTodosOsEventos() {
        Object sourceNaoSerializavel = new Object();
        List<ApplicationEvent> eventos = List.of(
                new PalpitesAbertosEvent(sourceNaoSerializavel, 1L),
                new PalpitesFechadosEvent(sourceNaoSerializavel, 1L),
                new PartidaIniciadaEvent(sourceNaoSerializavel, 1L),
                new PartidaFinalizadaEvent(sourceNaoSerializavel, 1L),
                new PartidaConsolidadaEvent(sourceNaoSerializavel, 1L),
                new RankingAtualizadoEvent(sourceNaoSerializavel, 2L),
                new MudancaGrupoPosicaoEvent(sourceNaoSerializavel, "Usuario", 2, 1, 2L)
        );

        for (ApplicationEvent evento : eventos) {
            assertDoesNotThrow(() -> serializar(evento));
        }
    }

    private static void serializar(ApplicationEvent evento) throws IOException {
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
             ObjectOutputStream output = new ObjectOutputStream(bytes)) {
            output.writeObject(evento);
        }
    }
}
