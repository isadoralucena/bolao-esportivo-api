package com.ufcg.psoft.project.controller.grupo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;

import com.ufcg.psoft.project.dto.grupo.CriteriosDesempatePutRequestDTO;
import com.ufcg.psoft.project.service.grupo.desempate.CriterioDesempateService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(
    value = "/grupos",
    produces = MediaType.APPLICATION_JSON_VALUE
)
public class CriterioDesempateController {
    @Autowired
    CriterioDesempateService criterioDesempateService;
    
    @PutMapping("/{grupoId}/criterios-desempate")
	public ResponseEntity<?> configurarCriteriosDesempate(
					@RequestParam Long usuarioId,
					@RequestParam String codigoAcesso,
					@PathVariable Long grupoId,
					@RequestBody @Valid CriteriosDesempatePutRequestDTO criteriosDesempatePutRequestDTO) {
			return ResponseEntity
							.status(HttpStatus.OK)
							.body(criterioDesempateService.configurarCriteriosDesempate(grupoId, usuarioId, codigoAcesso, criteriosDesempatePutRequestDTO));
	}

	@GetMapping("/{grupoId}/criterios-desempate")
	public ResponseEntity<?> listarCriteriosDesempate(
					@RequestParam Long usuarioId,
					@RequestParam String codigoAcesso,
					@PathVariable Long grupoId) {
			return ResponseEntity
							.status(HttpStatus.OK)
							.body(criterioDesempateService.listarCriteriosDesempate(usuarioId, codigoAcesso, grupoId));
	}
}
