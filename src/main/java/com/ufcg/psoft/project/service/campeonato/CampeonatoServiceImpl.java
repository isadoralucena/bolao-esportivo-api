package com.ufcg.psoft.project.service.campeonato;

import com.ufcg.psoft.project.dto.campeonato.CampeonatoPostPutRequestDTO;
import com.ufcg.psoft.project.dto.campeonato.CampeonatoResponseDTO;
import com.ufcg.psoft.project.exception.CodigoDeAcessoInvalidoException;
import com.ufcg.psoft.project.exception.campeonato.CampeonatoNaoExisteException;
import com.ufcg.psoft.project.exception.campeonato.CampeonatoSyncException;
import com.ufcg.psoft.project.model.Campeonato;
import com.ufcg.psoft.project.repository.CampeonatoRepository;
import com.ufcg.psoft.project.model.Usuario;

import com.ufcg.psoft.project.repository.UsuarioRepository;
import com.ufcg.psoft.project.service.partida.PartidaService;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Value;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CampeonatoServiceImpl implements CampeonatoService {

	@Autowired
	private CampeonatoRepository campeonatoRepository;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private PartidaService partidaService;

    @Autowired
	private ClassificacaoCampeonatoService classificacaoCampeonatoService;

	@Autowired
	private ModelMapper modelMapper;

	@Value("${project.football-data.api-token:}")
	private String apiToken;

	private final RestTemplate restTemplate = new RestTemplate();

	@Override
    @Transactional
	public CampeonatoResponseDTO sincronizarCampeonato(Long campeonatoId, Long usuarioId, String codigo) {
		verificaAdmin(usuarioId, codigo);
		Campeonato campeonato = campeonatoRepository.findById(campeonatoId).orElseThrow(CampeonatoNaoExisteException::new);
    
        return sincronizarCampeonato(campeonato);
	}

    @Override
    @Transactional
    public CampeonatoResponseDTO sincronizarCampeonato(Campeonato campeonato) {
        sincronizarDadosDoCampeonato(campeonato);
        partidaService.sincronizarPartidas(campeonato);
        classificacaoCampeonatoService.sincronizarClassificacao(campeonato.getId());

        campeonato.setUltimaSincronizacao(LocalDateTime.now());
        campeonatoRepository.save(campeonato);

        return modelMapper.map(campeonato, CampeonatoResponseDTO.class);
    }

	@Override
	public CampeonatoResponseDTO criar(Long userId, String codigo, CampeonatoPostPutRequestDTO campeonatoPostPutRequestDTO) {
		verificaAdmin(userId, codigo);

		Campeonato campeonato = modelMapper.map(campeonatoPostPutRequestDTO, Campeonato.class);
		campeonato.setAtivo(false);
		campeonatoRepository.save(campeonato);
		return modelMapper.map(campeonato, CampeonatoResponseDTO.class);
	}

	@Override
    @Transactional
	public void remover(Long userId, String codigo, Long id) {
		verificaAdmin(userId, codigo);

		Campeonato campeonato = campeonatoRepository.findById(id).orElseThrow(CampeonatoNaoExisteException::new);

        classificacaoCampeonatoService.deleteByCampeonatoId(id);
        partidaService.deleteByCampeonatoId(id);
		campeonatoRepository.delete(campeonato);
	}

	@Override
	public List<CampeonatoResponseDTO> listar() {
		List<Campeonato> campeonatos = campeonatoRepository.findAll();
		return campeonatos.stream()
			.map(CampeonatoResponseDTO::new)
			.collect(Collectors.toList());
	}

	@Override
	public CampeonatoResponseDTO recuperar(Long id) {
		Campeonato campeonato = campeonatoRepository.findById(id).orElseThrow(CampeonatoNaoExisteException::new);
		return modelMapper.map(campeonato, CampeonatoResponseDTO.class);
	}

	@Override
	public List<CampeonatoResponseDTO> recuperarNome(String nome) {
		List<Campeonato> campeonatos = campeonatoRepository.findByNomeContaining(nome);
		return campeonatos.stream()
			.map(CampeonatoResponseDTO::new)
			.collect(Collectors.toList());
	}

	@Override
	public CampeonatoResponseDTO ativar(Long userId, String codigo, Long id) {
		verificaAdmin(userId, codigo);

		Campeonato campeonato = campeonatoRepository.findById(id).orElseThrow(CampeonatoNaoExisteException::new);
		campeonato.setAtivo(true);
		campeonatoRepository.save(campeonato);
		return modelMapper.map(campeonato, CampeonatoResponseDTO.class);
	}

	@Override
	public CampeonatoResponseDTO desativar(Long userId, String codigo, Long id) {
		verificaAdmin(userId, codigo);

		Campeonato campeonato = campeonatoRepository.findById(id).orElseThrow(CampeonatoNaoExisteException::new);
		campeonato.setAtivo(false);
		campeonatoRepository.save(campeonato);
		return modelMapper.map(campeonato, CampeonatoResponseDTO.class);
	}

	private void verificaAdmin(Long userId, String codigo) {
		Usuario usuario = usuarioRepository.findById(userId).orElse(null);
		if (usuario == null || !usuario.getCodigo().equals(codigo) || !usuario.isAdministrador()) {
			throw new CodigoDeAcessoInvalidoException();
		}
	}

    private void sincronizarDadosDoCampeonato(Campeonato campeonato) {
        HttpHeaders headers = new HttpHeaders();

        if (apiToken != null && !apiToken.isEmpty()) {
            headers.set("X-Auth-Token", apiToken);
        }

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                campeonato.getUrl(),
                HttpMethod.GET,
                entity,
                Map.class
        );

        Map<String, Object> body = response.getBody();

        if (body == null) {
            throw new CampeonatoSyncException("Resposta da API sem corpo.");
        }

        if (body.containsKey("name")) {
            campeonato.setNome((String) body.get("name"));
        }

        if (body.containsKey("code")) {
            campeonato.setCodigo((String) body.get("code"));
        }
    }
}
