import { useEffect, useRef, useState, type CSSProperties, type FormEvent } from 'react';
import { Link } from 'react-router-dom';
import { Bank, Camera, CheckCircle, Handshake, MapPinArea, SoccerBall, Storefront, Trophy, UsersThree } from '@phosphor-icons/react';
import { Escudo } from '../components/Escudo';
import { BarraProgresso, ChuteGol, Confete, Contador, Letreiro, Palavras, Revelar, Torcida } from '../components/Movimento';
import { useMovimentoReduzido, useNaTela, useRolagem } from '../lib/movimento';
import { useSessao } from '../lib/sessao';
import { mensagemErro, supabase, type GolRecente, type LinhaTabela, type Partida } from '../lib/supabase';

type ComVariaveis = CSSProperties & Record<`--${string}`, string | number>;

const PASSOS = [
  { Icone: Camera, titulo: 'Reporte', texto: 'Viu um buraco, entulho ou calçada quebrada? Abra um chamado para o seu distrito em menos de um minuto.' },
  { Icone: CheckCircle, titulo: 'Marque como resolvido', texto: 'Quando o conserto chegar, um morador do distrito avisa. Isso ainda não é gol.' },
  { Icone: UsersThree, titulo: 'Passe pelo VAR', texto: 'Dois vizinhos conferem na rua e confirmam com foto. Com duas contestações, o chamado volta a ficar aberto.' },
  { Icone: SoccerBall, titulo: 'Grite gol', texto: 'O chamado confirmado soma gols para o distrito e movimenta a tabela da rodada.' },
];

const GOLS_EXEMPLO = [
  { servico: 'Guias e sarjetas', gols: 8, chance: 33.6 },
  { servico: 'Calçada pública', gols: 5, chance: 59.6 },
  { servico: 'Tapa-buraco', gols: 2, chance: 91.0 },
  { servico: 'Remoção de entulho', gols: 1, chance: 98.0 },
];

const TIPOS = [
  { valor: 'comercio', rotulo: 'Comércio ou empresa' },
  { valor: 'prefeitura', rotulo: 'Prefeitura ou subprefeitura' },
  { valor: 'associacao', rotulo: 'Associação de bairro' },
  { valor: 'outro', rotulo: 'Outro' },
];

function Rasgo({ inverso = false }: { inverso?: boolean }) {
  return (
    <svg className={`rasgo ${inverso ? 'rasgo-baixo' : ''}`} viewBox="0 0 1200 40" preserveAspectRatio="none" aria-hidden>
      <path d="M0 40 L0 22 L38 26 L71 12 L118 20 L160 9 L204 24 L259 15 L301 28 L352 11 L410 19 L446 7 L503 23 L562 13 L607 27 L661 10 L712 21 L768 8 L821 25 L874 14 L927 28 L981 12 L1034 20 L1089 6 L1141 22 L1200 13 L1200 40 Z" />
    </svg>
  );
}

function Pincelada() {
  return (
    <svg className="pincelada" viewBox="0 0 300 14" preserveAspectRatio="none" aria-hidden>
      <path d="M2 10 C60 4 150 2 298 1 C230 6 150 9 70 12 C40 13 12 14 2 10 Z" />
    </svg>
  );
}

function Skyline() {
  const fundo = useRef<SVGGElement>(null);
  const frente = useRef<SVGGElement>(null);
  const reduzido = useMovimentoReduzido();

  useRolagem(() => {
    const y = reduzido ? 0 : Math.min(window.scrollY, 900);
    fundo.current?.setAttribute('transform', `translate(0 ${y * 0.18})`);
    frente.current?.setAttribute('transform', `translate(0 ${y * 0.07})`);
  });

  return (
    <svg className="skyline" viewBox="0 0 1440 220" preserveAspectRatio="xMidYMax slice" aria-hidden>
      <g ref={fundo}>
        <path d="M0 220 V150 h40 v-30 h34 v50 h28 v-80 h46 v60 h30 v-96 h22 v-22 h10 v22 h24 v120 h38 v-56 h52 v76 h26 v-112 h48 v84 h34 v-44 h40 v70 H1440 V220 Z" fill="#1A1919" />
        <path d="M760 220 V170 h30 v-40 h44 v64 h36 v-94 h52 v70 h28 v-50 h40 v84 h32 v-120 h42 v96 h40 v-62 h46 v70 h34 v-104 h50 v92 h40 v-48 h46 v60 h40 v60 Z" fill="#262424" />
      </g>
      <g ref={frente} stroke="#3A3737" strokeWidth="2" fill="none">
        <path d="M640 220 L700 60 L760 220 M820 220 L700 60" strokeWidth="6" />
        {[0, 1, 2, 3, 4, 5, 6].map((i) => <path key={i} d={`M700 ${70 + i * 18} L${500 + i * 26} 212`} />)}
        {[0, 1, 2, 3, 4, 5, 6].map((i) => <path key={`b${i}`} d={`M700 ${70 + i * 18} L${900 - i * 26} 212`} />)}
        <path d="M470 212 H930" strokeWidth="5" />
      </g>
    </svg>
  );
}

