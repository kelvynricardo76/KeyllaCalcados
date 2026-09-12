import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { EstoqueService } from './estoque.service';
import { EstoqueItem, Loja, Movimentacao, TipoMovEstoque } from './estoque.model';
import { ProdutoService } from '../produtos/produto.service';
import { Variacao } from '../produtos/produto.model';
import { ModalComponent } from '../../../shared/components/modal/modal.component';
import { corPorTexto, imagemFake } from '../../../shared/utils/fake-image';

interface MarcaResumo {
  id: number | null;
  nome: string;
  quantidadeProdutos: number;
}

interface ProdutoResumo {
  produtoId: number;
  produtoNome: string;
  marcaNome?: string | null;
  categoriaNome?: string | null;
  fotoPrincipalUrl?: string | null;
  produtoDataValidade?: string | null;
  quantidadeTotal: number;
  variacoesCount: number;
  statusPior: 'ZERADO' | 'BAIXO' | 'OK';
}

interface TotalPorTamanho {
  tamanho: string;
  total: number;
}

@Component({
  selector: 'app-estoque',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, ModalComponent],
  templateUrl: './estoque.component.html',
  styleUrl: './estoque.component.scss'
})
export class EstoqueComponent implements OnInit {
  private estoqueService = inject(EstoqueService);
  private produtoService = inject(ProdutoService);
  private fb = inject(FormBuilder);

  itens = signal<EstoqueItem[]>([]);
  variacoesCatalogo = signal<Variacao[]>([]);
  lojas = signal<Loja[]>([]);
  movimentacoes = signal<Movimentacao[]>([]);

  loading = signal(true);
  saving = signal(false);
  error = signal('');

  // ── Filtros ──────────────────────────────────────────────────────
  marcaSelecionada = signal<number | null>(null);
  busca = signal('');
  filtroTamanho = signal('');
  filtroCor = signal<number | null>(null);
  filtroCodigoBarras = signal('');

  produtoSelecionado = signal<ProdutoResumo | null>(null);
  tamanhoDrill = signal<string | null>(null);
  corDrill = signal<number | null>(null);

  showAjuste = signal(false);
  showHistorico = signal(false);
  historicoTitulo = signal('');

  tiposMovimento: { valor: TipoMovEstoque; label: string }[] = [
    { valor: 'ENTRADA', label: 'Entrada (compra/recebimento)' },
    { valor: 'SAIDA', label: 'Saída (venda manual/perda)' },
    { valor: 'AJUSTE_POSITIVO', label: 'Ajuste positivo (inventário)' },
    { valor: 'AJUSTE_NEGATIVO', label: 'Ajuste negativo (inventário)' },
    { valor: 'DEVOLUCAO', label: 'Devolução' }
  ];

  form = this.fb.nonNullable.group({
    variacaoId: [null as number | null, Validators.required],
    lojaId: [null as number | null, Validators.required],
    tipo: ['ENTRADA' as TipoMovEstoque, Validators.required],
    quantidade: [1, [Validators.required, Validators.min(1)]],
    motivo: ['']
  });

  ngOnInit() {
    this.carregarTudo();
  }

  private carregarTudo() {
    this.loading.set(true);
    this.produtoService.listarTodasVariacoes().subscribe(v => this.variacoesCatalogo.set(v));
    this.estoqueService.listarLojas().subscribe(lojas => {
      this.lojas.set(lojas);
      if (lojas.length > 0) this.form.controls.lojaId.setValue(lojas[0].id);
    });
    this.carregarEstoque();
  }

  private carregarEstoque() {
    this.loading.set(true);
    this.estoqueService.listar().subscribe({
      next: itens => { this.itens.set(itens); this.loading.set(false); },
      error: () => { this.error.set('Não foi possível carregar o estoque.'); this.loading.set(false); }
    });
  }

  // ── Dados derivados (marcas, tamanhos, cores disponíveis) ────────
  marcas = computed<MarcaResumo[]>(() => {
    const porMarca = new Map<string, MarcaResumo>();
    for (const item of this.itens()) {
      const chave = String(item.marcaId ?? 'sem-marca');
      const atual = porMarca.get(chave);
      if (atual) {
        atual.quantidadeProdutos += 0; // contagem de produtos únicos é ajustada abaixo
      } else {
        porMarca.set(chave, { id: item.marcaId ?? null, nome: item.marcaNome ?? 'Sem marca', quantidadeProdutos: 0 });
      }
    }
    // conta produtos únicos por marca
    const produtosPorMarca = new Map<string, Set<number>>();
    for (const item of this.itens()) {
      const chave = String(item.marcaId ?? 'sem-marca');
      if (!produtosPorMarca.has(chave)) produtosPorMarca.set(chave, new Set());
      produtosPorMarca.get(chave)!.add(item.produtoId);
    }
    for (const [chave, resumo] of porMarca) {
      resumo.quantidadeProdutos = produtosPorMarca.get(chave)?.size ?? 0;
    }
    return Array.from(porMarca.values()).sort((a, b) => a.nome.localeCompare(b.nome));
  });

