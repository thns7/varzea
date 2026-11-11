import { lazy, Suspense, useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { CaretDown, CaretUp, MagnifyingGlass, Pause, Play, SoccerBall } from '@phosphor-icons/react';
import { Casca } from '../components/Casca';
import { Escudo } from '../components/Escudo';
import type { PlacarDistrito } from '../components/Cidade3D';
import { carregarDistritosGeo, chaveDistrito, type DistritoGeo } from '../lib/geo';
import { useMovimentoReduzido } from '../lib/movimento';
import { useSessao } from '../lib/sessao';
import { mensagemErro, plural, supabase, type LinhaTabela, type Partida } from '../lib/supabase';

// O Three.js só é baixado quando o mapa aparece, para a primeira tela abrir rápido.
const Cidade3D = lazy(() => import('../components/Cidade3D').then((m) => ({ default: m.Cidade3D })));

interface Linha extends LinhaTabela {
  chave: string;
  posicao: number;
}

export function Mapa() {
  const { sessao, perfil } = useSessao();
  const reduzido = useMovimentoReduzido();
  const [geo, setGeo] = useState<DistritoGeo[]>([]);
  const [tabela, setTabela] = useState<Linha[]>([]);
  const [partidas, setPartidas] = useState<Partida[]>([]);
  const [selecionado, setSelecionado] = useState<string | null>(null);
  const [busca, setBusca] = useState('');
  const [aberto, setAberto] = useState(false);
  const [girar, setGirar] = useState(true);
  const [erro, setErro] = useState('');

  useEffect(() => {
    Promise.all([carregarDistritosGeo(), supabase.rpc('classificacao'), supabase.rpc('placar_partidas')])
      .then(([distritos, classificacao, placar]) => {
        if (classificacao.error) throw classificacao.error;
        setGeo(distritos);
        setTabela(((classificacao.data ?? []) as LinhaTabela[]).map((l, i) => ({ ...l, chave: chaveDistrito(l.nome), posicao: i + 1 })));
        setPartidas((placar.data ?? []) as Partida[]);
      })
      .catch((e) => setErro(mensagemErro(e)));
  }, []);

  const placar = useMemo(
    () => new Map<string, PlacarDistrito>(tabela.map((l) => [l.chave, { nome: l.nome, gols: l.gols_pro }])),
    [tabela],
  );
  const meuTime = perfil?.distritos?.nome ? chaveDistrito(perfil.distritos.nome) : null;
  const linha = tabela.find((l) => l.chave === selecionado) ?? null;
  const partida = linha ? partidas.find((p) => p.casa_id === linha.distrito_id || p.fora_id === linha.distrito_id) : null;

  const encontrados = useMemo(() => {
    const termo = chaveDistrito(busca);
    return termo.length < 2 ? [] : tabela.filter((l) => l.chave.includes(termo)).slice(0, 6);
  }, [busca, tabela]);

  function selecionar(chave: string) {
    setSelecionado(chave);
    setGirar(false);
    // No celular a gaveta fecha para mostrar a câmera indo até o distrito; o nome fica na alça.
    setAberto(false);
    setBusca('');
  }

  const logado = Boolean(sessao && perfil);

  return (
    <Casca cheia>
      <div className="mapa-pagina">
        <div className="mapa-palco">
          {geo.length > 0 && tabela.length > 0 ? (
            <Suspense fallback={<p className="carregando mapa-aviso">Montando a cidade</p>}>
              <Cidade3D
                distritos={geo}
                placar={placar}
                selecionado={selecionado}
                meuTime={meuTime}
                girar={girar && !reduzido}
                onSelecionar={selecionar}
              />
            </Suspense>
          ) : (
            !erro && <p className="carregando mapa-aviso">Montando a cidade</p>
          )}
          {erro && <p className="alerta alerta-erro mapa-aviso" role="alert">{erro}</p>}

          <div className="mapa-legenda" aria-hidden>
            <span>Menos gols</span>
            <i />
            <span>Mais gols</span>
          </div>
          {!reduzido && (
            <button type="button" className="mapa-giro" onClick={() => setGirar(!girar)} aria-pressed={!girar}>
              {girar ? <Pause size={16} weight="fill" aria-hidden /> : <Play size={16} weight="fill" aria-hidden />}
              {girar ? 'Parar giro' : 'Girar mapa'}
            </button>
          )}
        </div>

        <section className={`mapa-painel ${aberto ? 'aberto' : ''}`} aria-label="Campeonato">
          <button type="button" className="painel-alca" onClick={() => setAberto(!aberto)} aria-expanded={aberto}>
            {aberto ? <CaretDown size={18} weight="bold" aria-hidden /> : <CaretUp size={18} weight="bold" aria-hidden />}
            <span>{aberto ? 'Ver o mapa' : linha ? `${linha.nome}: ver detalhes` : 'Ver a classificação'}</span>
          </button>

          <div className="painel-conteudo">
            {!linha && (
              <header className="painel-intro">
                <h1>O campeonato dos bairros</h1>
                <p className="apoio">Cada distrito é um time. Conserto confirmado por vizinhos vira gol, e quanto mais gols, mais alto o distrito fica no mapa.</p>
              </header>
            )}

            <label className="busca-distrito">
              <MagnifyingGlass size={18} aria-hidden />
              <input type="search" value={busca} onChange={(e) => setBusca(e.target.value)} placeholder="Procurar distrito" aria-label="Procurar distrito" />
            </label>
            {encontrados.length > 0 && (
              <ul className="busca-resultados">
                {encontrados.map((l) => (
                  <li key={l.chave}><button type="button" onClick={() => selecionar(l.chave)}>{l.nome}</button></li>
                ))}
              </ul>
            )}

            {linha && (
              <article className="cartao distrito-cartao">
                <div className="distrito-topo">
                  <Escudo nome={linha.nome} tamanho={52} />
                  <div>
                    <p className="rotulo">{linha.zona} · {linha.posicao}º lugar</p>
                    <h2>{linha.nome}</h2>
                  </div>
                </div>
                <dl className="distrito-numeros">
                  <div><dt>Gols</dt><dd>{linha.gols_pro}</dd></div>
                  <div><dt>Pontos</dt><dd>{linha.pontos}</dd></div>
                  <div><dt>Vitórias</dt><dd>{linha.vitorias}</dd></div>
                </dl>
                {partida ? (
                  <p className="distrito-partida">
                    <SoccerBall size={16} weight="fill" aria-hidden />
                    Rodada {partida.rodada_numero}: {partida.casa} {partida.gols_casa} x {partida.gols_fora} {partida.fora}
                  </p>
                ) : (
                  <p className="apoio">Sem partida nesta rodada.</p>
                )}
                {logado ? (
                  <Link to={`/app/bairros?distrito=${linha.distrito_id}`} className="botao botao-primario">Ver chamados</Link>
                ) : (
                  <Link to="/entrar?modo=criar" className="botao botao-primario">Entrar para jogar</Link>
                )}
                <button type="button" className="botao-texto" onClick={() => setSelecionado(null)}>Voltar para a classificação</button>
              </article>
            )}

            {!linha && (
              <>
                <h2 className="titulo-secao">Classificação</h2>
                <ol className="ranking">
                  {tabela.slice(0, 10).map((l) => (
                    <li key={l.chave}>
                      <button type="button" onClick={() => selecionar(l.chave)} className={l.chave === meuTime ? 'meu' : ''}>
                        <span className="ranking-pos">{l.posicao}</span>
                        <Escudo nome={l.nome} tamanho={26} />
                        <span className="ranking-nome">{l.nome}</span>
                        <span className="ranking-gols">{plural(l.gols_pro, 'gol', 'gols')}</span>
                      </button>
                    </li>
                  ))}
                </ol>
                {logado ? (
                  <Link to="/app/tabela" className="botao botao-secundario">Ver os 96 distritos</Link>
                ) : (
                  <Link to="/entrar?modo=criar" className="botao botao-primario">Escolher meu time</Link>
                )}
                <p className="meta painel-nota">Dados de demonstração. Mapa: GeoSampa, Prefeitura de São Paulo. A Várzea não é um canal oficial; pedidos oficiais continuam no SP156.</p>
              </>
            )}
          </div>
        </section>
      </div>
    </Casca>
  );
}