/** O celular do hero repete um lance: o VAR confirma, o placar muda e a torcida ganha um gol. */
const ETAPAS = [
  { gols: 7, votos: 1, gol: false, duracao: 1700 },
  { gols: 7, votos: 2, gol: false, duracao: 900 },
  { gols: 12, votos: 2, gol: true, duracao: 3200 },
];

function CelularDemo() {
  const [ref, naTela] = useNaTela<HTMLDivElement>(false, 0.3);
  const reduzido = useMovimentoReduzido();
  const [indice, setIndice] = useState(2);
  const etapa = reduzido ? ETAPAS[2] : ETAPAS[indice];

  useEffect(() => {
    if (reduzido || !naTela) return;
    const tempo = window.setTimeout(() => setIndice((i) => (i + 1) % ETAPAS.length), ETAPAS[indice].duracao);
    return () => window.clearTimeout(tempo);
  }, [indice, naTela, reduzido]);

  return (
    <div className="celular-palco" ref={ref}>
      <div className="celular-flutua">
        <div className="celular" role="img" aria-label="Exemplo de tela do app, com dados de demonstração: Mooca 10 x 12 Tatuapé">
          <div className="celular-tela">
            <img src="/logo-curto.png" alt="" className="celular-logo" />
            <p className="rotulo">Rodada 1 · demonstração</p>
            <div className="placar">
              <div><Escudo nome="Mooca" tamanho={48} /><span>Mooca</span></div>
              <strong>10 <small>x</small> <span key={etapa.gols} className="placar-numero">{etapa.gols}</span></strong>
              <div><Escudo nome="Tatuapé" tamanho={48} /><span>Tatuapé</span></div>
            </div>
            {etapa.gol ? (
              <div className="celular-gol" key="gol">
                <span className="selo-resolvido selo-carimbo">Resolvido</span>
                <p><strong>Gol marcado!</strong></p>
                <p className="apoio">Calçada pública: +5 gols para o Tatuapé</p>
                <Confete ativo={!reduzido} quantidade={22} />
              </div>
            ) : (
              <div className="celular-gol" key="var">
                <span className="tag tag-resolvido">VAR da torcida</span>
                <p><strong>Calçada pública</strong></p>
                <div className="votos-var">
                  <span className="voto ligado" />
                  <span className={`voto ${etapa.votos > 1 ? 'ligado' : ''}`} />
                </div>
                <p className="apoio">{etapa.votos} de 2 confirmações</p>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

/** A bola percorre os passos conforme a rolagem da página, até o gol. */
function PassosComBola() {
  const lista = useRef<HTMLOListElement>(null);
  const bola = useRef<HTMLDivElement>(null);
  const linha = useRef<HTMLDivElement>(null);
  const reduzido = useMovimentoReduzido();
  const [ativos, setAtivos] = useState(0);
  const [marcou, setMarcou] = useState(false);

  useRolagem(() => {
    const el = lista.current;
    if (!el) return;
    if (reduzido) {
      setAtivos(PASSOS.length);
      return;
    }
    const caixa = el.getBoundingClientRect();
    const altura = window.innerHeight;
    const progresso = Math.max(0, Math.min(1, (altura * 0.85 - caixa.top) / Math.max(altura * 0.5, caixa.height)));
    const vertical = window.innerWidth <= 900;
    const curso = (vertical ? caixa.height : caixa.width) - 40;
    const deslocamento = progresso * curso;
    if (bola.current) {
      bola.current.style.transform = `${vertical ? `translateY(${deslocamento}px)` : `translateX(${deslocamento}px)`} rotate(${progresso * 900}deg)`;
    }
    if (linha.current) linha.current.style.transform = vertical ? `scaleY(${progresso})` : `scaleX(${progresso})`;
    setAtivos(PASSOS.reduce((total, _, i) => total + (progresso >= i * 0.25 + 0.02 ? 1 : 0), 0));
    if (progresso >= 0.98) setMarcou(true);
  });

  return (
    <div className="passos-palco">
      <div className="trilho" aria-hidden>
        <div className="trilho-linha" ref={linha} />
        <div className="trilho-bola" ref={bola}><SoccerBall size={40} weight="fill" /></div>
      </div>
      <ol className="passos" ref={lista}>
        {PASSOS.map(({ Icone, titulo, texto }, i) => (
          <li key={titulo} className={i < ativos ? 'ativo' : ''}>
            <Icone size={40} weight="fill" aria-hidden />
            <h3>{titulo}</h3>
            <p>{texto}</p>
            {i === PASSOS.length - 1 && <Confete ativo={marcou} quantidade={30} />}
          </li>
        ))}
      </ol>
    </div>
  );
}

function Narracao() {
  const [ref, visivel] = useNaTela<HTMLElement>(true, 0.35);
  return (
    <section className={`narracao-palco ${visivel ? 'em-cena' : ''}`} ref={ref}>
      <div className="secao narracao">
        <blockquote>
          <p>
            <span className="grito">
              “É{' '}
              <span className="gooool" aria-label="GOOOL">
                {'GOOOL'.split('').map((letra, i) => {
                  const estilo: ComVariaveis = { '--i': i };
                  return <span key={i} style={estilo} aria-hidden>{letra}</span>;
                })}
              </span>
            </span>{' '}
            <span className="narracao-resto">da Vila Prudente! Depois de 40 dias, a calçada da Rua das Flores ficou nova!”</span>
          </p>
          <cite>Exemplo de narração. A rua é fictícia.</cite>
        </blockquote>
      </div>
      <Torcida />
    </section>
  );
}

function BarraChance({ valor }: { valor: number }) {
  const [ref, visivel] = useNaTela<HTMLDivElement>(true, 0.8);
  const estilo: ComVariaveis = { '--valor': valor / 100 };
  return (
    <div ref={ref} className={`barra-chance ${visivel ? 'cheia' : ''}`} style={estilo}>
      <span />
    </div>
  );
}

export function Vendas() {
  const { sessao } = useSessao();
  const destino = sessao ? '/app' : '/entrar?modo=criar';
  const [tabela, setTabela] = useState<LinhaTabela[]>([]);
  const [gols, setGols] = useState<GolRecente[]>([]);
  const [placares, setPlacares] = useState<string[]>([]);
  const [form, setForm] = useState({ nome: '', email: '', tipo: 'comercio', mensagem: '', consentiu: false });
  const [enviando, setEnviando] = useState(false);
  const [enviado, setEnviado] = useState(false);
  const [erro, setErro] = useState('');

  useEffect(() => {
    supabase.rpc('classificacao').then(({ data }) => setTabela(((data ?? []) as LinhaTabela[]).slice(0, 5)));
    supabase.rpc('ultimos_gols', { p_limite: 4 }).then(({ data }) => setGols((data ?? []) as GolRecente[]));
    supabase.rpc('placar_partidas', { p_rodada: 1 }).then(({ data }) => {
      const comGol = ((data ?? []) as Partida[]).filter((p) => p.gols_casa + p.gols_fora > 0);
      setPlacares(comGol.map((p) => `${p.casa} ${p.gols_casa} x ${p.gols_fora} ${p.fora}`));
    });
  }, []);

  const letreiro = [...placares, 'Gol difícil vale mais', '96 distritos em campo', 'Quem marca ponto melhora a rua', 'Placares de demonstração'];

  async function enviarInteresse(evento: FormEvent) {
    evento.preventDefault();
    setErro('');
    setEnviando(true);
    const { error } = await supabase.from('interessados').insert({
      nome: form.nome.trim(),
      email: form.email.trim(),
      tipo: form.tipo,
      mensagem: form.mensagem.trim() || null,
      consentiu: form.consentiu,
    });
    setEnviando(false);
    if (error) setErro(mensagemErro(error));
    else setEnviado(true);
  }

  return (
    <div className="vendas">
      <header className="vendas-topo">
        <Link to="/"><img src="/logo-curto.png" alt="Várzea, início" /></Link>
        <nav aria-label="Seções">
          <a href="#como">Como funciona</a>
          <a href="#tabela">Tabela</a>
          <a href="#apoie">Apoie seu time</a>
        </nav>
        <Link to={sessao ? '/app' : '/entrar'} className="botao botao-secundario botao-pequeno">{sessao ? 'Abrir o app' : 'Entrar'}</Link>
        <BarraProgresso />
      </header>

      <section className="hero">
        <Skyline />
        <div className="bola-rolando" aria-hidden><SoccerBall size={30} weight="fill" /></div>
        <div className="hero-conteudo">
          <div>
            <img src="/logo.png" alt="Várzea, o Brasileirão dos bairros" className="hero-logo" />
            <h1>
              <Palavras texto="E se a Mooca jogasse contra o Tatuapé, e cada buraco tapado fosse" />
              <span className="destaque">um gol?<Pincelada /></span>
            </h1>
            <p className="hero-texto entra-depois">A Várzea transforma São Paulo em um campeonato. Cada distrito é um time, com escudo, torcida e rival. Quem marca ponto é quem melhora a rua de verdade.</p>
            <div className="hero-acoes entra-depois">
              <Link to={destino} className="botao botao-primario botao-grande">Escolha seu time <SoccerBall className="bola-botao" size={22} weight="fill" aria-hidden /></Link>
              <a href="#apoie" className="botao botao-secundario botao-grande">Quero apoiar</a>
            </div>
            <p className="meta entra-depois">Grátis para moradores. Os 96 distritos da capital já estão na tabela.</p>
          </div>
          <CelularDemo />
        </div>
      </section>

      <Letreiro itens={letreiro} />

      <section className="papel" data-theme="light" id="como">
        <Rasgo />
        <div className="secao">
          <Revelar><h2 className="titulo-marca">Como o jogo funciona</h2></Revelar>
          <PassosComBola />
        </div>
        <Rasgo inverso />
      </section>

      <Narracao />

      <section className="secao dados">
        <div>
          <Revelar><h2 className="titulo-marca">Gol difícil vale mais</h2></Revelar>
          <div className="numeros">
            <Revelar><strong><Contador para={104604} /></strong><span>chamados de rua analisados no SP156</span></Revelar>
            <Revelar atraso={120}><strong><Contador para={88.1} casas={1} sufixo="%" /></strong><span>fecham em até 30 dias</span></Revelar>
            <Revelar atraso={240}><strong className="destaque-texto"><Contador para={33.6} casas={1} sufixo="%" /></strong><span>em guias e sarjetas</span></Revelar>
          </div>
          <Revelar>
            <p>A média esconde quem espera. Por isso, cada problema vale gols conforme a dificuldade, e a torcida não corre só atrás do mais fácil.</p>
            <p className="meta">Fonte: SP156, Prefeitura de São Paulo, último trimestre de 2025. Fechar o chamado não prova o conserto, e é por isso que existe o VAR.</p>
          </Revelar>
        </div>
        <Revelar atraso={120} className="tabela-rolagem">
          <table className="tabela tabela-chance">
            <thead><tr><th scope="col">Problema</th><th scope="col">Fecha em 30 dias</th><th scope="col">Vale</th></tr></thead>
            <tbody>
              {GOLS_EXEMPLO.map((g) => (
                <tr key={g.servico}>
                  <td>{g.servico}</td>
                  <td>
                    {g.chance.toLocaleString('pt-BR', { minimumFractionDigits: 1 })}%
                    <BarraChance valor={g.chance} />
                  </td>
                  <td className="forte destaque-texto celula-gols">{g.gols} {g.gols === 1 ? 'gol' : 'gols'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </Revelar>
      </section>

      <section className="secao ao-vivo" id="tabela">
        <Revelar>
          <h2 className="titulo-marca">A tabela agora</h2>
          <p className="meta">Dados de demonstração enquanto o primeiro campeonato não começa.</p>
          <div className="tabela-rolagem">
            <table className="tabela">
              <thead><tr><th scope="col">#</th><th scope="col">Time</th><th scope="col">P</th><th scope="col">G</th></tr></thead>
              <tbody className="linhas-entram">
                {tabela.map((l, i) => {
                  const estilo: ComVariaveis = { '--i': i };
                  return (
                    <tr key={l.distrito_id} style={estilo}>
                      <td>{i + 1}</td>
                      <td className="celula-time"><Escudo nome={l.nome} tamanho={24} /><span>{l.nome}</span></td>
                      <td className="forte">{l.pontos}</td>
                      <td>{l.gols_pro}</td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
            {tabela.length === 0 && <p className="carregando">Carregando</p>}
          </div>
        </Revelar>
        <Revelar atraso={120}>
          <h2 className="titulo-marca">Últimos gols</h2>
          <p className="meta">Também de demonstração.</p>
          <ul className="lista-gols linhas-entram">
            {gols.map((g, i) => {
              const estilo: ComVariaveis = { '--i': i };
              return (
                <li key={`${g.distrito}-${i}`} style={estilo}>
                  <Escudo nome={g.distrito} tamanho={32} />
                  <div><strong>{g.distrito}</strong><span>{g.servico}</span></div>
                  <b className="destaque-texto">+{g.valor}</b>
                </li>
              );
            })}
          </ul>
        </Revelar>
      </section>

      <section className="papel" data-theme="light" id="apoie">
        <Rasgo />
        <div className="secao apoie">
          <div>
            <Revelar>
              <h2 className="titulo-marca">Apoie o seu time</h2>
              <p className="apoie-intro">Morador joga de graça. Quem quer ver o bairro em campo pode entrar antes de todo mundo.</p>
            </Revelar>
            <ul className="planos">
              <li><Revelar><Storefront size={28} weight="fill" aria-hidden /><div><h3>Comércio do bairro</h3><p>Sua marca na camisa do time do distrito. Em preparação.</p></div></Revelar></li>
              <li><Revelar atraso={100}><Bank size={28} weight="fill" aria-hidden /><div><h3>Prefeituras e subprefeituras</h3><p>Um painel para ver onde os chamados atrasam e o que os moradores confirmam. Em preparação.</p></div></Revelar></li>
              <li><Revelar atraso={200}><Handshake size={28} weight="fill" aria-hidden /><div><h3>Associações de bairro</h3><p>Ajuda para colocar o seu distrito em campo na primeira temporada.</p></div></Revelar></li>
            </ul>
          </div>
          <Revelar atraso={100}>
            {enviado ? (
              <div className="cartao recebido" role="status">
                <Trophy size={40} weight="fill" aria-hidden />
                <h3>Recebemos o seu interesse</h3>
                <p>Vamos falar com você pelo e-mail informado.</p>
                <Confete ativo quantidade={30} />
              </div>
            ) : (
              <form className="formulario cartao" onSubmit={enviarInteresse}>
                <h3 className="titulo-formulario">Entre na lista de interesse</h3>
                <label>Seu nome<input required minLength={2} maxLength={80} autoComplete="name" value={form.nome} onChange={(e) => setForm({ ...form, nome: e.target.value })} /></label>
                <label>E-mail<input type="email" required maxLength={160} autoComplete="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} /></label>
                <label>
                  Você representa
                  <select value={form.tipo} onChange={(e) => setForm({ ...form, tipo: e.target.value })}>
                    {TIPOS.map((t) => <option key={t.valor} value={t.valor}>{t.rotulo}</option>)}
                  </select>
                </label>
                <label>Mensagem (opcional)<textarea rows={3} maxLength={500} value={form.mensagem} onChange={(e) => setForm({ ...form, mensagem: e.target.value })} /></label>
                <label className="consentimento">
                  <input type="checkbox" required checked={form.consentiu} onChange={(e) => setForm({ ...form, consentiu: e.target.checked })} />
                  <span>Concordo em receber contato da Várzea sobre este interesse.</span>
                </label>
                {erro && <p className="alerta alerta-erro" role="alert">{erro}</p>}
                <button className="botao botao-primario" disabled={enviando}>{enviando ? 'Enviando' : 'Quero saber mais'}</button>
              </form>
            )}
          </Revelar>
        </div>
        <Rasgo inverso />
      </section>

      <section className="final">
        <ChuteGol />
        <Revelar><h2>Seu distrito vai ficar no banco?</h2></Revelar>
        <Revelar atraso={100}><p>Escolha o time, chame os vizinhos e coloque a primeira bola em jogo.</p></Revelar>
        <Revelar atraso={200}><Link to={destino} className="botao botao-primario botao-grande">Entrar em campo <SoccerBall className="bola-botao" size={22} weight="fill" aria-hidden /></Link></Revelar>
      </section>

      <footer className="vendas-rodape">
        <img src="/logo.png" alt="Várzea" />
        <p>Projeto da Global Solution FIAP 2026. Não é um canal oficial da Prefeitura de São Paulo: pedidos oficiais continuam no SP156.</p>
        <p className="meta"><MapPinArea size={14} weight="fill" aria-hidden /> São Paulo, SP</p>
      </footer>
    </div>
  );
}