  tamanhosDisponiveis = computed<string[]>(() => {
    const set = new Set<string>();
    this.itens().forEach(i => { if (i.tamanhoValor) set.add(i.tamanhoValor); });
    return Array.from(set).sort((a, b) => a.localeCompare(b, undefined, { numeric: true }));
  });

  coresDisponiveis = computed<{ id: number; nome: string; hex?: string | null }[]>(() => {
    const mapa = new Map<number, { id: number; nome: string; hex?: string | null }>();
    this.itens().forEach(i => { if (i.corId) mapa.set(i.corId, { id: i.corId, nome: i.corNome ?? '', hex: i.corHex }); });
    return Array.from(mapa.values()).sort((a, b) => a.nome.localeCompare(b.nome));
  });

  // ── Filtragem e agrupamento ──────────────────────────────────────
  itensFiltrados(): EstoqueItem[] {
    const marca = this.marcaSelecionada();
    const termo = this.busca().trim().toLowerCase();
    const tamanho = this.filtroTamanho();
    const cor = this.filtroCor();
    const codBarras = this.filtroCodigoBarras().trim().toLowerCase();

    return this.itens().filter(i => {
      if (marca !== null && i.marcaId !== marca) return false;
      if (termo && !i.produtoNome.toLowerCase().includes(termo)) return false;
      if (tamanho && i.tamanhoValor !== tamanho) return false;
      if (cor !== null && i.corId !== cor) return false;
      if (codBarras && !(i.codigoBarras ?? '').toLowerCase().includes(codBarras) && !(i.sku ?? '').toLowerCase().includes(codBarras)) return false;
      return true;
    });
  }

  produtosAgrupados(): ProdutoResumo[] {
    const mapa = new Map<number, ProdutoResumo>();
    for (const item of this.itensFiltrados()) {
      let resumo = mapa.get(item.produtoId);
      if (!resumo) {
        resumo = {
          produtoId: item.produtoId, produtoNome: item.produtoNome, marcaNome: item.marcaNome,
          categoriaNome: item.categoriaNome, fotoPrincipalUrl: item.fotoPrincipalUrl,
          produtoDataValidade: item.produtoDataValidade,
          quantidadeTotal: 0, variacoesCount: 0, statusPior: 'OK'
        };
        mapa.set(item.produtoId, resumo);
      }
      resumo.quantidadeTotal += item.quantidade;
      resumo.variacoesCount += 1;
      if (item.statusEstoque === 'ZERADO') resumo.statusPior = 'ZERADO';
      else if (item.statusEstoque === 'BAIXO' && resumo.statusPior !== 'ZERADO') resumo.statusPior = 'BAIXO';
    }
    return Array.from(mapa.values()).sort((a, b) => a.produtoNome.localeCompare(b.produtoNome));
  }

  /** Toda a grade do produto selecionado, sem os filtros da barra principal — a navegação dentro do
   *  painel de detalhe (numeração/cor) é independente dos filtros usados para achar o produto. */
  private variacoesDoProdutoBase(): EstoqueItem[] {
    const produto = this.produtoSelecionado();
    if (!produto) return [];
    return this.itens().filter(i => i.produtoId === produto.produtoId);
  }

  tamanhosDoProdutoSelecionado(): string[] {
    const set = new Set<string>();
    this.variacoesDoProdutoBase().forEach(i => { if (i.tamanhoValor) set.add(i.tamanhoValor); });
    return Array.from(set).sort((a, b) => a.localeCompare(b, undefined, { numeric: true }));
  }

  coresDoTamanhoSelecionado(): { id: number; nome: string; hex?: string | null }[] {
    const tamanho = this.tamanhoDrill();
    const mapa = new Map<number, { id: number; nome: string; hex?: string | null }>();
    this.variacoesDoProdutoBase()
      .filter(i => !tamanho || i.tamanhoValor === tamanho)
      .forEach(i => { if (i.corId) mapa.set(i.corId, { id: i.corId, nome: i.corNome ?? '', hex: i.corHex }); });
    return Array.from(mapa.values()).sort((a, b) => a.nome.localeCompare(b.nome));
  }

