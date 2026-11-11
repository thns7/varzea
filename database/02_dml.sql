-- ============================================================
-- VÁRZEA: o campeonato dos bairros
-- Script DML - Oracle Database
--
-- Moradores, referências, fotos e chamados são FICTÍCIOS.
-- Nomes de distritos e serviços e as chances em 30 dias vêm do SP156 (4º trimestre de 2025).
-- O tempo em minutos é uma estimativa da equipe.
-- ============================================================

-- Distritos
INSERT INTO t_vz_distrito (id_distrito, nm_distrito, nm_zona) VALUES (1, 'MOOCA', 'Leste');
INSERT INTO t_vz_distrito (id_distrito, nm_distrito, nm_zona) VALUES (2, 'TATUAPE', 'Leste');
INSERT INTO t_vz_distrito (id_distrito, nm_distrito, nm_zona) VALUES (3, 'VILA PRUDENTE', 'Leste');
INSERT INTO t_vz_distrito (id_distrito, nm_distrito, nm_zona) VALUES (4, 'CIDADE TIRADENTES', 'Leste');
INSERT INTO t_vz_distrito (id_distrito, nm_distrito, nm_zona) VALUES (5, 'PINHEIROS', 'Oeste');
INSERT INTO t_vz_distrito (id_distrito, nm_distrito, nm_zona) VALUES (6, 'PERUS', 'Norte');

-- Categorias
INSERT INTO t_vz_categoria (id_categoria, nm_categoria) VALUES (1, 'Pavimento');
INSERT INTO t_vz_categoria (id_categoria, nm_categoria) VALUES (2, 'Calçada');
INSERT INTO t_vz_categoria (id_categoria, nm_categoria) VALUES (3, 'Drenagem');
INSERT INTO t_vz_categoria (id_categoria, nm_categoria) VALUES (4, 'Entulho e lixo');
INSERT INTO t_vz_categoria (id_categoria, nm_categoria) VALUES (5, 'Limpeza e mato');
INSERT INTO t_vz_categoria (id_categoria, nm_categoria) VALUES (6, 'Praças');

-- Serviços
INSERT INTO t_vz_servico (id_servico, id_categoria, nm_servico, nr_gols, nr_minutos, vl_chance_30d) VALUES (1, 1, 'Guias, sarjetas e sarjetões - solicitar manutenção', 8, 120, 0.336);
INSERT INTO t_vz_servico (id_servico, id_categoria, nm_servico, nr_gols, nr_minutos, vl_chance_30d) VALUES (2, 1, 'Recolocação e ajuste de paralelepípedo', 7, 120, 0.393);
INSERT INTO t_vz_servico (id_servico, id_categoria, nm_servico, nr_gols, nr_minutos, vl_chance_30d) VALUES (3, 1, 'Tapa-buraco', 2, 45, 0.910);
INSERT INTO t_vz_servico (id_servico, id_categoria, nm_servico, nr_gols, nr_minutos, vl_chance_30d) VALUES (4, 2, 'Calçada pública - solicitar manutenção', 5, 150, 0.596);
INSERT INTO t_vz_servico (id_servico, id_categoria, nm_servico, nr_gols, nr_minutos, vl_chance_30d) VALUES (5, 3, 'Reformar galerias de águas de chuvas (pluviais)', 5, 240, 0.555);
INSERT INTO t_vz_servico (id_servico, id_categoria, nm_servico, nr_gols, nr_minutos, vl_chance_30d) VALUES (6, 3, 'Solicitar limpeza de bueiros, boca de lobo e poços de visita', 2, 40, 0.924);
INSERT INTO t_vz_servico (id_servico, id_categoria, nm_servico, nr_gols, nr_minutos, vl_chance_30d) VALUES (7, 4, 'Entulho e grandes objetos em via pública - solicitar remoção', 1, 30, 0.980);
INSERT INTO t_vz_servico (id_servico, id_categoria, nm_servico, nr_gols, nr_minutos, vl_chance_30d) VALUES (8, 5, 'Praça e canteiro central - solicitar roçada', 4, 90, 0.699);
INSERT INTO t_vz_servico (id_servico, id_categoria, nm_servico, nr_gols, nr_minutos, vl_chance_30d) VALUES (9, 6, 'Praças - Solicitar manutenção de equipamentos (mesas, bancos, brinquedos e equipamentos de ginástica)', 6, 120, 0.526);

