package com.ufcg.psoft.project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.ufcg.psoft.project.dto.palpite.PalpitePostPutRequestDTO;
import com.ufcg.psoft.project.dto.palpite.PalpiteResponseDTO;
import com.ufcg.psoft.project.service.palpite.PalpiteService;
import com.ufcg.psoft.project.service.premium.RequisicaoAutenticadaEvent;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Palpites", description = "Criação, consulta e gerenciamento de palpites")
public class PalpiteController {

    private final PalpiteService palpiteService;

    private final ApplicationEventPublisher eventPublisher;

    @Operation(summary = "Criar palpite para uma partida")
    @PostMapping("/grupos/{grupoId}/partidas/{partidaId}/palpites")
    public ResponseEntity<PalpiteResponseDTO> criarPalpite(
            @PathVariable Long grupoId,
            @PathVariable Long partidaId,
            @RequestParam Long usuarioId,
            @RequestParam String codigoUsuario,
            @RequestBody @Valid PalpitePostPutRequestDTO dto) {
        var resultado = palpiteService.criar(usuarioId, codigoUsuario, grupoId, partidaId, dto);
        eventPublisher.publishEvent(new RequisicaoAutenticadaEvent(usuarioId));
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(resultado);
    }

    @Operation(summary = "Listar palpites da partida no grupo")
    @GetMapping("/grupos/{grupoId}/partidas/{partidaId}/palpites")
    public ResponseEntity<List<PalpiteResponseDTO>> listarPalpitesDaPartida(
            @PathVariable Long grupoId,
            @PathVariable Long partidaId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(palpiteService.listarPorGrupoEPartida(grupoId, partidaId));
    }

    @Operation(summary = "Listar palpites do grupo")
    @GetMapping("/grupos/{grupoId}/palpites")
    public ResponseEntity<List<PalpiteResponseDTO>> listarPalpitesDoGrupo(@PathVariable Long grupoId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(palpiteService.listarPorGrupo(grupoId));
    }

    @Operation(summary = "Listar palpites do usuário")
    @GetMapping("/usuarios/{usuarioId}/palpites")
    public ResponseEntity<List<PalpiteResponseDTO>> listarPalpitesDoUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(palpiteService.listarPorUsuario(usuarioId));
    }

    @Operation(summary = "Editar palpite")
    @PutMapping("/grupos/{grupoId}/partidas/{partidaId}/palpites/{palpiteId}")
    public ResponseEntity<PalpiteResponseDTO> editarPalpite(
            @PathVariable Long grupoId,
            @PathVariable Long partidaId,
            @PathVariable Long palpiteId,
            @RequestParam Long usuarioId,
            @RequestParam String codigoUsuario,
            @RequestBody @Valid PalpitePostPutRequestDTO dto) {
        var resultado = palpiteService.editar(palpiteId, usuarioId, codigoUsuario, dto);
        eventPublisher.publishEvent(new RequisicaoAutenticadaEvent(usuarioId));
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(resultado);
    }

    @Operation(summary = "Excluir palpite")
    @DeleteMapping("/grupos/{grupoId}/partidas/{partidaId}/palpites/{palpiteId}")
    public ResponseEntity<Void> deletarPalpite(
            @PathVariable Long grupoId,
            @PathVariable Long partidaId,
            @PathVariable Long palpiteId,
            @RequestParam Long usuarioId,
            @RequestParam String codigoUsuario) {
        palpiteService.deletar(palpiteId, usuarioId, codigoUsuario);
        eventPublisher.publishEvent(new RequisicaoAutenticadaEvent(usuarioId));
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}