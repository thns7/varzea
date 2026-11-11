import { useEffect, useMemo, useState, type CSSProperties } from 'react';
import { useMovimentoReduzido, useNaTela } from '../lib/movimento';

type ComVariaveis = CSSProperties & Record<`--${string}`, string | number>;

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
