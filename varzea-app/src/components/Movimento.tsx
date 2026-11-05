import { Fragment, useEffect, useMemo, useRef, useState, type CSSProperties, type ReactNode } from 'react';
import { Pause, Play, SoccerBall } from '@phosphor-icons/react';
import { alternarMovimento, useMovimentoReduzido, useNaTela, useRolagem } from '../lib/movimento';

type ComVariaveis = CSSProperties & Record<`--${string}`, string | number>;

/** Conteúdo sobe e aparece ao entrar na tela. */
export function Revelar({ children, atraso = 0, className = '' }: { children: ReactNode; atraso?: number; className?: string }) {
  const [ref, visivel] = useNaTela<HTMLDivElement>();
  const estilo: ComVariaveis = { '--atraso': `${atraso}ms` };
  return (
    <div ref={ref} className={`revelar ${visivel ? 'visivel' : ''} ${className}`} style={estilo}>
      {children}
    </div>
  );
}

/** Frase que entra palavra por palavra. */
export function Palavras({ texto, inicio = 0 }: { texto: string; inicio?: number }) {
  return (
    <>
      {texto.split(' ').map((palavra, i) => {
        const estilo: ComVariaveis = { '--i': i + inicio };
        return (
          <Fragment key={`${palavra}-${i}`}>
            <span className="palavra"><span style={estilo}>{palavra}</span></span>{' '}
          </Fragment>
        );
      })}
    </>
  );
}

/** Número que conta até o valor final quando aparece. */
export function Contador({ para, casas = 0, sufixo = '', duracao = 1400 }: { para: number; casas?: number; sufixo?: string; duracao?: number }) {
  const [ref, visivel] = useNaTela<HTMLSpanElement>(true, 0.6);
  const reduzido = useMovimentoReduzido();
  const [valor, setValor] = useState(reduzido ? para : 0);
  const formatar = (n: number) => `${n.toLocaleString('pt-BR', { minimumFractionDigits: casas, maximumFractionDigits: casas })}${sufixo}`;

  useEffect(() => {
    if (reduzido) {
      setValor(para);
      return;
    }
    if (!visivel) return;
    let quadro = 0;
    const inicio = performance.now();
    const passo = (agora: number) => {
      const t = Math.min(1, (agora - inicio) / duracao);
      setValor(para * (1 - Math.pow(1 - t, 4)));
      if (t < 1) quadro = requestAnimationFrame(passo);
    };
    quadro = requestAnimationFrame(passo);
    return () => cancelAnimationFrame(quadro);
  }, [visivel, reduzido, para, duracao]);

  return (
    <span ref={ref} className="contador" aria-label={formatar(para)}>
      <span aria-hidden>{formatar(valor)}</span>
    </span>
  );
}

/** Confete de papel picado nas cores da marca. Só existe enquanto `ativo` for verdadeiro. */
export function Confete({ ativo, quantidade = 36 }: { ativo: boolean; quantidade?: number }) {
  const reduzido = useMovimentoReduzido();
  const pedacos = useMemo(
    () =>
      Array.from({ length: quantidade }, (_, i): ComVariaveis => {
        const angulo = Math.random() * Math.PI * 2;
        const raio = 80 + Math.random() * 200;
        return {
          '--x': `${Math.cos(angulo) * raio}px`,
          '--y': `${Math.sin(angulo) * raio * 0.7 - 60}px`,
          '--r': `${Math.random() * 900 - 450}deg`,
          background: ['#A31923', '#E5343F', '#EBE6E6', '#827E7D'][i % 4],
          animationDelay: `${Math.random() * 120}ms`,
        };
      }),
    [quantidade],
  );
  if (!ativo || reduzido) return null;
  return (
    <div className="confete" aria-hidden>
      {pedacos.map((estilo, i) => <span key={i} style={estilo} />)}
    </div>
  );
}

/** Faixa de letreiro de estádio, com botão para pausar todo o movimento da página. */
export function Letreiro({ itens }: { itens: string[] }) {
  const reduzido = useMovimentoReduzido();
  const faixa = (
    <>
      {itens.map((item, i) => (
        <span key={`${item}-${i}`} className="letreiro-item">
          {item}
          <SoccerBall size={18} weight="fill" aria-hidden />
        </span>
      ))}
    </>
  );
  return (
    <div className="letreiro">
      <div className="letreiro-faixa" aria-hidden>
        <div>{faixa}</div>
        <div>{faixa}</div>
      </div>
      <button type="button" className="botao-pausa" onClick={alternarMovimento} aria-pressed={reduzido}>
        {reduzido ? <Play size={16} weight="fill" aria-hidden /> : <Pause size={16} weight="fill" aria-hidden />}
        {reduzido ? 'Ligar animações' : 'Pausar animações'}
      </button>
    </div>
  );
}

