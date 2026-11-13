import type { ReactNode } from 'react';
import { Link, NavLink } from 'react-router-dom';
import { Camera, House, MapPinArea, MapTrifold, User } from '@phosphor-icons/react';
import { useSessao } from '../lib/sessao';
import { BotaoAvaliar } from './Avaliacao';

const ITENS = [
  { para: '/', rotulo: 'Mapa', Icone: MapTrifold, fim: true },
  { para: '/app', rotulo: 'Início', Icone: House, fim: true },
  { para: '/app/reportar', rotulo: 'Reportar', Icone: Camera },
  { para: '/app/bairros', rotulo: 'Distritos', Icone: MapPinArea },
  { para: '/app/perfil', rotulo: 'Perfil', Icone: User },
];

/**
 * Estrutura comum das telas: barra lateral no computador e navegação inferior no celular.
 * Sem login, mostra só o topo com o botão de entrar.
 */
export function Casca({ children, cheia = false }: { children: ReactNode; cheia?: boolean }) {
  const { sessao, perfil } = useSessao();
  const logado = Boolean(sessao && perfil);

  return (
    <div className={`casca ${logado ? 'casca-logada' : 'casca-visitante'}`}>
      {logado ? (
        <aside className="lateral">
          <Link to="/" className="lateral-logo"><img src="/logo-curto.png" alt="Várzea, mapa do campeonato" /></Link>
          <nav aria-label="Navegação principal" className="lateral-nav">
            {ITENS.map(({ para, rotulo, Icone, fim }) => (
              <NavLink key={para} to={para} end={fim}>
                <Icone size={22} weight="fill" aria-hidden />
                <span>{rotulo}</span>
              </NavLink>
            ))}
          </nav>
          <p className="lateral-time">
            Torcendo por <strong>{perfil?.distritos?.nome}</strong>
          </p>
        </aside>
      ) : (
        <header className="topo-visitante">
          <Link to="/"><img src="/logo-curto.png" alt="Várzea, mapa do campeonato" /></Link>
          <div>
            <Link to="/entrar" className="botao botao-secundario botao-pequeno">Entrar</Link>
            <BotaoAvaliar classe="botao botao-primario botao-pequeno" texto="Testar agora" />
          </div>
        </header>
      )}

      <main className={cheia ? 'casca-cheia' : 'app-conteudo'}>{children}</main>

      {logado && (
        <nav className="nav-inferior" aria-label="Navegação principal">
          {ITENS.map(({ para, rotulo, Icone, fim }) => (
            <NavLink key={para} to={para} end={fim}>
              <Icone size={24} weight="fill" aria-hidden />
              <span>{rotulo}</span>
            </NavLink>
          ))}
        </nav>
      )}
    </div>
  );
}
