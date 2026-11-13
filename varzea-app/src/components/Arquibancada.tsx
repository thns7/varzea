import { Fragment, useState, type ReactNode } from 'react';
import { Check, Clock, Pause, Play, SoccerBall, X } from '@phosphor-icons/react';
import { Escudo } from './Escudo';
import { useMovimentoReduzido } from '../lib/movimento';
import type { Chamado, GolRecente, Partida } from '../lib/supabase';

/* Peças visuais da Várzea: placar de chapas, letreiro, papel rasgado, pincelada e pódio. */

const DIA = 86_400_000;

function diaLocal(iso: string) {
  const [a, m, d] = iso.split('-').map(Number);
  return new Date(a, m - 1, d).getTime();
}

/** Dias da rodada já jogados (1 a 7) e quantos faltam. */
export function andamentoRodada(inicio: string, fim: string) {
  const hoje = new Date();
  const agora = new Date(hoje.getFullYear(), hoje.getMonth(), hoje.getDate()).getTime();
  const total = Math.round((diaLocal(fim) - diaLocal(inicio)) / DIA) + 1;
  const passados = Math.min(total, Math.max(0, Math.round((agora - diaLocal(inicio)) / DIA) + 1));
  return { total, passados, faltam: total - passados };
}

export function tempoAtras(iso: string) {
  const minutos = Math.max(1, Math.round((Date.now() - new Date(iso).getTime()) / 60_000));
  if (minutos < 60) return `há ${minutos} min`;
  const horas = Math.round(minutos / 60);
  if (horas < 24) return `há ${horas} h`;
  const dias = Math.round(horas / 24);
  return dias === 1 ? 'ontem' : `há ${dias} dias`;
}

/** Número em chapas, como no placar manual de campo de várzea. */
export function Chapas({ valor, digitos = 2, destaque = false }: { valor: number; digitos?: number; destaque?: boolean }) {
  const texto = String(Math.max(0, valor)).padStart(digitos, '0');
  return (
    <span className={`chapas ${destaque ? 'chapas-destaque' : ''}`} aria-label={String(valor)}>
      {[...texto].map((d, i) => <span key={i} aria-hidden>{d}</span>)}
    </span>
  );
}

/** Placar de madeira da partida da rodada: uma linha por time e lâmpadas com os dias da rodada. */
export function Placar({ partida, meuTime }: { partida: Partida; meuTime?: number }) {
  const { total, passados, faltam } = andamentoRodada(partida.inicio, partida.fim);
  const times = [
    { id: partida.casa_id, nome: partida.casa, gols: partida.gols_casa, outro: partida.gols_fora },
    { id: partida.fora_id, nome: partida.fora, gols: partida.gols_fora, outro: partida.gols_casa },
  ];
  return (
    <section className="placar-madeira" aria-label={`Rodada ${partida.rodada_numero}: ${partida.casa} ${partida.gols_casa} x ${partida.gols_fora} ${partida.fora}`}>
      <header>
        <span>Rodada {partida.rodada_numero}</span>
        <span>{faltam === 0 ? 'Último dia' : faltam === 1 ? 'Falta 1 dia' : `Faltam ${faltam} dias`}</span>
      </header>
      {times.map((t) => (
        <div key={t.id} className={`placar-linha ${t.id === meuTime ? 'meu' : ''}`}>
          <Escudo nome={t.nome} tamanho={30} />
          <span className="placar-nome">{t.nome}</span>
          <Chapas valor={t.gols} destaque={t.gols > t.outro} />
        </div>
      ))}
      <footer aria-hidden>
        {Array.from({ length: total }, (_, i) => <i key={i} className={i < passados ? 'aceso' : ''} />)}
      </footer>
    </section>
  );
}

/** Letreiro com os últimos gols. Movimento contínuo, então tem botão de pausa. */
export function Letreiro({ gols }: { gols: GolRecente[] }) {
  const reduzido = useMovimentoReduzido();
  const [pausado, setPausado] = useState(false);
  if (gols.length === 0) return null;
  const itens = (copia: number) =>
    gols.map((g, i) => (
      <li key={`${copia}-${i}`} aria-hidden={copia > 0 || undefined}>
        <SoccerBall size={14} weight="fill" aria-hidden />
        <strong>Gol</strong> {g.distrito} <b>+{g.valor}</b> <span>{g.servico} · {tempoAtras(g.criado_em)}</span>
      </li>
    ));
  return (
    <div className={`letreiro ${pausado || reduzido ? 'parado' : ''}`}>
      <span className="letreiro-selo">Últimos gols</span>
      <div className="letreiro-trilho">
        <ul style={{ animationDuration: `${Math.max(24, gols.length * 7)}s` }}>
          {itens(0)}
          {!reduzido && itens(1)}
        </ul>
      </div>
      {!reduzido && (
        <button type="button" onClick={() => setPausado(!pausado)} aria-label={pausado ? 'Continuar o letreiro' : 'Pausar o letreiro'}>
          {pausado ? <Play size={14} weight="fill" aria-hidden /> : <Pause size={14} weight="fill" aria-hidden />}
        </button>
      )}
    </div>
  );
}

