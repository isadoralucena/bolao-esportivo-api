package com.ufcg.psoft.project.service;

import static com.ufcg.psoft.project.config.TestClockConfig.FIXED_CLOCK;

import com.ufcg.psoft.project.repository.GrupoRepository;
import com.ufcg.psoft.project.repository.PalpiteRepository;
import com.ufcg.psoft.project.repository.PontuacaoPalpiteRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import org.springframework.boot.test.context.SpringBootTest;

import com.ufcg.psoft.project.controller.CampeonatoController;
import com.ufcg.psoft.project.dto.estatisticas.EstatisticasResponseDTO;
import com.ufcg.psoft.project.dto.pontuacao.PontuacaoParticipanteResponseDTO;
import com.ufcg.psoft.project.dto.ranking.RankingEntryResponseDTO;
import com.ufcg.psoft.project.dto.ranking.RankingResponseDTO;
import com.ufcg.psoft.project.event.PartidaConsolidadaEvent;
import com.ufcg.psoft.project.exception.estatistica.EstatisticaNaoExisteException;
import com.ufcg.psoft.project.model.Estatisticas;
import com.ufcg.psoft.project.model.Grupo;
import com.ufcg.psoft.project.model.Palpite;
import com.ufcg.psoft.project.model.Partida;
import com.ufcg.psoft.project.model.PontuacaoPalpite;
import com.ufcg.psoft.project.model.Usuario;
import com.ufcg.psoft.project.repository.EstatisticasRepository;
import com.ufcg.psoft.project.service.estatisticas.EstatisticasServiceImpl;
import com.ufcg.psoft.project.service.grupo.GrupoAutorizacaoService;
import com.ufcg.psoft.project.service.pontuacao.PontuacaoService;
import com.ufcg.psoft.project.service.ranking.RankingService;

@SpringBootTest
@DisplayName("Testes do EstatisticasServiceImpl")
class EstatisticasServiceImplTest {

    @Mock
    CampeonatoController campeonatoController;

    @Mock
    GrupoRepository grupoRepository;

    @Mock
    PalpiteRepository palpiteRepository;

    @Mock
    EstatisticasRepository estatisticasRepository;

    @Mock
    GrupoAutorizacaoService grupoAutorizacaoService;

    @Mock
    PontuacaoService pontuacaoService;

    @Mock
    RankingService rankingService;

    @Mock
    PontuacaoPalpiteRepository pontuacaoPalpiteRepository;

    EstatisticasServiceImpl estatisticasService;

    @BeforeEach
    void setUp() {
        estatisticasService = new EstatisticasServiceImpl(
                campeonatoController,
                grupoRepository,
                palpiteRepository,
                estatisticasRepository,
                grupoAutorizacaoService,
                pontuacaoService,
                rankingService,
                pontuacaoPalpiteRepository,
                FIXED_CLOCK
        );
    }

    @Test
    @DisplayName("Calcula taxa de acerto")
    void quandoPossuiUmAcertoEmDoisPalpitesCalculaTaxaDeCinquentaPorCento() {
        // Arrange: o usuário possui dois palpites avaliados, sendo um acerto e um erro.
        Usuario usuario = Usuario.builder()
                .id(1L)
                .build();

        Grupo grupo = Grupo.builder()
                .id(1L)
                .build();

        grupo.getParticipantes().add(usuario);

        Partida partida2 = Partida.builder()
                .id(2L)
                .build();

        Palpite palpite2 = Palpite.builder()
                .grupo(grupo)
                .build();

        PontuacaoParticipanteResponseDTO pontuacao = PontuacaoParticipanteResponseDTO.builder()
                .usuarioId(1L)
                .totalPalpitesAvaliados(2)
                .erros(1)
                .build();

        when(palpiteRepository.findByPartidaId(2L))
                .thenReturn(List.of(palpite2));

        when(pontuacaoService.calcularPontuacaoGlobalDoParticipante(1L))
                .thenReturn(pontuacao);

        // Act
        EstatisticasResponseDTO resultado = estatisticasService.calcularEstatisticasAssociadasAPartida(partida2.getId()).get(0);

        // Assert: um acerto entre dois palpites corresponde a uma taxa de 50%.
        assertEquals(0.5f, resultado.getTaxaAcerto(), 0.0001f);
        assertEquals(LocalDateTime.now(FIXED_CLOCK), resultado.getDataRegistro());
    }

