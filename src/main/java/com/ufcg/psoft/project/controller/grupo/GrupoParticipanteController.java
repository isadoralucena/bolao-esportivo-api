package com.ufcg.psoft.project.controller.grupo;

import lombok.RequiredArgsConstructor;

import java.util.Set;

import org.springframework.context.ApplicationEventPublisher;
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

import com.ufcg.psoft.project.dto.grupo.GrupoResponseDTO;
import com.ufcg.psoft.project.dto.usuario.UsuarioResponseDTO;
import com.ufcg.psoft.project.service.grupo.participante.GrupoParticipanteService;
import com.ufcg.psoft.project.service.premium.RequisicaoAutenticadaEvent;

@RestController
@RequestMapping(
    value = "/grupos",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@RequiredArgsConstructor
public class GrupoParticipanteController {
    final GrupoParticipanteService grupoParticipanteService;

    private final ApplicationEventPublisher eventPublisher;

    @GetMapping("/{grupoId}/participantes")
    public ResponseEntity<Set<UsuarioResponseDTO>> listarParticipantes(
            @RequestParam Long usuarioId,
            @RequestParam String codigoUsuario,
            @PathVariable Long grupoId) {
        var resultado = grupoParticipanteService.listarParticipantes(usuarioId, codigoUsuario, grupoId);
        eventPublisher.publishEvent(new RequisicaoAutenticadaEvent(usuarioId));
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(resultado);
    }

    @DeleteMapping("/{grupoId}/participantes/{participanteId}")
    public ResponseEntity<Void> removerParticipante(
            @RequestParam Long usuarioId,
            @RequestParam String codigoUsuario,
            @PathVariable Long grupoId,
            @PathVariable Long participanteId) {
        grupoParticipanteService.removerParticipante(usuarioId, codigoUsuario, grupoId, participanteId);
        eventPublisher.publishEvent(new RequisicaoAutenticadaEvent(usuarioId));
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @PostMapping("/{grupoId}/entrar")
    public ResponseEntity<GrupoResponseDTO> entrarEmGrupoPublico(
                @RequestParam Long usuarioId,
                @RequestParam String codigoUsuario,
                @PathVariable Long grupoId) {
        var resultado = grupoParticipanteService.entrarEmGrupoPublico(grupoId, usuarioId, codigoUsuario);
        eventPublisher.publishEvent(new RequisicaoAutenticadaEvent(usuarioId));
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(resultado);
    
	}
}