-- Moradores (fictícios). Senha de demonstração dos perfis USER: varzea123.
-- A senha do ADMIN não fica no repositório: só o hash BCrypt.
INSERT INTO t_vz_morador (id_morador, id_distrito, nm_morador, ds_email, ds_senha, ds_perfil, dt_cadastro) VALUES (1, 1, 'Ana Fictícia', 'ana.ficticia@example.com', '$2a$10$qZoNmyPo1T5YnJ3qYIGN3.UQ70hAsXKdrh2U4fNGM34sZ69ePQxpq', 'ADMIN', DATE '2026-09-20');
INSERT INTO t_vz_morador (id_morador, id_distrito, nm_morador, ds_email, ds_senha, ds_perfil, dt_cadastro) VALUES (2, 1, 'Bruno Exemplo', 'bruno.exemplo@example.com', '$2a$10$LnwMsqqTHRkKle1vftk57uK7hyMrbsxT4rCNBdLdDvxE..UIZ34pi', 'USER', DATE '2026-09-21');
INSERT INTO t_vz_morador (id_morador, id_distrito, nm_morador, ds_email, ds_senha, ds_perfil, dt_cadastro) VALUES (3, 1, 'Carla Teste', 'carla.teste@example.com', '$2a$10$LnwMsqqTHRkKle1vftk57uK7hyMrbsxT4rCNBdLdDvxE..UIZ34pi', 'USER', DATE '2026-09-22');
INSERT INTO t_vz_morador (id_morador, id_distrito, nm_morador, ds_email, ds_senha, ds_perfil, dt_cadastro) VALUES (4, 2, 'Davi Modelo', 'davi.modelo@example.com', '$2a$10$LnwMsqqTHRkKle1vftk57uK7hyMrbsxT4rCNBdLdDvxE..UIZ34pi', 'USER', DATE '2026-09-20');
INSERT INTO t_vz_morador (id_morador, id_distrito, nm_morador, ds_email, ds_senha, ds_perfil, dt_cadastro) VALUES (5, 2, 'Elisa Amostra', 'elisa.amostra@example.com', '$2a$10$LnwMsqqTHRkKle1vftk57uK7hyMrbsxT4rCNBdLdDvxE..UIZ34pi', 'USER', DATE '2026-09-23');
INSERT INTO t_vz_morador (id_morador, id_distrito, nm_morador, ds_email, ds_senha, ds_perfil, dt_cadastro) VALUES (6, 2, 'Fábio Simulado', 'fabio.simulado@example.com', '$2a$10$LnwMsqqTHRkKle1vftk57uK7hyMrbsxT4rCNBdLdDvxE..UIZ34pi', 'USER', DATE '2026-09-24');
INSERT INTO t_vz_morador (id_morador, id_distrito, nm_morador, ds_email, ds_senha, ds_perfil, dt_cadastro) VALUES (7, 3, 'Gabi Protótipo', 'gabi.prototipo@example.com', '$2a$10$LnwMsqqTHRkKle1vftk57uK7hyMrbsxT4rCNBdLdDvxE..UIZ34pi', 'USER', DATE '2026-09-25');
INSERT INTO t_vz_morador (id_morador, id_distrito, nm_morador, ds_email, ds_senha, ds_perfil, dt_cadastro) VALUES (8, 3, 'Hugo Fictício', 'hugo.ficticio@example.com', '$2a$10$LnwMsqqTHRkKle1vftk57uK7hyMrbsxT4rCNBdLdDvxE..UIZ34pi', 'USER', DATE '2026-09-25');
INSERT INTO t_vz_morador (id_morador, id_distrito, nm_morador, ds_email, ds_senha, ds_perfil, dt_cadastro) VALUES (9, 3, 'Iara Exemplo', 'iara.exemplo@example.com', '$2a$10$LnwMsqqTHRkKle1vftk57uK7hyMrbsxT4rCNBdLdDvxE..UIZ34pi', 'USER', DATE '2026-09-26');

