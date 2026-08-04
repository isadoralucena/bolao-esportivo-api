package com.ufcg.psoft.project.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;
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

import com.ufcg.psoft.project.dto.convite.ConvitePostPutRequestDTO;
import com.ufcg.psoft.project.dto.convite.ConviteResponseDTO;
import com.ufcg.psoft.project.repository.UsuarioRepository;
import com.ufcg.psoft.project.service.convite.ConviteService;
import com.ufcg.psoft.project.service.premium.RequisicaoAutenticadaEvent;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.MediaType; 
import jakarta.validation.Valid;

@RestController
@RequestMapping(
    value = "/convites",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@RequiredArgsConstructor
public class ConviteController {
    
    private final ConviteService conviteService;

    private final ApplicationEventPublisher eventPublisher;

    private final UsuarioRepository usuarioRepository;

    @PostMapping("")
    public ResponseEntity<ConviteResponseDTO> criarConvite(
        @RequestParam String codigoUsuario,
        @RequestBody @Valid ConvitePostPutRequestDTO convitePostPutRequestDto
        ) {
        var resultado = conviteService.criar(codigoUsuario, convitePostPutRequestDto);
        eventPublisher.publishEvent(new RequisicaoAutenticadaEvent(convitePostPutRequestDto.getOrganizador()));
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(resultado);

    }

    @PostMapping("/{id}/aceitar")
    public ResponseEntity<ConviteResponseDTO> aceitarConvite(
            @RequestParam String codigoUsuario,
            @PathVariable Long id) {
        var resultado = conviteService.aceitar(id, codigoUsuario);
        publicarEventoPorCodigo(codigoUsuario);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(resultado);
    }

    @PostMapping("/{id}/recusar")
    public ResponseEntity<ConviteResponseDTO> recusarConvite(
            @RequestParam String codigoUsuario,
            @PathVariable Long id) {
        var resultado = conviteService.recusar(id, codigoUsuario);
        publicarEventoPorCodigo(codigoUsuario);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(resultado);
    }

    @PostMapping("/{id}/ignorar")
    public ResponseEntity<ConviteResponseDTO> ignorarConvite(
            @RequestParam String codigoUsuario,
            @PathVariable Long id) {
        var resultado = conviteService.ignorar(id, codigoUsuario);
        publicarEventoPorCodigo(codigoUsuario);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(resultado);
    }

    @DeleteMapping("/{id}/remover")
    public ResponseEntity<Void> removerConvite(
            @RequestParam String codigoUsuario,
            @PathVariable Long id) {
        conviteService.remover(id, codigoUsuario);
        publicarEventoPorCodigo(codigoUsuario);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @GetMapping("/usuario/{convidadoId}/pendentes")
    public ResponseEntity<List<ConviteResponseDTO>> listarConvitesPendentes(
            @RequestParam String codigoUsuario,
            @PathVariable Long convidadoId) {
        var resultado = conviteService.listarConvitesPendentesPorConvidado(convidadoId, codigoUsuario);
        eventPublisher.publishEvent(new RequisicaoAutenticadaEvent(convidadoId));
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(resultado);
    }

    private void publicarEventoPorCodigo(String codigo) {
        usuarioRepository.findByCodigoIgnoreCase(codigo)
                .ifPresent(u -> eventPublisher.publishEvent(new RequisicaoAutenticadaEvent(u.getId())));
    }

}
