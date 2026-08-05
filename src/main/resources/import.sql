-- Usuarios: administrador, usuarios padrao e usuario premium.
INSERT INTO usuario (id, nome, email, username, endereco, perfil, codigo, administrador) VALUES (10001, 'Administrador', 'admin@bolao.com', 'admin', 'Rua da Administracao, 1', 'PADRAO', '123456', TRUE);
INSERT INTO usuario (id, nome, email, username, endereco, perfil, codigo, administrador) VALUES (10002, 'Ana Silva', 'ana@bolao.com', 'ana.silva', 'Rua das Flores, 10', 'PREMIUM', '234567', FALSE);
INSERT INTO usuario (id, nome, email, username, endereco, perfil, codigo, administrador) VALUES (10003, 'Bruno Souza', 'bruno@bolao.com', 'bruno.souza', 'Avenida Central, 20', 'PADRAO', '345678', FALSE);
INSERT INTO usuario (id, nome, email, username, endereco, perfil, codigo, administrador) VALUES (10004, 'Carla Lima', 'carla@bolao.com', 'carla.lima', 'Rua do Sol, 30', 'PADRAO', '456789', FALSE);
INSERT INTO usuario (id, nome, email, username, endereco, perfil, codigo, administrador) VALUES (10005, 'Diego Alves', 'diego@bolao.com', 'diego.alves', 'Avenida Brasil, 40', 'PADRAO', '567890', FALSE);

-- Campeonatos ativos e inativo.
INSERT INTO campeonato (id, nome, url, codigo, ativo, ultima_sincronizacao) VALUES (11001, 'Campeonato Brasileiro Serie A', 'https://api.football-data.org/v4/competitions/BSA', 'BSA', TRUE, DATEADD('MINUTE', -15, CURRENT_TIMESTAMP));
INSERT INTO campeonato (id, nome, url, codigo, ativo, ultima_sincronizacao) VALUES (11002, 'Copa Libertadores', 'https://api.football-data.org/v4/competitions/CLI', 'CLI', TRUE, DATEADD('MINUTE', -30, CURRENT_TIMESTAMP));
INSERT INTO campeonato (id, nome, url, codigo, ativo, ultima_sincronizacao) VALUES (11003, 'Copa do Brasil', 'https://api.football-data.org/v4/competitions/CDB', 'CDB', FALSE, NULL);

-- Classificacoes sincronizadas dos campeonatos ativos.
INSERT INTO classificacao_campeonato (id, campeonato_id, posicao, nome_time) VALUES (11101, 11001, 1, 'Flamengo');
INSERT INTO classificacao_campeonato (id, campeonato_id, posicao, nome_time) VALUES (11102, 11001, 2, 'Palmeiras');
INSERT INTO classificacao_campeonato (id, campeonato_id, posicao, nome_time) VALUES (11103, 11001, 3, 'Fortaleza');
INSERT INTO classificacao_campeonato (id, campeonato_id, posicao, nome_time) VALUES (11104, 11002, 1, 'River Plate');
INSERT INTO classificacao_campeonato (id, campeonato_id, posicao, nome_time) VALUES (11105, 11002, 2, 'Palmeiras');
INSERT INTO classificacao_campeonato (id, campeonato_id, posicao, nome_time) VALUES (11106, 11002, 3, 'Penarol');

-- Grupos publicos e privado, com diferentes janelas de palpites.
INSERT INTO grupo (id, nome, descricao, campeonato_id, privacidade, limite_participantes, organizador_id, minutos_abertura_palpites, minutos_fechamento_palpites) VALUES (12001, 'Brasileirao entre Amigos', 'Bolao publico do Campeonato Brasileiro', 11001, 'PUBLICA', 20, 10002, 120, 0);
INSERT INTO grupo (id, nome, descricao, campeonato_id, privacidade, limite_participantes, organizador_id, minutos_abertura_palpites, minutos_fechamento_palpites) VALUES (12002, 'Familia no Brasileirao', 'Bolao privado da familia', 11001, 'PRIVADA', 10, 10003, 180, 30);
INSERT INTO grupo (id, nome, descricao, campeonato_id, privacidade, limite_participantes, organizador_id, minutos_abertura_palpites, minutos_fechamento_palpites) VALUES (12003, 'Libertadores 2026', 'Bolao publico da Libertadores', 11002, 'PUBLICA', 30, 10002, 240, 15);

