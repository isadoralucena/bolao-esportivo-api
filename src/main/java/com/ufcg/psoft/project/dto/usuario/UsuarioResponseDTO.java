package com.ufcg.psoft.project.dto.usuario;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufcg.psoft.project.model.PerfilUsuario;
import com.ufcg.psoft.project.model.Usuario;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
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
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @JsonProperty("nome")
    @NotBlank(message = "Nome obrigatorio")
    private String nome;

    @JsonProperty("email")
    @NotBlank(message = "Email obrigatorio")
    private String email;

    @JsonProperty("username")
    @NotBlank(message = "Username obrigatorio")
    private String username;

    @JsonProperty("endereco")
    @NotBlank(message = "Endereco obrigatorio")
    private String endereco;

    @JsonProperty("perfil")
    @NotBlank(message = "Perfil obrigatorio")
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
