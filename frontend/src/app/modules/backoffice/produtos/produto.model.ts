export interface Marca {
  id: number;
  nome: string;
  descricao?: string | null;
  logoUrl?: string | null;
  ativo: boolean;
}

export interface MarcaRequest {
  nome: string;
  descricao?: string | null;
  logoUrl?: string | null;
}

export interface Categoria {
  id: number;
  nome: string;
  categoriaPaiId?: number | null;
  categoriaPaiNome?: string | null;
  descricao?: string | null;
  ordem?: number | null;
  ativo: boolean;
}

export interface Tamanho {
  id: number;
  valor: string;
  tipo: 'NUMERO' | 'LETRA' | 'UNICO';
  ordem?: number | null;
}

export interface Cor {
  id: number;
  nome: string;
  hexCode?: string | null;
}

export interface Produto {
  id: number;
  nome: string;
  descricao?: string | null;
  marcaId?: number | null;
  marcaNome?: string | null;
  categoriaId?: number | null;
  categoriaNome?: string | null;
  codigoBarras?: string | null;
  sku?: string | null;
  precoCusto: number;
  precoVenda: number;
  margemPercentual: number;
  temGrade: boolean;
  fotoPrincipalUrl?: string | null;
  dataValidade?: string | null;
  ativo: boolean;
}

export interface ProdutoRequest {
  nome: string;
  descricao?: string | null;
  marcaId?: number | null;
  categoriaId?: number | null;
  codigoBarras?: string | null;
  sku?: string | null;
  precoCusto: number;
  precoVenda: number;
  temGrade: boolean;
  fotoPrincipalUrl?: string | null;
  dataValidade?: string | null;
}

export interface Variacao {
  id: number;
  produtoId: number;
  produtoNome?: string | null;
  tamanhoId?: number | null;
  tamanhoValor?: string | null;
  corId?: number | null;
  corNome?: string | null;
  corHex?: string | null;
  sku: string;
  codigoBarras?: string | null;
  precoCustoOverride?: number | null;
  precoVendaOverride?: number | null;
  ativo: boolean;
}

export interface VariacaoRequest {
  tamanhoId?: number | null;
  corId?: number | null;
  sku: string;
  codigoBarras?: string | null;
  precoCustoOverride?: number | null;
  precoVendaOverride?: number | null;
}