-- Participantes, incluindo sempre o organizador do grupo.
INSERT INTO grupo_participantes (grupo_id, usuario_id) VALUES (12001, 10002);
INSERT INTO grupo_participantes (grupo_id, usuario_id) VALUES (12001, 10003);
INSERT INTO grupo_participantes (grupo_id, usuario_id) VALUES (12001, 10004);
INSERT INTO grupo_participantes (grupo_id, usuario_id) VALUES (12002, 10003);
INSERT INTO grupo_participantes (grupo_id, usuario_id) VALUES (12002, 10004);
INSERT INTO grupo_participantes (grupo_id, usuario_id) VALUES (12003, 10002);
INSERT INTO grupo_participantes (grupo_id, usuario_id) VALUES (12003, 10005);

-- Convites representando todos os estados possiveis.
INSERT INTO convite (id, descricao, status, grupo_id, organizador_id, convidado_id) VALUES (12101, 'Venha participar do nosso bolao privado', 'PENDENTE', 12002, 10003, 10005);
INSERT INTO convite (id, descricao, status, grupo_id, organizador_id, convidado_id) VALUES (12102, 'Convite aceito pela participante', 'ACEITO', 12002, 10003, 10004);
INSERT INTO convite (id, descricao, status, grupo_id, organizador_id, convidado_id) VALUES (12103, 'Convite recusado pelo administrador', 'RECUSADO', 12002, 10003, 10001);
INSERT INTO convite (id, descricao, status, grupo_id, organizador_id, convidado_id) VALUES (12104, 'Convite ignorado pela usuaria', 'IGNORADO', 12002, 10003, 10002);

-- Regras de pontuacao configuradas por grupo.
INSERT INTO regra_pontuacao (id, grupo_id, tipo_regra_pontuacao, pontos) VALUES (12201, 12001, 'ACERTO_VENCEDOR', 3);
INSERT INTO regra_pontuacao (id, grupo_id, tipo_regra_pontuacao, pontos) VALUES (12202, 12001, 'ACERTO_EMPATE', 2);
INSERT INTO regra_pontuacao (id, grupo_id, tipo_regra_pontuacao, pontos) VALUES (12203, 12001, 'PLACAR_EXATO', 5);
INSERT INTO regra_pontuacao (id, grupo_id, tipo_regra_pontuacao, pontos) VALUES (12204, 12001, 'BONUS_RODADA', 1);
INSERT INTO regra_pontuacao (id, grupo_id, tipo_regra_pontuacao, pontos) VALUES (12205, 12001, 'BONUS_MATA_MATA', 2);
INSERT INTO regra_pontuacao (id, grupo_id, tipo_regra_pontuacao, pontos) VALUES (12206, 12002, 'ACERTO_VENCEDOR', 3);
INSERT INTO regra_pontuacao (id, grupo_id, tipo_regra_pontuacao, pontos) VALUES (12207, 12002, 'ACERTO_EMPATE', 2);
INSERT INTO regra_pontuacao (id, grupo_id, tipo_regra_pontuacao, pontos) VALUES (12208, 12002, 'PLACAR_EXATO', 5);
INSERT INTO regra_pontuacao (id, grupo_id, tipo_regra_pontuacao, pontos) VALUES (12209, 12002, 'BONUS_RODADA', 1);
INSERT INTO regra_pontuacao (id, grupo_id, tipo_regra_pontuacao, pontos) VALUES (12210, 12002, 'BONUS_MATA_MATA', 2);
INSERT INTO regra_pontuacao (id, grupo_id, tipo_regra_pontuacao, pontos) VALUES (12211, 12003, 'ACERTO_VENCEDOR', 3);
INSERT INTO regra_pontuacao (id, grupo_id, tipo_regra_pontuacao, pontos) VALUES (12212, 12003, 'ACERTO_EMPATE', 2);
INSERT INTO regra_pontuacao (id, grupo_id, tipo_regra_pontuacao, pontos) VALUES (12213, 12003, 'PLACAR_EXATO', 5);
INSERT INTO regra_pontuacao (id, grupo_id, tipo_regra_pontuacao, pontos) VALUES (12214, 12003, 'BONUS_RODADA', 1);
INSERT INTO regra_pontuacao (id, grupo_id, tipo_regra_pontuacao, pontos) VALUES (12215, 12003, 'BONUS_MATA_MATA', 3);

