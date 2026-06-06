package com.ufcg.psoft.project.controller;

import com.ufcg.psoft.project.dto.CampeonatoPostPutRequestDTO;
import com.ufcg.psoft.project.service.campeonato.CampeonatoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
value = "/campeonato",
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

	@GetMapping("/search")
	public ResponseEntity<?> buscarCampeonatoPorNome(@RequestParam String nome) {
		return ResponseEntity
			.status(HttpStatus.OK)
			.body(campeonatoService.recuperarNome(nome));
	}

	@PostMapping("")
	public ResponseEntity<?> criarCampeonato(
			@RequestParam String codigoAdmin,
			@RequestBody @Valid CampeonatoPostPutRequestDTO dto) {
			return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(campeonatoService.criar(codigoAdmin, dto));
			}

	@PutMapping("/{id}/ativar")
	public ResponseEntity<?> ativarCampeonato(
			@PathVariable Long id,
			@RequestParam String codigoAdmin) {
			return ResponseEntity
				.status(HttpStatus.OK)
				.body(campeonatoService.ativar(codigoAdmin, id));
			}

	@PutMapping("/{id}/desativar")
	public ResponseEntity<?> desativarCampeonato(
			@PathVariable Long id,
			@RequestParam String codigoAdmin) {
			return ResponseEntity
				.status(HttpStatus.OK)
				.body(campeonatoService.desativar(codigoAdmin, id));
			}

	@DeleteMapping("/{id}")
	public ResponseEntity<?> excluirCampeonato(
			@PathVariable Long id,
			@RequestParam String codigoAdmin) {
			campeonatoService.remover(codigoAdmin, id);
			return ResponseEntity
				.status(HttpStatus.NO_CONTENT)
				.body("");
			}

	@PostMapping("/sincronizar")
	public ResponseEntity<?> sincronizarCampeonatos(@RequestParam String codigoAdmin) {
		return ResponseEntity
			.status(HttpStatus.OK)
			.body(campeonatoService.sincronizar(codigoAdmin));
	}
}
