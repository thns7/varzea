import { createClient } from '@supabase/supabase-js';

const url = import.meta.env.VITE_SUPABASE_URL as string;
const chave = import.meta.env.VITE_SUPABASE_PUBLISHABLE_KEY as string;

if (!url || !chave) {
  throw new Error('Configure VITE_SUPABASE_URL e VITE_SUPABASE_PUBLISHABLE_KEY no arquivo .env');
}

export const supabase = createClient(url, chave);

export interface Distrito { id: number; nome: string; zona: string }
export interface Servico { id: number; nome: string; gols: number; chance_30d: number; categoria_id: number }
export interface Perfil { id: string; nome: string; distrito_id: number; distritos?: { nome: string; zona: string } }
export interface Partida {
  partida_id: number; rodada_numero: number; inicio: string; fim: string;
  casa_id: number; casa: string; gols_casa: number;
  fora_id: number; fora: string; gols_fora: number;
}
export interface LinhaTabela {
  distrito_id: number; nome: string; zona: string; jogos: number; vitorias: number;
  empates: number; derrotas: number; gols_pro: number; pontos: number;
}
export interface GolRecente { distrito: string; servico: string; valor: number; criado_em: string; demo: boolean }
export interface Chamado {
  id: number; referencia: string; status: 'aberto' | 'resolvido' | 'validado' | 'cancelado';
  foto_path: string | null; demo: boolean; criado_em: string; resolvido_em: string | null;
  autor_id: string | null; resolvido_por: string | null; distrito_id: number; servico_id: number;
  servicos: { nome: string; gols: number };
  distritos: { nome: string };
}

export const SELECT_CHAMADO =
  'id, referencia, status, foto_path, demo, criado_em, resolvido_em, autor_id, resolvido_por, distrito_id, servico_id, servicos(nome, gols), distritos(nome)';

export function urlFoto(caminho: string) {
  return supabase.storage.from('fotos').getPublicUrl(caminho).data.publicUrl;
}

export const ROTULO_STATUS: Record<Chamado['status'], string> = {
  aberto: 'Aberto',
  resolvido: 'Aguardando o VAR',
  validado: 'Gol validado',
  cancelado: 'Cancelado',
};

export function dataCurta(valor: string) {
  const data = valor.length === 10 ? new Date(`${valor}T12:00:00`) : new Date(valor);
  return data.toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit' });
}

export function plural(n: number, um: string, varios: string) {
  return `${n} ${n === 1 ? um : varios}`;
}

export function mensagemErro(erro: unknown) {
  const texto = erro instanceof Error ? erro.message : (erro as { message?: string })?.message;
  if (!texto) return 'Algo deu errado. Tente de novo.';
  if (texto.includes('Failed to fetch')) return 'Sem conexão. Confira a internet e tente de novo.';
  if (texto.includes('Invalid login credentials')) return 'E-mail ou senha incorretos.';
  if (texto.includes('Email not confirmed')) return 'Confirme seu e-mail antes de entrar.';
  if (texto.includes('User already registered')) return 'Este e-mail já tem conta. Entre com sua senha.';
  if (texto.includes('Password should be')) return 'A senha precisa ter pelo menos 6 caracteres.';
  if (texto.includes('rate limit')) return 'Muitas tentativas agora. Espere um pouco e tente de novo.';
  return texto;
}
