package com.ufcg.psoft.project.dto.grupo;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufcg.psoft.project.dto.usuario.UsuarioResponseDTO;
import com.ufcg.psoft.project.dto.campeonato.CampeonatoResponseDTO;
import com.ufcg.psoft.project.model.Grupo;
import com.ufcg.psoft.project.model.PrivacidadeGrupo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrupoResponseDTO {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("nome")
	private String nome;

    @JsonProperty("descricao")
    private String descricao;

    @JsonProperty("campeonato")
    private CampeonatoResponseDTO campeonato;

    @JsonProperty("organizador")
    private UsuarioResponseDTO organizador;

    @JsonProperty("privacidade")
    private PrivacidadeGrupo privacidade;

    @JsonProperty("limiteParticipantes")
    private Integer limiteParticipantes;

    @JsonProperty("participantes")
    private Set<UsuarioResponseDTO> participantes;

    public GrupoResponseDTO(Grupo grupo) {
        this.id = grupo.getId();
        this.nome = grupo.getNome();
        this.descricao = grupo.getDescricao();
        this.campeonato = new CampeonatoResponseDTO(grupo.getCampeonato());
        this.organizador = new UsuarioResponseDTO(grupo.getOrganizador());
        this.participantes = grupo.getParticipantes().stream()
                .map(UsuarioResponseDTO::new)
                .collect(Collectors.toSet());
        this.privacidade = grupo.getPrivacidade();
        this.limiteParticipantes = grupo.getLimiteParticipantes();
    }
}