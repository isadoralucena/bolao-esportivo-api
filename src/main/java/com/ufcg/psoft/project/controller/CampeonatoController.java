package com.ufcg.psoft.project.controller;

import com.ufcg.psoft.project.dto.campeonato.CampeonatoPostPutRequestDTO;
import com.ufcg.psoft.project.dto.campeonato.CampeonatoResponseDTO;
import com.ufcg.psoft.project.service.campeonato.CampeonatoService;
import com.ufcg.psoft.project.service.premium.RequisicaoAutenticadaEvent;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
value = "/campeonatos",
produces = MediaType.APPLICATION_JSON_VALUE
)
@RequiredArgsConstructor
public class CampeonatoController {

	private final CampeonatoService campeonatoService;

	private final ApplicationEventPublisher eventPublisher;

	@GetMapping("/{id}")
	public ResponseEntity<CampeonatoResponseDTO> recuperarCampeonato(@PathVariable Long id) {
		return ResponseEntity
			.status(HttpStatus.OK)
			.body(campeonatoService.recuperar(id));
	}

	@GetMapping("")
	public ResponseEntity<List<CampeonatoResponseDTO>> listarCampeonatos() {

		return ResponseEntity
			.status(HttpStatus.OK)
			.body(campeonatoService.listar());
	}

	@GetMapping("/buscar")
	public ResponseEntity<List<CampeonatoResponseDTO>> buscarCampeonatoPorNome(@RequestParam String nome) {
		return ResponseEntity
			.status(HttpStatus.OK)
			.body(campeonatoService.recuperarNome(nome));
	}

	@PostMapping("")
	public ResponseEntity<CampeonatoResponseDTO> criarCampeonato(
		@RequestParam Long userId,
		@RequestParam String senha,
		@RequestBody @Valid CampeonatoPostPutRequestDTO dto) {

	var resultado = campeonatoService.criar(userId, senha, dto);
	eventPublisher.publishEvent(new RequisicaoAutenticadaEvent(userId));
	return ResponseEntity
		.status(HttpStatus.CREATED)
		.body(resultado);
	}

	@PutMapping("/{id}/ativar")
	public ResponseEntity<CampeonatoResponseDTO> ativarCampeonato(
		@PathVariable Long id,
		@RequestParam Long userId,
		@RequestParam String senha) {

		var resultado = campeonatoService.ativar(userId, senha, id);
		eventPublisher.publishEvent(new RequisicaoAutenticadaEvent(userId));
		return ResponseEntity
			.status(HttpStatus.OK)
			.body(resultado);
	}

	@PutMapping("/{id}/desativar")
	public ResponseEntity<CampeonatoResponseDTO> desativarCampeonato(
		@PathVariable Long id,
		@RequestParam Long userId,
		@RequestParam String senha) {

		var resultado = campeonatoService.desativar(userId, senha, id);
		eventPublisher.publishEvent(new RequisicaoAutenticadaEvent(userId));
		return ResponseEntity
			.status(HttpStatus.OK)
			.body(resultado);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> excluirCampeonato(
		@PathVariable Long id,
		@RequestParam Long userId,
		@RequestParam String senha) {

		campeonatoService.remover(userId, senha, id);
		eventPublisher.publishEvent(new RequisicaoAutenticadaEvent(userId));

		return ResponseEntity
			.status(HttpStatus.NO_CONTENT)
			.build();
	}

    @PostMapping("/{campeonatoId}/sincronizar")
    public ResponseEntity<CampeonatoResponseDTO> sincronizarCampeonato(
            @PathVariable Long campeonatoId,
            @RequestParam Long userId,
            @RequestParam String senha) {

        var resultado = campeonatoService.sincronizarCampeonato(campeonatoId, userId, senha);
        eventPublisher.publishEvent(new RequisicaoAutenticadaEvent(userId));
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(resultado);
    }
}