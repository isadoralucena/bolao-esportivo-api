package com.ufcg.psoft.project.comparator;

import com.ufcg.psoft.project.dto.pontuacao.PontuacaoParticipanteResponseDTO;
import com.ufcg.psoft.project.model.TipoCriterioDesempate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Testes do builder de comparador de critérios de desempate")
public class ComparadorCriterioDesempateBuilderTest {

    private PontuacaoParticipanteResponseDTO participante(
            String nome, int placaresExatos, int erros, int acertosVencedor, int acertosEmpate) {
        return PontuacaoParticipanteResponseDTO.builder()
                .usuarioNome(nome)
                .placaresExatos(placaresExatos)
                .erros(erros)
                .acertosVencedor(acertosVencedor)
                .acertosEmpate(acertosEmpate)
                .build();
    }

    @Nested
    @DisplayName("Comportamento com um único critério")
    class UnicoCriterio {

        @Test
        @DisplayName("PLACAR_EXATO ordena do maior para o menor número de placares exatos")
        void placarExatoOrdenaDecrescente() {
            PontuacaoParticipanteResponseDTO a = participante("A", 5, 0, 0, 0);
            PontuacaoParticipanteResponseDTO b = participante("B", 2, 0, 0, 0);
            PontuacaoParticipanteResponseDTO c = participante("C", 8, 0, 0, 0);

            Comparator<PontuacaoParticipanteResponseDTO> comparador = ComparadorCriterioDesempateBuilder.builder()
                    .comCriterios(List.of(TipoCriterioDesempate.PLACAR_EXATO))
                    .build();

            List<PontuacaoParticipanteResponseDTO> ordenados = new ArrayList<>(List.of(a, b, c));
            ordenados.sort(comparador);

            assertEquals(List.of("C", "A", "B"), nomes(ordenados));
        }

        @Test
        @DisplayName("ERRO ordena do menor para o maior número de erros")
        void erroOrdenaCrescente() {
            PontuacaoParticipanteResponseDTO a = participante("A", 0, 3, 0, 0);
            PontuacaoParticipanteResponseDTO b = participante("B", 0, 1, 0, 0);
            PontuacaoParticipanteResponseDTO c = participante("C", 0, 5, 0, 0);

            Comparator<PontuacaoParticipanteResponseDTO> comparador = ComparadorCriterioDesempateBuilder.builder()
                    .comCriterios(List.of(TipoCriterioDesempate.ERRO))
                    .build();

            List<PontuacaoParticipanteResponseDTO> ordenados = new ArrayList<>(List.of(a, b, c));
            ordenados.sort(comparador);

            assertEquals(List.of("B", "A", "C"), nomes(ordenados));
        }

        @Test
        @DisplayName("ACERTO_VENCEDOR ordena do maior para o menor número de acertos")
        void acertoVencedorOrdenaDecrescente() {
            PontuacaoParticipanteResponseDTO a = participante("A", 0, 0, 4, 0);
            PontuacaoParticipanteResponseDTO b = participante("B", 0, 0, 9, 0);
            PontuacaoParticipanteResponseDTO c = participante("C", 0, 0, 1, 0);

            Comparator<PontuacaoParticipanteResponseDTO> comparador = ComparadorCriterioDesempateBuilder.builder()
                    .comCriterios(List.of(TipoCriterioDesempate.ACERTO_VENCEDOR))
                    .build();

            List<PontuacaoParticipanteResponseDTO> ordenados = new ArrayList<>(List.of(a, b, c));
            ordenados.sort(comparador);

            assertEquals(List.of("B", "A", "C"), nomes(ordenados));
        }

        @Test
        @DisplayName("ACERTO_EMPATE ordena do maior para o menor número de acertos")
        void acertoEmpateOrdenaDecrescente() {
            PontuacaoParticipanteResponseDTO a = participante("A", 0, 0, 0, 2);
            PontuacaoParticipanteResponseDTO b = participante("B", 0, 0, 0, 7);
            PontuacaoParticipanteResponseDTO c = participante("C", 0, 0, 0, 4);

            Comparator<PontuacaoParticipanteResponseDTO> comparador = ComparadorCriterioDesempateBuilder.builder()
                    .comCriterios(List.of(TipoCriterioDesempate.ACERTO_EMPATE))
                    .build();

            List<PontuacaoParticipanteResponseDTO> ordenados = new ArrayList<>(List.of(a, b, c));
            ordenados.sort(comparador);

            assertEquals(List.of("B", "C", "A"), nomes(ordenados));
        }
    }

    @Nested
    @DisplayName("Comportamento com múltiplos critérios encadeados")
    class MultiplosCriterios {

