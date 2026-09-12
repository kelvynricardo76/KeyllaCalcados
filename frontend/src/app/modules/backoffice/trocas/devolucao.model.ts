export type TipoDevolucao = 'TROCA' | 'DEVOLUCAO';

export interface ItemDevolucaoRequest {
  variacaoId: number;
  quantidade: number;
}

export interface DevolucaoRequest {
  tipo: TipoDevolucao;
  motivo: string;
  itens: ItemDevolucaoRequest[];
}

export interface ItemDevolvidoResumo {
  nomeProduto: string;
  tamanho?: string | null;
  cor?: string | null;
  quantidade: number;
}

export interface Devolucao {
  id: number;
  vendaOrigemId: number;
  clienteNome: string;
  tipo: TipoDevolucao;
  motivo: string;
  valorDevolvido: number;
  usuarioNome?: string | null;
  createdAt: string;
  itens: ItemDevolvidoResumo[];
}
