import { useEffect, useRef, useState, useSyncExternalStore } from 'react';

const CHAVE = 'varzea-movimento';
const consulta = window.matchMedia('(prefers-reduced-motion: reduce)');
const ouvintes = new Set<() => void>();

function lerEscolha() {
  try {
    return localStorage.getItem(CHAVE) === 'reduzido';
  } catch {
    return false;
  }
}

let escolha = lerEscolha();
let reduzido = consulta.matches || escolha;

function aplicar() {
  reduzido = consulta.matches || escolha;
  document.documentElement.dataset.movimento = reduzido ? 'reduzido' : 'completo';
  ouvintes.forEach((avisar) => avisar());
}

aplicar();
consulta.addEventListener('change', aplicar);

export function alternarMovimento() {
  escolha = !reduzido;
  try {
    localStorage.setItem(CHAVE, escolha ? 'reduzido' : 'completo');
  } catch {
    // Sem armazenamento, a escolha vale até fechar a página.
  }
  aplicar();
}

export function useMovimentoReduzido() {
  return useSyncExternalStore(
    (avisar) => {
      ouvintes.add(avisar);
      return () => ouvintes.delete(avisar);
    },
    () => reduzido,
  );
}

/** Diz quando o elemento entrou na tela. Com `umaVez`, continua verdadeiro depois disso. */
export function useNaTela<T extends Element>(umaVez = true, limiar = 0.2) {
  const ref = useRef<T>(null);
  const [visivel, setVisivel] = useState(false);

  useEffect(() => {
    const elemento = ref.current;
    if (!elemento) return;
    const observador = new IntersectionObserver(
      ([entrada]) => {
        if (entrada.isIntersecting) {
          setVisivel(true);
          if (umaVez) observador.disconnect();
        } else if (!umaVez) {
          setVisivel(false);
        }
      },
      { threshold: limiar },
    );
    observador.observe(elemento);
    return () => observador.disconnect();
  }, [umaVez, limiar]);

  return [ref, visivel] as const;
}

/** Roda `aoRolar` no máximo uma vez por quadro, sem bloquear a rolagem. */
export function useRolagem(aoRolar: () => void) {
  const callback = useRef(aoRolar);
  callback.current = aoRolar;

  useEffect(() => {
    let quadro = 0;
    const agendar = () => {
      if (quadro) return;
      quadro = requestAnimationFrame(() => {
        quadro = 0;
        callback.current();
      });
    };
    agendar();
    window.addEventListener('scroll', agendar, { passive: true });
    window.addEventListener('resize', agendar);
    return () => {
      cancelAnimationFrame(quadro);
      window.removeEventListener('scroll', agendar);
      window.removeEventListener('resize', agendar);
    };
  }, []);
}
