import { useEffect, useRef, useState } from 'react';
import * as THREE from 'three';
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js';
import { centroide, dentro, type DistritoGeo } from '../lib/geo';

export interface PlacarDistrito {
  nome: string;
  gols: number;
}

interface Props {
  distritos: DistritoGeo[];
  placar: Map<string, PlacarDistrito>;
  selecionado: string | null;
  meuTime: string | null;
  girar: boolean;
  onSelecionar: (chave: string) => void;
}

const COR = {
  semGol: new THREE.Color('#1E1D1D'),
  poucoGol: new THREE.Color('#3A3737'),
  muitoGol: new THREE.Color('#A31923'),
  selecionado: new THREE.Color('#E5343F'),
  borda: new THREE.Color('#3A3737'),
  bordaDestaque: new THREE.Color('#EBE6E6'),
};

// Centro da Sé no sistema de coordenadas do mapa, usado para adensar os prédios no centro.
const CENTRO_SE: [number, number] = [-0.4, 11.2];

/** Gerador pseudoaleatório com semente fixa: a cidade é sempre igual entre visitas. */
function aleatorio(semente: number) {
  let s = semente >>> 0;
  return () => {
    s = (s * 1664525 + 1013904223) >>> 0;
    return s / 4294967296;
  };
}

interface Bloco {
  chave: string;
  malha: THREE.Mesh;
  material: THREE.MeshStandardMaterial;
  borda: THREE.LineSegments;
  altura: number;
  centro: [number, number];
}

/**
 * Mapa 3D de São Paulo: cada distrito é um bloco cuja altura e cor crescem com os gols,
 * coberto por prédios gerados dentro do contorno real do distrito.
 */