-- Criterios de desempate em ordem de prioridade.
INSERT INTO criterio_desempate (id, grupo_id, criterio, prioridade) VALUES (12301, 12001, 'PLACAR_EXATO', 1);
INSERT INTO criterio_desempate (id, grupo_id, criterio, prioridade) VALUES (12302, 12001, 'ERRO', 2);
INSERT INTO criterio_desempate (id, grupo_id, criterio, prioridade) VALUES (12303, 12001, 'ACERTO_VENCEDOR', 3);
INSERT INTO criterio_desempate (id, grupo_id, criterio, prioridade) VALUES (12304, 12001, 'ACERTO_EMPATE', 4);
INSERT INTO criterio_desempate (id, grupo_id, criterio, prioridade) VALUES (12305, 12002, 'PLACAR_EXATO', 1);
INSERT INTO criterio_desempate (id, grupo_id, criterio, prioridade) VALUES (12306, 12002, 'ACERTO_VENCEDOR', 2);
INSERT INTO criterio_desempate (id, grupo_id, criterio, prioridade) VALUES (12307, 12002, 'ERRO', 3);
INSERT INTO criterio_desempate (id, grupo_id, criterio, prioridade) VALUES (12308, 12003, 'PLACAR_EXATO', 1);
INSERT INTO criterio_desempate (id, grupo_id, criterio, prioridade) VALUES (12309, 12003, 'ERRO', 2);
INSERT INTO criterio_desempate (id, grupo_id, criterio, prioridade) VALUES (12310, 12003, 'ACERTO_VENCEDOR', 3);

-- Partidas cobrindo os quatro estados do dominio e jogos mata-mata.
INSERT INTO partida (id, campeonato_id, codigo_externo, mandante, visitante, gols_mandante, gols_visitante, consolidada, data, status, mata_mata) VALUES (13001, 11001, 900001, 'Flamengo', 'Palmeiras', 2, 1, TRUE, DATEADD('DAY', -10, CURRENT_TIMESTAMP), 'FINALIZADO', FALSE);
INSERT INTO partida (id, campeonato_id, codigo_externo, mandante, visitante, gols_mandante, gols_visitante, consolidada, data, status, mata_mata) VALUES (13002, 11001, 900002, 'Fortaleza', 'Bahia', 1, 1, TRUE, DATEADD('DAY', -5, CURRENT_TIMESTAMP), 'FINALIZADO', FALSE);
INSERT INTO partida (id, campeonato_id, codigo_externo, mandante, visitante, gols_mandante, gols_visitante, consolidada, data, status, mata_mata) VALUES (13003, 11001, 900003, 'Santos', 'Corinthians', NULL, NULL, FALSE, DATEADD('MINUTE', 60, CURRENT_TIMESTAMP), 'ABERTO', FALSE);
INSERT INTO partida (id, campeonato_id, codigo_externo, mandante, visitante, gols_mandante, gols_visitante, consolidada, data, status, mata_mata) VALUES (13004, 11001, 900004, 'Cruzeiro', 'Gremio', 1, 0, FALSE, DATEADD('MINUTE', -30, CURRENT_TIMESTAMP), 'EM_ANDAMENTO', FALSE);
INSERT INTO partida (id, campeonato_id, codigo_externo, mandante, visitante, gols_mandante, gols_visitante, consolidada, data, status, mata_mata) VALUES (13005, 11001, 900005, 'Vasco', 'Botafogo', NULL, NULL, FALSE, DATEADD('DAY', 2, CURRENT_TIMESTAMP), 'CANCELADO', FALSE);
INSERT INTO partida (id, campeonato_id, codigo_externo, mandante, visitante, gols_mandante, gols_visitante, consolidada, data, status, mata_mata) VALUES (13006, 11002, 910001, 'River Plate', 'Palmeiras', 0, 2, TRUE, DATEADD('DAY', -7, CURRENT_TIMESTAMP), 'FINALIZADO', TRUE);
INSERT INTO partida (id, campeonato_id, codigo_externo, mandante, visitante, gols_mandante, gols_visitante, consolidada, data, status, mata_mata) VALUES (13007, 11002, 910002, 'Penarol', 'Flamengo', NULL, NULL, FALSE, DATEADD('MINUTE', 120, CURRENT_TIMESTAMP), 'ABERTO', TRUE);
INSERT INTO partida (id, campeonato_id, codigo_externo, mandante, visitante, gols_mandante, gols_visitante, consolidada, data, status, mata_mata) VALUES (13008, 11003, 920001, 'Sport', 'Ceara', NULL, NULL, FALSE, DATEADD('DAY', 4, CURRENT_TIMESTAMP), 'ABERTO', TRUE);