/** Pincelada vermelha sob uma palavra. No máximo uma por tela. */
export function Pincelada({ children }: { children: ReactNode }) {
  return (
    <span className="pincelada">
      <svg viewBox="0 0 300 60" preserveAspectRatio="none" aria-hidden>
        <path d="M6 22 C40 8 96 14 150 10 C204 6 262 4 296 14 C292 26 298 38 290 48 C236 56 170 50 112 54 C70 57 30 56 8 50 C2 40 10 32 6 22 Z" />
        <path d="M18 54 C60 60 110 58 150 58" fill="none" strokeWidth="3" />
      </svg>
      <span>{children}</span>
    </span>
  );
}

/** Seção de papel com bordas rasgadas, no tema claro. */
export function PapelRasgado({ children, className = '', rotulo }: { children: ReactNode; className?: string; rotulo?: string }) {
  return (
    <section className={`papel-rasgado ${className}`} data-theme="light" aria-label={rotulo}>
      {children}
    </section>
  );
}

export interface LinhaPodio { chave: string; nome: string; gols: number; posicao: number }

/** Pódio dos três primeiros: segundo, primeiro e terceiro, como na premiação. */
export function Podio({ linhas, onEscolher }: { linhas: LinhaPodio[]; onEscolher?: (chave: string) => void }) {
  const [primeiro, segundo, terceiro] = linhas;
  const ordem = [segundo, primeiro, terceiro].filter(Boolean);
  return (
    <ol className="podio" aria-label="Três primeiros colocados">
      {ordem.map((l) => {
        const conteudo = (
          <>
            <Escudo nome={l.nome} tamanho={l.posicao === 1 ? 52 : 40} />
            <span className="podio-nome">{l.nome}</span>
            <span className="podio-gols">{l.gols} {l.gols === 1 ? 'gol' : 'gols'}</span>
            <span className="podio-degrau" aria-hidden>{l.posicao}º</span>
          </>
        );
        return (
          <li key={l.chave} className={`podio-${l.posicao}`} value={l.posicao}>
            {onEscolher ? <button type="button" onClick={() => onEscolher(l.chave)}>{conteudo}</button> : <div>{conteudo}</div>}
          </li>
        );
      })}
    </ol>
  );
}

const ETAPAS = ['Reportado', 'Resolvido', 'VAR', 'Gol'] as const;

/** Caminho do chamado até virar gol. */
export function LinhaDoTempo({ status }: { status: Chamado['status'] }) {
  const atual = status === 'aberto' ? 1 : status === 'resolvido' ? 2 : status === 'validado' ? 4 : 0;
  return (
    <ol className="linha-tempo" aria-label="Andamento do chamado">
      {ETAPAS.map((etapa, i) => {
        const feito = i < atual;
        const agora = i === atual;
        return (
          <Fragment key={etapa}>
            <li className={`${feito ? 'feito' : ''} ${agora ? 'agora' : ''} ${etapa === 'Gol' && feito ? 'gol' : ''}`} aria-current={agora ? 'step' : undefined}>
              <span className="linha-tempo-marca">{feito ? <Check size={14} weight="bold" aria-hidden /> : i + 1}</span>
              <span>{etapa}</span>
            </li>
          </Fragment>
        );
      })}
    </ol>
  );
}

/** Tela do VAR com as duas vagas de vizinho. */
export function MonitorVar({ confirmacoes, contestacoes, validado = false }: { confirmacoes: number; contestacoes: number; validado?: boolean }) {
  return (
    <div className="monitor-var">
      <header>
        <span><i aria-hidden /> VAR da torcida</span>
        <span>{validado ? 'Gol confirmado' : 'Em revisão'}</span>
      </header>
      <div className="monitor-vagas">
        {[0, 1].map((i) => {
          const ok = validado || confirmacoes > i;
          return (
            <div key={i} className={`vaga ${ok ? 'confirma' : 'livre'}`}>
              <span className="vaga-icone">{ok ? <Check size={28} weight="bold" aria-hidden /> : <Clock size={26} weight="fill" aria-hidden />}</span>
              <strong>Vizinho {i + 1}</strong>
              <small>{ok ? 'Confirmou com foto' : 'Aguardando'}</small>
            </div>
          );
        })}
      </div>
      {!validado && (
        <p className="monitor-contestacoes">
          <X size={14} weight="bold" aria-hidden /> {contestacoes} de 2 contestações · 2 contestações reabrem o chamado
        </p>
      )}
    </div>
  );
}