  selecionarTamanhoDrill(tamanho: string) {
    this.tamanhoDrill.set(this.tamanhoDrill() === tamanho ? null : tamanho);
    this.corDrill.set(null);
  }

  variacoesDoProdutoSelecionado(): EstoqueItem[] {
    const tamanho = this.tamanhoDrill();
    const cor = this.corDrill();
    return this.variacoesDoProdutoBase()
      .filter(i => (!tamanho || i.tamanhoValor === tamanho) && (cor === null || i.corId === cor))
      .sort((a, b) => (a.tamanhoValor ?? '').localeCompare(b.tamanhoValor ?? '', undefined, { numeric: true }));
  }

  totaisPorTamanho(): TotalPorTamanho[] {
    const mapa = new Map<string, number>();
    for (const item of this.variacoesDoProdutoBase()) {
      const chave = item.tamanhoValor ?? '—';
      mapa.set(chave, (mapa.get(chave) ?? 0) + item.quantidade);
    }
    return Array.from(mapa.entries())
      .map(([tamanho, total]) => ({ tamanho, total }))
      .sort((a, b) => a.tamanho.localeCompare(b.tamanho, undefined, { numeric: true }));
  }

  limparFiltros() {
    this.marcaSelecionada.set(null);
    this.busca.set('');
    this.filtroTamanho.set('');
    this.filtroCor.set(null);
    this.filtroCodigoBarras.set('');
  }

  imagemDoProduto(nome: string, foto?: string | null, categoria?: string | null): string {
    return foto || imagemFake(nome, categoria);
  }

  corMarca(nome: string): string {
    return corPorTexto(nome);
  }

  diasParaVencer(dataValidade?: string | null): number | null {
    if (!dataValidade) return null;
    const hoje = new Date();
    hoje.setHours(0, 0, 0, 0);
    const validade = new Date(dataValidade + 'T00:00:00');
    return Math.round((validade.getTime() - hoje.getTime()) / (1000 * 60 * 60 * 24));
  }

  // ── Detalhe do produto ───────────────────────────────────────────
  abrirProduto(produto: ProdutoResumo) {
    this.produtoSelecionado.set(produto);
    this.tamanhoDrill.set(null);
    this.corDrill.set(null);
  }

  fecharProduto() {
    this.produtoSelecionado.set(null);
  }

  // ── Ajuste de estoque ────────────────────────────────────────────
  abrirLancamento() {
    this.error.set('');
    this.form.reset({
      variacaoId: null,
      lojaId: this.lojas()[0]?.id ?? null,
      tipo: 'ENTRADA',
      quantidade: 1,
      motivo: ''
    });
    this.showAjuste.set(true);
  }

  ajustarLinha(item: EstoqueItem) {
    this.error.set('');
    this.form.reset({
      variacaoId: item.variacaoId,
      lojaId: item.lojaId,
      tipo: 'ENTRADA',
      quantidade: 1,
      motivo: ''
    });
    this.showAjuste.set(true);
  }

  fecharAjuste() {
    this.showAjuste.set(false);
  }

  salvarAjuste() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving.set(true);
    this.error.set('');
    const valor = this.form.getRawValue();
    this.estoqueService.ajustar({
      variacaoId: valor.variacaoId!,
      lojaId: valor.lojaId!,
      tipo: valor.tipo,
      quantidade: valor.quantidade,
      motivo: valor.motivo
    }).subscribe({
      next: () => {
        this.saving.set(false);
        this.showAjuste.set(false);
        this.carregarEstoque();
      },
      error: err => {
        this.saving.set(false);
        this.error.set(err.error?.message ?? 'Não foi possível lançar a movimentação.');
      }
    });
  }

  verHistorico(item: EstoqueItem) {
    this.historicoTitulo.set(`${item.produtoNome} ${item.tamanhoValor ?? ''} ${item.corNome ?? ''} · ${item.lojaNome}`);
    this.movimentacoes.set([]);
    this.showHistorico.set(true);
    this.estoqueService.listarMovimentacoes(item.id).subscribe(m => this.movimentacoes.set(m));
  }

  fecharHistorico() {
    this.showHistorico.set(false);
  }

  descricaoVariacao(v: Variacao): string {
    const partes = [v.produtoNome, v.tamanhoValor, v.corNome, v.sku].filter(Boolean);
    return partes.join(' · ');
  }
}
