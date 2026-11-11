-- ============================================================
-- VÁRZEA: apaga somente as tabelas e sequências da Várzea (prefixo T_VZ_ e SQ_VZ_).
-- Use para recriar o banco do zero. Depois, execute 01_ddl.sql e 02_dml.sql.
-- Na primeira execução, erros ORA-00942 e ORA-02289 apenas indicam que o objeto ainda não existia.
-- ============================================================

DROP TABLE t_vz_gol CASCADE CONSTRAINTS PURGE;
DROP TABLE t_vz_confirmacao CASCADE CONSTRAINTS PURGE;
DROP TABLE t_vz_chamado CASCADE CONSTRAINTS PURGE;
DROP TABLE t_vz_partida CASCADE CONSTRAINTS PURGE;
DROP TABLE t_vz_rodada CASCADE CONSTRAINTS PURGE;
DROP TABLE t_vz_morador CASCADE CONSTRAINTS PURGE;
DROP TABLE t_vz_servico CASCADE CONSTRAINTS PURGE;
DROP TABLE t_vz_categoria CASCADE CONSTRAINTS PURGE;
DROP TABLE t_vz_distrito CASCADE CONSTRAINTS PURGE;

DROP SEQUENCE sq_vz_morador;
DROP SEQUENCE sq_vz_chamado;
DROP SEQUENCE sq_vz_confirmacao;
DROP SEQUENCE sq_vz_gol;
