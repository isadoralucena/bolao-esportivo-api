package com.ufcg.psoft.project.service.grupo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.ufcg.psoft.project.exception.CodigoDeAcessoInvalidoException;
import com.ufcg.psoft.project.exception.grupo.PermissaoNegadaException;
import com.ufcg.psoft.project.exception.usuario.UsuarioNaoExisteException;
import com.ufcg.psoft.project.model.Grupo;
import com.ufcg.psoft.project.model.PrivacidadeGrupo;
import com.ufcg.psoft.project.model.Usuario;
import com.ufcg.psoft.project.repository.UsuarioRepository;

@Service
@RequiredArgsConstructor
public class GrupoAutorizacaoService {
    private final UsuarioRepository usuarioRepository;

    public Usuario obterUsuarioValido(Long usuarioId, String codigo) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(UsuarioNaoExisteException::new);

        if (!usuario.getCodigo().equals(codigo)) {
            throw new CodigoDeAcessoInvalidoException();
        }

        return usuario;
    }

    public void garantirOrganizador(Grupo grupo, Usuario usuario) {
        if (!grupo.getOrganizador().equals(usuario)) {
            throw new PermissaoNegadaException();
        }
    }

    public void garantirAcessoLeitura(Grupo grupo, Usuario usuario) {
        if (!temAcessoLeitura(grupo, usuario)) {
            throw new PermissaoNegadaException();
        }
    }

    public boolean temAcessoLeitura(Grupo grupo, Usuario usuario) {
        return grupo.getPrivacidade() == PrivacidadeGrupo.PUBLICA
                || grupo.getOrganizador().equals(usuario)
                || grupo.getParticipantes().contains(usuario);
    }
}