    @Test
    @DisplayName("Calcula total de palpites corretos")
    void quandoPossuiUmErroEmQuatroPalpitesCalculaTresCorretos() {
        // Arrange: o usuário possui quatro palpites avaliados, com três acertos e um erro.
        Usuario usuario = Usuario.builder()
                .id(1L)
                .build();

        Grupo grupo = Grupo.builder()
                .id(1L)
                .build();

        grupo.getParticipantes().add(usuario);

        Partida partida4 = Partida.builder()
                .id(4L)
                .build();

        Palpite palpite4 = Palpite.builder()
                .grupo(grupo)
                .build();

        PontuacaoParticipanteResponseDTO pontuacao = PontuacaoParticipanteResponseDTO.builder()
                .usuarioId(1L)
                .totalPalpitesAvaliados(4)
                .erros(1)
                .build();

        when(palpiteRepository.findByPartidaId(4L))
                .thenReturn(List.of(palpite4));

        when(pontuacaoService.calcularPontuacaoGlobalDoParticipante(1L))
                .thenReturn(pontuacao);

        // Act
        EstatisticasResponseDTO resultado =
                estatisticasService.calcularEstatisticasAssociadasAPartida(partida4.getId()).get(0);

        // Assert
        assertEquals(3, resultado.getTotalPalpitesCorretos());
    }

    @Test
    @DisplayName("Calcula taxa zero quando não existem palpites avaliados")
    void quandoParticipanteNaoPossuiPalpitesAvaliadosTaxaEhZero() {
        // Arrange: o usuário 2 pertence ao grupo afetado, mas não possui palpites avaliados.
        Usuario usuario1 = Usuario.builder()
                .id(1L)
                .build();

        Usuario usuario2 = Usuario.builder()
                .id(2L)
                .build();

        Grupo grupo = Grupo.builder()
                .id(1L)
                .build();

        grupo.getParticipantes().add(usuario1);
        grupo.getParticipantes().add(usuario2);

        Partida partida1 = Partida.builder()
                .id(1L)
                .build();

        Palpite palpite1 = Palpite.builder()
                .grupo(grupo)
                .build();

        PontuacaoParticipanteResponseDTO pontuacaoUsuario1 =
                PontuacaoParticipanteResponseDTO.builder()
                        .usuarioId(1L)
                        .totalPalpitesAvaliados(1)
                        .build();

        PontuacaoParticipanteResponseDTO pontuacaoUsuario2 =
                PontuacaoParticipanteResponseDTO.builder()
                        .usuarioId(2L)
                        .totalPalpitesAvaliados(0)
                        .build();

        when(palpiteRepository.findByPartidaId(1L))
                .thenReturn(List.of(palpite1));

        when(pontuacaoService.calcularPontuacaoGlobalDoParticipante(1L))
                .thenReturn(pontuacaoUsuario1);

        when(pontuacaoService.calcularPontuacaoGlobalDoParticipante(2L))
                .thenReturn(pontuacaoUsuario2);

        // Act
        List<EstatisticasResponseDTO> resultado =
                estatisticasService.calcularEstatisticasAssociadasAPartida(partida1.getId());

        EstatisticasResponseDTO estatisticaUsuario2 = resultado.stream()
                .filter(e -> e.getUsuarioId().equals(2L))
                .findFirst()
                .orElseThrow();

        // Assert
        assertEquals(0f, estatisticaUsuario2.getTaxaAcerto());
        assertEquals(0, estatisticaUsuario2.getTotalPalpitesCorretos());
    }

