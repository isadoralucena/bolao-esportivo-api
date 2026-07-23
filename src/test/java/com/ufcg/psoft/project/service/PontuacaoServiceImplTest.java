package com.ufcg.psoft.project.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import com.ufcg.psoft.project.event.MudancaGrupoPosicaoEvent;
import com.ufcg.psoft.project.event.RankingAtualizadoEvent;
import com.ufcg.psoft.project.exception.grupo.GrupoNaoExisteException;
import com.ufcg.psoft.project.exception.usuario.UsuarioNaoExisteException;
import com.ufcg.psoft.project.exception.usuario.UsuarioNaoParticipanteException;
import com.ufcg.psoft.project.model.Grupo;
import com.ufcg.psoft.project.model.Palpite;
import com.ufcg.psoft.project.model.Partida;
import com.ufcg.psoft.project.model.PartidaStatus;
import com.ufcg.psoft.project.model.PontuacaoPalpite;
import com.ufcg.psoft.project.model.TipoRegraPontuacao;
import com.ufcg.psoft.project.model.Usuario;
import com.ufcg.psoft.project.repository.GrupoRepository;
import com.ufcg.psoft.project.repository.PalpiteRepository;
import com.ufcg.psoft.project.repository.PartidaRepository;
import com.ufcg.psoft.project.repository.PontuacaoPalpiteRepository;
import com.ufcg.psoft.project.repository.RegraPontuacaoRepository;
import com.ufcg.psoft.project.repository.UsuarioRepository;
import com.ufcg.psoft.project.service.pontuacao.PontuacaoServiceImpl;
import com.ufcg.psoft.project.service.pontuacao.Pontuador;
import com.ufcg.psoft.project.service.ranking.RankingCalculator;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Testes unitarios do PontuacaoServiceImpl")
class PontuacaoServiceImplTest {

    @Mock
    private PartidaRepository partidaRepository;

    @Mock
    private PalpiteRepository palpiteRepository;

    @Mock
    private GrupoRepository grupoRepository;

    @Mock
    private RegraPontuacaoRepository regraPontuacaoRepository;

    @Mock
    private PontuacaoPalpiteRepository pontuacaoPalpiteRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Spy
    private RankingCalculator rankingCalculator = new RankingCalculator();

    @InjectMocks
    private PontuacaoServiceImpl pontuacaoService;

    private Usuario usuario;
    private Grupo grupo;
    private Partida partida;
    private Palpite palpite;
    private PontuacaoPalpite pontuacaoPalpite;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .id(1L)
                .nome("Teste")
                .email("teste@teste.com")
                .codigo("123456")
                .build();

        grupo = Grupo.builder()
                .id(1L)
                .nome("Grupo Teste")
                .build();
        grupo.getParticipantes().add(usuario);

        partida = Partida.builder()
                .id(1L)
                .mandante("Time A")
                .visitante("Time B")
                .golsMandante(2)
                .golsVisitante(1)
                .status(PartidaStatus.FINALIZADO)
                .data(LocalDateTime.now().minusHours(2))
                .build();

        palpite = Palpite.builder()
                .id(1L)
                .partida(partida)
                .usuario(usuario)
                .grupo(grupo)
                .golsMandante(1)
                .golsVisitante(0)
                .build();

        pontuacaoPalpite = PontuacaoPalpite.builder()
                .id(1L)
                .palpite(palpite)
                .pontuacao(10)
                .acertouVencedor(true)
                .acertouEmpate(false)
                .acertouPlacarExato(false)
                .build();

