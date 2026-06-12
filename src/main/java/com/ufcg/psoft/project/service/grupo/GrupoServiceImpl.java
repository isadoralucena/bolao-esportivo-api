package com.ufcg.psoft.project.service.grupo;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ufcg.psoft.project.dto.grupo.GrupoPostRequestDTO;
import com.ufcg.psoft.project.dto.grupo.GrupoPutRequestDTO;
import com.ufcg.psoft.project.dto.grupo.GrupoResponseDTO;
import com.ufcg.psoft.project.dto.grupo.ParticipantePostRequestDTO;
import com.ufcg.psoft.project.dto.usuario.UsuarioResponseDTO;
import com.ufcg.psoft.project.exception.CampeonatoNaoExisteException;
import com.ufcg.psoft.project.exception.CodigoDeAcessoInvalidoException;
import com.ufcg.psoft.project.exception.GrupoNaoExisteException;
import com.ufcg.psoft.project.exception.LimiteDeParticipantesAtingidoException;
import com.ufcg.psoft.project.exception.PermissaoNegadaException;
import com.ufcg.psoft.project.exception.UsuarioNaoExisteException;
import com.ufcg.psoft.project.model.Campeonato;
import com.ufcg.psoft.project.model.Grupo;
import com.ufcg.psoft.project.model.Privacidade;
import com.ufcg.psoft.project.model.Usuario;
import com.ufcg.psoft.project.repository.CampeonatoRepository;
import com.ufcg.psoft.project.repository.GrupoRepository;
import com.ufcg.psoft.project.repository.UsuarioRepository;
import com.ufcg.psoft.project.exception.UsuarioJaParticipanteException;
import com.ufcg.psoft.project.exception.CampeonatoInativoException;

@Service
public class GrupoServiceImpl implements GrupoService {
    @Autowired
    GrupoRepository grupoRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private CampeonatoRepository campeonatoRepository;
    @Autowired
    ModelMapper modelMapper;

    public GrupoResponseDTO criar(GrupoPostRequestDTO grupoPostRequestDto, String codigoAcesso) {
        Usuario usuarioLogado = obterUsuario(codigoAcesso);

        if (!usuarioLogado.getId().equals(grupoPostRequestDto.getOrganizadorId())) {
            throw new CodigoDeAcessoInvalidoException();
        }

        Campeonato campeonato = campeonatoRepository.findById(grupoPostRequestDto.getCampeonatoId())
            .orElseThrow(CampeonatoNaoExisteException::new);
        
        Grupo grupo = modelMapper.map(grupoPostRequestDto, Grupo.class);
        grupo.setOrganizador(usuarioLogado);
        grupo.setCampeonato(campeonato);

        grupoRepository.save(grupo);
        return modelMapper.map(grupo, GrupoResponseDTO.class);
    }

    public GrupoResponseDTO recuperar(Long id, String codigoAcesso) {
        Usuario usuarioLogado = obterUsuario(codigoAcesso);
        Grupo grupo = grupoRepository.findById(id).orElseThrow(GrupoNaoExisteException::new);

        if (grupo.getPrivacidade() == Privacidade.PRIVADA) {
            boolean isMembroOuDono = grupo.getParticipantes().contains(usuarioLogado) || grupo.getOrganizador().equals(usuarioLogado);
            if (!isMembroOuDono) {
                throw new PermissaoNegadaException();
            }
        }

        return modelMapper.map(grupo, GrupoResponseDTO.class);
    }
    public List<GrupoResponseDTO> listar(String codigoAcesso) {
        Usuario usuarioLogado = obterUsuario(codigoAcesso);
        List<Grupo> grupos = grupoRepository.findAll();
        
        return grupos.stream()
                .filter(g -> g.getPrivacidade() == Privacidade.PUBLICA || g.getParticipantes().contains(usuarioLogado))
                .map(grupo -> modelMapper.map(grupo, GrupoResponseDTO.class))
                .collect(Collectors.toList());
    }
    public GrupoResponseDTO alterar(Long id, GrupoPutRequestDTO grupoPutRequestDto, String codigoAcesso) {
        Grupo grupo = grupoRepository.findById(id).orElseThrow(GrupoNaoExisteException::new);

        Usuario usuarioLogado = obterUsuario(codigoAcesso);
        if (!grupo.getOrganizador().equals(usuarioLogado)) {
            throw new PermissaoNegadaException();
        }
        modelMapper.map(grupoPutRequestDto, grupo);
        grupoRepository.save(grupo);
        return modelMapper.map(grupo, GrupoResponseDTO.class);
    }
    public void remover(Long id, String codigoAcesso) {
        Grupo grupo = grupoRepository.findById(id).orElseThrow(GrupoNaoExisteException::new);
        Usuario usuarioLogado = obterUsuario(codigoAcesso);
        if (!grupo.getOrganizador().equals(usuarioLogado)) {
            throw new PermissaoNegadaException();
        }

        grupoRepository.delete(grupo);
    }