    @Test
    @DisplayName("Calcula quantidade de placares exatos")
    void quandoPossuiDoisPlacaresExatosCalculaQuantidadeCorretamente() {
        // Arrange: os dois palpites avaliados possuem acerto de placar exato.
        Usuario usuario = Usuario.builder()
                .id(1L)
                .build();

        Grupo grupo = Grupo.builder()
                .id(1L)
                .build();

        grupo.getParticipantes().add(usuario);

        Partida partida2 = Partida.builder()
                .id(2L)
                .build();

        Palpite palpite2 = Palpite.builder()
                .grupo(grupo)
                .build();

        PontuacaoParticipanteResponseDTO pontuacao = PontuacaoParticipanteResponseDTO.builder()
                .usuarioId(1L)
                .totalPalpitesAvaliados(2)
                .placaresExatos(2)
                .build();

        when(palpiteRepository.findByPartidaId(2L))
                .thenReturn(List.of(palpite2));

        when(pontuacaoService.calcularPontuacaoGlobalDoParticipante(1L))
                .thenReturn(pontuacao);

        // Act
        EstatisticasResponseDTO resultado = estatisticasService.calcularEstatisticasAssociadasAPartida(partida2.getId()).get(0);

        // Assert
        assertEquals(2, resultado.getPlacaresExatos());
    }

    @Test
    @DisplayName("Calcula maior sequência de acertos")
    void quandoPossuiTresAcertosConsecutivosCalculaMaiorSequencia() {
        // Arrange: o histórico contém três acertos, um erro e depois um novo acerto.
        Usuario usuario = Usuario.builder()
                .id(1L)
                .build();

        Grupo grupo = Grupo.builder()
                .id(1L)
                .build();

        grupo.getParticipantes().add(usuario);

        Partida partida5 = Partida.builder()
                .id(5L)
                .build();

        Palpite palpite5 = Palpite.builder()
                .grupo(grupo)
                .build();

        PontuacaoPalpite pontuacao1 = PontuacaoPalpite.builder()
                .acertouEmpate(true)
                .build();

        PontuacaoPalpite pontuacao2 = PontuacaoPalpite.builder()
                .acertouPlacarExato(true)
                .build();

        PontuacaoPalpite pontuacao3 = PontuacaoPalpite.builder()
                .acertouVencedor(true)
                .build();

        PontuacaoPalpite pontuacao4 = PontuacaoPalpite.builder()
                .build();

        PontuacaoPalpite pontuacao5 = PontuacaoPalpite.builder()
                .acertouVencedor(true)
                .build();

        PontuacaoParticipanteResponseDTO pontuacao = PontuacaoParticipanteResponseDTO.builder()
                .usuarioId(1L)
                .totalPalpitesAvaliados(5)
                .erros(1)
                .build();

        when(palpiteRepository.findByPartidaId(5L))
                .thenReturn(List.of(palpite5));

        when(pontuacaoService.calcularPontuacaoGlobalDoParticipante(1L))
                .thenReturn(pontuacao);

        when(pontuacaoPalpiteRepository
                .findByPalpite_Usuario_IdOrderByPalpite_DataAsc(1L))
                .thenReturn(List.of(pontuacao1, pontuacao2, pontuacao3, pontuacao4, pontuacao5
            ));

        // Act
        EstatisticasResponseDTO resultado = estatisticasService.calcularEstatisticasAssociadasAPartida(partida5.getId()).get(0);

        // Assert
        assertEquals(3, resultado.getMaiorSequenciaAcertos());
    }