-- Palpites finalizados e ainda abertos, sempre no campeonato do grupo.
INSERT INTO palpite (id, partida_id, usuario_id, grupo_id, gols_mandante, gols_visitante, data) VALUES (14001, 13001, 10002, 12001, 2, 1, DATEADD('DAY', -11, CURRENT_TIMESTAMP));
INSERT INTO palpite (id, partida_id, usuario_id, grupo_id, gols_mandante, gols_visitante, data) VALUES (14002, 13001, 10003, 12001, 1, 0, DATEADD('DAY', -11, CURRENT_TIMESTAMP));
INSERT INTO palpite (id, partida_id, usuario_id, grupo_id, gols_mandante, gols_visitante, data) VALUES (14003, 13001, 10004, 12001, 1, 1, DATEADD('DAY', -11, CURRENT_TIMESTAMP));
INSERT INTO palpite (id, partida_id, usuario_id, grupo_id, gols_mandante, gols_visitante, data) VALUES (14004, 13002, 10002, 12001, 1, 1, DATEADD('DAY', -6, CURRENT_TIMESTAMP));
INSERT INTO palpite (id, partida_id, usuario_id, grupo_id, gols_mandante, gols_visitante, data) VALUES (14005, 13002, 10003, 12001, 0, 0, DATEADD('DAY', -6, CURRENT_TIMESTAMP));
INSERT INTO palpite (id, partida_id, usuario_id, grupo_id, gols_mandante, gols_visitante, data) VALUES (14006, 13002, 10004, 12001, 2, 2, DATEADD('DAY', -6, CURRENT_TIMESTAMP));
INSERT INTO palpite (id, partida_id, usuario_id, grupo_id, gols_mandante, gols_visitante, data) VALUES (14007, 13001, 10003, 12002, 2, 1, DATEADD('DAY', -11, CURRENT_TIMESTAMP));
INSERT INTO palpite (id, partida_id, usuario_id, grupo_id, gols_mandante, gols_visitante, data) VALUES (14008, 13001, 10004, 12002, 2, 0, DATEADD('DAY', -11, CURRENT_TIMESTAMP));
INSERT INTO palpite (id, partida_id, usuario_id, grupo_id, gols_mandante, gols_visitante, data) VALUES (14009, 13006, 10002, 12003, 0, 2, DATEADD('DAY', -8, CURRENT_TIMESTAMP));
INSERT INTO palpite (id, partida_id, usuario_id, grupo_id, gols_mandante, gols_visitante, data) VALUES (14010, 13006, 10005, 12003, 1, 2, DATEADD('DAY', -8, CURRENT_TIMESTAMP));
INSERT INTO palpite (id, partida_id, usuario_id, grupo_id, gols_mandante, gols_visitante, data) VALUES (14011, 13003, 10002, 12001, 2, 0, DATEADD('MINUTE', -15, CURRENT_TIMESTAMP));
INSERT INTO palpite (id, partida_id, usuario_id, grupo_id, gols_mandante, gols_visitante, data) VALUES (14012, 13007, 10005, 12003, 1, 1, DATEADD('MINUTE', -30, CURRENT_TIMESTAMP));