-- Rodadas
INSERT INTO t_vz_rodada (id_rodada, nr_rodada, dt_inicio, dt_fim) VALUES (1, 1, DATE '2026-10-05', DATE '2026-10-11');
INSERT INTO t_vz_rodada (id_rodada, nr_rodada, dt_inicio, dt_fim) VALUES (2, 2, DATE '2026-10-12', DATE '2026-10-18');
INSERT INTO t_vz_rodada (id_rodada, nr_rodada, dt_inicio, dt_fim) VALUES (3, 3, DATE '2026-10-19', DATE '2026-10-25');
INSERT INTO t_vz_rodada (id_rodada, nr_rodada, dt_inicio, dt_fim) VALUES (4, 4, DATE '2026-10-26', DATE '2026-11-01');
INSERT INTO t_vz_rodada (id_rodada, nr_rodada, dt_inicio, dt_fim) VALUES (5, 5, DATE '2026-11-02', DATE '2026-11-08');

-- Partidas
INSERT INTO t_vz_partida (id_partida, id_rodada, id_distrito_casa, id_distrito_fora) VALUES (1, 1, 1, 2);
INSERT INTO t_vz_partida (id_partida, id_rodada, id_distrito_casa, id_distrito_fora) VALUES (2, 2, 3, 1);
INSERT INTO t_vz_partida (id_partida, id_rodada, id_distrito_casa, id_distrito_fora) VALUES (3, 3, 2, 3);
INSERT INTO t_vz_partida (id_partida, id_rodada, id_distrito_casa, id_distrito_fora) VALUES (4, 4, 2, 1);
INSERT INTO t_vz_partida (id_partida, id_rodada, id_distrito_casa, id_distrito_fora) VALUES (5, 5, 1, 3);

