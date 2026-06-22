package com.ufcg.psoft.project.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ufcg.psoft.project.dto.grupo.GrupoPostRequestDTO;
import com.ufcg.psoft.project.dto.grupo.GrupoPutRequestDTO;
import com.ufcg.psoft.project.dto.grupo.ParticipantePostRequestDTO;
import com.ufcg.psoft.project.dto.grupo.RegraPontuacaoPostPutRequestDTO;
import com.ufcg.psoft.project.service.grupo.GrupoService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping(
    value = "/grupos",
    produces = MediaType.APPLICATION_JSON_VALUE
)
public class GrupoController {
    @Autowired
    GrupoService grupoService;

    @PostMapping("")
    public ResponseEntity<?> criarGrupo(
            @RequestParam Long usuarioId,
            @RequestParam String codigoAcesso,
            @RequestBody @Valid GrupoPostRequestDTO grupoPostRequestDto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(grupoService.criar(usuarioId, codigoAcesso, grupoPostRequestDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> recuperarGrupo(
            @RequestParam Long usuarioId,
            @RequestParam String codigoAcesso,
            @PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(grupoService.recuperar(usuarioId, codigoAcesso, id));
    }

    @GetMapping("")
    public ResponseEntity<?> listarGrupos(
            @RequestParam Long usuarioId,
            @RequestParam String codigoAcesso
        ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(grupoService.listar(usuarioId, codigoAcesso));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarGrupo(
            @RequestParam Long usuarioId,
            @RequestParam String codigoAcesso,
            @PathVariable Long id,
            @RequestBody @Valid GrupoPutRequestDTO grupoPutRequestDto) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(grupoService.alterar(usuarioId, codigoAcesso, id, grupoPutRequestDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> removerGrupo(
            @RequestParam Long usuarioId,
            @RequestParam String codigoAcesso,
            @PathVariable Long id) {
        grupoService.remover(usuarioId, codigoAcesso, id);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @PostMapping("/{grupoId}/participantes")
    public ResponseEntity<?> adicionarParticipante(
            @RequestParam Long usuarioId,
            @RequestParam String codigoAcesso,
            @PathVariable Long grupoId,
            @RequestBody @Valid ParticipantePostRequestDTO participantePostRequestDto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(grupoService.adicionarParticipante(usuarioId, codigoAcesso, grupoId, participantePostRequestDto));
    }

    @GetMapping("/{grupoId}/participantes")
    public ResponseEntity<?> listarParticipantes(
            @RequestParam Long usuarioId,
            @RequestParam String codigoAcesso,
            @PathVariable Long grupoId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(grupoService.listarParticipantes(usuarioId, codigoAcesso, grupoId));
    }

    @DeleteMapping("/{grupoId}/participantes/{participanteId}")
    public ResponseEntity<?> removerParticipante(
            @RequestParam Long usuarioId,
            @RequestParam String codigoAcesso,
            @PathVariable Long grupoId,
            @PathVariable Long participanteId) {
        grupoService.removerParticipante(usuarioId, codigoAcesso, grupoId, participanteId);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @PostMapping("/{grupoId}/entrar")
    public ResponseEntity<?> entrarEmGrupoPublico(
                @RequestParam Long usuarioId,
                @RequestParam String codigoAcesso,
                @PathVariable Long grupoId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(grupoService.entrarEmGrupoPublico(grupoId, usuarioId, codigoAcesso));
    
	}

	@PostMapping("/{grupoId}/regras-pontuacao")
	public ResponseEntity<?> inserirRegraPontuacao(
			@RequestParam Long usuarioId,
			@RequestParam String codigoAcesso,
			@PathVariable Long grupoId,
			@RequestBody @Valid RegraPontuacaoPostPutRequestDTO regraPontuacaoPostPutRequestDto) {
		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(grupoService.inserirRegraPontuacao(usuarioId, codigoAcesso, grupoId, regraPontuacaoPostPutRequestDto));
	}

	@GetMapping("/{grupoId}/regras-pontuacao")
	public ResponseEntity<?> listarRegrasPontuacao(
			@RequestParam Long usuarioId,
			@RequestParam String codigoAcesso,
			@PathVariable Long grupoId) {
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(grupoService.listarRegrasPontuacao(usuarioId, codigoAcesso, grupoId));
	}

	@DeleteMapping("/{grupoId}/regras-pontuacao/{regraId}")
	public ResponseEntity<?> removerRegraPontuacao(
			@RequestParam Long usuarioId,
			@RequestParam String codigoAcesso,
			@PathVariable Long grupoId,
			@PathVariable Long regraId) {
		grupoService.removerRegraPontuacao(usuarioId, codigoAcesso, grupoId, regraId);
		return ResponseEntity
				.status(HttpStatus.NO_CONTENT)
				.build();
	}
}