import { supabase } from './supabase';

/**
 * Redesenha a foto em um canvas antes de enviar. Isso reduz o tamanho e remove os
 * metadados do arquivo, como a localização exata gravada pelo celular.
 */
async function prepararFoto(arquivo: File): Promise<Blob> {
  if (!arquivo.type.startsWith('image/')) throw new Error('Escolha um arquivo de imagem.');
  const imagem = await createImageBitmap(arquivo);
  const escala = Math.min(1, 1280 / Math.max(imagem.width, imagem.height));
  const canvas = document.createElement('canvas');
  canvas.width = Math.round(imagem.width * escala);
  canvas.height = Math.round(imagem.height * escala);
  canvas.getContext('2d')!.drawImage(imagem, 0, 0, canvas.width, canvas.height);
  imagem.close();
  return new Promise((resolver, rejeitar) => {
    canvas.toBlob(
      (blob) => (blob ? resolver(blob) : rejeitar(new Error('Não foi possível ler esta foto.'))),
      'image/jpeg',
      0.8,
    );
  });
}

export async function enviarFoto(arquivo: File, usuarioId: string) {
  const blob = await prepararFoto(arquivo);
  const caminho = `${usuarioId}/${crypto.randomUUID()}.jpg`;
  const { error } = await supabase.storage.from('fotos').upload(caminho, blob, { contentType: 'image/jpeg' });
  if (error) throw error;
  return caminho;
}
