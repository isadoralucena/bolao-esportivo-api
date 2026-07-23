package com.ufcg.psoft.project.controller.grupo;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ufcg.psoft.project.dto.grupo.CriteriosDesempatePutRequestDTO;
import com.ufcg.psoft.project.dto.grupo.GrupoPostRequestDTO;
import com.ufcg.psoft.project.dto.grupo.GrupoPutRequestDTO;
import com.ufcg.psoft.project.dto.grupo.RegraPontuacaoPostPutRequestDTO;
import com.ufcg.psoft.project.dto.palpite.RegrasPalpitesRequestDTO;
import com.ufcg.psoft.project.service.grupo.GrupoService;
import com.ufcg.psoft.project.service.grupo.desempate.CriterioDesempateService;
import com.ufcg.psoft.project.service.grupo.participante.GrupoParticipanteService;
import com.ufcg.psoft.project.service.grupo.pontuacao.RegraPontuacaoService;
import com.ufcg.psoft.project.service.pontuacao.PontuacaoService;
import com.ufcg.psoft.project.service.ranking.RankingService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping(
    value = "/grupos",
    produces = MediaType.APPLICATION_JSON_VALUE
)
public class GrupoController {
    @Autowired
    GrupoService grupoService;
    @Autowired
    private PontuacaoService pontuacaoService;
    @Autowired
    private RankingService rankingService;

    @PostMapping("")
    public ResponseEntity<?> criarGrupo(
            @RequestParam Long usuarioId,
            @RequestParam String codigoAcesso,
            @RequestBody @Valid GrupoPostRequestDTO grupoPostRequestDto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(grupoService.criar(usuarioId, codigoAcesso, grupoPostRequestDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> recuperarGrupo(
            @RequestParam Long usuarioId,
            @RequestParam String codigoAcesso,
            @PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(grupoService.recuperar(usuarioId, codigoAcesso, id));
    }

    @GetMapping("")
    public ResponseEntity<?> listarGrupos(
            @RequestParam Long usuarioId,
            @RequestParam String codigoAcesso
        ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(grupoService.listar(usuarioId, codigoAcesso));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarGrupo(
            @RequestParam Long usuarioId,
            @RequestParam String codigoAcesso,
            @PathVariable Long id,
            @RequestBody @Valid GrupoPutRequestDTO grupoPutRequestDto) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(grupoService.alterar(usuarioId, codigoAcesso, id, grupoPutRequestDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> removerGrupo(
            @RequestParam Long usuarioId,
            @RequestParam String codigoAcesso,
            @PathVariable Long id) {
        grupoService.remover(usuarioId, codigoAcesso, id);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

	@PutMapping("/{grupoId}/regras-palpites")
    public ResponseEntity<?> configurarRegrasPalpites(
            @PathVariable Long grupoId,
            @RequestParam Long usuarioId,
            @RequestParam String codigo,
            @RequestBody @Valid RegrasPalpitesRequestDTO regrasPalpitesRequestDTO) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(grupoService.configurarRegrasPalpites(grupoId, usuarioId, codigo, regrasPalpitesRequestDTO));
    }

    @GetMapping("/{grupoId}/pontuacoes")
    public ResponseEntity<?> listarPontuacoesDoGrupo(
            @RequestParam Long usuarioId,
            @RequestParam String codigoAcesso,
            @PathVariable Long grupoId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(pontuacaoService.listarPontuacoesParticipantesDoGrupo(grupoId, usuarioId, codigoAcesso));
    }

    @GetMapping("{grupoId}/ranking")
    public ResponseEntity<?> rankingDoGrupo(
            @RequestParam Long usuarioId,
            @RequestParam String codigoAcesso,
            @PathVariable Long grupoId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(rankingService.rankingDoGrupo(grupoId, usuarioId, codigoAcesso));
    }
}