    @Test
    @DisplayName("Conta vitórias em rankings")
    void quandoUsuarioVenceUmDeDoisRankingsCalculaUmaVitoria() {
        // Arrange: o usuário perde o ranking do grupo 1 e vence o ranking do grupo 2.
        Usuario usuario = Usuario.builder()
                .id(1L)
                .codigo("123456")
                .build();

        Grupo grupoAfetado = Grupo.builder()
                .build();

        grupoAfetado.getParticipantes().add(usuario);

        Grupo grupo1 = Grupo.builder()
                .id(1L)
                .build();

        Grupo grupo2 = Grupo.builder()
                .id(2L)
                .build();

        Partida partida1 = Partida.builder()
                .id(1L)
                .build();

        Palpite palpite1 = Palpite.builder()
                .grupo(grupoAfetado)
                .build();

        PontuacaoParticipanteResponseDTO pontuacaoUsuario =
                PontuacaoParticipanteResponseDTO.builder()
                        .usuarioId(1L)
                        .build();

        RankingResponseDTO rankingGrupo1 = RankingResponseDTO.builder()
                .grupoId(1L)
                .rankingEntrys(List.of(
                        new RankingEntryResponseDTO(2, pontuacaoUsuario)
                ))
                .build();

        RankingResponseDTO rankingGrupo2 = RankingResponseDTO.builder()
                .grupoId(2L)
                .rankingEntrys(List.of(
                        new RankingEntryResponseDTO(1, pontuacaoUsuario)
                ))
                .build();

        when(palpiteRepository.findByPartidaId(1L))
                .thenReturn(List.of(palpite1));

        when(pontuacaoService.calcularPontuacaoGlobalDoParticipante(1L))
                .thenReturn(pontuacaoUsuario);

        when(grupoRepository.findByParticipantes_Id(1L))
                .thenReturn(List.of(grupo1, grupo2));

        when(rankingService.rankingDoGrupo(1L, 1L, "123456"))
                .thenReturn(rankingGrupo1);

        when(rankingService.rankingDoGrupo(2L, 1L, "123456"))
                .thenReturn(rankingGrupo2);

        // Act
        EstatisticasResponseDTO resultado = estatisticasService.calcularEstatisticasAssociadasAPartida(partida1.getId()).get(0);

        // Assert
        assertEquals(1, resultado.getVitoriasRankings());
    }

    @Test
    @DisplayName("Atualiza todos os participantes sem duplicar chamadas")
    void quandoPartidaAfetaMaisDeUmGrupoAtualizaCadaUsuarioUmaUnicaVez() {
        // Arrange: dois grupos são afetados e os mesmos usuários aparecem neles.
        Usuario usuario1 = Usuario.builder()
                .id(1L)
                .build();

        Usuario usuario2 = Usuario.builder()
                .id(2L)
                .build();

        Grupo grupo1 = Grupo.builder()
                .id(1L)
                .build();

        Grupo grupo2 = Grupo.builder()
                .id(2L)
                .build();

        grupo1.getParticipantes().add(usuario1);
        grupo1.getParticipantes().add(usuario2);
        grupo2.getParticipantes().add(usuario1);
        grupo2.getParticipantes().add(usuario2);

        Partida partida1 = Partida.builder()
                .id(1L)
                .build();

        Palpite palpite1 = Palpite.builder()
                .grupo(grupo1)
                .build();

        Palpite palpite2 = Palpite.builder()
                .grupo(grupo2)
                .build();

        PontuacaoParticipanteResponseDTO pontuacaoUsuario1 =
                PontuacaoParticipanteResponseDTO.builder()
                        .usuarioId(1L)
                        .build();

        PontuacaoParticipanteResponseDTO pontuacaoUsuario2 =
                PontuacaoParticipanteResponseDTO.builder()
                        .usuarioId(2L)
                        .build();

        when(palpiteRepository.findByPartidaId(1L))
                .thenReturn(List.of(palpite1, palpite2));

        when(pontuacaoService.calcularPontuacaoGlobalDoParticipante(1L))
                .thenReturn(pontuacaoUsuario1);

        when(pontuacaoService.calcularPontuacaoGlobalDoParticipante(2L))
                .thenReturn(pontuacaoUsuario2);

        // Act
        List<EstatisticasResponseDTO> resultado = estatisticasService.calcularEstatisticasAssociadasAPartida(partida1.getId());

        // Assert: cada usuário deve gerar somente um novo snapshot de estatísticas.
        assertEquals(2, resultado.size());

        verify(pontuacaoService, times(1))
                .calcularPontuacaoGlobalDoParticipante(1L);

        verify(pontuacaoService, times(1))
                .calcularPontuacaoGlobalDoParticipante(2L);

        verify(estatisticasRepository, times(2))
                .save(any(Estatisticas.class));
    }

    @Test
    @DisplayName("Não cria estatísticas quando a partida não possui palpites")
    void quandoPartidaNaoPossuiPalpitesNaoCriaEstatisticas() {
        // Arrange: nenhum palpite foi feito para a partida consolidada.
        Partida partida1 = Partida.builder()
                .id(1L)
                .build();

        when(palpiteRepository.findByPartidaId(1L))
                .thenReturn(List.of());

        // Act
        List<EstatisticasResponseDTO> resultado =
                estatisticasService.calcularEstatisticasAssociadasAPartida(partida1.getId());

        // Assert
        assertTrue(resultado.isEmpty());

        verify(estatisticasRepository, never())
                .save(any(Estatisticas.class));
    }

