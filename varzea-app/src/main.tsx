import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { SessaoProvider } from './lib/sessao';
import { LayoutApp } from './components/LayoutApp';
import { Mapa } from './pages/Mapa';
import { Entrar } from './pages/Entrar';
import { EscolherTime } from './pages/EscolherTime';
import { Inicio } from './pages/Inicio';
import { Tabela } from './pages/Tabela';
import { Reportar } from './pages/Reportar';
import { Distritos } from './pages/Distritos';
import { DetalheChamado } from './pages/DetalheChamado';
import { MeuPerfil } from './pages/MeuPerfil';
import './styles.css';
import './movimento.css';
import './mapa.css';
import './arquibancada.css';

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <SessaoProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Mapa />} />
          <Route path="/entrar" element={<Entrar />} />
          <Route path="/escolher-time" element={<EscolherTime />} />
          <Route path="/app" element={<LayoutApp />}>
            <Route index element={<Inicio />} />
            <Route path="tabela" element={<Tabela />} />
            <Route path="reportar" element={<Reportar />} />
            <Route path="bairros" element={<Distritos />} />
            <Route path="chamado/:id" element={<DetalheChamado />} />
            <Route path="perfil" element={<MeuPerfil />} />
          </Route>
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </SessaoProvider>
  </StrictMode>,
);