export function Cidade3D({ distritos, placar, selecionado, meuTime, girar, onSelecionar }: Props) {
  const host = useRef<HTMLDivElement>(null);
  const rotulo = useRef<HTMLDivElement>(null);
  const estado = useRef<{
    blocos: Map<string, Bloco>;
    controles: OrbitControls;
    camera: THREE.PerspectiveCamera;
    alvo: THREE.Vector3 | null;
    hover: string | null;
  } | null>(null);
  const callback = useRef(onSelecionar);
  callback.current = onSelecionar;
  const selecionadoAtual = useRef(selecionado);
  selecionadoAtual.current = selecionado;
  const [falhou, setFalhou] = useState(false);

  // Monta a cena uma vez por conjunto de dados.
  useEffect(() => {
    const el = host.current;
    if (!el || distritos.length === 0) return;

    let renderer: THREE.WebGLRenderer;
    try {
      renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true, powerPreference: 'high-performance' });
    } catch {
      setFalhou(true);
      return;
    }
    const celular = window.matchMedia('(max-width: 900px)').matches;
    renderer.setPixelRatio(Math.min(window.devicePixelRatio, celular ? 1.5 : 2));
    renderer.setSize(el.clientWidth, el.clientHeight);
    renderer.domElement.setAttribute('aria-hidden', 'true');
    el.appendChild(renderer.domElement);

    const cena = new THREE.Scene();

    const xs = distritos.flatMap((d) => d.anel.map((p) => p[0]));
    const ys = distritos.flatMap((d) => d.anel.map((p) => p[1]));
    const meio = new THREE.Vector3((Math.min(...xs) + Math.max(...xs)) / 2, 0, -(Math.min(...ys) + Math.max(...ys)) / 2);

    const camera = new THREE.PerspectiveCamera(42, el.clientWidth / el.clientHeight, 0.1, 400);
    const retrato = el.clientHeight > el.clientWidth;
    camera.position.set(meio.x + 18, retrato ? 118 : 62, meio.z + (retrato ? 92 : 58));
    // A névoa acompanha a distância da câmera, para o celular não ficar escuro.
    const distancia = camera.position.distanceTo(meio);
    cena.fog = new THREE.Fog('#0F0F0F', distancia * 0.9, distancia * 2.2);

    const controles = new OrbitControls(camera, renderer.domElement);
    controles.target.copy(meio);
    controles.enableDamping = true;
    controles.dampingFactor = 0.08;
    controles.minDistance = 12;
    controles.maxDistance = 170;
    controles.maxPolarAngle = Math.PI * 0.44;
    controles.autoRotateSpeed = 0.35;
    controles.screenSpacePanning = false;

    cena.add(new THREE.HemisphereLight('#EBE6E6', '#0F0F0F', 1.4));
    const sol = new THREE.DirectionalLight('#FFFFFF', 1.6);
    sol.position.set(-30, 60, 20);
    cena.add(sol);

    const chao = new THREE.Mesh(new THREE.CircleGeometry(160, 48), new THREE.MeshBasicMaterial({ color: '#121111' }));
    chao.rotation.x = -Math.PI / 2;
    chao.position.set(meio.x, -0.02, meio.z);
    cena.add(chao);

    // A cidade inteira sobe junto na abertura.
    const cidade = new THREE.Group();
    cidade.scale.y = 0.001;
    cena.add(cidade);

    const maxGols = Math.max(1, ...[...placar.values()].map((p) => p.gols));
    const blocos = new Map<string, Bloco>();
    const geometrias: THREE.BufferGeometry[] = [];
    const materiais: THREE.Material[] = [];

    for (const d of distritos) {
      const gols = placar.get(d.chave)?.gols ?? 0;
      const t = gols / maxGols;
      const altura = 0.25 + Math.sqrt(t) * 2.6;
      const forma = new THREE.Shape(d.anel.map(([x, y]) => new THREE.Vector2(x, y)));
      const geo = new THREE.ExtrudeGeometry(forma, { depth: 1, bevelEnabled: false });
      const material = new THREE.MeshStandardMaterial({
        color: gols === 0 ? COR.semGol : COR.poucoGol.clone().lerp(COR.muitoGol, Math.sqrt(t)),
        roughness: 0.85,
        metalness: 0.05,
      });
      const malha = new THREE.Mesh(geo, material);
      malha.rotation.x = -Math.PI / 2;
      malha.scale.z = altura;
      malha.userData.chave = d.chave;
      cidade.add(malha);

      const bordaGeo = new THREE.EdgesGeometry(geo, 30);
      const bordaMat = new THREE.LineBasicMaterial({ color: COR.borda, transparent: true, opacity: 0.9 });
      const borda = new THREE.LineSegments(bordaGeo, bordaMat);
      borda.rotation.x = -Math.PI / 2;
      borda.scale.z = altura;
      cidade.add(borda);

      geometrias.push(geo, bordaGeo);
      materiais.push(material, bordaMat);
      blocos.set(d.chave, { chave: d.chave, malha, material, borda, altura, centro: centroide(d.anel) });
    }

    // Prédios: mais densos e altos perto do centro, sempre dentro do contorno real.
    const sorteio = aleatorio(2026);
    const limite = celular ? 1400 : 2800;
    const pontos: { x: number; y: number; base: number; alt: number; lado: number; tom: number }[] = [];
    const areaTotal = distritos.reduce((s, d) => s + d.km2, 0);
    for (const d of distritos) {
      const bloco = blocos.get(d.chave)!;
      const [cx, cy] = bloco.centro;
      const densidade = Math.exp(-Math.hypot(cx - CENTRO_SE[0], cy - CENTRO_SE[1]) / 11);
      const quantidade = Math.round(limite * (d.km2 / areaTotal) * (0.4 + densidade * 3.2));
      const minX = Math.min(...d.anel.map((p) => p[0]));
      const maxX = Math.max(...d.anel.map((p) => p[0]));
      const minY = Math.min(...d.anel.map((p) => p[1]));
      const maxY = Math.max(...d.anel.map((p) => p[1]));
      let tentativas = 0;
      let colocados = 0;
      while (colocados < quantidade && tentativas < quantidade * 12) {
        tentativas++;
        const x = minX + sorteio() * (maxX - minX);
        const y = minY + sorteio() * (maxY - minY);
        if (!dentro(x, y, d.anel)) continue;
        colocados++;
        pontos.push({
          x,
          y,
          base: bloco.altura,
          alt: 0.15 + Math.pow(sorteio(), 2.2) * (0.6 + densidade * 3.4),
          lado: 0.12 + sorteio() * 0.22,
          tom: 0.16 + sorteio() * 0.2,
        });
      }
    }
    const caixa = new THREE.BoxGeometry(1, 1, 1);
    caixa.translate(0, 0.5, 0);
    const matPredio = new THREE.MeshStandardMaterial({ roughness: 0.7, metalness: 0.1 });
    const predios = new THREE.InstancedMesh(caixa, matPredio, pontos.length);
    const m = new THREE.Matrix4();
    const cor = new THREE.Color();
    pontos.forEach((p, i) => {
      m.compose(new THREE.Vector3(p.x, p.base, -p.y), new THREE.Quaternion(), new THREE.Vector3(p.lado, p.alt, p.lado));
      predios.setMatrixAt(i, m);
      predios.setColorAt(i, cor.setRGB(p.tom, p.tom * 0.97, p.tom * 0.96));
    });
    predios.raycast = () => {};
    cidade.add(predios);
    geometrias.push(caixa, chao.geometry);
    materiais.push(matPredio, chao.material as THREE.Material);

    estado.current = { blocos, controles, camera, alvo: null, hover: null };

    // Toque ou clique seleciona um distrito; arrastar só gira o mapa.
    const raio = new THREE.Raycaster();
    const ponteiro = new THREE.Vector2();
    const malhas = [...blocos.values()].map((b) => b.malha);
    const acertar = (ev: PointerEvent) => {
      const r = renderer.domElement.getBoundingClientRect();
      ponteiro.set(((ev.clientX - r.left) / r.width) * 2 - 1, -((ev.clientY - r.top) / r.height) * 2 + 1);
      raio.setFromCamera(ponteiro, camera);
      return (raio.intersectObjects(malhas, false)[0]?.object.userData.chave as string | undefined) ?? null;
    };
    let inicio: { x: number; y: number } | null = null;
    const aoPressionar = (ev: PointerEvent) => {
      inicio = { x: ev.clientX, y: ev.clientY };
      controles.autoRotate = false;
    };
    const aoSoltar = (ev: PointerEvent) => {
      if (inicio && Math.hypot(ev.clientX - inicio.x, ev.clientY - inicio.y) < 6) {
        const chave = acertar(ev);
        if (chave) callback.current(chave);
      }
      inicio = null;
    };
    const aoMover = (ev: PointerEvent) => {
      if (ev.pointerType !== 'mouse' || inicio) return;
      const chave = acertar(ev);
      renderer.domElement.style.cursor = chave ? 'pointer' : 'grab';
      if (estado.current) estado.current.hover = chave;
    };
    renderer.domElement.addEventListener('pointerdown', aoPressionar);
    renderer.domElement.addEventListener('pointerup', aoSoltar);
    renderer.domElement.addEventListener('pointermove', aoMover);

    // No computador o painel ocupa a direita: desloca o enquadramento para a cidade ficar no espaço livre.
    const enquadrar = () => {
      const w = el.clientWidth;
      const h = el.clientHeight;
      camera.aspect = w / h;
      if (w >= 900) camera.setViewOffset(w, h, 196, 0, w, h);
      else camera.clearViewOffset();
      camera.updateProjectionMatrix();
      renderer.setSize(w, h);
    };
    enquadrar();
    const redimensionar = new ResizeObserver(enquadrar);
    redimensionar.observe(el);

    const reduzido = document.documentElement.dataset.movimento === 'reduzido';
    const abertura = performance.now();
    const projetado = new THREE.Vector3();
    renderer.setAnimationLoop((agora) => {
      const t = reduzido ? 1 : Math.min(1, (agora - abertura) / 1400);
      cidade.scale.y = Math.max(0.001, 1 - Math.pow(1 - t, 3));

      const st = estado.current!;
      if (st.alvo) {
        const passo = st.alvo.clone().sub(controles.target).multiplyScalar(reduzido ? 1 : 0.08);
        controles.target.add(passo);
        camera.position.add(passo);
        if (passo.length() < 0.01) st.alvo = null;
      }
      for (const b of st.blocos.values()) {
        const destaque = b.chave === st.hover && b.chave !== selecionadoAtual.current;
        b.material.emissive.set(destaque ? '#2A1416' : '#000000');
      }
      controles.update();
      renderer.render(cena, camera);

      // Rótulo HTML acompanhando o distrito selecionado.
      const sel = selecionadoAtual.current ? st.blocos.get(selecionadoAtual.current) : null;
      const etiqueta = rotulo.current;
      if (etiqueta) {
        if (sel) {
          projetado.set(sel.centro[0], sel.altura * cidade.scale.y + 0.6, -sel.centro[1]).project(camera);
          const visivel = projetado.z < 1;
          etiqueta.style.opacity = visivel ? '1' : '0';
          etiqueta.style.transform = `translate(${((projetado.x + 1) / 2) * el.clientWidth}px, ${((1 - projetado.y) / 2) * el.clientHeight}px) translate(-50%, -100%)`;
        } else {
          etiqueta.style.opacity = '0';
        }
      }
    });

    return () => {
      renderer.setAnimationLoop(null);
      redimensionar.disconnect();
      controles.dispose();
      geometrias.forEach((g) => g.dispose());
      materiais.forEach((mt) => mt.dispose());
      predios.dispose();
      renderer.dispose();
      renderer.domElement.remove();
      estado.current = null;
    };
  }, [distritos, placar]);

  // Destaques de seleção e do próprio time, sem remontar a cena.
  useEffect(() => {
    const st = estado.current;
    if (!st) return;
    const maxGols = Math.max(1, ...[...placar.values()].map((p) => p.gols));
    for (const b of st.blocos.values()) {
      const gols = placar.get(b.chave)?.gols ?? 0;
      const t = gols / maxGols;
      if (b.chave === selecionado) b.material.color.copy(COR.selecionado);
      else b.material.color.copy(gols === 0 ? COR.semGol : COR.poucoGol.clone().lerp(COR.muitoGol, Math.sqrt(t)));
      const linha = b.borda.material as THREE.LineBasicMaterial;
      linha.color.copy(b.chave === meuTime || b.chave === selecionado ? COR.bordaDestaque : COR.borda);
    }
    const sel = selecionado ? st.blocos.get(selecionado) : null;
    if (sel) {
      st.alvo = new THREE.Vector3(sel.centro[0], 0, -sel.centro[1]);
      st.controles.autoRotate = false;
    }
  }, [selecionado, meuTime, placar]);

  useEffect(() => {
    if (estado.current) estado.current.controles.autoRotate = girar && document.documentElement.dataset.movimento !== 'reduzido';
  }, [girar, distritos, placar]);

  if (falhou) {
    return <p className="mapa-falha">Seu navegador não conseguiu abrir o mapa 3D. A classificação continua disponível ao lado.</p>;
  }

  const sel = selecionado ? placar.get(selecionado) : null;
  return (
    <div className="cidade3d" ref={host}>
      <div className="rotulo-distrito" ref={rotulo} aria-hidden>
        {sel && (
          <>
            <strong>{sel.nome}</strong>
            <span>{sel.gols} {sel.gols === 1 ? 'gol' : 'gols'}</span>
          </>
        )}
      </div>
    </div>
  );
}
