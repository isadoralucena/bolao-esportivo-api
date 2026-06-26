package com.ufcg.psoft.project.controller;

import com.ufcg.psoft.project.dto.campeonato.CampeonatoPostPutRequestDTO;
import com.ufcg.psoft.project.service.campeonato.CampeonatoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
value = "/campeonatos",
produces = MediaType.APPLICATION_JSON_VALUE
)
public class CampeonatoController {

	@Autowired
	private CampeonatoService campeonatoService;

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

	return ResponseEntity
		.status(HttpStatus.CREATED)
		.body(campeonatoService.criar(userId, senha, dto));
	}

	@PutMapping("/{id}/ativar")
	public ResponseEntity<?> ativarCampeonato(
		@PathVariable Long id,
		@RequestParam Long userId,
		@RequestParam String senha) {

		return ResponseEntity
			.status(HttpStatus.OK)
			.body(campeonatoService.ativar(userId, senha, id));
	}

	@PutMapping("/{id}/desativar")
	public ResponseEntity<?> desativarCampeonato(
		@PathVariable Long id,
		@RequestParam Long userId,
		@RequestParam String senha) {

		return ResponseEntity
			.status(HttpStatus.OK)
			.body(campeonatoService.desativar(userId, senha, id));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<?> excluirCampeonato(
		@PathVariable Long id,
		@RequestParam Long userId,
		@RequestParam String senha) {

		campeonatoService.remover(userId, senha, id);

		return ResponseEntity
			.status(HttpStatus.NO_CONTENT)
			.body("");
	}

    @PostMapping("/{campeonatoId}/sincronizar")
    public ResponseEntity<?> sincronizarCampeonato(
            @PathVariable Long campeonatoId,
            @RequestParam Long userId,
            @RequestParam String senha) {

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(campeonatoService.sincronizarCampeonato(campeonatoId, userId, senha));
    }
}
