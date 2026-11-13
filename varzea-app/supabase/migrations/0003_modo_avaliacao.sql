-- Modo avaliação: quem entra sem cadastro (conta anônima do Supabase) pode testar o jogo inteiro sozinho.
-- No jogo de verdade o gol exige 2 vizinhos diferentes. Aqui, dois vizinhos de teste confirmam o chamado
-- que o próprio avaliador marcou como resolvido. O gol fica marcado como demonstração (demo = true).
-- Para limpar: delete from public.chamados where demo;

create function public.var_demonstracao(p_chamado bigint) returns text
language plpgsql security definer set search_path = '' as $$
declare
  v_uid uuid := auth.uid();
  v_chamado public.chamados;
begin
  if v_uid is null then raise exception 'Entre na sua conta para continuar.'; end if;
  if not coalesce((auth.jwt() ->> 'is_anonymous')::boolean, false) then
    raise exception 'O VAR de teste só existe no modo avaliação.';
  end if;
  select * into v_chamado from public.chamados where id = p_chamado for update;
  if not found then raise exception 'Chamado não encontrado.'; end if;
  if v_chamado.status <> 'resolvido' then raise exception 'Marque o chamado como resolvido antes do VAR.'; end if;
  if v_chamado.resolvido_por is distinct from v_uid then
    raise exception 'O VAR de teste vale só para chamados que você marcou como resolvidos.';
  end if;
  if (select count(*) from public.chamados c where c.resolvido_por = v_uid and c.demo and c.status = 'validado') >= 5 then
    raise exception 'Limite de 5 gols de teste por avaliador.';
  end if;

  update public.chamados set status = 'validado', validado_em = now(), demo = true where id = p_chamado;
  insert into public.gols (chamado_id, rodada_id, distrito_id, valor)
  select p_chamado, (public.rodada_atual()).id, v_chamado.distrito_id, s.gols
    from public.servicos s where s.id = v_chamado.servico_id;
  return 'gol';
end;
$$;

revoke execute on function public.var_demonstracao(bigint) from public, anon;
grant execute on function public.var_demonstracao(bigint) to authenticated;
