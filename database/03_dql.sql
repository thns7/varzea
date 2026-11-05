-- ============================================================
-- VÁRZEA: o campeonato dos bairros
-- Script DQL - Oracle Database
-- ============================================================

-- Consulta 1 - INNER JOIN entre duas tabelas (10 pontos)
-- Pergunta: quais chamados já foram resolvidos, e de qual serviço eles são?
SELECT c.id_chamado,
       s.nm_servico,
       c.dt_abertura,
       c.dt_resolucao,
       s.nr_gols
  FROM t_vz_chamado c
 INNER JOIN t_vz_servico s
    ON s.id_servico = c.id_servico
 WHERE c.st_chamado = 'RESOLVIDO'
 ORDER BY s.nr_gols DESC, c.id_chamado;

-- Consulta 2 - INNER JOIN entre duas tabelas com agrupamento (10 pontos)
-- Pergunta: como cada distrito está em chamados, resolvidos e tempo médio de resolução?
SELECT d.nm_distrito,
       COUNT(*) AS qt_chamados,
       SUM(CASE WHEN c.st_chamado = 'RESOLVIDO' THEN 1 ELSE 0 END) AS qt_resolvidos,
       ROUND(AVG(c.dt_resolucao - c.dt_abertura), 1) AS media_dias_resolucao
  FROM t_vz_chamado c
 INNER JOIN t_vz_distrito d
    ON d.id_distrito = c.id_distrito
 GROUP BY d.nm_distrito
 ORDER BY qt_resolvidos DESC, d.nm_distrito;

-- Consulta 3 - LEFT OUTER JOIN entre duas tabelas, com WHERE e ORDER BY (10 pontos)
-- Pergunta: quais chamados resolvidos ainda não passaram pelo VAR?
-- O LEFT OUTER JOIN mantém os chamados sem confirmação, com as colunas de t_vz_confirmacao nulas.
SELECT c.id_chamado,
       c.ds_referencia,
       c.dt_resolucao
  FROM t_vz_chamado c
  LEFT OUTER JOIN t_vz_confirmacao v
    ON v.id_chamado = c.id_chamado
 WHERE c.st_chamado = 'RESOLVIDO'
   AND v.id_confirmacao IS NULL
 ORDER BY c.dt_resolucao;
