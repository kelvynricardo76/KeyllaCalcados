export interface VendaResumo {
  id: number;
  createdAt: string;
  clienteNome: string;
  usuarioNome: string;
  valorTotal: number;
}

export interface ProdutoRanking {
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
  topProdutos: ProdutoRanking[];
}
