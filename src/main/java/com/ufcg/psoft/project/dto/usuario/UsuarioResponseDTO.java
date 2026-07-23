package com.ufcg.psoft.project.dto.usuario;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufcg.psoft.project.model.PerfilUsuario;
import com.ufcg.psoft.project.model.Usuario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponseDTO {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("nome")
    private String nome;

    @JsonProperty("email")
    private String email;

    @JsonProperty("username")
    private String username;

    @JsonProperty("endereco")
    private String endereco;

    @JsonProperty("perfil")
    private PerfilUsuario perfil;

    public UsuarioResponseDTO(Usuario usuario) {
        this.id = usuario.getId();
        this.nome = usuario.getNome();
        this.email = usuario.getEmail();
        this.username = usuario.getUsername();
        this.endereco = usuario.getEndereco();
        this.perfil = usuario.getPerfil();
    }
}
