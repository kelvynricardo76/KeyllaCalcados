export interface CaixaOption {
  id: number;
  nome: string;
  ativo: boolean;
  lojaId: number;
  sessaoAbertaId?: number | null;
}

export interface Sessao {
  id: number;
  caixaId: number;
  caixaNome: string;
  usuarioNome: string;
  dataAbertura: string;
  valorAbertura: number;
  dataFechamento?: string | null;
  valorFechamentoInformado?: number | null;
  valorFechamentoCalculado?: number | null;
  diferenca?: number | null;
  status: 'ABERTA' | 'FECHADA';
  observacoes?: string | null;
}

export type TipoMovCaixa = 'SUPRIMENTO' | 'SANGRIA' | 'DESPESA' | 'RECEITA_AVULSA';

export type FormaPagamento = 'DINHEIRO' | 'DEBITO' | 'CREDITO' | 'PIX' | 'FIADO' | 'TROCA' | 'OUTRO';

export interface ItemVenda {
  id: number;
  variacaoId: number;
  nomeProduto: string;
  sku?: string | null;
  tamanho?: string | null;
  cor?: string | null;
  quantidade: number;
  precoUnitario: number;
  descontoItem: number;
  subtotal: number;
}

export interface PagamentoVenda {
  id: number;
  forma: FormaPagamento;
  valor: number;
  parcelas: number;
  referencia?: string | null;
}

export interface Venda {
  id: number;
  sessaoId: number;
  clienteId?: number | null;
  clienteNome?: string | null;
  usuarioNome: string;
  lojaId: number;
  status: 'ABERTA' | 'FECHADA' | 'CANCELADA';
  subtotal: number;
  descontoGeral: number;
  valorTotal: number;
  troco?: number | null;
  observacoes?: string | null;
  itens: ItemVenda[];
  pagamentos: PagamentoVenda[];
  createdAt: string;
}

export interface PagamentoInput {
  forma: FormaPagamento;
  valor: number;
  parcelas?: number;
  referencia?: string | null;
}
