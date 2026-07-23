package com.ufcg.psoft.project.controller.grupo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;

import com.ufcg.psoft.project.dto.grupo.RegraPontuacaoPostPutRequestDTO;
import com.ufcg.psoft.project.service.grupo.pontuacao.RegraPontuacaoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(
    value = "/grupos",
    produces = MediaType.APPLICATION_JSON_VALUE
)
public class RegraPontuacaoController {
    @Autowired
    RegraPontuacaoService regraPontuacaoService;
    
    @PostMapping("/{grupoId}/regras-pontuacao")
	public ResponseEntity<?> inserirRegraPontuacao(
			@RequestParam Long usuarioId,
			@RequestParam String codigoAcesso,
			@PathVariable Long grupoId,
			@RequestBody @Valid RegraPontuacaoPostPutRequestDTO regraPontuacaoPostPutRequestDto) {
		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(regraPontuacaoService.inserirRegraPontuacao(usuarioId, codigoAcesso, grupoId, regraPontuacaoPostPutRequestDto));
	}

	@GetMapping("/{grupoId}/regras-pontuacao")
	public ResponseEntity<?> listarRegrasPontuacao(
			@RequestParam Long usuarioId,
			@RequestParam String codigoAcesso,
			@PathVariable Long grupoId) {
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(regraPontuacaoService.listarRegrasPontuacao(usuarioId, codigoAcesso, grupoId));
	}

	@DeleteMapping("/{grupoId}/regras-pontuacao/{regraId}")
	public ResponseEntity<?> removerRegraPontuacao(
			@RequestParam Long usuarioId,
			@RequestParam String codigoAcesso,
			@PathVariable Long grupoId,
			@PathVariable Long regraId) {
		regraPontuacaoService.removerRegraPontuacao(usuarioId, codigoAcesso, grupoId, regraId);
		return ResponseEntity
				.status(HttpStatus.NO_CONTENT)
				.build();
	}
}
