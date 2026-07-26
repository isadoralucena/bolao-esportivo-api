package com.ufcg.psoft.project.controller;

import com.ufcg.psoft.project.service.recomendacao.RecomendacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class RecomendacaoController {

    @Autowired
    private RecomendacaoService recomendacaoService;

    @GetMapping("/grupos/{grupoId}/partidas/{partidaId}/recomendacao")
    public ResponseEntity<?> recomendarPalpite(
            @PathVariable Long grupoId,
            @PathVariable Long partidaId,
            @RequestParam Long usuarioId,
            @RequestParam String codigoUsuario) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(recomendacaoService.recomendar(grupoId, partidaId, usuarioId, codigoUsuario));
    }
}