package com.ufcg.psoft.project.service.campeonato;

import com.ufcg.psoft.project.dto.CampeonatoPostPutRequestDTO;
import com.ufcg.psoft.project.dto.CampeonatoResponseDTO;
import com.ufcg.psoft.project.exception.CampeonatoNaoExisteException;
import com.ufcg.psoft.project.exception.CodigoDeAcessoInvalidoException;
import com.ufcg.psoft.project.model.Campeonato;
import com.ufcg.psoft.project.repository.CampeonatoRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CampeonatoServiceImpl implements CampeonatoService {

	@Autowired
	private CampeonatoRepository campeonatoRepository;

	@Autowired
	private ModelMapper modelMapper;

	@Value("${project.admin-code:123456}")
	private String adminCodigo;

	@Value("${project.football-data.api-token:}")
	private String apiToken;

	private final RestTemplate restTemplate = new RestTemplate();

	@Override
	public List<CampeonatoResponseDTO> sincronizar(String codigo) {
		verificaAdmin(codigo);
		List<Campeonato> campeonatos = campeonatoRepository.findAll();

		HttpHeaders headers = new HttpHeaders();
		if (apiToken != null && !apiToken.isEmpty()) {
			headers.set("X-Auth-Token", apiToken);
		}
		HttpEntity<Void> entity = new HttpEntity<>(headers);

		for (Campeonato campeonato : campeonatos) {
			try {
				ResponseEntity<Map> response = restTemplate.exchange(
						campeonato.getUrl(),
						HttpMethod.GET,
						entity,
						Map.class
						);

				if (response.getBody() != null) {
					Map<String, Object> body = response.getBody();
					if (body.containsKey("name")) {
						campeonato.setNome((String) body.get("name"));
					}
					if (body.containsKey("code")) {
						campeonato.setCodigo((String) body.get("code"));
					}
					campeonatoRepository.save(campeonato);
				}
			} catch (Exception e) {
			}
		}

		return campeonatoRepository.findAll().stream()
			.map(CampeonatoResponseDTO::new)
			.collect(Collectors.toList());
	}

	@Override
	public CampeonatoResponseDTO criar(String codigo, CampeonatoPostPutRequestDTO campeonatoPostPutRequestDTO) {
		verificaAdmin(codigo);

		Campeonato campeonato = modelMapper.map(campeonatoPostPutRequestDTO, Campeonato.class);
		campeonato.setAtivo(false);
		campeonatoRepository.save(campeonato);
		return modelMapper.map(campeonato, CampeonatoResponseDTO.class);
	}

	@Override
	public void remover(String codigo, Long id) {
		verificaAdmin(codigo);

		Campeonato campeonato = campeonatoRepository.findById(id).orElseThrow(CampeonatoNaoExisteException::new);
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
	public CampeonatoResponseDTO ativar(String codigo, Long id) {
		verificaAdmin(codigo);

		Campeonato campeonato = campeonatoRepository.findById(id).orElseThrow(CampeonatoNaoExisteException::new);
		campeonato.setAtivo(true);
		campeonatoRepository.save(campeonato);
		return modelMapper.map(campeonato, CampeonatoResponseDTO.class);
	}

	@Override
	public CampeonatoResponseDTO desativar(String codigo, Long id) {
		verificaAdmin(codigo);

		Campeonato campeonato = campeonatoRepository.findById(id).orElseThrow(CampeonatoNaoExisteException::new);
		campeonato.setAtivo(false);
		campeonatoRepository.save(campeonato);
		return modelMapper.map(campeonato, CampeonatoResponseDTO.class);
	}

	private void verificaAdmin(String codigo) {
		if (!adminCodigo.equals(codigo)) {
			throw new CodigoDeAcessoInvalidoException();
		}
	}
}
