package com.ufcg.psoft.project.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ufcg.psoft.project.dto.convite.ConvitePostPutRequestDTO;
import com.ufcg.psoft.project.dto.convite.ConviteResponseDTO;
import com.ufcg.psoft.project.service.convite.ConviteService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
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

    @PostMapping
    public ResponseEntity<ConviteResponseDTO> criarConvite(@RequestBody @Valid ConvitePostPutRequestDTO convitePostPutRequestDto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(conviteService.criar(convitePostPutRequestDto));

    }

    @PostMapping("/{id}/aceitar")
    public ResponseEntity<ConviteResponseDTO> aceitarConvite(
            @RequestParam Long idConvidado,
            @PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(conviteService.aceitar(id, idConvidado));
    }

    @PostMapping("/{id}/recusar")
    public ResponseEntity<ConviteResponseDTO> recusarConvite(
            @RequestParam Long idConvidado,
            @PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(conviteService.recusar(id, idConvidado));
    }

    @PostMapping("/{id}/ignorar")
    public ResponseEntity<ConviteResponseDTO> ignorarConvite(
            @RequestParam Long idConvidado,
            @PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(conviteService.ignorar(id, idConvidado));
    }

    @PostMapping("/{id}/remover")
    public ResponseEntity<Void> removerConvite(
            @RequestParam Long idOrganizador,
            @PathVariable Long id) {
        conviteService.remover(id, idOrganizador);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @PostMapping("/usuario/{convidadoId}/pendentes")
    public ResponseEntity<List<ConviteResponseDTO>> listarConvitesPendentes(
            @PathVariable Long convidadoId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(conviteService.listarConvitesPendentesPorConvidado(convidadoId));
    }

}
