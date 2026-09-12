export interface VendaResumo {
  id: number;
  createdAt: string;
  clienteNome: string;
  usuarioNome: string;
  valorTotal: number;
}

export interface ProdutoRanking {
  produtoId: number;
  nomeProduto: string;
  quantidadeVendida: number;
  valorTotal: number;
}

export interface RelatorioVendas {
  inicio: string;
  fim: string;
  faturamentoTotal: number;
  quantidadeVendas: number;
  ticketMedio: number;
  vendas: VendaResumo[];
  maisVendidos: ProdutoRanking[];
  menosVendidos: ProdutoRanking[];
  produtosSemVenda: string[];
}

export interface FuncionarioRanking {
  usuarioId: number;
  nome: string;
  perfil: string;
  quantidadeVendas: number;
  valorTotal: number;
  ticketMedio: number;
}

export interface ProdutoVencendo {
  produtoId: number;
  nomeProduto: string;
  marcaNome?: string | null;
  dataValidade: string;
  diasParaVencer: number;
  vencido: boolean;
}

export interface ClienteRanking {
  clienteId: number;
  nome: string;
  telefone?: string | null;
  quantidadeCompras: number;
  valorTotal: number;
  ticketMedio: number;
}

export interface ProdutoParado {
  produtoId: number;
  nomeProduto: string;
  marcaNome?: string | null;
  ultimaVenda?: string | null;
  diasSemVenda?: number | null;
  quantidadeEmEstoque: number;
}