        @Test
        @DisplayName("O primeiro critério da lista tem prioridade sobre os seguintes")
        void primeiroCriterioTemPrioridade() {
            // A e B empatam em placares exatos; o desempate deve ser por ERRO (segundo critério)
            PontuacaoParticipanteResponseDTO a = participante("A", 3, 5, 0, 0);
            PontuacaoParticipanteResponseDTO b = participante("B", 3, 1, 0, 0);
            PontuacaoParticipanteResponseDTO c = participante("C", 1, 0, 0, 0);

            Comparator<PontuacaoParticipanteResponseDTO> comparador = ComparadorCriterioDesempateBuilder.builder()
                    .comCriterios(List.of(
                            TipoCriterioDesempate.PLACAR_EXATO,
                            TipoCriterioDesempate.ERRO))
                    .build();

            List<PontuacaoParticipanteResponseDTO> ordenados = new ArrayList<>(List.of(a, b, c));
            ordenados.sort(comparador);

            // B vem antes de A porque tem menos erros, mesmo com o mesmo nº de placares exatos
            assertEquals(List.of("B", "A", "C"), nomes(ordenados));
        }

        @Test
        @DisplayName("Inverter a ordem dos critérios na lista muda o resultado do desempate")
        void ordemDosCriteriosAlteraResultado() {
            PontuacaoParticipanteResponseDTO a = participante("A", 3, 5, 0, 0);
            PontuacaoParticipanteResponseDTO b = participante("B", 1, 1, 0, 0);

            Comparator<PontuacaoParticipanteResponseDTO> priorizaPlacarExato = ComparadorCriterioDesempateBuilder.builder()
                    .comCriterios(List.of(TipoCriterioDesempate.PLACAR_EXATO, TipoCriterioDesempate.ERRO))
                    .build();

            Comparator<PontuacaoParticipanteResponseDTO> priorizaErro = ComparadorCriterioDesempateBuilder.builder()
                    .comCriterios(List.of(TipoCriterioDesempate.ERRO, TipoCriterioDesempate.PLACAR_EXATO))
                    .build();

            List<PontuacaoParticipanteResponseDTO> ordenadoPorPlacar = new ArrayList<>(List.of(a, b));
            ordenadoPorPlacar.sort(priorizaPlacarExato);

            List<PontuacaoParticipanteResponseDTO> ordenadoPorErro = new ArrayList<>(List.of(a, b));
            ordenadoPorErro.sort(priorizaErro);

            assertEquals(List.of("A", "B"), nomes(ordenadoPorPlacar)); // A vence por ter mais placares exatos
            assertEquals(List.of("B", "A"), nomes(ordenadoPorErro));  // B vence por ter menos erros
        }

        @Test
        @DisplayName("Encadeamento de três critérios resolve empates em cascata")
        void encadeamentoDeTresCriteriosResolveEmpateEmCascata() {
            // Empatam em PLACAR_EXATO e ERRO; desempate final por ACERTO_VENCEDOR
            PontuacaoParticipanteResponseDTO a = participante("A", 2, 3, 4, 0);
            PontuacaoParticipanteResponseDTO b = participante("B", 2, 3, 9, 0);

            Comparator<PontuacaoParticipanteResponseDTO> comparador = ComparadorCriterioDesempateBuilder.builder()
                    .comCriterios(List.of(
                            TipoCriterioDesempate.PLACAR_EXATO,
                            TipoCriterioDesempate.ERRO,
                            TipoCriterioDesempate.ACERTO_VENCEDOR))
                    .build();

            List<PontuacaoParticipanteResponseDTO> ordenados = new ArrayList<>(List.of(a, b));
            ordenados.sort(comparador);

            assertEquals(List.of("B", "A"), nomes(ordenados));
        }
    }

    @Nested
    @DisplayName("Casos limite")
    class CasosLimite {

        @Test
        @DisplayName("Sem critérios configurados, o comparador considera todos equivalentes")
        void semCriteriosConsideraTodosEquivalentes() {
            Comparator<PontuacaoParticipanteResponseDTO> comparador = ComparadorCriterioDesempateBuilder.builder()
                    .build();

            PontuacaoParticipanteResponseDTO a = participante("A", 10, 0, 10, 10);
            PontuacaoParticipanteResponseDTO b = participante("B", 0, 10, 0, 0);

            assertEquals(0, comparador.compare(a, b));
            assertEquals(0, comparador.compare(b, a));
        }

        @Test
        @DisplayName("Participantes totalmente empatados em todos os critérios permanecem em ordem estável")
        void empateTotalMantemOrdemEstavel() {
            PontuacaoParticipanteResponseDTO a = participante("A", 3, 1, 2, 1);
            PontuacaoParticipanteResponseDTO b = participante("B", 3, 1, 2, 1);

            Comparator<PontuacaoParticipanteResponseDTO> comparador = ComparadorCriterioDesempateBuilder.builder()
                    .comCriterios(List.of(
                            TipoCriterioDesempate.PLACAR_EXATO,
                            TipoCriterioDesempate.ERRO,
                            TipoCriterioDesempate.ACERTO_VENCEDOR,
                            TipoCriterioDesempate.ACERTO_EMPATE))
                    .build();

            List<PontuacaoParticipanteResponseDTO> ordenados = new ArrayList<>(List.of(a, b));
            ordenados.sort(comparador);

            // Collections.sort no Java é estável: ordem original é preservada em caso de empate
            assertEquals(List.of("A", "B"), nomes(ordenados));
        }
    }

    private List<String> nomes(List<PontuacaoParticipanteResponseDTO> lista) {
        return lista.stream().map(PontuacaoParticipanteResponseDTO::getUsuarioNome).toList();
    }
}