-- Chamados (fictícios)
INSERT INTO t_vz_chamado (id_chamado, id_servico, id_distrito, id_morador, ds_referencia, st_chamado, dt_abertura, dt_resolucao) VALUES (1, 1, 1, 1, 'Trecho fictício A, perto da feira', 'RESOLVIDO', DATE '2026-09-28', DATE '2026-10-06');
INSERT INTO t_vz_chamado (id_chamado, id_servico, id_distrito, id_morador, ds_referencia, st_chamado, dt_abertura, dt_resolucao) VALUES (2, 3, 2, 4, 'Trecho fictício B, perto da escola', 'RESOLVIDO', DATE '2026-10-01', DATE '2026-10-07');
INSERT INTO t_vz_chamado (id_chamado, id_servico, id_distrito, id_morador, ds_referencia, st_chamado, dt_abertura, dt_resolucao) VALUES (3, 4, 3, 7, 'Trecho fictício C, perto da praça', 'RESOLVIDO', DATE '2026-10-02', DATE '2026-10-14');
INSERT INTO t_vz_chamado (id_chamado, id_servico, id_distrito, id_morador, ds_referencia, st_chamado, dt_abertura, dt_resolucao) VALUES (4, 7, 1, 2, 'Trecho fictício D, esquina do mercado', 'RESOLVIDO', DATE '2026-10-10', DATE '2026-10-12');
INSERT INTO t_vz_chamado (id_chamado, id_servico, id_distrito, id_morador, ds_referencia, st_chamado, dt_abertura, dt_resolucao) VALUES (5, 2, 2, 5, 'Trecho fictício E, ao lado do ponto de ônibus', 'RESOLVIDO', DATE '2026-10-05', DATE '2026-10-20');
INSERT INTO t_vz_chamado (id_chamado, id_servico, id_distrito, id_morador, ds_referencia, st_chamado, dt_abertura, dt_resolucao) VALUES (6, 8, 3, 8, 'Trecho fictício F, canteiro central', 'RESOLVIDO', DATE '2026-10-15', DATE '2026-10-27');
INSERT INTO t_vz_chamado (id_chamado, id_servico, id_distrito, id_morador, ds_referencia, st_chamado, dt_abertura, dt_resolucao) VALUES (7, 6, 1, 3, 'Trecho fictício G, boca de lobo', 'RESOLVIDO', DATE '2026-10-20', DATE '2026-10-21');
INSERT INTO t_vz_chamado (id_chamado, id_servico, id_distrito, id_morador, ds_referencia, st_chamado, dt_abertura, dt_resolucao) VALUES (8, 5, 2, 6, 'Trecho fictício H, galeria da avenida', 'RESOLVIDO', DATE '2026-10-12', DATE '2026-11-03');
INSERT INTO t_vz_chamado (id_chamado, id_servico, id_distrito, id_morador, ds_referencia, st_chamado, dt_abertura, dt_resolucao) VALUES (9, 3, 3, 9, 'Trecho fictício I, subida do morro', 'RESOLVIDO', DATE '2026-10-30', DATE '2026-11-02');
INSERT INTO t_vz_chamado (id_chamado, id_servico, id_distrito, id_morador, ds_referencia, st_chamado, dt_abertura, dt_resolucao) VALUES (10, 9, 1, 1, 'Trecho fictício J, praça do bairro', 'EM_ANDAMENTO', DATE '2026-10-25', NULL);
INSERT INTO t_vz_chamado (id_chamado, id_servico, id_distrito, id_morador, ds_referencia, st_chamado, dt_abertura, dt_resolucao) VALUES (11, 1, 3, 7, 'Trecho fictício K, rua sem saída', 'ABERTO', DATE '2026-11-01', NULL);
INSERT INTO t_vz_chamado (id_chamado, id_servico, id_distrito, id_morador, ds_referencia, st_chamado, dt_abertura, dt_resolucao) VALUES (12, 7, 2, 4, 'Trecho fictício L, pedido duplicado', 'CANCELADO', DATE '2026-10-18', NULL);

