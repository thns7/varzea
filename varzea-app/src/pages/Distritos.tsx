import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { CartaoChamado } from '../components/CartaoChamado';
import { useSessao } from '../lib/sessao';
import { mensagemErro, SELECT_CHAMADO, supabase, type Chamado, type Distrito } from '../lib/supabase';

const STATUS = [
  { valor: 'todos', rotulo: 'Todos' },
  { valor: 'resolvido', rotulo: 'No VAR' },
  { valor: 'aberto', rotulo: 'Abertos' },
  { valor: 'validado', rotulo: 'Gols' },
];

export function Distritos() {
  const { perfil } = useSessao();
  const [distritos, setDistritos] = useState<Distrito[]>([]);
  const [distritoId, setDistritoId] = useState(perfil?.distrito_id ?? 0);
  const [status, setStatus] = useState('todos');
  const [chamados, setChamados] = useState<Chamado[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState('');

  useEffect(() => {
    supabase.from('distritos').select('id, nome, zona').order('nome').then(({ data }) => setDistritos(data ?? []));
  }, []);

  useEffect(() => {
    if (!distritoId) return;
    let ativo = true;
    setCarregando(true);
    setErro('');
    let consulta = supabase.from('chamados').select(SELECT_CHAMADO).eq('distrito_id', distritoId).neq('status', 'cancelado');
    if (status !== 'todos') consulta = consulta.eq('status', status);
    consulta.order('criado_em', { ascending: false }).limit(50).then(({ data, error }) => {
      if (!ativo) return;
      if (error) setErro(mensagemErro(error));
      else setChamados((data ?? []) as unknown as Chamado[]);
      setCarregando(false);
    });
    return () => {
      ativo = false;
    };
  }, [distritoId, status]);

  return (
    <>
      <h1 className="titulo-pagina">Distritos</h1>
      <label className="campo-solto">
        Ver chamados de
        <select value={distritoId} onChange={(e) => setDistritoId(Number(e.target.value))}>
          {distritos.map((d) => <option key={d.id} value={d.id}>{d.nome}</option>)}
        </select>
      </label>
      <div className="filtros" role="group" aria-label="Filtrar por situação">
        {STATUS.map((s) => (
          <button key={s.valor} type="button" className={status === s.valor ? 'ativo' : ''} aria-pressed={status === s.valor} onClick={() => setStatus(s.valor)}>{s.rotulo}</button>
        ))}
      </div>
      {carregando && <p className="carregando">Carregando</p>}
      {erro && <p className="alerta alerta-erro" role="alert">{erro}</p>}
      {!carregando && !erro && chamados.length === 0 && (
        <div className="vazio">
          <h3>Nenhum chamado aqui</h3>
          <p className="apoio">{distritoId === perfil?.distrito_id ? 'Seja o primeiro a colocar a bola em jogo.' : 'Este time ainda não entrou em campo.'}</p>
          {distritoId === perfil?.distrito_id && <Link to="/app/reportar" className="botao botao-secundario">Reportar problema</Link>}
        </div>
      )}
      {!carregando && !erro && chamados.length > 0 && <div className="lista">{chamados.map((c) => <CartaoChamado key={c.id} chamado={c} />)}</div>}
    </>
  );
}