    public GrupoResponseDTO adicionarParticipante(Long grupoId, ParticipantePostRequestDTO participantePostRequestDto, String codigoAcesso) {
        Grupo grupo = grupoRepository.findById(grupoId).orElseThrow(GrupoNaoExisteException::new);
        Usuario usuarioLogado = obterUsuario(codigoAcesso);

        if (grupo.getLimiteParticipantes() != null && grupo.getParticipantes().size() >= grupo.getLimiteParticipantes()) {
            throw new LimiteDeParticipantesAtingidoException(); 
        }

        Usuario participanteParaAdicionar = usuarioRepository.findById(participantePostRequestDto.getUsuarioId())
                .orElseThrow(UsuarioNaoExisteException::new);

        if (grupo.getPrivacidade() == Privacidade.PRIVADA) {
            if (!grupo.getOrganizador().equals(usuarioLogado)) {
                throw new PermissaoNegadaException();
            }
        } else {
            if (!usuarioLogado.equals(participanteParaAdicionar) && !grupo.getOrganizador().equals(usuarioLogado)) {
                throw new PermissaoNegadaException();
            }
        }

        grupo.getParticipantes().add(participanteParaAdicionar);
        grupoRepository.save(grupo);
        return modelMapper.map(grupo, GrupoResponseDTO.class);
    }

    public void removerParticipante(Long grupoId, Long usuarioId, String codigoAcesso) {
        Grupo grupo = grupoRepository.findById(grupoId).orElseThrow(GrupoNaoExisteException::new);
        Usuario usuarioLogado = obterUsuario(codigoAcesso);

        Usuario participante = usuarioRepository.findById(usuarioId)
                .orElseThrow(UsuarioNaoExisteException::new);

        boolean podeRemover = grupo.getOrganizador().equals(usuarioLogado) || usuarioLogado.equals(participante);
        if (!podeRemover) {
            throw new PermissaoNegadaException();
        }

        grupo.getParticipantes().remove(participante);
        grupoRepository.save(grupo);
    }

    public Set<UsuarioResponseDTO> listarParticipantes(Long grupoId, String codigoAcesso) {
        Grupo grupo = grupoRepository.findById(grupoId).orElseThrow(GrupoNaoExisteException::new);
        Usuario usuarioLogado = obterUsuario(codigoAcesso);

        if (grupo.getPrivacidade() == Privacidade.PRIVADA) {
            boolean isMembroOuDono = grupo.getParticipantes().contains(usuarioLogado) || grupo.getOrganizador().equals(usuarioLogado);
            if (!isMembroOuDono) {
                throw new PermissaoNegadaException();
            }
        }

        return grupo.getParticipantes().stream()
                .map(UsuarioResponseDTO::new)
                .collect(Collectors.toSet());
    }

    public GrupoResponseDTO entrarEmGrupoPublico(Long grupoId, String codigoAcesso) {
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(GrupoNaoExisteException::new);
        
        Usuario usuarioLogado = obterUsuario(codigoAcesso);
        
        validarEntradaEmGrupoPublico(grupo, usuarioLogado);
        
        grupo.getParticipantes().add(usuarioLogado);
        grupoRepository.save(grupo);
        return modelMapper.map(grupo, GrupoResponseDTO.class);
    }

    private void validarEntradaEmGrupoPublico(Grupo grupo, Usuario usuario) {
        if (grupo.getPrivacidade() == Privacidade.PRIVADA) {
            throw new PermissaoNegadaException();
        }
        if (!grupo.getCampeonato().getAtivo()) {
            throw new CampeonatoInativoException();
        }
        if (grupo.getParticipantes().contains(usuario)) {
            throw new UsuarioJaParticipanteException();
        }
        if (grupo.getLimiteParticipantes() != null &&
            grupo.getParticipantes().size() >= grupo.getLimiteParticipantes()) {
            throw new LimiteDeParticipantesAtingidoException();
        }
    }

    private Usuario obterUsuario(String codigoAcesso) {
        return usuarioRepository.findByCodigoIgnoreCase(codigoAcesso)
                .orElseThrow(CodigoDeAcessoInvalidoException::new);
    }
}
