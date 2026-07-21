package com.ufcg.psoft.project.controller.grupo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;

import com.ufcg.psoft.project.service.grupo.participante.GrupoParticipanteService;

@RestController
@RequestMapping(
    value = "/grupos",
    produces = MediaType.APPLICATION_JSON_VALUE
)
public class GrupoParticipanteController {
    @Autowired
    GrupoParticipanteService grupoParticipanteService;

    @GetMapping("/{grupoId}/participantes")
    public ResponseEntity<?> listarParticipantes(
            @RequestParam Long usuarioId,
            @RequestParam String codigoAcesso,
            @PathVariable Long grupoId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(grupoParticipanteService.listarParticipantes(usuarioId, codigoAcesso, grupoId));
    }

    @DeleteMapping("/{grupoId}/participantes/{participanteId}")
    public ResponseEntity<?> removerParticipante(
            @RequestParam Long usuarioId,
            @RequestParam String codigoAcesso,
            @PathVariable Long grupoId,
            @PathVariable Long participanteId) {
        grupoParticipanteService.removerParticipante(usuarioId, codigoAcesso, grupoId, participanteId);
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
                .body(grupoParticipanteService.entrarEmGrupoPublico(grupoId, usuarioId, codigoAcesso));
    
	}
}
