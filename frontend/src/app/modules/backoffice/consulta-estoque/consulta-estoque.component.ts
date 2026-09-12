import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { EstoqueService } from '../estoque/estoque.service';
import { EstoqueItem } from '../estoque/estoque.model';
import { imagemFake } from '../../../shared/utils/fake-image';

interface MarcaResumo {
  id: number | null;
  nome: string;
}

interface ProdutoResumo {
  produtoId: number;
  produtoNome: string;
  marcaId?: number | null;
  marcaNome?: string | null;
  categoriaId?: number | null;
  categoriaNome?: string | null;
  fotoPrincipalUrl?: string | null;
  quantidadeTotal: number;
}

interface Sugestao {
  produtoId: number;
  produtoNome: string;
  fotoPrincipalUrl?: string | null;
  categoriaNome?: string | null;
  tamanhoValor?: string | null;
  corNome?: string | null;
  corHex?: string | null;
  quantidade: number;
  corIgual: boolean;
}

/**
 * Tela de consulta somente-leitura do estoque, pensada para o perfil VENDEDOR:
 * busca um produto (ex.: "Kenner masculino 39") e, se não houver saldo naquela
 * numeração/cor, sugere automaticamente um produto similar (mesma marca,
 * categoria e numeração) com saldo disponível.
 */
@Component({
  selector: 'app-consulta-estoque',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './consulta-estoque.component.html',
  styleUrl: './consulta-estoque.component.scss'
})
export class ConsultaEstoqueComponent implements OnInit {
  private estoqueService = inject(EstoqueService);

  itens = signal<EstoqueItem[]>([]);
  loading = signal(true);
  error = signal('');

  marcaSelecionada = signal<number | null>(null);
  busca = signal('');

  produtoSelecionado = signal<ProdutoResumo | null>(null);
  tamanhoDrill = signal<string | null>(null);
  corDrill = signal<number | null>(null);

  ngOnInit() {
    this.estoqueService.listar().subscribe({
      next: itens => { this.itens.set(itens); this.loading.set(false); },
      error: () => { this.error.set('Não foi possível carregar o estoque.'); this.loading.set(false); }
    });
  }

  marcas = computed<MarcaResumo[]>(() => {
    const mapa = new Map<string, MarcaResumo>();
    for (const item of this.itens()) {
      const chave = String(item.marcaId ?? 'sem-marca');
      if (!mapa.has(chave)) mapa.set(chave, { id: item.marcaId ?? null, nome: item.marcaNome ?? 'Sem marca' });
    }
    return Array.from(mapa.values()).sort((a, b) => a.nome.localeCompare(b.nome));
  });

  itensFiltrados(): EstoqueItem[] {
    const marca = this.marcaSelecionada();
    const termo = this.busca().trim().toLowerCase();
    return this.itens().filter(i => {
      if (marca !== null && i.marcaId !== marca) return false;
      if (termo && !i.produtoNome.toLowerCase().includes(termo)) return false;
      return true;
    });
  }

  produtosAgrupados(): ProdutoResumo[] {
    const mapa = new Map<number, ProdutoResumo>();
    for (const item of this.itensFiltrados()) {
      let resumo = mapa.get(item.produtoId);
      if (!resumo) {
        resumo = {
          produtoId: item.produtoId, produtoNome: item.produtoNome, marcaId: item.marcaId,
          marcaNome: item.marcaNome, categoriaId: item.categoriaId, categoriaNome: item.categoriaNome,
          fotoPrincipalUrl: item.fotoPrincipalUrl, quantidadeTotal: 0
        };
        mapa.set(item.produtoId, resumo);
      }
      resumo.quantidadeTotal += item.quantidade;
    }
    return Array.from(mapa.values()).sort((a, b) => a.produtoNome.localeCompare(b.produtoNome));
  }

  imagemDoProduto(nome: string, foto?: string | null, categoria?: string | null): string {
    return foto || imagemFake(nome, categoria);
  }

  abrirProduto(produto: ProdutoResumo) {
    this.produtoSelecionado.set(produto);
    this.tamanhoDrill.set(null);
    this.corDrill.set(null);
  }

  fecharProduto() {
    this.produtoSelecionado.set(null);
  }

  variacoesDoProduto(): EstoqueItem[] {
    const produto = this.produtoSelecionado();
    if (!produto) return [];
    return this.itens()
      .filter(i => i.produtoId === produto.produtoId)
      .sort((a, b) => (a.tamanhoValor ?? '').localeCompare(b.tamanhoValor ?? '', undefined, { numeric: true }));
  }

  tamanhosDoProduto(): string[] {
    const set = new Set<string>();
    this.variacoesDoProduto().forEach(i => { if (i.tamanhoValor) set.add(i.tamanhoValor); });
    return Array.from(set).sort((a, b) => a.localeCompare(b, undefined, { numeric: true }));
  }

  coresDoTamanho(): { id: number; nome: string; hex?: string | null }[] {
    const tamanho = this.tamanhoDrill();
    const mapa = new Map<number, { id: number; nome: string; hex?: string | null }>();
    this.variacoesDoProduto()
      .filter(i => !tamanho || i.tamanhoValor === tamanho)
      .forEach(i => { if (i.corId) mapa.set(i.corId, { id: i.corId, nome: i.corNome ?? '', hex: i.corHex }); });
    return Array.from(mapa.values()).sort((a, b) => a.nome.localeCompare(b.nome));
  }

  selecionarTamanho(tamanho: string) {
    this.tamanhoDrill.set(this.tamanhoDrill() === tamanho ? null : tamanho);
    this.corDrill.set(null);
  }

  variacoesFiltradasDoDrill(): EstoqueItem[] {
    const tamanho = this.tamanhoDrill();
    const cor = this.corDrill();
    return this.variacoesDoProduto().filter(i =>
      (!tamanho || i.tamanhoValor === tamanho) && (cor === null || i.corId === cor));
  }

  /** Quando a numeração escolhida está zerada (considerando a cor, se selecionada), sugere alternativas. */
  semSaldoNaSelecao(): boolean {
    if (!this.tamanhoDrill()) return false;
    const total = this.variacoesFiltradasDoDrill().reduce((soma, i) => soma + i.quantidade, 0);
    return total === 0;
  }

  sugestoes(): Sugestao[] {
    const produto = this.produtoSelecionado();
    const tamanho = this.tamanhoDrill();
    if (!produto || !tamanho || !this.semSaldoNaSelecao()) return [];

    const corAlvo = this.corDrill();
    const candidatos = this.itens().filter(i =>
      i.produtoId !== produto.produtoId &&
      i.marcaId === produto.marcaId &&
      i.categoriaId === produto.categoriaId &&
      i.tamanhoValor === tamanho &&
      i.quantidade > 0);

    return candidatos
      .map(i => ({
        produtoId: i.produtoId, produtoNome: i.produtoNome, fotoPrincipalUrl: i.fotoPrincipalUrl,
        categoriaNome: i.categoriaNome, tamanhoValor: i.tamanhoValor, corNome: i.corNome, corHex: i.corHex,
        quantidade: i.quantidade, corIgual: corAlvo !== null && i.corId === corAlvo
      }))
      .sort((a, b) => (b.corIgual ? 1 : 0) - (a.corIgual ? 1 : 0))
      .slice(0, 6);
  }

  limparFiltros() {
    this.marcaSelecionada.set(null);
    this.busca.set('');
  }
}
