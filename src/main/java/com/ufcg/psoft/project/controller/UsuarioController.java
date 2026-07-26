package com.ufcg.psoft.project.controller;

import com.ufcg.psoft.project.dto.usuario.UsuarioPostPutRequestDTO;
import com.ufcg.psoft.project.service.premium.RequisicaoAutenticadaEvent;
import com.ufcg.psoft.project.service.usuario.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
public class UsuarioController {

    @Autowired
    UsuarioService usuarioService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @GetMapping("/{id}")
    public ResponseEntity<?> recuperarUsuario(
            @PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(usuarioService.recuperar(id));
    }

    @GetMapping("")
    public ResponseEntity<?> listarUsuarios(
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
    public ResponseEntity<?> criarUsuario(
            @RequestBody @Valid UsuarioPostPutRequestDTO usuarioPostPutRequestDto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(usuarioService.criar(usuarioPostPutRequestDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarUsuario(
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
    public ResponseEntity<?> obterPromocaoPremium(
            @PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(usuarioService.obterPromocao(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluirUsuario(
            @PathVariable Long id,
            @RequestParam String codigoUsuario) {
        usuarioService.remover(id, codigoUsuario);
        eventPublisher.publishEvent(new RequisicaoAutenticadaEvent(id));
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body("");
    }
}