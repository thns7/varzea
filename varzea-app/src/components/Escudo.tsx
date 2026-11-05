// Escudo gerado pelo nome do distrito: cada time tem sempre as mesmas cores e o mesmo padrão.
const ESPECIAIS: Record<string, [string, string, number]> = {
  Mooca: ['#C8102E', '#FFFFFF', 0],
  'Tatuapé': ['#1F4FA3', '#FFFFFF', 1],
  'Vila Prudente': ['#111111', '#F4F4F4', 0],
};

const CORES: [string, string][] = [
  ['#C8102E', '#FFFFFF'], ['#1F4FA3', '#FFFFFF'], ['#111111', '#F4F4F4'], ['#14733C', '#FFFFFF'],
  ['#E0A100', '#111111'], ['#5B2A86', '#FFFFFF'], ['#0E7C86', '#FFFFFF'], ['#B4501E', '#FFFFFF'],
];

function hash(texto: string) {
  return [...texto].reduce((soma, letra) => (soma * 31 + letra.charCodeAt(0)) >>> 0, 7);
}

function iniciais(nome: string) {
  const partes = nome.split(' ').filter((p) => !['de', 'do', 'da'].includes(p));
  return partes.length > 1 ? partes.map((p) => p[0]).join('').slice(0, 3) : nome.slice(0, 3).toUpperCase();
}

export function Escudo({ nome, tamanho = 48 }: { nome: string; tamanho?: number }) {
  const h = hash(nome);
  const especial = ESPECIAIS[nome];
  const [cor, contraste] = especial ? [especial[0], especial[1]] : CORES[h % CORES.length];
  const padrao = especial ? especial[2] : h % 3;
  const id = `escudo-${h}`;

  return (
    <svg width={tamanho} height={tamanho * 1.15} viewBox="0 0 48 55" role="img" aria-label={`Escudo de ${nome}`} className="escudo">
      <defs>
        <clipPath id={id}>
          <path d="M4 6 L24 2 L44 6 V30 C44 42 34 49 24 53 C14 49 4 42 4 30 Z" />
        </clipPath>
      </defs>
      <g clipPath={`url(#${id})`}>
        <rect width="48" height="55" fill={cor} />
        {padrao === 0 && [10, 22, 34].map((x) => <rect key={x} x={x} y="0" width="6" height="55" fill={contraste} />)}
        {padrao === 1 && <rect x="0" y="0" width="48" height="16" fill={contraste} opacity=".16" />}
        {padrao === 2 && <polygon points="0,38 48,12 48,26 0,52" fill={contraste} />}
        <rect x="0" y="16" width="48" height="19" fill={cor} opacity={padrao === 1 ? 0 : 0.92} />
      </g>
      <path d="M4 6 L24 2 L44 6 V30 C44 42 34 49 24 53 C14 49 4 42 4 30 Z" fill="none" stroke={contraste} strokeWidth="2" />
      <text x="24" y="32" textAnchor="middle" fontFamily="Bebas Neue, Impact, sans-serif" fontSize="16" fill={contraste}>
        {iniciais(nome)}
      </text>
    </svg>
  );
}
