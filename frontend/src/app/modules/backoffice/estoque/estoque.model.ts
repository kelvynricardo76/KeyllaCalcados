export type TipoMovEstoque =
  | 'ENTRADA' | 'SAIDA' | 'AJUSTE_POSITIVO' | 'AJUSTE_NEGATIVO'
  | 'TRANSFERENCIA_SAIDA' | 'TRANSFERENCIA_ENTRADA' | 'DEVOLUCAO';

export interface Loja {
  id: number;
  nome: string;
  ativo: boolean;
}

export interface EstoqueItem {
  id: number;
  variacaoId: number;
  produtoNome: string;
  tamanhoValor?: string | null;
  corNome?: string | null;
  sku?: string | null;
  lojaId: number;
  lojaNome: string;
  quantidade: number;
  estoqueMinimo: number;
  statusEstoque: 'ZERADO' | 'BAIXO' | 'OK';
  updatedAt: string;
}

export interface AjusteEstoqueRequest {
  variacaoId: number;
  lojaId: number;
  tipo: TipoMovEstoque;
  quantidade: number;
  motivo?: string | null;
}

export interface Movimentacao {
  id: number;
  tipo: TipoMovEstoque;
  quantidade: number;
  quantidadeAnterior: number;
  quantidadePosterior: number;
  motivo?: string | null;
  usuarioNome?: string | null;
  createdAt: string;
}
