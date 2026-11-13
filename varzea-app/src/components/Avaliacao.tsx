import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Flask } from '@phosphor-icons/react';
import { entrarComoAvaliador, mensagemErro } from '../lib/supabase';

/** Entra no modo avaliação (sem e-mail) e leva para a escolha do time. */
export function BotaoAvaliar({ distritoId, classe = 'botao botao-primario', texto = 'Testar agora, sem cadastro' }: { distritoId?: number; classe?: string; texto?: string }) {
  const navegar = useNavigate();
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState('');

  async function entrar() {
    setErro('');
    setEnviando(true);
    try {
      await entrarComoAvaliador();
      navegar(distritoId ? `/escolher-time?distrito=${distritoId}` : '/escolher-time');
    } catch (e) {
      setErro(mensagemErro(e));
      setEnviando(false);
    }
  }

  return (
    <>
      <button type="button" className={classe} onClick={entrar} disabled={enviando}>
        {enviando ? 'Entrando' : texto}
      </button>
      {erro && <p className="alerta alerta-erro" role="alert">{erro}</p>}
    </>
  );
}

/** Roteiro curto para quem está avaliando o MVP. */
export function GuiaAvaliacao() {
  return (
    <section className="cartao guia-avaliacao" aria-label="Como testar">
      <p className="rotulo"><Flask size={16} weight="fill" aria-hidden /> Modo avaliação</p>
      <h2>Faça um gol em 3 passos</h2>
      <ol>
        <li><Link to="/app/reportar">Reporte um problema</Link> no seu distrito. A foto é opcional.</li>
        <li>Abra o chamado e toque em <strong>Marcar como resolvido</strong>.</li>
        <li>Use o <strong>VAR de teste</strong>: dois vizinhos simulados confirmam e o gol entra no placar e no mapa.</li>
      </ol>
      <p className="meta">Sua conta é temporária e os gols ficam marcados como demonstração. No jogo real, o gol só vale com 2 vizinhos de verdade.</p>
    </section>
  );
}
