import { useEffect, useMemo, useState, type FormEvent } from 'react';
import { Navigate, useNavigate, useSearchParams } from 'react-router-dom';
import { Escudo } from '../components/Escudo';
import { useSessao } from '../lib/sessao';
import { mensagemErro, supabase, type Distrito } from '../lib/supabase';

export function EscolherTime() {
  const { sessao, perfil, carregando, avaliador, atualizarPerfil } = useSessao();
  const navegar = useNavigate();
  const [params] = useSearchParams();
  const [distritos, setDistritos] = useState<Distrito[]>([]);
  const [nome, setNome] = useState(avaliador ? 'Avaliador' : '');
  const [busca, setBusca] = useState('');
  const [escolhido, setEscolhido] = useState<Distrito | null>(null);
  const [erro, setErro] = useState('');
  const [enviando, setEnviando] = useState(false);

  useEffect(() => {
    supabase.from('distritos').select('id, nome, zona').order('nome').then(({ data, error }) => {
      if (error) setErro(mensagemErro(error));
      else {
        setDistritos(data ?? []);
        const vindo = (data ?? []).find((d) => d.id === Number(params.get('distrito')));
        if (vindo) setEscolhido(vindo);
      }
    });
  }, []);

  const filtrados = useMemo(() => {
    const normalizar = (t: string) => t.normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase();
    return distritos.filter((d) => normalizar(d.nome).includes(normalizar(busca)));
  }, [distritos, busca]);

  if (carregando) return <p className="carregando tela-cheia">Carregando</p>;
  if (!sessao) return <Navigate to="/entrar" replace />;
  if (perfil) return <Navigate to="/app" replace />;

  async function salvar(evento: FormEvent) {
    evento.preventDefault();
    if (!escolhido) {
      setErro('Escolha o seu distrito.');
      return;
    }
    setErro('');
    setEnviando(true);
    const { error } = await supabase.from('perfis').insert({ id: sessao!.user.id, nome: nome.trim(), distrito_id: escolhido.id });
    if (error) {
      setErro(mensagemErro(error));
      setEnviando(false);
      return;
    }
    await atualizarPerfil();
    navegar('/app', { replace: true });
  }

  return (
    <main className="tela-auth tela-time">
      <h1>Escolha seu time</h1>
      <p className="apoio">Seu time é o distrito onde você mora. Só vizinhos confirmam os gols, por isso a escolha não muda depois.</p>
      <form onSubmit={salvar} className="formulario">
        <label>
          Como a torcida vai te chamar?
          <input required minLength={2} maxLength={60} autoComplete="nickname" value={nome} onChange={(e) => setNome(e.target.value)} />
        </label>
        <label>
          Procure o seu distrito
          <input type="search" value={busca} onChange={(e) => setBusca(e.target.value)} placeholder="Ex.: Mooca" />
        </label>
        <div className="grade-times" role="radiogroup" aria-label="Distritos">
          {filtrados.map((d) => (
            <button
              key={d.id}
              type="button"
              role="radio"
              aria-checked={escolhido?.id === d.id}
              className={`opcao-time ${escolhido?.id === d.id ? 'ativo' : ''}`}
              onClick={() => setEscolhido(d)}
            >
              <Escudo nome={d.nome} tamanho={36} />
              <span>{d.nome}</span>
              <small>{d.zona}</small>
            </button>
          ))}
          {distritos.length > 0 && filtrados.length === 0 && <p className="apoio">Nenhum distrito com esse nome.</p>}
        </div>
        {erro && <p className="alerta alerta-erro" role="alert">{erro}</p>}
        <div className="rodape-fixo">
          <button className="botao botao-primario" disabled={enviando}>
            {enviando ? 'Aguarde' : escolhido ? `Torcer por ${escolhido.nome}` : 'Escolha um distrito'}
          </button>
        </div>
      </form>
    </main>
  );
}
