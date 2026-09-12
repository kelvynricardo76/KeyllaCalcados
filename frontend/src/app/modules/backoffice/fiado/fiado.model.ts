export type StatusFiado = 'ABERTO' | 'PARCIAL' | 'PAGO' | 'VENCIDO';

export interface Fiado {
  id: number;
  clienteId: number;
  clienteNome: string;
  lojaId: number;
  lojaNome: string;
  valorTotal: number;
  valorPago: number;
  valorRestante: number;
  dataLancamento: string;
  dataVencimento: string;
  status: StatusFiado;
  vencido: boolean;
  observacoes?: string | null;
}

export interface FiadoRequest {
  clienteId: number;
  lojaId: number;
  valorTotal: number;
  dataVencimento: string;
  observacoes?: string | null;
}

export interface PagamentoRequest {
  valor: number;
  formaPagamento: string;
  observacoes?: string | null;
}

export interface Pagamento {
  id: number;
  valor: number;
  formaPagamento: string;
  dataPagamento: string;
  usuarioNome?: string | null;
  observacoes?: string | null;
  createdAt: string;
}
