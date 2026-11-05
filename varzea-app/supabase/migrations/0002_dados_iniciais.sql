-- Várzea: distritos, serviços, tabela e dados de demonstração
-- Serviços e gols: SP156, 4º trimestre de 2025. Chamados com demo = true são fictícios.
-- Para limpar a demonstração: delete from public.chamados where demo;

insert into public.distritos (nome, zona)
select unnest(array['Bela Vista','Bom Retiro','Cambuci','Consolação','Liberdade','República','Santa Cecília','Sé']), 'Centro'
union all select unnest(array['Anhanguera','Brasilândia','Cachoeirinha','Casa Verde','Freguesia do Ó','Jaçanã','Jaraguá','Limão','Mandaqui','Perus','Pirituba','Santana','São Domingos','Tremembé','Tucuruvi','Vila Guilherme','Vila Maria','Vila Medeiros']), 'Norte'
union all select unnest(array['Alto de Pinheiros','Barra Funda','Butantã','Itaim Bibi','Jaguara','Jaguaré','Jardim Paulista','Lapa','Morumbi','Perdizes','Pinheiros','Raposo Tavares','Rio Pequeno','Vila Leopoldina','Vila Sônia']), 'Oeste'
union all select unnest(array['Campo Belo','Campo Grande','Campo Limpo','Capão Redondo','Cidade Ademar','Cidade Dutra','Cursino','Grajaú','Ipiranga','Jabaquara','Jardim Ângela','Jardim São Luís','Marsilac','Moema','Parelheiros','Pedreira','Sacomã','Santo Amaro','Saúde','Socorro','Vila Andrade','Vila Mariana']), 'Sul'
union all select unnest(array['Mooca','Água Rasa','Aricanduva','Artur Alvim','Belém','Brás','Cangaíba','Carrão','Cidade Líder','Cidade Tiradentes','Ermelino Matarazzo','Guaianases','Iguatemi','Itaim Paulista','Itaquera','Jardim Helena','José Bonifácio','Lajeado','Pari','Parque do Carmo','Penha','Ponte Rasa','São Lucas','São Mateus','São Miguel','São Rafael','Sapopemba','Vila Curuçá','Vila Formosa','Vila Jacuí','Vila Matilde','Vila Prudente','Tatuapé']), 'Leste';

insert into public.categorias (nome) values ('Pavimento'), ('Calçada'), ('Drenagem'), ('Entulho e lixo'), ('Limpeza e mato'), ('Praças');

insert into public.servicos (categoria_id, nome, gols, chance_30d)
select c.id, v.nome, v.gols, v.chance
from (values
  ('Pavimento', 'Guias, sarjetas e sarjetões', 8, 0.336),
  ('Pavimento', 'Recolocação e ajuste de paralelepípedo', 7, 0.393),
  ('Pavimento', 'Tapa-buraco em faixa exclusiva ou corredor de ônibus', 3, 0.775),
  ('Pavimento', 'Tapa-buraco', 2, 0.910),
  ('Calçada', 'Calçada pública', 5, 0.596),
  ('Calçada', 'Vielas e escadarias', 5, 0.623),
  ('Drenagem', 'Reforma de galerias de águas de chuva', 5, 0.555),
  ('Drenagem', 'Drenagem de água da chuva', 5, 0.574),
  ('Drenagem', 'Limpeza de córrego', 4, 0.653),
  ('Drenagem', 'Microdrenagem', 3, 0.815),
  ('Drenagem', 'Limpeza de bueiros e bocas de lobo', 2, 0.924),
  ('Entulho e lixo', 'Remoção de entulho e grandes objetos', 1, 0.980),
  ('Entulho e lixo', 'Descarte irregular de entulho', 1, 0.998),
  ('Entulho e lixo', 'Cata Bagulho não retirado', 1, 0.988),
  ('Entulho e lixo', 'Lixeiras públicas', 1, 0.987),
  ('Limpeza e mato', 'Roçada de praça e canteiro central', 4, 0.699),
  ('Limpeza e mato', 'Capinação de guias e sarjetas', 1, 0.971),
  ('Limpeza e mato', 'Falta de varrição', 1, 0.977),
  ('Limpeza e mato', 'Sacos de varrição não removidos', 1, 0.985),
  ('Praças', 'Manutenção de equipamentos de praças', 6, 0.526)
) as v (categoria, nome, gols, chance)
join public.categorias c on c.nome = v.categoria;

-- Rodadas semanais, de segunda a domingo
insert into public.rodadas (numero, inicio, fim)
select n, date '2026-10-26' + (n - 1) * 7, date '2026-10-26' + (n - 1) * 7 + 6
from generate_series(1, 14) as n;

-- Partidas: todos contra todos dentro de cada zona, pelo método de rotação
do $$
declare
  v_zona text;
  v_times smallint[];
  v_qtd int;
  v_rodada int;
  v_i int;
  v_a smallint;
  v_b smallint;
