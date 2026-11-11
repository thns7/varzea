-- ============================================================
-- VÁRZEA: o campeonato dos bairros
-- Script DDL - Oracle Database
-- Global Solution 2026.2 - Database Design
-- ============================================================

-- Para recriar o banco do zero, execute antes o arquivo 00_apagar.sql.

-- ------------------------------------------------------------
-- Tabelas
-- ------------------------------------------------------------

-- Distrito: é o time do campeonato. Usa os nomes de distrito do SP156.
CREATE TABLE t_vz_distrito (
    id_distrito  NUMBER(5)     NOT NULL,
    nm_distrito  VARCHAR2(60)  NOT NULL,
    nm_zona      VARCHAR2(20)  NOT NULL,
    CONSTRAINT pk_vz_distrito PRIMARY KEY (id_distrito),
    CONSTRAINT un_vz_distrito_nome UNIQUE (nm_distrito)
);

-- Categoria: agrupa os serviços, como Pavimento ou Drenagem.
CREATE TABLE t_vz_categoria (
    id_categoria  NUMBER(5)     NOT NULL,
    nm_categoria  VARCHAR2(40)  NOT NULL,
    CONSTRAINT pk_vz_categoria PRIMARY KEY (id_categoria),
    CONSTRAINT un_vz_categoria_nome UNIQUE (nm_categoria)
);

-- Serviço: tipo de problema. Gol difícil vale mais.
CREATE TABLE t_vz_servico (
    id_servico    NUMBER(5)      NOT NULL,
    id_categoria  NUMBER(5)      NOT NULL,
    nm_servico    VARCHAR2(150)  NOT NULL,
    nr_gols       NUMBER(2)      NOT NULL,
    nr_minutos    NUMBER(4)      NOT NULL,
    vl_chance_30d NUMBER(4,3)    NOT NULL,
    CONSTRAINT pk_vz_servico PRIMARY KEY (id_servico),
    CONSTRAINT un_vz_servico_nome UNIQUE (nm_servico),
    CONSTRAINT ck_vz_servico_gols CHECK (nr_gols >= 1),
    CONSTRAINT ck_vz_servico_minutos CHECK (nr_minutos > 0),
    CONSTRAINT ck_vz_servico_chance CHECK (vl_chance_30d BETWEEN 0 AND 1)
);

-- Morador: integrante da torcida de um distrito.
CREATE TABLE t_vz_morador (
    id_morador   NUMBER(10)     NOT NULL,
    id_distrito  NUMBER(5)      NOT NULL,
    nm_morador   VARCHAR2(80)   NOT NULL,
    ds_email     VARCHAR2(120)  NOT NULL,
    ds_senha     VARCHAR2(100)  NOT NULL,
    ds_perfil    VARCHAR2(10)   NOT NULL,
    dt_cadastro  DATE           NOT NULL,
    CONSTRAINT pk_vz_morador PRIMARY KEY (id_morador),
    CONSTRAINT un_vz_morador_email UNIQUE (ds_email),
    CONSTRAINT ck_vz_morador_perfil CHECK (ds_perfil IN ('USER', 'ADMIN'))
);

-- Rodada: período de disputa do campeonato.
CREATE TABLE t_vz_rodada (
    id_rodada  NUMBER(5)  NOT NULL,
    nr_rodada  NUMBER(3)  NOT NULL,
    dt_inicio  DATE       NOT NULL,
    dt_fim     DATE       NOT NULL,
    CONSTRAINT pk_vz_rodada PRIMARY KEY (id_rodada),
    CONSTRAINT un_vz_rodada_numero UNIQUE (nr_rodada),
    CONSTRAINT ck_vz_rodada_periodo CHECK (dt_fim >= dt_inicio)
);

-- Partida: confronto entre dois distritos em uma rodada.
CREATE TABLE t_vz_partida (
    id_partida        NUMBER(10)  NOT NULL,
    id_rodada         NUMBER(5)   NOT NULL,
    id_distrito_casa  NUMBER(5)   NOT NULL,
    id_distrito_fora  NUMBER(5)   NOT NULL,
    CONSTRAINT pk_vz_partida PRIMARY KEY (id_partida),
    CONSTRAINT un_vz_partida UNIQUE (id_rodada, id_distrito_casa, id_distrito_fora),
    CONSTRAINT ck_vz_partida_times CHECK (id_distrito_casa <> id_distrito_fora)
);

-- Chamado: problema da rua relatado por um morador. Não guarda endereço com número.
CREATE TABLE t_vz_chamado (
    id_chamado     NUMBER(10)     NOT NULL,
    id_servico     NUMBER(5)      NOT NULL,
    id_distrito    NUMBER(5)      NOT NULL,
    id_morador     NUMBER(10)     NOT NULL,
    ds_referencia  VARCHAR2(120)  NOT NULL,
    st_chamado     VARCHAR2(15)   NOT NULL,
    dt_abertura    DATE           NOT NULL,
    dt_resolucao   DATE,
    id_morador_resolveu NUMBER(10),
    CONSTRAINT pk_vz_chamado PRIMARY KEY (id_chamado),
    CONSTRAINT ck_vz_chamado_status CHECK (st_chamado IN ('ABERTO', 'EM_ANDAMENTO', 'RESOLVIDO', 'CANCELADO')),
    CONSTRAINT ck_vz_chamado_datas CHECK (dt_resolucao IS NULL OR dt_resolucao >= dt_abertura),
    CONSTRAINT ck_vz_chamado_resolvido CHECK (st_chamado <> 'RESOLVIDO' OR dt_resolucao IS NOT NULL)
);

