export type StatusSessao = 'ABERTA' | 'FECHADA';
export type TipoMovCaixa = 'SUPRIMENTO' | 'SANGRIA' | 'DESPESA' | 'RECEITA_AVULSA';

export interface SessaoCaixa {
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
  status: StatusSessao;
  observacoes?: string | null;
}

export interface MovimentacaoCaixa {
  id: number;
  tipo: TipoMovCaixa;
  valor: number;
  descricao: string;
  categoria?: string | null;
  usuarioNome: string;
  createdAt: string;
}