    @Test
    @DisplayName("Evento de partida consolidada dispara cálculo de estatísticas")
    void quandoRecebeEventoDePartidaConsolidadaProcessaPartidaDoEvento() {
        // Arrange: o evento informa que a partida 1 foi consolidada.
        Partida partida1 = Partida.builder()
                .id(1L)
                .build();

        PartidaConsolidadaEvent event =
                new PartidaConsolidadaEvent(this, partida1.getId());

        when(palpiteRepository.findByPartidaId(1L))
                .thenReturn(List.of());

        // Act
        estatisticasService.aoConsolidarPartida(event);

        // Assert: checa se disparou
        verify(palpiteRepository).findByPartidaId(1L);
    }

    @Test
    @DisplayName("Retorna estatística mais recente")
    void quandoUsuarioPossuiEstatisticasRetornaRegistroMaisRecente() {
        // Arrange: existem três estatísticas.
        Usuario usuario = Usuario.builder()
                .id(1L)
                .codigo("123456")
                .build();

        Estatisticas estatistica1 = Estatisticas.builder()
                .id(1L)
                .usuario(usuario)
                .build();

        Estatisticas estatistica2 = Estatisticas.builder()
                .id(2L)
                .usuario(usuario)
                .build();

        Estatisticas estatistica3 = Estatisticas.builder()
                .id(3L)
                .usuario(usuario)
                .build();

        when(grupoAutorizacaoService.obterUsuarioValido(1L, "123456"))
                .thenReturn(usuario);

        when(estatisticasRepository.findByUsuarioIdOrderByDataRegistroDesc(1L))
                .thenReturn(List.of(estatistica3, estatistica2, estatistica1));

        // Act
        EstatisticasResponseDTO resultado = estatisticasService.obterEstatisticaMaisRecente(1L, "123456");

        // Assert
        assertEquals(3L, resultado.getId());
    }

    @Test
    @DisplayName("Lança exceção quando estatística não existe")
    void quandoUsuarioNaoPossuiEstatisticaLancaExcecao() {
        // Arrange: o usuário é válido, mas ainda não possui snapshots de estatísticas.
        Usuario usuario = Usuario.builder()
                .id(1L)
                .codigo("123456")
                .build();

        when(grupoAutorizacaoService.obterUsuarioValido(1L, "123456"))
                .thenReturn(usuario);

        when(estatisticasRepository
                .findByUsuarioIdOrderByDataRegistroDesc(1L))
                .thenReturn(List.of());

        // Act e Assert
        assertThrows(
                EstatisticaNaoExisteException.class,
                () -> estatisticasService
                        .obterEstatisticaMaisRecente(1L, "123456")
        );
    }

    @Test
    @DisplayName("Retorna evolução das estatísticas em ordem cronológica")
    void quandoUsuarioPossuiHistoricoRetornaEvolucaoCronologica() {
        // Arrange: o usuário possui três snapshots registrados em momentos sucessivos.
        Usuario usuario = Usuario.builder()
                .id(1L)
                .codigo("123456")
                .build();

        Estatisticas estatistica1 = Estatisticas.builder()
                .id(1L)
                .usuario(usuario)
                .build();

        Estatisticas estatistica2 = Estatisticas.builder()
                .id(2L)
                .usuario(usuario)
                .build();

        Estatisticas estatistica3 = Estatisticas.builder()
                .id(3L)
                .usuario(usuario)
                .build();

        when(grupoAutorizacaoService.obterUsuarioValido(1L, "123456"))
                .thenReturn(usuario);

        when(estatisticasRepository
                .findByUsuarioIdOrderByDataRegistroAsc(1L))
                .thenReturn(List.of(estatistica1, estatistica2, estatistica3));

        // Act
        List<EstatisticasResponseDTO> resultado = estatisticasService.obterEvolucaoEstatisticas(1L, "123456");

        // Assert
        assertEquals(List.of(1L, 2L, 3L), resultado.stream().map(EstatisticasResponseDTO::getId).toList());
    }
}