-- Confirmação: o VAR da torcida. Cada morador opina uma vez por chamado.
CREATE TABLE t_vz_confirmacao (
    id_confirmacao  NUMBER(10)     NOT NULL,
    id_chamado      NUMBER(10)     NOT NULL,
    id_morador      NUMBER(10)     NOT NULL,
    st_resultado    VARCHAR2(10)   NOT NULL,
    ds_foto_url     VARCHAR2(200)  NOT NULL,
    dt_confirmacao  DATE           NOT NULL,
    CONSTRAINT pk_vz_confirmacao PRIMARY KEY (id_confirmacao),
    CONSTRAINT un_vz_confirmacao UNIQUE (id_chamado, id_morador),
    CONSTRAINT ck_vz_confirmacao_res CHECK (st_resultado IN ('CONFIRMA', 'CONTESTA'))
);

-- Gol: chamado resolvido e confirmado por pelo menos 2 vizinhos.
CREATE TABLE t_vz_gol (
    id_gol        NUMBER(10)  NOT NULL,
    id_chamado    NUMBER(10)  NOT NULL,
    id_rodada     NUMBER(5)   NOT NULL,
    nr_gols       NUMBER(2)   NOT NULL,
    dt_validacao  DATE        NOT NULL,
    CONSTRAINT pk_vz_gol PRIMARY KEY (id_gol),
    CONSTRAINT un_vz_gol_chamado UNIQUE (id_chamado),
    CONSTRAINT ck_vz_gol_valor CHECK (nr_gols >= 1)
);

-- ------------------------------------------------------------
-- Chaves estrangeiras
-- ------------------------------------------------------------

ALTER TABLE t_vz_servico
    ADD CONSTRAINT fk_vz_servico_categoria FOREIGN KEY (id_categoria) REFERENCES t_vz_categoria (id_categoria);

ALTER TABLE t_vz_morador
    ADD CONSTRAINT fk_vz_morador_distrito FOREIGN KEY (id_distrito) REFERENCES t_vz_distrito (id_distrito);

ALTER TABLE t_vz_partida
    ADD CONSTRAINT fk_vz_partida_rodada FOREIGN KEY (id_rodada) REFERENCES t_vz_rodada (id_rodada);

ALTER TABLE t_vz_partida
    ADD CONSTRAINT fk_vz_partida_casa FOREIGN KEY (id_distrito_casa) REFERENCES t_vz_distrito (id_distrito);

ALTER TABLE t_vz_partida
    ADD CONSTRAINT fk_vz_partida_fora FOREIGN KEY (id_distrito_fora) REFERENCES t_vz_distrito (id_distrito);

ALTER TABLE t_vz_chamado
    ADD CONSTRAINT fk_vz_chamado_servico FOREIGN KEY (id_servico) REFERENCES t_vz_servico (id_servico);

ALTER TABLE t_vz_chamado
    ADD CONSTRAINT fk_vz_chamado_distrito FOREIGN KEY (id_distrito) REFERENCES t_vz_distrito (id_distrito);

ALTER TABLE t_vz_chamado
    ADD CONSTRAINT fk_vz_chamado_morador FOREIGN KEY (id_morador) REFERENCES t_vz_morador (id_morador);

ALTER TABLE t_vz_chamado
    ADD CONSTRAINT fk_vz_chamado_resolveu FOREIGN KEY (id_morador_resolveu) REFERENCES t_vz_morador (id_morador);

ALTER TABLE t_vz_confirmacao
    ADD CONSTRAINT fk_vz_confirm_chamado FOREIGN KEY (id_chamado) REFERENCES t_vz_chamado (id_chamado);

ALTER TABLE t_vz_confirmacao
    ADD CONSTRAINT fk_vz_confirm_morador FOREIGN KEY (id_morador) REFERENCES t_vz_morador (id_morador);

ALTER TABLE t_vz_gol
    ADD CONSTRAINT fk_vz_gol_chamado FOREIGN KEY (id_chamado) REFERENCES t_vz_chamado (id_chamado);

ALTER TABLE t_vz_gol
    ADD CONSTRAINT fk_vz_gol_rodada FOREIGN KEY (id_rodada) REFERENCES t_vz_rodada (id_rodada);

-- ------------------------------------------------------------
-- Sequências usadas pela API. Começam em 1000 para não colidir com os dados de exemplo.
-- ------------------------------------------------------------

CREATE SEQUENCE sq_vz_morador START WITH 1000;
CREATE SEQUENCE sq_vz_chamado START WITH 1000;
CREATE SEQUENCE sq_vz_confirmacao START WITH 1000;
CREATE SEQUENCE sq_vz_gol START WITH 1000;
