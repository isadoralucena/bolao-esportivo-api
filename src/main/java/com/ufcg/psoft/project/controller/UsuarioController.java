package com.ufcg.psoft.project.controller;

import com.ufcg.psoft.project.dto.usuario.PromocaoPremiumResponseDTO;
import com.ufcg.psoft.project.dto.usuario.UsuarioPostPutRequestDTO;
import com.ufcg.psoft.project.dto.usuario.UsuarioResponseDTO;
import com.ufcg.psoft.project.service.premium.RequisicaoAutenticadaEvent;
import com.ufcg.psoft.project.service.usuario.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
        value = "/usuarios",
        produces = MediaType.APPLICATION_JSON_VALUE
)
@RequiredArgsConstructor
public class UsuarioController {

    final UsuarioService usuarioService;

    private final ApplicationEventPublisher eventPublisher;

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> recuperarUsuario(
            @PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(usuarioService.recuperar(id));
    }

    @GetMapping("")
    public ResponseEntity<List<UsuarioResponseDTO>> listarUsuarios(
            @RequestParam(required = false, defaultValue = "") String nome) {

        if (nome != null && !nome.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(usuarioService.listarPorNome(nome));
        }
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(usuarioService.listar());
    }

    @PostMapping()
    public ResponseEntity<UsuarioResponseDTO> criarUsuario(
            @RequestBody @Valid UsuarioPostPutRequestDTO usuarioPostPutRequestDto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(usuarioService.criar(usuarioPostPutRequestDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> atualizarUsuario(
            @PathVariable Long id,
            @RequestParam String codigoUsuario,
            @RequestBody @Valid UsuarioPostPutRequestDTO usuarioPostPutRequestDto) {
        var resultado = usuarioService.alterar(id, codigoUsuario, usuarioPostPutRequestDto);
        eventPublisher.publishEvent(new RequisicaoAutenticadaEvent(id));
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(resultado);
    }

    @GetMapping("/{id}/promocao-premium")
    public ResponseEntity<PromocaoPremiumResponseDTO> obterPromocaoPremium(
            @PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(usuarioService.obterPromocao(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirUsuario(
            @PathVariable Long id,
            @RequestParam String codigoUsuario) {
        usuarioService.remover(id, codigoUsuario);
        eventPublisher.publishEvent(new RequisicaoAutenticadaEvent(id));
        return ResponseEntity
				.status(HttpStatus.NO_CONTENT)
				.build();
    }
}