        Map<TipoRegraPontuacao, Pontuador> pontuadores = new EnumMap<>(TipoRegraPontuacao.class);
        Pontuador mockPontuador = mock(Pontuador.class);
        when(mockPontuador.getTipo()).thenReturn(TipoRegraPontuacao.ACERTO_VENCEDOR);
        when(mockPontuador.calcular(any(), any())).thenReturn(10);
        pontuadores.put(TipoRegraPontuacao.ACERTO_VENCEDOR, mockPontuador);
        ReflectionTestUtils.setField(pontuacaoService, "pontuadores", pontuadores);
    }

    @Test
    @DisplayName("calcularPontuacaoGlobalDoParticipante com usuario inexistente")
    void quandoUsuarioNaoExisteGlobal() {
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(UsuarioNaoExisteException.class,
                () -> pontuacaoService.calcularPontuacaoGlobalDoParticipante(999L));
    }

    @Test
    @DisplayName("calcularPontuacaoGlobalDoParticipante retorna dados corretos")
    void quandoCalcularPontuacaoGlobal() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(pontuacaoPalpiteRepository.findByPalpite_Usuario_Id(1L))
                .thenReturn(List.of(pontuacaoPalpite));

        var resultado = pontuacaoService.calcularPontuacaoGlobalDoParticipante(1L);

        assertAll(
                () -> assertEquals(1L, resultado.getUsuarioId()),
                () -> assertEquals("Teste", resultado.getUsuarioNome()),
                () -> assertEquals(10, resultado.getPontuacao()),
                () -> assertEquals(0, resultado.getErros()),
                () -> assertEquals(1, resultado.getAcertosVencedor()),
                () -> assertEquals(0, resultado.getAcertosEmpate()),
                () -> assertEquals(0, resultado.getPlacaresExatos())
        );
    }

    @Test
    @DisplayName("listarPontuacoesGlobais com repositorio vazio retorna lista vazia")
    void quandoListarGlobaisVazio() {
        when(usuarioRepository.findAll()).thenReturn(List.of());

        var resultado = pontuacaoService.listarPontuacoesGlobais();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("listarPontuacoesGlobais com usuarios retorna pontuacoes")
    void quandoListarGlobaisComUsuarios() {
        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(pontuacaoPalpiteRepository.findByPalpite_Usuario_Id(1L))
                .thenReturn(List.of(pontuacaoPalpite));

        var resultado = pontuacaoService.listarPontuacoesGlobais();

        assertEquals(1, resultado.size());
        assertEquals(10, resultado.get(0).getPontuacao());
    }

    @Test
    @DisplayName("calcularPontuacaoParticipanteNoGrupo com grupo inexistente")
    void quandoGrupoNaoExiste() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(grupoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(GrupoNaoExisteException.class,
                () -> pontuacaoService.calcularPontuacaoParticipanteNoGrupo(999L, 1L));
    }

    @Test
    @DisplayName("calcularPontuacaoParticipanteNoGrupo com usuario nao participante")
    void quandoUsuarioNaoParticipante() {
        Usuario outro = Usuario.builder().id(2L).nome("Outro").build();

        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(outro));
        when(grupoRepository.findById(1L)).thenReturn(Optional.of(grupo));

        assertThrows(UsuarioNaoParticipanteException.class,
                () -> pontuacaoService.calcularPontuacaoParticipanteNoGrupo(1L, 2L));
    }

    @Test
    @DisplayName("calcularPontuacaoParticipanteNoGrupo com dados retorna pontuacao correta")
    void quandoCalcularPontuacaoParticipanteNoGrupo() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(grupoRepository.findById(1L)).thenReturn(Optional.of(grupo));
        when(pontuacaoPalpiteRepository.findByPalpite_Grupo_IdAndPalpite_Usuario_Id(1L, 1L))
                .thenReturn(List.of(pontuacaoPalpite));

        var resultado = pontuacaoService.calcularPontuacaoParticipanteNoGrupo(1L, 1L);

        assertAll(
                () -> assertEquals(1L, resultado.getUsuarioId()),
                () -> assertEquals(10, resultado.getPontuacao()),
                () -> assertEquals(1, resultado.getAcertosVencedor())
        );
    }

    @Test
    @DisplayName("calcular pontuações publica evento de atualização do ranking")
    void quandoCalculaPontuacoesPublicaEventoDeRanking() {
        Grupo grupoComDois = Grupo.builder().id(1L).nome("Grupo").build();
        Usuario u1 = Usuario.builder().id(1L).nome("Um").build();
        Usuario u2 = Usuario.builder().id(2L).nome("Dois").build();
        grupoComDois.getParticipantes().add(u1);
        grupoComDois.getParticipantes().add(u2);

        partida = Partida.builder()
                .id(1L)
                .mandante("A").visitante("B")
                .golsMandante(2).golsVisitante(1)
                .status(PartidaStatus.FINALIZADO)
                .data(LocalDateTime.now().minusHours(2))
                .build();

        Palpite p1 = Palpite.builder().id(10L).partida(partida).usuario(u1).grupo(grupoComDois)
                .golsMandante(2).golsVisitante(0).build();
        Palpite p2 = Palpite.builder().id(11L).partida(partida).usuario(u2).grupo(grupoComDois)
                .golsMandante(0).golsVisitante(1).build();

        when(partidaRepository.findById(1L)).thenReturn(Optional.of(partida));
        when(palpiteRepository.findByPartidaId(1L)).thenReturn(List.of(p1, p2));
        when(grupoRepository.findById(1L)).thenReturn(Optional.of(grupoComDois));
        when(pontuacaoPalpiteRepository.findByPalpiteId(any()))
                .thenReturn(Optional.empty());
        when(regraPontuacaoRepository.findByGrupoId(1L)).thenReturn(List.of());
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(u1));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(u2));
        PontuacaoPalpite pp1 = PontuacaoPalpite.builder().id(10L).palpite(p1).pontuacao(0).build();
        PontuacaoPalpite pp2 = PontuacaoPalpite.builder().id(11L).palpite(p2).pontuacao(0).build();
        when(pontuacaoPalpiteRepository.findByPalpite_Grupo_IdAndPalpite_Usuario_Id(1L, 1L))
                .thenReturn(List.of(pp1));
        when(pontuacaoPalpiteRepository.findByPalpite_Grupo_IdAndPalpite_Usuario_Id(1L, 2L))
                .thenReturn(List.of(pp2));
        when(pontuacaoPalpiteRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var resultado = pontuacaoService.calcularPontuacoesAssociadasAPartida(1L);

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        ArgumentCaptor<RankingAtualizadoEvent> eventoCaptor =
                ArgumentCaptor.forClass(RankingAtualizadoEvent.class);

        verify(eventPublisher).publishEvent(eventoCaptor.capture());
        assertEquals(1L, eventoCaptor.getValue().getGrupoId());
        verify(eventPublisher, never())
                .publishEvent(any(MudancaGrupoPosicaoEvent.class));
    }

    @Test
    @DisplayName("calcularPontuacoesDoGrupo com grupo inexistente")
    void quandoCalcularPontuacoesDoGrupoGrupoNaoExiste() {
        when(grupoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(GrupoNaoExisteException.class,
                () -> pontuacaoService.calcularPontuacoesDoGrupo(999L));
    }

    @Test
    @DisplayName("calcularPontuacoesDoGrupo retorna lista vazia quando sem palpites")
    void quandoCalcularPontuacoesDoGrupoSemPalpites() {
        when(grupoRepository.findById(1L)).thenReturn(Optional.of(grupo));
        when(palpiteRepository.findByGrupoId(1L)).thenReturn(List.of());
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        var resultado = pontuacaoService.calcularPontuacoesDoGrupo(1L);
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }
}
