package com.ufcg.psoft.project.controller;

import com.ufcg.psoft.project.dto.campeonato.CampeonatoPostPutRequestDTO;
import com.ufcg.psoft.project.service.campeonato.CampeonatoService;
import com.ufcg.psoft.project.service.premium.RequisicaoAutenticadaEvent;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
	public ResponseEntity<?> recuperarCampeonato(@PathVariable Long id) {
		return ResponseEntity
			.status(HttpStatus.OK)
			.body(campeonatoService.recuperar(id));
	}

	@GetMapping("")
	public ResponseEntity<?> listarCampeonatos() {

		return ResponseEntity
			.status(HttpStatus.OK)
			.body(campeonatoService.listar());
	}

	@GetMapping("/buscar")
	public ResponseEntity<?> buscarCampeonatoPorNome(@RequestParam String nome) {
		return ResponseEntity
			.status(HttpStatus.OK)
			.body(campeonatoService.recuperarNome(nome));
	}

	@PostMapping("")
	public ResponseEntity<?> criarCampeonato(
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
	public ResponseEntity<?> ativarCampeonato(
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
	public ResponseEntity<?> desativarCampeonato(
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
	public ResponseEntity<?> excluirCampeonato(
		@PathVariable Long id,
		@RequestParam Long userId,
		@RequestParam String senha) {

		campeonatoService.remover(userId, senha, id);
		eventPublisher.publishEvent(new RequisicaoAutenticadaEvent(userId));

		return ResponseEntity
			.status(HttpStatus.NO_CONTENT)
			.body("");
	}

    @PostMapping("/{campeonatoId}/sincronizar")
    public ResponseEntity<?> sincronizarCampeonato(
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
