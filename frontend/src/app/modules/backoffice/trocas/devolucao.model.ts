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

export interface Devolucao {
  id: number;
  vendaOrigemId: number;
  tipo: TipoDevolucao;
  motivo: string;
  valorDevolvido: number;
  usuarioNome?: string | null;
  createdAt: string;
}
