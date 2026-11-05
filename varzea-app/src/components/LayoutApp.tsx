import { NavLink, Navigate, Outlet } from 'react-router-dom';
import { Camera, House, MapPinArea, Trophy, User } from '@phosphor-icons/react';
import { useSessao } from '../lib/sessao';

const ITENS = [
  { para: '/app', rotulo: 'Início', Icone: House, fim: true },
  { para: '/app/tabela', rotulo: 'Tabela', Icone: Trophy },
  { para: '/app/reportar', rotulo: 'Reportar', Icone: Camera },
  { para: '/app/bairros', rotulo: 'Distritos', Icone: MapPinArea },
  { para: '/app/perfil', rotulo: 'Perfil', Icone: User },
];

export function LayoutApp() {
  const { sessao, perfil, carregando } = useSessao();
  if (carregando) return <p className="carregando tela-cheia">Carregando</p>;
  if (!sessao) return <Navigate to="/entrar" replace />;
  if (!perfil) return <Navigate to="/escolher-time" replace />;

  return (
    <div className="app">
      <main className="app-conteudo">
        <Outlet />
      </main>
      <nav className="nav-inferior" aria-label="Navegação principal">
        {ITENS.map(({ para, rotulo, Icone, fim }) => (
          <NavLink key={para} to={para} end={fim}>
            <Icone size={24} weight="fill" aria-hidden />
            <span>{rotulo}</span>
          </NavLink>
        ))}
      </nav>
    </div>
  );
}