begin
  foreach v_zona in array array['Centro', 'Norte', 'Sul', 'Leste', 'Oeste'] loop
    select array_agg(id order by id) into v_times from public.distritos where zona = v_zona;
    -- Zona com número ímpar de times: o 0 marca a folga, sem cair logo no primeiro time.
    if array_length(v_times, 1) % 2 = 1 then
      v_times := v_times[1:2] || 0::smallint || v_times[3:];
    end if;
    v_qtd := array_length(v_times, 1);
    for v_rodada in 1..14 loop
      for v_i in 1..(v_qtd / 2) loop
        v_a := v_times[v_i];
        v_b := v_times[v_qtd + 1 - v_i];
        if v_a <> 0 and v_b <> 0 then
          insert into public.partidas (rodada_id, casa_id, fora_id)
          select r.id,
                 case when v_rodada % 2 = 1 then v_a else v_b end,
                 case when v_rodada % 2 = 1 then v_b else v_a end
            from public.rodadas r where r.numero = v_rodada;
        end if;
      end loop;
      v_times := array[v_times[1], v_times[v_qtd]] || v_times[2:v_qtd - 1];
    end loop;
  end loop;
end;
$$;

-- Chamados de demonstração (fictícios)
insert into public.chamados (servico_id, distrito_id, referencia, status, demo, criado_em, resolvido_em, validado_em)
select s.id, d.id, v.referencia, v.status, true,
       v.criado::timestamptz, v.resolvido::timestamptz, v.validado::timestamptz
from (values
  ('Guias, sarjetas e sarjetões', 'Mooca', 'Trecho fictício A, perto da feira', 'validado', '2026-10-20 09:00-03', '2026-10-27 15:00-03', '2026-10-28 18:00-03'),
  ('Tapa-buraco', 'Mooca', 'Trecho fictício B, em frente à escola', 'validado', '2026-10-22 10:00-03', '2026-10-28 11:00-03', '2026-10-29 19:00-03'),
  ('Recolocação e ajuste de paralelepípedo', 'Tatuapé', 'Trecho fictício C, rua do mercado', 'validado', '2026-10-19 08:00-03', '2026-10-27 16:00-03', '2026-10-28 20:00-03'),
  ('Calçada pública', 'Tatuapé', 'Trecho fictício D, ponto de ônibus', 'validado', '2026-10-21 08:00-03', '2026-10-29 14:00-03', '2026-10-30 10:00-03'),
  ('Calçada pública', 'Vila Prudente', 'Trecho fictício E, subida da praça', 'validado', '2026-10-18 08:00-03', '2026-10-28 09:00-03', '2026-10-29 09:00-03'),
  ('Remoção de entulho e grandes objetos', 'Pinheiros', 'Trecho fictício F, esquina da padaria', 'validado', '2026-10-25 08:00-03', '2026-10-26 15:00-03', '2026-10-27 10:00-03'),
  ('Reforma de galerias de águas de chuva', 'Perus', 'Trecho fictício G, avenida principal', 'validado', '2026-10-10 08:00-03', '2026-10-29 17:00-03', '2026-10-31 11:00-03'),
  ('Tapa-buraco', 'Mooca', 'Trecho fictício H, perto do ponto final', 'resolvido', '2026-11-01 08:00-03', '2026-11-04 16:00-03', null),
  ('Limpeza de bueiros e bocas de lobo', 'Mooca', 'Trecho fictício I, boca de lobo entupida', 'resolvido', '2026-11-02 08:00-03', '2026-11-05 10:00-03', null),
  ('Manutenção de equipamentos de praças', 'Mooca', 'Trecho fictício J, praça do bairro', 'aberto', '2026-11-03 08:00-03', null, null),
  ('Guias, sarjetas e sarjetões', 'Tatuapé', 'Trecho fictício K, sarjeta quebrada', 'resolvido', '2026-11-01 08:00-03', '2026-11-05 09:00-03', null),
  ('Roçada de praça e canteiro central', 'Tatuapé', 'Trecho fictício L, canteiro central', 'aberto', '2026-11-04 08:00-03', null, null),
  ('Vielas e escadarias', 'Vila Prudente', 'Trecho fictício M, escadaria da viela', 'aberto', '2026-11-02 08:00-03', null, null),
  ('Tapa-buraco', 'Vila Prudente', 'Trecho fictício N, rua sem saída', 'resolvido', '2026-11-03 08:00-03', '2026-11-05 11:00-03', null),
  ('Limpeza de córrego', 'Pinheiros', 'Trecho fictício O, margem do córrego', 'aberto', '2026-11-03 08:00-03', null, null),
  ('Tapa-buraco', 'Perus', 'Trecho fictício P, descida da estação', 'aberto', '2026-11-04 08:00-03', null, null)
) as v (servico, distrito, referencia, status, criado, resolvido, validado)
join public.servicos s on s.nome = v.servico
join public.distritos d on d.nome = v.distrito;

insert into public.gols (chamado_id, rodada_id, distrito_id, valor, criado_em)
select c.id, r.id, c.distrito_id, s.gols, c.validado_em
from public.chamados c
join public.servicos s on s.id = c.servico_id
join public.rodadas r on c.validado_em::date between r.inicio and r.fim
where c.status = 'validado' and c.demo;
