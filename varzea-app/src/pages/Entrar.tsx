import { useState, type FormEvent } from 'react';
import { Link, Navigate, useSearchParams } from 'react-router-dom';
import { mensagemErro, supabase } from '../lib/supabase';
import { useSessao } from '../lib/sessao';
import { BotaoAvaliar } from '../components/Avaliacao';

export function Entrar() {
  const [params] = useSearchParams();
  const { sessao, carregando } = useSessao();
  const [modo, setModo] = useState<'entrar' | 'criar'>(params.get('modo') === 'criar' ? 'criar' : 'entrar');
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState('');
  const [aviso, setAviso] = useState('');

  if (!carregando && sessao) return <Navigate to="/app" replace />;

  async function enviar(evento: FormEvent) {
    evento.preventDefault();
    setErro('');
    setAviso('');
    setEnviando(true);
    try {
      if (modo === 'criar') {
        const { data, error } = await supabase.auth.signUp({
          email,
          password: senha,
          options: { emailRedirectTo: `${window.location.origin}/app` },
        });
        if (error) throw error;
        if (!data.session) setAviso('Conta criada. Abra o link que enviamos para o seu e-mail e depois entre aqui.');
      } else {
        const { error } = await supabase.auth.signInWithPassword({ email, password: senha });
        if (error) throw error;
      }
    } catch (e) {
      setErro(mensagemErro(e));
    } finally {
      setEnviando(false);
    }
  }

  return (
    <main className="tela-auth">
      <Link to="/"><img src="/logo.png" alt="Várzea, o Brasileirão dos bairros" className="logo-auth" /></Link>
      <h1>{modo === 'criar' ? 'Entre em campo' : 'De volta ao jogo'}</h1>
      <p className="apoio">{modo === 'criar' ? 'Crie sua conta e escolha o seu time. É de graça.' : 'Entre para torcer pelo seu distrito.'}</p>

      <section className="atalho-avaliacao">
        <BotaoAvaliar />
        <p className="meta">Para professores e avaliadores: entra na hora, sem e-mail.</p>
      </section>
      <p className="divisor"><span>ou com e-mail</span></p>

      <form onSubmit={enviar} className="formulario">
        <label>
          E-mail
          <input type="email" required autoComplete="email" value={email} onChange={(e) => setEmail(e.target.value)} />
        </label>
        <label>
          Senha
          <input
            type="password"
            required
            minLength={6}
            autoComplete={modo === 'criar' ? 'new-password' : 'current-password'}
            value={senha}
            onChange={(e) => setSenha(e.target.value)}
          />
        </label>
        {erro && <p className="alerta alerta-erro" role="alert">{erro}</p>}
        {aviso && <p className="alerta alerta-ok" role="status">{aviso}</p>}
        <button className="botao botao-primario" disabled={enviando}>
          {enviando ? 'Aguarde' : modo === 'criar' ? 'Criar conta' : 'Entrar'}
        </button>
      </form>

      <button
        type="button"
        className="botao-texto"
        onClick={() => {
          setModo(modo === 'criar' ? 'entrar' : 'criar');
          setErro('');
          setAviso('');
        }}
      >
        {modo === 'criar' ? 'Já tenho conta' : 'Ainda não tenho conta'}
      </button>
    </main>
  );
}