/** Barra fina no topo que acompanha a leitura da página. */
export function BarraProgresso() {
  const ref = useRef<HTMLDivElement>(null);
  useRolagem(() => {
    const total = document.documentElement.scrollHeight - window.innerHeight;
    if (ref.current) ref.current.style.transform = `scaleX(${total > 0 ? window.scrollY / total : 0})`;
  });
  return <div className="barra-progresso" ref={ref} aria-hidden />;
}

/** Torcida pulando. Só anima enquanto está na tela. */
export function Torcida() {
  const [ref, naTela] = useNaTela<SVGSVGElement>(false, 0.05);
  const fileiras = useMemo(
    () => [
      { y: 58, cor: '#262424', qtd: 40, escala: 0.85 },
      { y: 84, cor: '#3A3737', qtd: 32, escala: 1.1 },
    ],
    [],
  );
  return (
    <svg ref={ref} className={`torcida ${naTela ? 'em-cena' : ''}`} viewBox="0 0 1200 150" preserveAspectRatio="xMidYMax slice" aria-hidden>
      {fileiras.map((f, linha) =>
        Array.from({ length: f.qtd }, (_, i) => {
          const x = ((i + 0.5) / f.qtd) * 1200 + ((i * 37) % 11) - 5;
          const bracoErguido = (i + linha) % 3 === 0;
          const estilo: ComVariaveis = { animationDelay: `${((i * 73) % 9) * 90}ms` };
          return (
            <g key={`${linha}-${i}`} transform={`translate(${x} ${f.y}) scale(${f.escala})`}>
              <g className="torcedor" style={estilo} fill={f.cor}>
                <circle cx="0" cy="0" r="9" />
                <path d="M-14 14 C-14 7 14 7 14 14 V80 H-14 Z" />
                {bracoErguido && <path d="M10 12 L18 -18 L23 -16 L17 16 Z" />}
              </g>
            </g>
          );
        }),
      )}
      {[180, 620, 1010].map((x, i) => {
        const estilo: ComVariaveis = { animationDelay: `${i * 200}ms` };
        return (
          <g key={x} transform={`translate(${x} 10)`}>
            <rect x="0" y="0" width="4" height="120" fill="#3A3737" />
            <path className="bandeira" style={estilo} d="M4 2 H70 L60 20 L70 38 H4 Z" fill={i === 1 ? '#EBE6E6' : '#A31923'} />
          </g>
        );
      })}
    </svg>
  );
}

/** Cena do chute: a bola faz a curva, balança a rede e a tela grita gol. */
export function ChuteGol() {
  const [ref, visivel] = useNaTela<HTMLDivElement>(true, 0.5);
  const reduzido = useMovimentoReduzido();
  const [festa, setFesta] = useState(false);

  useEffect(() => {
    if (!visivel || reduzido) return;
    const tempo = window.setTimeout(() => setFesta(true), 800);
    return () => window.clearTimeout(tempo);
  }, [visivel, reduzido]);

  return (
    <div ref={ref} className={`chute ${visivel ? 'em-cena' : ''}`} aria-hidden>
      <svg viewBox="0 0 640 240">
        <path d="M0 232 H640" stroke="#3A3737" strokeWidth="3" strokeDasharray="14 10" />
        <g className="rede" stroke="#827E7D" strokeWidth="1.5">
          {Array.from({ length: 9 }, (_, i) => <path key={`v${i}`} d={`M${440 + i * 20} 44 V232`} />)}
          {Array.from({ length: 10 }, (_, i) => <path key={`h${i}`} d={`M440 ${44 + i * 21} H600`} />)}
        </g>
        <path d="M440 232 V40 H604 V232" fill="none" stroke="#EBE6E6" strokeWidth="8" strokeLinejoin="round" />
        <g className="bola-chute">
          <g className="bola-giro">
            <circle r="17" fill="#EBE6E6" />
            <path d="M0 -7 L7 -2 L4 6 L-4 6 L-7 -2 Z" fill="#0F0F0F" />
            <path d="M0 -17 L0 -7 M16 -5 L7 -2 M10 14 L4 6 M-10 14 L-4 6 M-16 -5 L-7 -2" stroke="#0F0F0F" strokeWidth="2" />
          </g>
        </g>
      </svg>
      <strong className="chute-gol">Gol!</strong>
      <Confete ativo={festa} quantidade={44} />
    </div>
  );
}
