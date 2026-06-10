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
            @RequestParam String codigoAcesso,
            @RequestBody @Valid GrupoPostRequestDTO grupoPostRequestDto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(grupoService.criar(grupoPostRequestDto, codigoAcesso));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> recuperarGrupo(
            @RequestParam String codigoAcesso,
            @PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(grupoService.recuperar(id, codigoAcesso));
    }

    @GetMapping("")
    public ResponseEntity<?> listarGrupos(
            @RequestParam String codigoAcesso
        ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(grupoService.listar(codigoAcesso));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarGrupo(
            @RequestParam String codigoAcesso,
            @PathVariable Long id,
            @RequestBody @Valid GrupoPutRequestDTO grupoPutRequestDto) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(grupoService.alterar(id, grupoPutRequestDto, codigoAcesso));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> removerGrupo(
            @RequestParam String codigoAcesso,
            @PathVariable Long id) {
        grupoService.remover(id, codigoAcesso);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @PostMapping("/{id}/participantes")
    public ResponseEntity<?> adicionarParticipante(
            @RequestParam String codigoAcesso,
            @PathVariable Long id,
            @RequestBody @Valid ParticipantePostRequestDTO participantePostRequestDto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(grupoService.adicionarParticipante(id, participantePostRequestDto, codigoAcesso));
    }

    @GetMapping("/{id}/participantes")
    public ResponseEntity<?> listarParticipantes(
            @RequestParam String codigoAcesso,
            @PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(grupoService.listarParticipantes(id, codigoAcesso));
    }

    @DeleteMapping("/{id}/participantes/{usuarioId}")
    public ResponseEntity<?> removerParticipante(
            @RequestParam String codigoAcesso,
            @PathVariable Long id,
            @PathVariable Long usuarioId) {
        grupoService.removerParticipante(id, usuarioId, codigoAcesso);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}