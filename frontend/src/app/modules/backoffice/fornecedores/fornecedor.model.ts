export interface Fornecedor {
  id: number;
  razaoSocial: string;
  cnpj?: string | null;
  telefone?: string | null;
  email?: string | null;
  endereco?: string | null;
  contato?: string | null;
  observacoes?: string | null;
  ativo: boolean;
}

export interface FornecedorRequest {
  razaoSocial: string;
  cnpj?: string | null;
  telefone?: string | null;
  email?: string | null;
  endereco?: string | null;
  contato?: string | null;
  observacoes?: string | null;
}

export type StatusCompra = 'PENDENTE' | 'RECEBIDA' | 'PARCIAL' | 'CANCELADA';

export interface ItemCompra {
  id: number;
  variacaoId: number;
  produtoNome: string;
  tamanhoValor?: string | null;
  corNome?: string | null;
  sku?: string | null;
  quantidade: number;
  quantidadeRecebida: number;
  precoCustoUnitario: number;
  subtotal: number;
}

export interface Compra {
  id: number;
  fornecedorId?: number | null;
  fornecedorNome?: string | null;
  lojaId: number;
  dataCompra: string;
  dataEntrega?: string | null;
  numeroNf?: string | null;
  status: StatusCompra;
  valorTotal: number;
  observacoes?: string | null;
  itens: ItemCompra[];
}

export interface ItemCompraRequest {
  variacaoId: number;
  quantidade: number;
  precoCustoUnitario: number;
}

export interface CompraRequest {
  fornecedorId?: number | null;
  lojaId: number;
  dataEntrega?: string | null;
  numeroNf?: string | null;
  observacoes?: string | null;
  itens: ItemCompraRequest[];
}