-- Pontuacoes ja consolidadas dos palpites em partidas finalizadas.
INSERT INTO pontuacao_palpite (id, palpite_id, pontuacao, acertou_vencedor, acertou_empate, acertou_placar_exato) VALUES (15001, 14001, 9, TRUE, FALSE, TRUE);
INSERT INTO pontuacao_palpite (id, palpite_id, pontuacao, acertou_vencedor, acertou_empate, acertou_placar_exato) VALUES (15002, 14002, 4, TRUE, FALSE, FALSE);
INSERT INTO pontuacao_palpite (id, palpite_id, pontuacao, acertou_vencedor, acertou_empate, acertou_placar_exato) VALUES (15003, 14003, 0, FALSE, FALSE, FALSE);
INSERT INTO pontuacao_palpite (id, palpite_id, pontuacao, acertou_vencedor, acertou_empate, acertou_placar_exato) VALUES (15004, 14004, 8, FALSE, TRUE, TRUE);
INSERT INTO pontuacao_palpite (id, palpite_id, pontuacao, acertou_vencedor, acertou_empate, acertou_placar_exato) VALUES (15005, 14005, 3, FALSE, TRUE, FALSE);
INSERT INTO pontuacao_palpite (id, palpite_id, pontuacao, acertou_vencedor, acertou_empate, acertou_placar_exato) VALUES (15006, 14006, 3, FALSE, TRUE, FALSE);
INSERT INTO pontuacao_palpite (id, palpite_id, pontuacao, acertou_vencedor, acertou_empate, acertou_placar_exato) VALUES (15007, 14007, 9, TRUE, FALSE, TRUE);
INSERT INTO pontuacao_palpite (id, palpite_id, pontuacao, acertou_vencedor, acertou_empate, acertou_placar_exato) VALUES (15008, 14008, 4, TRUE, FALSE, FALSE);
INSERT INTO pontuacao_palpite (id, palpite_id, pontuacao, acertou_vencedor, acertou_empate, acertou_placar_exato) VALUES (15009, 14009, 11, TRUE, FALSE, TRUE);
INSERT INTO pontuacao_palpite (id, palpite_id, pontuacao, acertou_vencedor, acertou_empate, acertou_placar_exato) VALUES (15010, 14010, 6, TRUE, FALSE, FALSE);

-- Historico de posicao e pontuacao acumulada apos as partidas consolidadas.
INSERT INTO ranking_snapshot (id, grupo_id, usuario_id, partida_id, posicao, pontuacao, data_snapshot) VALUES (16001, 12001, 10002, 13001, 1, 9, DATEADD('DAY', -10, CURRENT_TIMESTAMP));
INSERT INTO ranking_snapshot (id, grupo_id, usuario_id, partida_id, posicao, pontuacao, data_snapshot) VALUES (16002, 12001, 10003, 13001, 2, 4, DATEADD('DAY', -10, CURRENT_TIMESTAMP));
INSERT INTO ranking_snapshot (id, grupo_id, usuario_id, partida_id, posicao, pontuacao, data_snapshot) VALUES (16003, 12001, 10004, 13001, 3, 0, DATEADD('DAY', -10, CURRENT_TIMESTAMP));
INSERT INTO ranking_snapshot (id, grupo_id, usuario_id, partida_id, posicao, pontuacao, data_snapshot) VALUES (16004, 12001, 10002, 13002, 1, 17, DATEADD('DAY', -5, CURRENT_TIMESTAMP));
INSERT INTO ranking_snapshot (id, grupo_id, usuario_id, partida_id, posicao, pontuacao, data_snapshot) VALUES (16005, 12001, 10003, 13002, 2, 7, DATEADD('DAY', -5, CURRENT_TIMESTAMP));
INSERT INTO ranking_snapshot (id, grupo_id, usuario_id, partida_id, posicao, pontuacao, data_snapshot) VALUES (16006, 12001, 10004, 13002, 3, 3, DATEADD('DAY', -5, CURRENT_TIMESTAMP));
INSERT INTO ranking_snapshot (id, grupo_id, usuario_id, partida_id, posicao, pontuacao, data_snapshot) VALUES (16007, 12002, 10003, 13001, 1, 9, DATEADD('DAY', -10, CURRENT_TIMESTAMP));
INSERT INTO ranking_snapshot (id, grupo_id, usuario_id, partida_id, posicao, pontuacao, data_snapshot) VALUES (16008, 12002, 10004, 13001, 2, 4, DATEADD('DAY', -10, CURRENT_TIMESTAMP));
INSERT INTO ranking_snapshot (id, grupo_id, usuario_id, partida_id, posicao, pontuacao, data_snapshot) VALUES (16009, 12003, 10002, 13006, 1, 11, DATEADD('DAY', -7, CURRENT_TIMESTAMP));
INSERT INTO ranking_snapshot (id, grupo_id, usuario_id, partida_id, posicao, pontuacao, data_snapshot) VALUES (16010, 12003, 10005, 13006, 2, 6, DATEADD('DAY', -7, CURRENT_TIMESTAMP));

