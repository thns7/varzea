import { Navigate, Outlet } from 'react-router-dom';
import { useSessao } from '../lib/sessao';
import { Casca } from './Casca';

/** Telas que exigem login e time escolhido. */
export function LayoutApp() {
  const { sessao, perfil, carregando } = useSessao();
  if (carregando) return <p className="carregando tela-cheia">Carregando</p>;
  if (!sessao) return <Navigate to="/entrar" replace />;
  if (!perfil) return <Navigate to="/escolher-time" replace />;

  return (
    <Casca>
      <Outlet />
    </Casca>
  );
}
