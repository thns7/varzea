import { createContext, useCallback, useContext, useEffect, useState, type ReactNode } from 'react';
import type { Session } from '@supabase/supabase-js';
import { supabase, type Perfil } from './supabase';

interface Contexto {
  sessao: Session | null;
  perfil: Perfil | null;
  carregando: boolean;
  atualizarPerfil: () => Promise<void>;
}

const SessaoContexto = createContext<Contexto>({ sessao: null, perfil: null, carregando: true, atualizarPerfil: async () => {} });

export function SessaoProvider({ children }: { children: ReactNode }) {
  const [sessao, setSessao] = useState<Session | null>(null);
  const [perfil, setPerfil] = useState<Perfil | null>(null);
  const [carregando, setCarregando] = useState(true);

  const buscarPerfil = useCallback(async (usuarioId: string | undefined) => {
    if (!usuarioId) {
      setPerfil(null);
      return;
    }
    const { data } = await supabase.from('perfis').select('id, nome, distrito_id, distritos(nome, zona)').eq('id', usuarioId).maybeSingle();
    setPerfil(data as unknown as Perfil | null);
  }, []);

  useEffect(() => {
    let ativo = true;
    supabase.auth.getSession().then(async ({ data }) => {
      if (!ativo) return;
      setSessao(data.session);
      await buscarPerfil(data.session?.user.id);
      if (ativo) setCarregando(false);
    });
    const { data } = supabase.auth.onAuthStateChange((_evento, novaSessao) => {
      setSessao(novaSessao);
      // O Supabase pede que chamadas ao banco fiquem fora deste callback.
      setTimeout(() => void buscarPerfil(novaSessao?.user.id), 0);
    });
    return () => {
      ativo = false;
      data.subscription.unsubscribe();
    };
  }, [buscarPerfil]);

  const atualizarPerfil = useCallback(() => buscarPerfil(sessao?.user.id), [buscarPerfil, sessao]);

  return <SessaoContexto.Provider value={{ sessao, perfil, carregando, atualizarPerfil }}>{children}</SessaoContexto.Provider>;
}

export const useSessao = () => useContext(SessaoContexto);
