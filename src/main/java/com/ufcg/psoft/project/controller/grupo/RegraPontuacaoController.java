package com.ufcg.psoft.project.controller.grupo;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
import com.ufcg.psoft.project.service.premium.RequisicaoAutenticadaEvent;

import jakarta.validation.Valid;

@RestController
@RequestMapping(
    value = "/grupos",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@RequiredArgsConstructor
public class RegraPontuacaoController {
    final RegraPontuacaoService regraPontuacaoService;

    private final ApplicationEventPublisher eventPublisher;
    
    @PostMapping("/{grupoId}/regras-pontuacao")
	public ResponseEntity<?> inserirRegraPontuacao(
			@RequestParam Long usuarioId,
			@RequestParam String codigoUsuario,
			@PathVariable Long grupoId,
			@RequestBody @Valid RegraPontuacaoPostPutRequestDTO regraPontuacaoPostPutRequestDto) {
		var resultado = regraPontuacaoService.inserirRegraPontuacao(usuarioId, codigoUsuario, grupoId, regraPontuacaoPostPutRequestDto);
		eventPublisher.publishEvent(new RequisicaoAutenticadaEvent(usuarioId));
		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(resultado);
	}

	@GetMapping("/{grupoId}/regras-pontuacao")
	public ResponseEntity<?> listarRegrasPontuacao(
			@RequestParam Long usuarioId,
			@RequestParam String codigoUsuario,
			@PathVariable Long grupoId) {
		var resultado = regraPontuacaoService.listarRegrasPontuacao(usuarioId, codigoUsuario, grupoId);
		eventPublisher.publishEvent(new RequisicaoAutenticadaEvent(usuarioId));
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(resultado);
	}

	@DeleteMapping("/{grupoId}/regras-pontuacao/{regraId}")
	public ResponseEntity<?> removerRegraPontuacao(
			@RequestParam Long usuarioId,
			@RequestParam String codigoUsuario,
			@PathVariable Long grupoId,
			@PathVariable Long regraId) {
		regraPontuacaoService.removerRegraPontuacao(usuarioId, codigoUsuario, grupoId, regraId);
		eventPublisher.publishEvent(new RequisicaoAutenticadaEvent(usuarioId));
		return ResponseEntity
				.status(HttpStatus.NO_CONTENT)
				.build();
	}
}