-- Confirmações (VAR): chamados 1 a 6 têm duas ou mais; o 8 está contestado; 7 e 9 aguardam
INSERT INTO t_vz_confirmacao (id_confirmacao, id_chamado, id_morador, st_resultado, ds_foto_url, dt_confirmacao) VALUES (1, 1, 2, 'CONFIRMA', 'https://example.com/fotos/var-001.jpg', DATE '2026-10-07');
INSERT INTO t_vz_confirmacao (id_confirmacao, id_chamado, id_morador, st_resultado, ds_foto_url, dt_confirmacao) VALUES (2, 1, 3, 'CONFIRMA', 'https://example.com/fotos/var-002.jpg', DATE '2026-10-08');
INSERT INTO t_vz_confirmacao (id_confirmacao, id_chamado, id_morador, st_resultado, ds_foto_url, dt_confirmacao) VALUES (3, 2, 5, 'CONFIRMA', 'https://example.com/fotos/var-003.jpg', DATE '2026-10-08');
INSERT INTO t_vz_confirmacao (id_confirmacao, id_chamado, id_morador, st_resultado, ds_foto_url, dt_confirmacao) VALUES (4, 2, 6, 'CONFIRMA', 'https://example.com/fotos/var-004.jpg', DATE '2026-10-09');
INSERT INTO t_vz_confirmacao (id_confirmacao, id_chamado, id_morador, st_resultado, ds_foto_url, dt_confirmacao) VALUES (5, 3, 8, 'CONFIRMA', 'https://example.com/fotos/var-005.jpg', DATE '2026-10-15');
INSERT INTO t_vz_confirmacao (id_confirmacao, id_chamado, id_morador, st_resultado, ds_foto_url, dt_confirmacao) VALUES (6, 3, 9, 'CONFIRMA', 'https://example.com/fotos/var-006.jpg', DATE '2026-10-16');
INSERT INTO t_vz_confirmacao (id_confirmacao, id_chamado, id_morador, st_resultado, ds_foto_url, dt_confirmacao) VALUES (7, 4, 1, 'CONFIRMA', 'https://example.com/fotos/var-007.jpg', DATE '2026-10-13');
INSERT INTO t_vz_confirmacao (id_confirmacao, id_chamado, id_morador, st_resultado, ds_foto_url, dt_confirmacao) VALUES (8, 4, 3, 'CONFIRMA', 'https://example.com/fotos/var-008.jpg', DATE '2026-10-14');
INSERT INTO t_vz_confirmacao (id_confirmacao, id_chamado, id_morador, st_resultado, ds_foto_url, dt_confirmacao) VALUES (9, 5, 4, 'CONFIRMA', 'https://example.com/fotos/var-009.jpg', DATE '2026-10-21');
INSERT INTO t_vz_confirmacao (id_confirmacao, id_chamado, id_morador, st_resultado, ds_foto_url, dt_confirmacao) VALUES (10, 5, 6, 'CONFIRMA', 'https://example.com/fotos/var-010.jpg', DATE '2026-10-22');
INSERT INTO t_vz_confirmacao (id_confirmacao, id_chamado, id_morador, st_resultado, ds_foto_url, dt_confirmacao) VALUES (11, 6, 7, 'CONFIRMA', 'https://example.com/fotos/var-011.jpg', DATE '2026-10-28');
INSERT INTO t_vz_confirmacao (id_confirmacao, id_chamado, id_morador, st_resultado, ds_foto_url, dt_confirmacao) VALUES (12, 6, 9, 'CONFIRMA', 'https://example.com/fotos/var-012.jpg', DATE '2026-10-29');
INSERT INTO t_vz_confirmacao (id_confirmacao, id_chamado, id_morador, st_resultado, ds_foto_url, dt_confirmacao) VALUES (13, 8, 4, 'CONFIRMA', 'https://example.com/fotos/var-013.jpg', DATE '2026-11-04');
INSERT INTO t_vz_confirmacao (id_confirmacao, id_chamado, id_morador, st_resultado, ds_foto_url, dt_confirmacao) VALUES (14, 8, 5, 'CONTESTA', 'https://example.com/fotos/var-014.jpg', DATE '2026-11-04');

-- Gols: um para cada chamado com pelo menos 2 confirmações
INSERT INTO t_vz_gol (id_gol, id_chamado, id_rodada, nr_gols, dt_validacao) VALUES (1, 1, 1, 8, DATE '2026-10-08');
INSERT INTO t_vz_gol (id_gol, id_chamado, id_rodada, nr_gols, dt_validacao) VALUES (2, 2, 1, 2, DATE '2026-10-09');
INSERT INTO t_vz_gol (id_gol, id_chamado, id_rodada, nr_gols, dt_validacao) VALUES (3, 3, 2, 5, DATE '2026-10-16');
INSERT INTO t_vz_gol (id_gol, id_chamado, id_rodada, nr_gols, dt_validacao) VALUES (4, 4, 2, 1, DATE '2026-10-14');
INSERT INTO t_vz_gol (id_gol, id_chamado, id_rodada, nr_gols, dt_validacao) VALUES (5, 5, 3, 7, DATE '2026-10-22');
INSERT INTO t_vz_gol (id_gol, id_chamado, id_rodada, nr_gols, dt_validacao) VALUES (6, 6, 4, 4, DATE '2026-10-29');

COMMIT;
