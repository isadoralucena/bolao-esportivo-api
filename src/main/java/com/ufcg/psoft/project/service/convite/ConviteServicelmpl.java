package com.ufcg.psoft.project.service.convite;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ufcg.psoft.project.dto.convite.ConvitePostPutRequestDTO;
import com.ufcg.psoft.project.dto.convite.ConviteResponseDTO;
import com.ufcg.psoft.project.exception.campeonato.CampeonatoInativoException;
import com.ufcg.psoft.project.exception.convite.ConviteDuplicadoException;
import com.ufcg.psoft.project.exception.convite.ConviteJaProcessadoException;
import com.ufcg.psoft.project.exception.convite.ConviteNaoExisteException;
import com.ufcg.psoft.project.exception.convite.OrganizadorInvalidoException;
import com.ufcg.psoft.project.exception.convite.PrivacidadeInvalidaException;
import com.ufcg.psoft.project.exception.grupo.GrupoNaoExisteException;
import com.ufcg.psoft.project.exception.usuario.UsuarioInvalidoException;
import com.ufcg.psoft.project.exception.usuario.UsuarioJaParticipanteException;
import com.ufcg.psoft.project.exception.usuario.UsuarioNaoExisteException;
import com.ufcg.psoft.project.model.Convite;
import com.ufcg.psoft.project.model.Grupo;
import com.ufcg.psoft.project.model.PrivacidadeGrupo;
import com.ufcg.psoft.project.model.StatusConvite;
import com.ufcg.psoft.project.model.Usuario;
import com.ufcg.psoft.project.repository.ConviteRepository;
import com.ufcg.psoft.project.repository.GrupoRepository;
import com.ufcg.psoft.project.repository.UsuarioRepository;
import com.ufcg.psoft.project.service.grupo.GrupoService;

@Service
public class ConviteServicelmpl implements ConviteService {

    @Autowired
    private ConviteRepository conviteRepository;

    @Autowired
    private GrupoRepository grupoRepository;

    @Autowired 
    private UsuarioRepository usuarioRepository;

    @Autowired
    private GrupoService grupoService;

    @Override
    public ConviteResponseDTO criar(String codigoAcessoOrganizador, ConvitePostPutRequestDTO convitePostPutRequestDTO) {
        Usuario organizador = obterUsuario(convitePostPutRequestDTO.getOrganizador());
        
        validarUsuário(organizador, codigoAcessoOrganizador);

        Usuario convidado = obterUsuario(convitePostPutRequestDTO.getConvidado());
        Grupo grupo = obterGrupo(convitePostPutRequestDTO.getGrupo());

        if (!organizador.equals(grupo.getOrganizador())) {
            throw new OrganizadorInvalidoException();
        }

        if (grupo.getPrivacidade() == PrivacidadeGrupo.PUBLICA) {
            throw new PrivacidadeInvalidaException();
        }

        if (grupo.getParticipantes().contains(convidado)) {
            throw new UsuarioJaParticipanteException();
        }

        if (conviteRepository.existsByGrupoAndConvidadoAndStatus(grupo, convidado, StatusConvite.PENDENTE)) {
            throw new ConviteDuplicadoException();
        }

        if (!grupo.getCampeonato().getAtivo()) {
            throw new CampeonatoInativoException();
        }

        Convite convite = Convite.builder()
                .descricao(convitePostPutRequestDTO.getDescricao())
                .grupo(grupo)
                .organizador(organizador)
                .convidado(convidado)
                .build();
        
        this.conviteRepository.save(convite);
        notificarConvidado(convite, "criado");

        return toResponseDTO(convite);
    }

    @Override
    public void remover(Long id, String codigoAcessoOrganizador) {
        Convite convite = obterConvite(id);
        Usuario organizador = convite.getOrganizador();
        
        validarUsuário(organizador, codigoAcessoOrganizador);

        this.conviteRepository.delete(convite);
    }

    @Override
    public ConviteResponseDTO aceitar(Long id, String codigoAcesso) {
        Convite convite = obterConvite(id);

        if (convite.getStatus() != StatusConvite.PENDENTE) {
            throw new ConviteJaProcessadoException();
        }

        Usuario convidado = convite.getConvidado();
        validarUsuário(convidado, codigoAcesso);

        Grupo grupo = convite.getGrupo();
        grupoService.validarEntradaGrupo(grupo, convidado);

        grupo.getParticipantes().add(convidado);
        convite.setStatus(StatusConvite.ACEITO);

        this.conviteRepository.save(convite);
        this.grupoRepository.save(grupo);
        notificarConvidado(convite, "aceito");

        return toResponseDTO(convite);
    }

    @Override
    public ConviteResponseDTO recusar(Long id, String codigoAcesso) {
        Convite convite = obterConvite(id);
        
        if (convite.getStatus() != StatusConvite.PENDENTE) {
            throw new ConviteJaProcessadoException();
        }

        validarUsuário(convite.getConvidado(), codigoAcesso);

        convite.setStatus(StatusConvite.RECUSADO);
        this.conviteRepository.save(convite);
        notificarConvidado(convite, "recusado");

        return toResponseDTO(convite);
    }

    @Override
    public ConviteResponseDTO ignorar(Long id, String codigoAcesso) {
        Convite convite = obterConvite(id);
        
        if (convite.getStatus() != StatusConvite.PENDENTE) {
            throw new ConviteJaProcessadoException();
        }

        validarUsuário(convite.getConvidado(), codigoAcesso);

        convite.setStatus(StatusConvite.IGNORADO);
        this.conviteRepository.save(convite);
        notificarConvidado(convite, "ignorado");

        return toResponseDTO(convite);
    }

    @Override
    public List<ConviteResponseDTO> listarConvitesPendentesPorConvidado(Long convidadoId, String codigoAcesso) {
        Usuario convidado = obterUsuario(convidadoId);

        validarUsuário(convidado, codigoAcesso);

        List<Convite> convitesPendentes = conviteRepository.findByConvidadoIdAndStatus(convidadoId, StatusConvite.PENDENTE);

        return convitesPendentes.stream()
                .map(this::toResponseDTO)
                .toList();
    }
    
    private Usuario obterUsuario(Long id) {
        return this.usuarioRepository.findById(id)
                .orElseThrow(UsuarioNaoExisteException::new);
    }

    private Grupo obterGrupo(Long id) {
        return this.grupoRepository.findById(id)
                .orElseThrow(GrupoNaoExisteException::new);
    }

    private Convite obterConvite(Long id) {
        return this.conviteRepository.findById(id)
                .orElseThrow(ConviteNaoExisteException::new);
    }

    private void notificarConvidado(Convite convite, String status) {
        String mensagem = String.format("Notificação: O convite para %s participar do grupo %s foi %s.", 
                convite.getConvidado().getNome(), 
                convite.getGrupo().getNome(), 
                status);
        System.out.println(mensagem);
    }

    private void validarUsuário(Usuario usuario, String codigoAcesso) {
        if (!usuario.getCodigo().equals(codigoAcesso)) throw new UsuarioInvalidoException();
    }

    private ConviteResponseDTO toResponseDTO(Convite convite) {
        return ConviteResponseDTO.builder()
                .id(convite.getId())
                .grupo(convite.getGrupo().getId())
                .organizador(convite.getOrganizador().getId())
                .convidado(convite.getConvidado().getId())
                .status(convite.getStatus())
                .build();
    }
}
