/** Contornos dos distritos, gerados a partir da camada distrito_municipal do GeoSampa. Coordenadas em km. */
export interface DistritoGeo {
  chave: string;
  sigla: string;
  zona: string;
  km2: number;
  anel: [number, number][];
}

/** Normaliza o nome para casar o GeoSampa ("JARDIM ANGELA") com o banco ("Jardim Ângela"). */
export function chaveDistrito(nome: string) {
  return nome.normalize('NFD').replace(/[̀-ͯ]/g, '').toUpperCase().trim();
}

let cache: Promise<DistritoGeo[]> | null = null;

export function carregarDistritosGeo() {
  cache ??= fetch('/distritos.json')
    .then((r) => {
      if (!r.ok) throw new Error('Não foi possível carregar o mapa.');
      return r.json();
    })
    .then((d: { distritos: DistritoGeo[] }) => d.distritos);
  return cache;
}

export function centroide(anel: [number, number][]) {
  let a = 0;
  let cx = 0;
  let cy = 0;
  for (let i = 0; i < anel.length; i++) {
    const [x1, y1] = anel[i];
    const [x2, y2] = anel[(i + 1) % anel.length];
    const f = x1 * y2 - x2 * y1;
    a += f;
    cx += (x1 + x2) * f;
    cy += (y1 + y2) * f;
  }
  return a === 0 ? anel[0] : ([cx / (3 * a), cy / (3 * a)] as [number, number]);
}

export function dentro(x: number, y: number, anel: [number, number][]) {
  let dentroDoAnel = false;
  for (let i = 0, j = anel.length - 1; i < anel.length; j = i++) {
    const [xi, yi] = anel[i];
    const [xj, yj] = anel[j];
    if (yi > y !== yj > y && x < ((xj - xi) * (y - yi)) / (yj - yi) + xi) dentroDoAnel = !dentroDoAnel;
  }
  return dentroDoAnel;
}
