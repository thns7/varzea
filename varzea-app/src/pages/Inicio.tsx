import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Escudo } from '../components/Escudo';
import { CartaoChamado } from '../components/CartaoChamado';
import { useSessao } from '../lib/sessao';
import { GuiaAvaliacao } from '../components/Avaliacao';
import { dataCurta, mensagemErro, plural, SELECT_CHAMADO, supabase, type Chamado, type LinhaTabela, type Partida } from '../lib/supabase';

export function Inicio() {
  const { perfil, avaliador } = useSessao();
  const [partida, setPartida] = useState<Partida | null>(null);
  const [folga, setFolga] = useState(false);
  const [posicao, setPosicao] = useState<{ linha: LinhaTabela; lugar: number } | null>(null);
  const [aguardando, setAguardando] = useState<Chamado[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState('');

  useEffect(() => {
    if (!perfil) return;
    (async () => {
      try {
        const [placar, tabela, chamados] = await Promise.all([
          supabase.rpc('placar_partidas'),
          supabase.rpc('classificacao'),
          supabase.from('chamados').select(SELECT_CHAMADO).eq('distrito_id', perfil.distrito_id).eq('status', 'resolvido').order('resolvido_em', { ascending: false }).limit(3),
        ]);
        for (const r of [placar, tabela, chamados]) if (r.error) throw r.error;
        const minha = (placar.data as Partida[]).find((p) => p.casa_id === perfil.distrito_id || p.fora_id === perfil.distrito_id);
        setPartida(minha ?? null);
        setFolga(!minha);
        const linhas = tabela.data as LinhaTabela[];
        const indice = linhas.findIndex((l) => l.distrito_id === perfil.distrito_id);
        if (indice >= 0) setPosicao({ linha: linhas[indice], lugar: indice + 1 });
        setAguardando((chamados.data ?? []) as unknown as Chamado[]);
      } catch (e) {
        setErro(mensagemErro(e));
      } finally {
        setCarregando(false);
      }
    })();
  }, [perfil]);

  if (carregando) return <p className="carregando">Carregando</p>;
  if (erro) return <p className="alerta alerta-erro" role="alert">{erro}</p>;

  return (
    <>
      <header className="topo-app">
        <img src="/logo-curto.png" alt="Várzea" />
        <p>Olá, {perfil?.nome}</p>
      </header>

      {avaliador && <GuiaAvaliacao />}

      {partida && (
        <section className="cartao partida-destaque" aria-label="Partida desta rodada">
          <p className="rotulo">Rodada {partida.rodada_numero} · {dataCurta(partida.inicio)} a {dataCurta(partida.fim)}</p>
          <div className="placar">
            <div><Escudo nome={partida.casa} tamanho={56} /><span>{partida.casa}</span></div>
            <strong>{partida.gols_casa} <small>x</small> {partida.gols_fora}</strong>
            <div><Escudo nome={partida.fora} tamanho={56} /><span>{partida.fora}</span></div>
          </div>
          <Link to="/app/tabela" className="botao botao-primario">Ver tabela</Link>
        </section>
      )}
      {folga && (
        <section className="cartao">
          <p className="rotulo">Nesta rodada</p>
          <h2>Seu time folga</h2>
          <p className="apoio">Mesmo assim, os gols validados agora contam para o seu distrito.</p>
          <Link to="/app/tabela" className="botao botao-primario">Ver tabela</Link>
        </section>
      )}

      {posicao && (
        <section className="cartao meu-time">
          <Escudo nome={posicao.linha.nome} tamanho={44} />
          <div>
            <p className="rotulo">Seu time no campeonato</p>
            <h2>{posicao.linha.nome}</h2>
            <p className="apoio">{plural(posicao.linha.pontos, 'ponto', 'pontos')} · {plural(posicao.linha.gols_pro, 'gol', 'gols')}</p>
          </div>
          <strong className="lugar">{posicao.lugar}º</strong>
        </section>
      )}

      <section>
        <h2 className="titulo-secao">Aguardando o VAR</h2>
        {aguardando.length === 0 ? (
          <div className="vazio">
            <h3>Nada para conferir agora</h3>
            <p className="apoio">Viu um problema na rua? Abra o primeiro chamado da rodada.</p>
            <Link to="/app/reportar" className="botao botao-secundario">Reportar problema</Link>
          </div>
        ) : (
          <div className="lista">{aguardando.map((c) => <CartaoChamado key={c.id} chamado={c} />)}</div>
        )}
      </section>
    </>
  );
}
