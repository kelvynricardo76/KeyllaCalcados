import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ProdutoService } from './produto.service';
import { Categoria, Cor, Marca, Produto, Tamanho, Variacao } from './produto.model';
import { ModalComponent } from '../../../shared/components/modal/modal.component';
import { imagemFake } from '../../../shared/utils/fake-image';

@Component({
  selector: 'app-produtos',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, ModalComponent],
  templateUrl: './produtos.component.html',
  styleUrl: './produtos.component.scss'
})
export class ProdutosComponent implements OnInit {
  private produtoService = inject(ProdutoService);
  private fb = inject(FormBuilder);

  produtos = signal<Produto[]>([]);
  marcas = signal<Marca[]>([]);
  categorias = signal<Categoria[]>([]);
  tamanhos = signal<Tamanho[]>([]);
  cores = signal<Cor[]>([]);
  variacoes = signal<Variacao[]>([]);

  loading = signal(true);
  saving = signal(false);
  error = signal('');
  busca = signal('');

  showForm = signal(false);
  editingId = signal<number | null>(null);

  private buscaTimeout: ReturnType<typeof setTimeout> | undefined;

  form = this.fb.nonNullable.group({
    nome: ['', [Validators.required, Validators.maxLength(200)]],
    descricao: [''],
    marcaId: [null as number | null],
    categoriaId: [null as number | null],
    codigoBarras: [''],
    sku: [''],
    precoCusto: [0, [Validators.required, Validators.min(0)]],
    precoVenda: [0, [Validators.required, Validators.min(0)]],
    temGrade: [false],
    fotoPrincipalUrl: [''],
    dataValidade: ['']
  });

  variacaoForm = this.fb.nonNullable.group({
    tamanhoId: [null as number | null],
    corId: [null as number | null],
    sku: ['', Validators.required],
    codigoBarras: ['']
  });

  ngOnInit() {
    this.carregarTudo();
  }

  private carregarTudo() {
    this.loading.set(true);
    this.produtoService.listarMarcas().subscribe(m => this.marcas.set(m));
    this.produtoService.listarCategorias().subscribe(c => this.categorias.set(c));
    this.produtoService.listarTamanhos().subscribe(t => this.tamanhos.set(t));
    this.produtoService.listarCores().subscribe(c => this.cores.set(c));
    this.buscarProdutos();
  }

  buscarProdutos() {
    this.loading.set(true);
    this.produtoService.listarProdutos(this.busca() || undefined).subscribe({
      next: produtos => { this.produtos.set(produtos); this.loading.set(false); },
      error: () => { this.error.set('Não foi possível carregar os produtos.'); this.loading.set(false); }
    });
  }

  onBuscaChange(valor: string) {
    this.busca.set(valor);
    clearTimeout(this.buscaTimeout);
    this.buscaTimeout = setTimeout(() => this.buscarProdutos(), 350);
  }

  abrirNovo() {
    this.editingId.set(null);
    this.variacoes.set([]);
    this.error.set('');
    this.form.reset({
      nome: '', descricao: '', marcaId: null, categoriaId: null,
      codigoBarras: '', sku: '', precoCusto: 0, precoVenda: 0, temGrade: false,
      fotoPrincipalUrl: '', dataValidade: ''
    });
    this.showForm.set(true);
  }

  editar(produto: Produto) {
    this.editingId.set(produto.id);
    this.error.set('');
    this.form.reset({
      nome: produto.nome,
      descricao: produto.descricao ?? '',
      marcaId: produto.marcaId ?? null,
      categoriaId: produto.categoriaId ?? null,
      codigoBarras: produto.codigoBarras ?? '',
      sku: produto.sku ?? '',
      precoCusto: produto.precoCusto,
      precoVenda: produto.precoVenda,
      temGrade: produto.temGrade,
      fotoPrincipalUrl: produto.fotoPrincipalUrl ?? '',
      dataValidade: produto.dataValidade ?? ''
    });
    this.showForm.set(true);
    if (produto.temGrade) {
      this.carregarVariacoes(produto.id);
    } else {
      this.variacoes.set([]);
    }
  }

  fecharForm() {
    this.showForm.set(false);
  }

  salvar() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving.set(true);
    this.error.set('');
    const payload = this.form.getRawValue();
    const id = this.editingId();

    const request$ = id
      ? this.produtoService.atualizarProduto(id, payload)
      : this.produtoService.criarProduto(payload);

    request$.subscribe({
      next: produto => {
        this.saving.set(false);
        this.editingId.set(produto.id);
        this.buscarProdutos();
        if (!produto.temGrade) {
          this.showForm.set(false);
        }
      },
      error: err => {
        this.saving.set(false);
        this.error.set(err.error?.message ?? 'Não foi possível salvar o produto.');
      }
    });
  }

  toggleAtivo(produto: Produto) {
    this.produtoService.alterarAtivoProduto(produto.id, !produto.ativo).subscribe(() => this.buscarProdutos());
  }

  // ── Grade / Variações ──────────────────────────────────────────
  private carregarVariacoes(produtoId: number) {
    this.produtoService.listarVariacoes(produtoId).subscribe(v => this.variacoes.set(v));
  }

  adicionarVariacao() {
    const produtoId = this.editingId();
    if (!produtoId || this.variacaoForm.invalid) { this.variacaoForm.markAllAsTouched(); return; }

    this.produtoService.criarVariacao(produtoId, this.variacaoForm.getRawValue()).subscribe({
      next: variacao => {
        this.variacoes.update(lista => [...lista, variacao]);
        this.variacaoForm.reset({ tamanhoId: null, corId: null, sku: '', codigoBarras: '' });
      },
      error: err => this.error.set(err.error?.message ?? 'Não foi possível adicionar a variação.')
    });
  }

  toggleAtivoVariacao(variacao: Variacao) {
    this.produtoService.alterarAtivoVariacao(variacao.id, !variacao.ativo).subscribe(atualizada => {
      this.variacoes.update(lista => lista.map(v => v.id === atualizada.id ? atualizada : v));
    });
  }

  removerVariacao(variacao: Variacao) {
    this.produtoService.excluirVariacao(variacao.id).subscribe(() => {
      this.variacoes.update(lista => lista.filter(v => v.id !== variacao.id));
    });
  }

  sugerirSkuVariacao() {
    const sku = this.form.controls.sku.value;
    const tamanhoId = this.variacaoForm.controls.tamanhoId.value;
    const corId = this.variacaoForm.controls.corId.value;
    const tamanho = this.tamanhos().find(t => t.id === tamanhoId);
    const cor = this.cores().find(c => c.id === corId);
    if (!sku || !tamanho || !cor) return;
    const partes = [sku, tamanho.valor, cor.nome.substring(0, 3).toUpperCase()];
    this.variacaoForm.controls.sku.setValue(partes.join('-'));
  }

  get nomeCtrl() { return this.form.controls.nome; }
  get precoCustoCtrl() { return this.form.controls.precoCusto; }
  get precoVendaCtrl() { return this.form.controls.precoVenda; }
  get temGrade() { return this.form.controls.temGrade.value; }

  imagemDoProduto(produto: Produto): string {
    return produto.fotoPrincipalUrl || imagemFake(produto.nome);
  }

  get previewImagem(): string {
    const url = this.form.controls.fotoPrincipalUrl.value;
    const nome = this.form.controls.nome.value;
    return url || imagemFake(nome || '?');
  }
}