-- Evolucao estatistica dos participantes com palpites consolidados.
INSERT INTO estatisticas (id, usuario_id, taxa_acerto, placares_exatos, vitorias_rankings, maior_sequencia_acertos, total_palpites_corretos, data_registro) VALUES (17001, 10002, 100.0, 3, 2, 3, 3, DATEADD('DAY', -1, CURRENT_TIMESTAMP));
INSERT INTO estatisticas (id, usuario_id, taxa_acerto, placares_exatos, vitorias_rankings, maior_sequencia_acertos, total_palpites_corretos, data_registro) VALUES (17002, 10003, 100.0, 1, 1, 3, 3, DATEADD('DAY', -1, CURRENT_TIMESTAMP));
INSERT INTO estatisticas (id, usuario_id, taxa_acerto, placares_exatos, vitorias_rankings, maior_sequencia_acertos, total_palpites_corretos, data_registro) VALUES (17003, 10004, 66.67, 0, 0, 2, 2, DATEADD('DAY', -1, CURRENT_TIMESTAMP));
INSERT INTO estatisticas (id, usuario_id, taxa_acerto, placares_exatos, vitorias_rankings, maior_sequencia_acertos, total_palpites_corretos, data_registro) VALUES (17004, 10005, 100.0, 0, 0, 1, 1, DATEADD('DAY', -1, CURRENT_TIMESTAMP));

-- Registro que justifica o perfil premium de Ana.
INSERT INTO promocao_premium (id, usuario_id, data, motivo, palpites, grupos_participa, requisicoes, acertos) VALUES (18001, 10002, DATEADD('DAY', -20, CURRENT_TIMESTAMP), 'Criterios automaticos de engajamento e desempenho atingidos', 65, 4, 140, 28);

-- Evita colisao entre os IDs pre-carregados e novos IDs gerados pelo Hibernate.
ALTER SEQUENCE usuario_seq RESTART WITH 20001;
ALTER SEQUENCE campeonato_seq RESTART WITH 20001;
ALTER SEQUENCE classificacao_campeonato_seq RESTART WITH 20001;
ALTER SEQUENCE grupo_seq RESTART WITH 20001;
ALTER SEQUENCE convite_seq RESTART WITH 20001;
ALTER SEQUENCE regra_pontuacao_seq RESTART WITH 20001;
ALTER SEQUENCE criterio_desempate_seq RESTART WITH 20001;
ALTER SEQUENCE partida_seq RESTART WITH 20001;
ALTER SEQUENCE palpite_seq RESTART WITH 20001;
ALTER SEQUENCE pontuacao_palpite_seq RESTART WITH 20001;
ALTER SEQUENCE ranking_snapshot_seq RESTART WITH 20001;
ALTER SEQUENCE promocao_premium_seq RESTART WITH 20001;
ALTER TABLE estatisticas ALTER COLUMN id RESTART WITH 20001;
