package com.ufcg.psoft.project.controller;

import com.ufcg.psoft.project.dto.palpite.PalpitePostPutRequestDTO;
import com.ufcg.psoft.project.service.palpite.PalpiteService;
import com.ufcg.psoft.project.service.premium.RequisicaoAutenticadaEvent;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class PalpiteController {

    @Autowired
    private PalpiteService palpiteService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @PostMapping("/grupos/{grupoId}/partidas/{partidaId}/palpites")
    public ResponseEntity<?> criarPalpite(
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

    @GetMapping("/grupos/{grupoId}/partidas/{partidaId}/palpites")
    public ResponseEntity<?> listarPalpitesDaPartida(
            @PathVariable Long grupoId,
            @PathVariable Long partidaId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(palpiteService.listarPorGrupoEPartida(grupoId, partidaId));
    }

    @GetMapping("/grupos/{grupoId}/palpites")
    public ResponseEntity<?> listarPalpitesDoGrupo(@PathVariable Long grupoId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(palpiteService.listarPorGrupo(grupoId));
    }

    @GetMapping("/usuarios/{usuarioId}/palpites")
    public ResponseEntity<?> listarPalpitesDoUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(palpiteService.listarPorUsuario(usuarioId));
    }

    @PutMapping("/grupos/{grupoId}/partidas/{partidaId}/palpites/{palpiteId}")
    public ResponseEntity<?> editarPalpite(
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

    @DeleteMapping("/grupos/{grupoId}/partidas/{partidaId}/palpites/{palpiteId}")
    public ResponseEntity<?> deletarPalpite(
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
