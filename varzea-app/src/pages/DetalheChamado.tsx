import { useCallback, useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Flask, SoccerBall } from '@phosphor-icons/react';
import { Escudo } from '../components/Escudo';
import { Confete, Contador } from '../components/Movimento';
import { LinhaDoTempo, MonitorVar } from '../components/Arquibancada';
import { enviarFoto } from '../lib/foto';
import { useSessao } from '../lib/sessao';
import { dataCurta, mensagemErro, ROTULO_STATUS, SELECT_CHAMADO, supabase, urlFoto, type Chamado } from '../lib/supabase';

interface Voto { id: number; morador_id: string; resultado: 'confirma' | 'contesta'; foto_path: string }

export function DetalheChamado() {
  const { id } = useParams();
  const { perfil, avaliador } = useSessao();
  const [chamado, setChamado] = useState<Chamado | null>(null);
  const [votos, setVotos] = useState<Voto[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [foto, setFoto] = useState<File | null>(null);
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState('');
  const [desfecho, setDesfecho] = useState<'gol' | 'anulado' | 'aguardando' | null>(null);

  const carregar = useCallback(async () => {
    const [resposta, respostaVotos] = await Promise.all([
      supabase.from('chamados').select(SELECT_CHAMADO).eq('id', Number(id)).maybeSingle(),
      supabase.from('confirmacoes').select('id, morador_id, resultado, foto_path').eq('chamado_id', Number(id)).order('criado_em'),
    ]);
    if (resposta.error) setErro(mensagemErro(resposta.error));
    setChamado(resposta.data as unknown as Chamado | null);
    setVotos((respostaVotos.data ?? []) as Voto[]);
    setCarregando(false);
  }, [id]);

  useEffect(() => {
    void carregar();
  }, [carregar]);

  if (carregando) return <p className="carregando">Carregando</p>;
  if (!chamado) {
    return (
      <div className="vazio">
        <h3>Chamado não encontrado</h3>
        <Link to="/app/bairros" className="botao botao-secundario">Ver chamados</Link>
      </div>
    );
  }

  if (desfecho === 'gol') {
    return (
      <section className="tela-gol" role="status">
        <SoccerBall size={64} weight="fill" aria-hidden />
        <h1>Gol marcado!</h1>
        <p>{chamado.servicos.nome}</p>
        <span className="selo-resolvido">Resolvido</span>
        <div className="gol-time">
          <Escudo nome={chamado.distritos.nome} tamanho={44} />
          <p><strong>+<Contador para={chamado.servicos.gols} duracao={700} /> {chamado.servicos.gols === 1 ? 'gol' : 'gols'}</strong> para {chamado.distritos.nome}</p>
        </div>
        <Link to="/app/tabela" className="botao botao-primario">Ver tabela</Link>
        <Confete ativo quantidade={48} />
      </section>
    );
  }

  const meuDistrito = perfil?.distrito_id === chamado.distrito_id;
  const envolvido = perfil?.id === chamado.autor_id || perfil?.id === chamado.resolvido_por;
  const jaVotou = votos.some((v) => v.morador_id === perfil?.id);
  const confirmacoes = votos.filter((v) => v.resultado === 'confirma').length;
  const contestacoes = votos.length - confirmacoes;

  async function executar(acao: () => Promise<void>) {
    setErro('');
    setEnviando(true);
    try {
      await acao();
    } catch (e) {
      setErro(mensagemErro(e));
    } finally {
      setEnviando(false);
    }
  }

  const marcarResolvido = () => executar(async () => {
    const { error } = await supabase.rpc('marcar_resolvido', { p_chamado: chamado.id });
    if (error) throw error;
    await carregar();
  });

  const cancelar = () => executar(async () => {
    const { error } = await supabase.rpc('cancelar_chamado', { p_chamado: chamado.id });
    if (error) throw error;
    await carregar();
  });

  const votar = (resultado: 'confirma' | 'contesta') => executar(async () => {
    if (!foto || !perfil) throw new Error('Tire ou escolha uma foto do local antes de votar.');
    const caminho = await enviarFoto(foto, perfil.id);
    const { data, error } = await supabase.rpc('votar_var', { p_chamado: chamado.id, p_resultado: resultado, p_foto_path: caminho });
    if (error) throw error;
    setFoto(null);
    setDesfecho(data as 'gol' | 'anulado' | 'aguardando');
    await carregar();
  });

  const varDeTeste = () => executar(async () => {
    const { data, error } = await supabase.rpc('var_demonstracao', { p_chamado: chamado.id });
    if (error) throw error;
    setDesfecho(data as 'gol');
    await carregar();
  });

  return (
    <>
      <Link to="/app/bairros" className="botao-texto voltar">Voltar para os chamados</Link>
      <div className="linha-tags">
        <span className={`tag tag-${chamado.status}`}>{ROTULO_STATUS[chamado.status]}</span>
        {chamado.demo && <span className="tag">Demonstração</span>}
      </div>
      <header className="chamado-cabecalho">
        <div>
          <h1 className="titulo-pagina">{chamado.servicos.nome}</h1>
          <p>{chamado.referencia}</p>
          <p className="meta">
            {chamado.distritos.nome} · aberto em {dataCurta(chamado.criado_em)}
            {chamado.resolvido_em && ` · resolvido em ${dataCurta(chamado.resolvido_em)}`}
          </p>
        </div>
        <div className="chamado-valor" aria-label={`Vale ${chamado.servicos.gols} ${chamado.servicos.gols === 1 ? 'gol' : 'gols'}`}>
          <span aria-hidden>Vale</span>
          <strong aria-hidden>{chamado.servicos.gols}</strong>
          <span aria-hidden>{chamado.servicos.gols === 1 ? 'gol' : 'gols'}</span>
        </div>
      </header>
      {chamado.status !== 'cancelado' && <LinhaDoTempo status={chamado.status} />}
      {chamado.foto_path && <img className="foto-chamado" src={urlFoto(chamado.foto_path)} alt={`Foto enviada para ${chamado.servicos.nome}`} />}
      {chamado.status === 'validado' && <MonitorVar confirmacoes={2} contestacoes={0} validado />}

      {desfecho === 'anulado' && <p className="alerta alerta-erro" role="status">Gol anulado: 2 vizinhos contestaram, e o chamado voltou a ficar aberto.</p>}
      {desfecho === 'aguardando' && <p className="alerta alerta-ok" role="status">Voto registrado. Falta mais vizinho confirmar.</p>}
      {erro && <p className="alerta alerta-erro" role="alert">{erro}</p>}

      {chamado.status === 'aberto' && (
        <section className="bloco-acao">
          <h2 className="titulo-secao">Já consertaram?</h2>
          {meuDistrito ? (
            <>
              <p className="apoio">Marque como resolvido para outros vizinhos conferirem no VAR.</p>
              <button className="botao botao-primario" disabled={enviando} onClick={marcarResolvido}>Marcar como resolvido</button>
            </>
          ) : (
            <p className="apoio">Só moradores de {chamado.distritos.nome} podem marcar este chamado.</p>
          )}
          {perfil?.id === chamado.autor_id && <button className="botao-texto" disabled={enviando} onClick={cancelar}>Cancelar este chamado</button>}
        </section>
      )}

      {chamado.status === 'resolvido' && (
        <section className="bloco-acao">
          <MonitorVar confirmacoes={confirmacoes} contestacoes={contestacoes} />
          {!meuDistrito && <p className="apoio">Só vizinhos de {chamado.distritos.nome} podem votar.</p>}
          {meuDistrito && envolvido && <p className="apoio">Quem relatou ou marcou como resolvido não vota neste chamado. Chame seus vizinhos.</p>}
          {avaliador && perfil?.id === chamado.resolvido_por && (
            <div className="var-teste">
              <p className="rotulo"><Flask size={16} weight="fill" aria-hidden /> Modo avaliação</p>
              <p className="apoio">Sem vizinhos por perto? Dois vizinhos de teste conferem o local e confirmam o conserto.</p>
              <button className="botao botao-primario" disabled={enviando} onClick={varDeTeste}>Chamar o VAR de teste</button>
            </div>
          )}
          {meuDistrito && !envolvido && jaVotou && <p className="apoio">Você já votou neste chamado.</p>}
          {meuDistrito && !envolvido && !jaVotou && (
            <>
              <label className="campo-solto">
                Foto do local agora
                <input type="file" accept="image/*" capture="environment" onChange={(e) => setFoto(e.target.files?.[0] ?? null)} />
                <small>Obrigatória para votar. Evite rostos e placas.</small>
              </label>
              <div className="botoes-var">
                <button className="botao botao-primario" disabled={enviando} onClick={() => votar('confirma')}>Foi resolvido</button>
                <button className="botao botao-secundario" disabled={enviando} onClick={() => votar('contesta')}>Não foi resolvido</button>
              </div>
            </>
          )}
        </section>
      )}

      {votos.length > 0 && (
        <section>
          <h2 className="titulo-secao">Fotos do VAR</h2>
          <div className="fotos-var">
            {votos.map((v) => (
              <figure key={v.id}>
                <img src={urlFoto(v.foto_path)} alt={v.resultado === 'confirma' ? 'Foto de quem confirmou' : 'Foto de quem contestou'} loading="lazy" />
                <figcaption>{v.resultado === 'confirma' ? 'Confirmou' : 'Contestou'}</figcaption>
              </figure>
            ))}
          </div>
        </section>
      )}
    </>
  );
}
