import { useEffect, useState } from 'react';
import { Escudo } from '../components/Escudo';
import { Podio } from '../components/Arquibancada';
import { useSessao } from '../lib/sessao';
import { mensagemErro, supabase, type LinhaTabela } from '../lib/supabase';

const ZONAS = ['Todas', 'Centro', 'Norte', 'Sul', 'Leste', 'Oeste'];

export function Tabela() {
  const { perfil } = useSessao();
  const [linhas, setLinhas] = useState<LinhaTabela[]>([]);
  const [zona, setZona] = useState('Todas');
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState('');

  useEffect(() => {
    supabase.rpc('classificacao').then(({ data, error }) => {
      if (error) setErro(mensagemErro(error));
      else setLinhas(data as LinhaTabela[]);
      setCarregando(false);
    });
  }, []);

  const visiveis = linhas.map((l, i) => ({ ...l, lugar: i + 1 })).filter((l) => zona === 'Todas' || l.zona === zona);

  return (
    <>
      <h1 className="titulo-pagina">Tabela</h1>
      <p className="apoio">Vitória vale 3 pontos e empate vale 1, em rodadas já encerradas. O desempate é por gols.</p>
      <div className="filtros" role="group" aria-label="Filtrar por zona">
        {ZONAS.map((z) => (
          <button key={z} type="button" className={zona === z ? 'ativo' : ''} aria-pressed={zona === z} onClick={() => setZona(z)}>{z}</button>
        ))}
      </div>
      {carregando && <p className="carregando">Carregando</p>}
      {erro && <p className="alerta alerta-erro" role="alert">{erro}</p>}
      {!carregando && !erro && zona === 'Todas' && linhas.length >= 3 && (
        <Podio linhas={linhas.slice(0, 3).map((l, i) => ({ chave: String(l.distrito_id), nome: l.nome, gols: l.gols_pro, posicao: i + 1 }))} />
      )}
      {!carregando && !erro && (
        <div className="tabela-rolagem">
          <table className="tabela">
            <thead>
              <tr><th scope="col">#</th><th scope="col">Time</th><th scope="col" title="Pontos">P</th><th scope="col" title="Jogos">J</th><th scope="col" title="Vitórias">V</th><th scope="col" title="Gols">G</th></tr>
            </thead>
            <tbody>
              {visiveis.map((l) => (
                <tr key={l.distrito_id} className={l.distrito_id === perfil?.distrito_id ? 'meu' : ''}>
                  <td><span className={`posicao ${l.lugar <= 3 ? 'posicao-topo' : ''}`}>{l.lugar}</span></td>
                  <td className="celula-time"><Escudo nome={l.nome} tamanho={24} /><span>{l.nome}</span></td>
                  <td className="forte">{l.pontos}</td>
                  <td>{l.jogos}</td>
                  <td>{l.vitorias}</td>
                  <td className="gols-tabela">{l.gols_pro}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </>
  );
}
