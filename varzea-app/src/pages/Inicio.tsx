import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Escudo } from '../components/Escudo';
import { CartaoChamado } from '../components/CartaoChamado';
import { useSessao } from '../lib/sessao';
import { GuiaAvaliacao } from '../components/Avaliacao';
import { Chapas, Placar } from '../components/Arquibancada';
import { mensagemErro, plural, SELECT_CHAMADO, supabase, type Chamado, type LinhaTabela, type Partida } from '../lib/supabase';

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

  const distrito = perfil?.distritos?.nome ?? '';
  return (
    <>
      <header className="inicio-cabecalho">
        <img src="/logo-curto.png" alt="Várzea" className="inicio-logo" />
        <div className="inicio-time">
          <Escudo nome={distrito} tamanho={64} />
          <div>
            <p className="rotulo">Olá, {perfil?.nome} · você torce por</p>
            <h1>{distrito}</h1>
            {posicao && <p className="apoio">{posicao.linha.zona} · {plural(posicao.linha.jogos, 'jogo', 'jogos')} · {plural(posicao.linha.vitorias, 'vitória', 'vitórias')}</p>}
          </div>
        </div>
        {posicao && (
          <dl className="faixa-numeros">
            <div><dt>Lugar</dt><dd><Chapas valor={posicao.lugar} /></dd></div>
            <div><dt>Gols</dt><dd><Chapas valor={posicao.linha.gols_pro} /></dd></div>
            <div><dt>Pontos</dt><dd><Chapas valor={posicao.linha.pontos} /></dd></div>
          </dl>
        )}
      </header>

      {partida && (
        <section className="bloco-inicio">
          <div className="titulo-com-acao">
            <h2 className="titulo-secao">Jogo da rodada</h2>
            <Link to="/app/tabela" className="botao-texto">Ver tabela</Link>
          </div>
          <Placar partida={partida} meuTime={perfil?.distrito_id} />
        </section>
      )}
      {folga && (
        <section className="cartao">
          <p className="rotulo">Nesta rodada</p>
          <h2>Seu time folga</h2>
          <p className="apoio">Mesmo assim, os gols validados agora contam para o seu distrito.</p>
          <Link to="/app/tabela" className="botao botao-secundario">Ver tabela</Link>
        </section>
      )}

      {avaliador && <GuiaAvaliacao />}

      <section className="bloco-inicio">
        <div className="titulo-com-acao">
          <h2 className="titulo-secao">Aguardando o VAR</h2>
          <Link to="/app/bairros" className="botao-texto">Ver todos</Link>
        </div>
        {aguardando.length === 0 ? (
          <div className="vazio">
            <h3>Nada para conferir agora</h3>
            <p className="apoio">Viu um problema na rua? Abra o primeiro chamado da rodada.</p>
            <Link to="/app/reportar" className="botao botao-primario">Reportar problema</Link>
          </div>
        ) : (
          <div className="lista">{aguardando.map((c) => <CartaoChamado key={c.id} chamado={c} />)}</div>
        )}
      </section>
    </>
  );
}
