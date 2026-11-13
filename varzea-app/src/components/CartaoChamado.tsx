import { Link } from 'react-router-dom';
import { dataCurta, plural, ROTULO_STATUS, type Chamado } from '../lib/supabase';

/** Chamado em forma de ingresso: o canhoto mostra quantos gols ele vale. */
export function CartaoChamado({ chamado }: { chamado: Chamado }) {
  const validado = chamado.status === 'validado';
  return (
    <Link to={`/app/chamado/${chamado.id}`} className={`cartao cartao-link ingresso ${validado ? 'ingresso-validado' : ''}`}>
      <div className="ingresso-corpo">
        <div className="linha-tags">
          <span className={`tag tag-${chamado.status}`}>{ROTULO_STATUS[chamado.status]}</span>
          {chamado.demo && <span className="tag">Demonstração</span>}
        </div>
        <h3>{chamado.servicos.nome}</h3>
        <p className="apoio">{chamado.referencia}</p>
        <p className="meta">{chamado.distritos.nome} · aberto em {dataCurta(chamado.criado_em)}</p>
      </div>
      <div className="ingresso-canhoto" aria-label={plural(chamado.servicos.gols, 'gol', 'gols')}>
        <strong aria-hidden>{validado ? '+' : ''}{chamado.servicos.gols}</strong>
        <span aria-hidden>{chamado.servicos.gols === 1 ? 'gol' : 'gols'}</span>
      </div>
    </Link>
  );
}
