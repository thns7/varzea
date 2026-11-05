import { useEffect, useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { enviarFoto } from '../lib/foto';
import { useSessao } from '../lib/sessao';
import { mensagemErro, plural, supabase, type Servico } from '../lib/supabase';

export function Reportar() {
  const { perfil } = useSessao();
  const navegar = useNavigate();
  const [servicos, setServicos] = useState<Servico[]>([]);
  const [servicoId, setServicoId] = useState('');
  const [referencia, setReferencia] = useState('');
  const [foto, setFoto] = useState<File | null>(null);
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState('');

  useEffect(() => {
    supabase.from('servicos').select('id, nome, gols, chance_30d, categoria_id').order('nome').then(({ data, error }) => {
      if (error) setErro(mensagemErro(error));
      else setServicos(data ?? []);
    });
  }, []);

  const servico = servicos.find((s) => String(s.id) === servicoId);

  async function enviar(evento: FormEvent) {
    evento.preventDefault();
    if (!perfil) return;
    setErro('');
    setEnviando(true);
    try {
      const foto_path = foto ? await enviarFoto(foto, perfil.id) : null;
      const { data, error } = await supabase
        .from('chamados')
        .insert({ servico_id: Number(servicoId), distrito_id: perfil.distrito_id, autor_id: perfil.id, referencia: referencia.trim(), foto_path })
        .select('id')
        .single();
      if (error) throw error;
      navegar(`/app/chamado/${data.id}`, { replace: true });
    } catch (e) {
      setErro(mensagemErro(e));
      setEnviando(false);
    }
  }

  return (
    <>
      <h1 className="titulo-pagina">Reportar problema</h1>
      <p className="apoio">No time de {perfil?.distritos?.nome}. A Várzea organiza a cobrança, mas não substitui o pedido oficial no SP156.</p>
      <form onSubmit={enviar} className="formulario">
        <label>
          O que está acontecendo?
          <select required value={servicoId} onChange={(e) => setServicoId(e.target.value)}>
            <option value="" disabled>Escolha o tipo de problema</option>
            {servicos.map((s) => <option key={s.id} value={s.id}>{s.nome}</option>)}
          </select>
        </label>
        {servico && (
          <p className="alerta alerta-info">
            Vale <strong>{plural(servico.gols, 'gol', 'gols')}</strong> se for resolvido e confirmado. No SP156, {Math.round(servico.chance_30d * 100)}% destes pedidos fecham em até 30 dias.
          </p>
        )}
        <label>
          Onde fica?
          <input
            required
            minLength={5}
            maxLength={120}
            value={referencia}
            onChange={(e) => setReferencia(e.target.value)}
            placeholder="Ex.: Rua das Flores, perto da feira"
          />
          <small>Use uma referência pública. Não informe número de casa nem nome de morador.</small>
        </label>
        <label>
          Foto (opcional)
          <input type="file" accept="image/*" capture="environment" onChange={(e) => setFoto(e.target.files?.[0] ?? null)} />
          <small>Evite rostos e placas. Removemos a localização gravada na foto antes de enviar.</small>
        </label>
        {erro && <p className="alerta alerta-erro" role="alert">{erro}</p>}
        <button className="botao botao-primario" disabled={enviando}>{enviando ? 'Enviando' : 'Abrir chamado'}</button>
      </form>
    </>
  );
}
