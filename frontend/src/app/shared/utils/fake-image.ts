/**
 * Gera uma "imagem" fake (avatar de iniciais) como data URI SVG, sem depender
 * de internet ou de um serviço externo de imagens. Usado como placeholder
 * visual para produtos que ainda não têm uma foto real cadastrada.
 */
const PALETA = [
  '#0047CC', '#7C3AED', '#DC2626', '#16A34A', '#EA580C',
  '#0891B2', '#DB2777', '#4338CA', '#059669', '#B45309'
];

function hashString(texto: string): number {
  let hash = 0;
  for (let i = 0; i < texto.length; i++) {
    hash = (hash << 5) - hash + texto.charCodeAt(i);
    hash |= 0;
  }
  return Math.abs(hash);
}

function iniciais(nome: string): string {
  const palavras = nome.trim().split(/\s+/).filter(Boolean);
  if (palavras.length === 0) return '?';
  if (palavras.length === 1) return palavras[0].substring(0, 2).toUpperCase();
  return (palavras[0][0] + palavras[1][0]).toUpperCase();
}

export function imagemFake(nome: string): string {
  const cor = PALETA[hashString(nome) % PALETA.length];
  const texto = iniciais(nome);
  const svg = `
    <svg xmlns="http://www.w3.org/2000/svg" width="200" height="200" viewBox="0 0 200 200">
      <rect width="200" height="200" fill="${cor}"/>
      <text x="100" y="100" font-family="Arial, sans-serif" font-size="72" font-weight="700"
            fill="#ffffff" text-anchor="middle" dominant-baseline="central">${texto}</text>
    </svg>`.trim();
  return `data:image/svg+xml;utf8,${encodeURIComponent(svg)}`;
}
