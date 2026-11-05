import { Link } from 'react-router-dom';
import { CaretRight } from '@phosphor-icons/react';
import { dataCurta, plural, ROTULO_STATUS, type Chamado } from '../lib/supabase';

export function CartaoChamado({ chamado }: { chamado: Chamado }) {
  return (
    <Link to={`/app/chamado/${chamado.id}`} className="cartao cartao-link cartao-chamado">
      <div className="cartao-chamado-texto">
        <div className="linha-tags">
          <span className={`tag tag-${chamado.status}`}>{ROTULO_STATUS[chamado.status]}</span>
          {chamado.demo && <span className="tag">Demonstração</span>}
        </div>
        <h3>{chamado.servicos.nome}</h3>
        <p className="apoio">{chamado.referencia}</p>
        <p className="meta">{chamado.distritos.nome} · aberto em {dataCurta(chamado.criado_em)}</p>
      </div>
      <div className="cartao-chamado-gols">
        <strong>{chamado.servicos.gols}</strong>
        <span>{chamado.servicos.gols === 1 ? 'gol' : 'gols'}</span>
        <CaretRight weight="bold" size={16} aria-hidden />
      </div>
      <span className="so-leitor">{plural(chamado.servicos.gols, 'gol', 'gols')}</span>
    </Link>
  );
}
