package com.ufcg.psoft.project.controller;

import java.util.List;

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

import com.ufcg.psoft.project.dto.convite.ConvitePostPutRequestDTO;
import com.ufcg.psoft.project.dto.convite.ConviteResponseDTO;
import com.ufcg.psoft.project.service.convite.ConviteService;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.MediaType; 
import jakarta.validation.Valid;

@RestController
@RequestMapping(
    value = "/convites",
    produces = MediaType.APPLICATION_JSON_VALUE
)
public class ConviteController {
    
    @Autowired
    private ConviteService conviteService;

    @PostMapping("")
    public ResponseEntity<ConviteResponseDTO> criarConvite(
        @RequestParam String codigoAcesso,
        @RequestBody @Valid ConvitePostPutRequestDTO convitePostPutRequestDto
        ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(conviteService.criar(codigoAcesso, convitePostPutRequestDto));

    }

    @PostMapping("/{id}/aceitar")
    public ResponseEntity<ConviteResponseDTO> aceitarConvite(
            @RequestParam String codigoAcessoConvidado,
            @PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(conviteService.aceitar(id, codigoAcessoConvidado));
    }

    @PostMapping("/{id}/recusar")
    public ResponseEntity<ConviteResponseDTO> recusarConvite(
            @RequestParam String codigoAcessoConvidado,
            @PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(conviteService.recusar(id, codigoAcessoConvidado));
    }

    @PostMapping("/{id}/ignorar")
    public ResponseEntity<ConviteResponseDTO> ignorarConvite(
            @RequestParam String codigoAcessoConvidado,
            @PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(conviteService.ignorar(id, codigoAcessoConvidado));
    }

    @DeleteMapping("/{id}/remover")
    public ResponseEntity<Void> removerConvite(
            @RequestParam String codigoAcessoOrganizador,
            @PathVariable Long id) {
        conviteService.remover(id, codigoAcessoOrganizador);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @GetMapping("/usuario/{convidadoId}/pendentes")
    public ResponseEntity<List<ConviteResponseDTO>> listarConvitesPendentes(
            @RequestParam String codigoAcessoConvidado,
            @PathVariable Long convidadoId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(conviteService.listarConvitesPendentesPorConvidado(convidadoId, codigoAcessoConvidado));
    }

}
