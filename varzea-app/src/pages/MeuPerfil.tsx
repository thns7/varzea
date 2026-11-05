import { useState, type FormEvent } from 'react';
import { Escudo } from '../components/Escudo';
import { useSessao } from '../lib/sessao';
import { mensagemErro, supabase } from '../lib/supabase';

export function MeuPerfil() {
  const { perfil, sessao, atualizarPerfil } = useSessao();
  const [nome, setNome] = useState(perfil?.nome ?? '');
  const [salvando, setSalvando] = useState(false);
  const [aviso, setAviso] = useState('');
  const [erro, setErro] = useState('');

  async function salvar(evento: FormEvent) {
    evento.preventDefault();
    setSalvando(true);
    setAviso('');
    setErro('');
    const { error } = await supabase.from('perfis').update({ nome: nome.trim() }).eq('id', perfil!.id);
    if (error) setErro(mensagemErro(error));
    else {
      await atualizarPerfil();
      setAviso('Nome salvo.');
    }
    setSalvando(false);
  }

  return (
    <>
      <h1 className="titulo-pagina">Perfil</h1>
      <section className="cartao meu-time">
        <Escudo nome={perfil?.distritos?.nome ?? ''} tamanho={44} />
        <div>
          <p className="rotulo">Seu time</p>
          <h2>{perfil?.distritos?.nome}</h2>
          <p className="apoio">{sessao?.user.email}</p>
        </div>
      </section>
      <form onSubmit={salvar} className="formulario">
        <label>
          Como a torcida te chama
          <input required minLength={2} maxLength={60} value={nome} onChange={(e) => setNome(e.target.value)} />
        </label>
        {aviso && <p className="alerta alerta-ok" role="status">{aviso}</p>}
        {erro && <p className="alerta alerta-erro" role="alert">{erro}</p>}
        <button className="botao botao-secundario" disabled={salvando}>{salvando ? 'Salvando' : 'Salvar nome'}</button>
      </form>
      <button className="botao-texto" onClick={() => supabase.auth.signOut()}>Sair da conta</button>
    </>
  );
}
