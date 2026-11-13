import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Flask } from '@phosphor-icons/react';
import { entrarComoAvaliador, mensagemErro } from '../lib/supabase';
import { PapelRasgado } from './Arquibancada';

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
    <PapelRasgado className="guia-avaliacao" rotulo="Como testar">
      <p className="rotulo"><Flask size={16} weight="fill" aria-hidden /> Modo avaliação</p>
      <h2>Faça um gol em 3 passos</h2>
      <ol>
        <li><span>1</span><p><Link to="/app/reportar">Reporte um problema</Link> no seu distrito. A foto é opcional.</p></li>
        <li><span>2</span><p>Abra o chamado e toque em <strong>Marcar como resolvido</strong>.</p></li>
        <li><span>3</span><p>Chame o <strong>VAR de teste</strong>: dois vizinhos simulados confirmam, e o gol entra no placar e no mapa.</p></li>
      </ol>
      <p className="meta">Conta temporária. Gols de teste ficam marcados como demonstração; no jogo real, só 2 vizinhos de verdade validam.</p>
    </PapelRasgado>
  );
}
