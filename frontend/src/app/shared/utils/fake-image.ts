/**
 * Gera uma "imagem" fake como data URI SVG, sem depender de internet ou de um
 * serviço externo de imagens. Quando a categoria do produto é reconhecida
 * (tênis, bota, cinto, boné...), desenha um ícone representativo daquele tipo
 * de produto; caso contrário cai para um avatar de iniciais. Usado como
 * placeholder visual para produtos que ainda não têm uma foto real.
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

/** Ícones simples (traço branco em fundo colorido) representando cada categoria de produto. */
const ICONES_POR_CATEGORIA: Record<string, string> = {
  'tênis': `<path d="M30 130 q0 -25 25 -30 l35 -8 q10 -18 30 -18 q15 0 20 12 l10 22 q25 2 35 18 q6 10 -2 16 q-8 6 -20 6 l-118 0 q-15 0 -15 -18 z" fill="#fff" opacity="0.92"/>
    <path d="M55 118 l20 -30 M85 110 l12 -26 M115 108 l8 -22" stroke="#0000004d" stroke-width="4" fill="none" stroke-linecap="round"/>`,
  'sandália': `<ellipse cx="100" cy="120" rx="60" ry="18" fill="#fff" opacity="0.92"/>
    <path d="M60 120 Q100 60 140 120" stroke="#fff" stroke-width="10" fill="none" stroke-linecap="round"/>
    <path d="M100 60 L100 120" stroke="#fff" stroke-width="8" stroke-linecap="round"/>`,
  'bota': `<path d="M75 40 h40 v55 l35 20 q8 5 8 14 q0 9 -10 9 h-95 q-8 0 -8 -9 v-70 q0 -19 30 -19 z" fill="#fff" opacity="0.92"/>
    <rect x="75" y="55" width="40" height="8" fill="#0000004d"/>
    <rect x="75" y="72" width="40" height="8" fill="#0000004d"/>`,
  'chinelo': `<ellipse cx="100" cy="125" rx="55" ry="16" fill="#fff" opacity="0.92"/>
    <path d="M65 125 Q100 65 135 125" stroke="#fff" stroke-width="16" fill="none" stroke-linecap="round"/>`,
  'sapato social': `<path d="M35 128 q-5 -20 25 -26 l50 -14 q18 -6 30 2 l25 16 q10 6 10 14 q0 8 -10 8 h-120 q-10 0 -10 -0 z" fill="#fff" opacity="0.92"/>
    <path d="M75 100 l20 -10 M100 96 l16 -8" stroke="#0000004d" stroke-width="4" stroke-linecap="round"/>`,
  'sapatilha': `<path d="M35 122 q-4 -22 30 -24 l55 -4 q22 -1 28 10 q5 9 -4 14 q-8 5 -20 5 h-79 q-10 0 -10 -1 z" fill="#fff" opacity="0.92"/>`,
  'cinto': `<rect x="25" y="90" width="150" height="20" rx="4" fill="#fff" opacity="0.92"/>
    <rect x="85" y="80" width="30" height="40" rx="4" fill="none" stroke="#fff" stroke-width="8"/>`,
  'carteira': `<rect x="45" y="60" width="110" height="80" rx="10" fill="#fff" opacity="0.92"/>
    <line x1="45" y1="100" x2="155" y2="100" stroke="#0000004d" stroke-width="4"/>
    <circle cx="130" cy="100" r="5" fill="#0000004d"/>`,
  'bolsa': `<path d="M55 90 h90 l10 60 q2 12 -12 12 h-86 q-14 0 -12 -12 z" fill="#fff" opacity="0.92"/>
    <path d="M75 90 v-15 q0 -25 25 -25 q25 0 25 25 v15" fill="none" stroke="#fff" stroke-width="9"/>`,
  'meia': `<path d="M80 40 h40 v60 q0 10 10 16 l25 15 q10 6 4 18 q-5 10 -18 10 h-50 q-14 0 -14 -14 v-105 z" fill="#fff" opacity="0.92"/>`,
  'meião': `<path d="M85 30 h30 v90 q0 8 8 13 l20 12 q9 5 4 15 q-4 9 -16 9 h-45 q-12 0 -12 -12 v-127 z" fill="#fff" opacity="0.92"/>`,
  'caneleira': `<rect x="70" y="35" width="60" height="115" rx="28" fill="#fff" opacity="0.92"/>
    <line x1="70" y1="70" x2="130" y2="70" stroke="#0000004d" stroke-width="4"/>
    <line x1="70" y1="110" x2="130" y2="110" stroke="#0000004d" stroke-width="4"/>`,
  'bola de futebol': `<circle cx="100" cy="100" r="55" fill="#fff" opacity="0.92"/>
    <polygon points="100,75 115,88 109,106 91,106 85,88" fill="#0000004d"/>
    <line x1="100" y1="45" x2="100" y2="75" stroke="#0000004d" stroke-width="3"/>
    <line x1="55" y1="100" x2="85" y2="88" stroke="#0000004d" stroke-width="3"/>
    <line x1="145" y1="100" x2="115" y2="88" stroke="#0000004d" stroke-width="3"/>`,
  'boné': `<path d="M40 110 q60 -55 120 0 z" fill="#fff" opacity="0.92"/>
    <path d="M40 110 q60 25 130 0 q10 -2 10 8 q0 8 -12 10 l-120 6 q-14 1 -16 -10 q-2 -10 8 -14 z" fill="#fff" opacity="0.92"/>`,
  'mala de viagem': `<rect x="45" y="65" width="110" height="80" rx="12" fill="#fff" opacity="0.92"/>
    <rect x="80" y="45" width="40" height="22" rx="6" fill="none" stroke="#fff" stroke-width="8"/>
    <line x1="100" y1="65" x2="100" y2="145" stroke="#0000004d" stroke-width="4"/>`
};

function normalizar(texto: string): string {
  return texto.trim().toLowerCase();
}

/** Cor determinística (mesma paleta das imagens fake) para diferenciar visualmente marcas, tags etc. */
export function corPorTexto(texto: string): string {
  return PALETA[hashString(texto) % PALETA.length];
}

export function imagemFake(nome: string, categoria?: string | null): string {
  const cor = PALETA[hashString(nome) % PALETA.length];
  const icone = categoria ? ICONES_POR_CATEGORIA[normalizar(categoria)] : undefined;

  const conteudo = icone
    ? icone
    : `<text x="100" y="100" font-family="Arial, sans-serif" font-size="72" font-weight="700"
         fill="#ffffff" text-anchor="middle" dominant-baseline="central">${iniciais(nome)}</text>`;

  const svg = `
    <svg xmlns="http://www.w3.org/2000/svg" width="200" height="200" viewBox="0 0 200 200">
      <rect width="200" height="200" rx="24" fill="${cor}"/>
      ${conteudo}
    </svg>`.trim();
  return `data:image/svg+xml;utf8,${encodeURIComponent(svg)}`;
}
