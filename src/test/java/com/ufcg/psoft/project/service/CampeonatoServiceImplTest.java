package com.ufcg.psoft.project.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.ufcg.psoft.project.dto.campeonato.CampeonatoResponseDTO;
import com.ufcg.psoft.project.exception.CodigoDeAcessoInvalidoException;
import com.ufcg.psoft.project.exception.campeonato.CampeonatoSyncException;
import com.ufcg.psoft.project.model.Campeonato;
import com.ufcg.psoft.project.model.Usuario;
import com.ufcg.psoft.project.repository.CampeonatoRepository;
import com.ufcg.psoft.project.repository.UsuarioRepository;
import com.ufcg.psoft.project.service.campeonato.CampeonatoServiceImpl;
import com.ufcg.psoft.project.service.campeonato.ClassificacaoCampeonatoService;
import com.ufcg.psoft.project.service.partida.PartidaService;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Testes de service de campeonato")
class CampeonatoServiceImplTest {
    @Mock
    private CampeonatoRepository campeonatoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PartidaService partidaService;

    @Mock
    private ClassificacaoCampeonatoService classificacaoCampeonatoService;

    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    @InjectMocks
    private CampeonatoServiceImpl campeonatoService;

    private MockRestServiceServer server;
    private Campeonato campeonato;
    private Usuario admin;

    @BeforeEach
    void setup() {
        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(campeonatoService, "restTemplate");
        server = MockRestServiceServer.createServer(restTemplate);

        campeonato = Campeonato.builder()
                .id(1L)
                .nome("Nome antigo")
                .url("http://api.test/competitions/1")
                .codigo("OLD")
                .ativo(true)
                .build();

        admin = Usuario.builder()
                .id(1L)
                .nome("Admin")
                .email("admin@email.com")
                .username("admin")
                .endereco("Rua Admin")
                .codigo("123456")
                .administrador(true)
                .build();
    }

    @Test
    @DisplayName("Quando administrador sincroniza campeonato por id")
    void quandoAdministradorSincronizaCampeonatoPorId() {
        String respostaAPI = """
            {
                "name": "Nome atualizado",
                "code": "NEW"
            }
        """;

        when(usuarioRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(campeonatoRepository.findById(campeonato.getId())).thenReturn(Optional.of(campeonato));

        server.expect(requestTo("http://api.test/competitions/1"))
                .andExpect(method(GET))
                .andRespond(withSuccess(respostaAPI, MediaType.APPLICATION_JSON));

        CampeonatoResponseDTO resultado = campeonatoService.sincronizarCampeonato(
            campeonato.getId(),
            admin.getId(),
            admin.getCodigo()
        );

        assertEquals("Nome atualizado", resultado.getNome());
        assertEquals("NEW", resultado.getCodigo());
        assertNotNull(campeonato.getUltimaSincronizacao());
    }

    @Test
    @DisplayName("Quando usuário administrador não existe, sincronização falha")
    void quandoUsuarioAdministradorNaoExisteSincronizacaoFalha() {
        when(usuarioRepository.findById(admin.getId())).thenReturn(Optional.empty());

        assertThrows(CodigoDeAcessoInvalidoException.class,
                () -> campeonatoService.sincronizarCampeonato(campeonato.getId(), admin.getId(), admin.getCodigo())
        );
    }


    @Test
    @DisplayName("Quando sincroniza campeonato sem novos dados")
    void quandoSincronizaCampeonatoSemNovosDados() {
        ReflectionTestUtils.setField(campeonatoService, "apiToken", "TOKEN");

        server.expect(requestTo("http://api.test/competitions/1"))
                .andExpect(method(GET))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        CampeonatoResponseDTO resultado = campeonatoService.sincronizarCampeonato(campeonato);

        assertEquals("Nome antigo", resultado.getNome());
        assertEquals("OLD", resultado.getCodigo());
    }

    @Test
    @DisplayName("Quando resposta da API não tem corpo, sincronização de campeonato falha")
    void quandoRespostaSemCorpoSincronizacaoCampeonatoFalha() {
        server.expect(requestTo("http://api.test/competitions/1"))
                .andExpect(method(GET))
                .andRespond(withSuccess("", MediaType.APPLICATION_JSON));

        assertThrows(CampeonatoSyncException.class,
                () -> campeonatoService.sincronizarCampeonato(campeonato)
        );
    }
}
