import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { FornecedorService } from './fornecedor.service';
import { Compra, Fornecedor, ItemCompraRequest } from './fornecedor.model';
import { LojaService, Loja } from '../../../core/services/loja.service';
import { ProdutoService } from '../produtos/produto.service';
import { Variacao } from '../produtos/produto.model';

@Component({
  selector: 'app-fornecedores',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './fornecedores.component.html',
  styleUrl: './fornecedores.component.scss'
})
export class FornecedoresComponent implements OnInit {
  private fornecedorService = inject(FornecedorService);
  private lojaService = inject(LojaService);
  private produtoService = inject(ProdutoService);
  private fb = inject(FormBuilder);

  aba = signal<'FORNECEDORES' | 'COMPRAS'>('FORNECEDORES');

  fornecedores = signal<Fornecedor[]>([]);
  compras = signal<Compra[]>([]);
  lojas = signal<Loja[]>([]);
  variacoes = signal<Variacao[]>([]);

  loading = signal(true);
  saving = signal(false);
  error = signal('');

  showFornecedorForm = signal(false);
  editingFornecedorId = signal<number | null>(null);

  showCompraForm = signal(false);
  itensCompra = signal<ItemCompraRequest[]>([]);

  fornecedorForm = this.fb.nonNullable.group({
    razaoSocial: ['', Validators.required],
    cnpj: [''],
    telefone: [''],
    email: [''],
    endereco: [''],
    contato: [''],
    observacoes: ['']
  });

  compraForm = this.fb.nonNullable.group({
    fornecedorId: [null as number | null],
    lojaId: [null as number | null, Validators.required],
    numeroNf: [''],
    observacoes: ['']
  });

  itemForm = this.fb.nonNullable.group({
    variacaoId: [null as number | null, Validators.required],
    quantidade: [1, [Validators.required, Validators.min(1)]],
    precoCustoUnitario: [0, [Validators.required, Validators.min(0)]]
  });

  ngOnInit() {
    this.lojaService.listar().subscribe(lojas => {
      this.lojas.set(lojas);
      if (lojas.length > 0) this.compraForm.controls.lojaId.setValue(lojas[0].id);
    });
    this.produtoService.listarTodasVariacoes().subscribe(v => this.variacoes.set(v));
    this.carregarFornecedores();
    this.carregarCompras();
  }

  private carregarFornecedores() {
    this.loading.set(true);
    this.fornecedorService.listarFornecedores().subscribe({
      next: f => { this.fornecedores.set(f); this.loading.set(false); },
      error: () => { this.error.set('Não foi possível carregar os fornecedores.'); this.loading.set(false); }
    });
  }

  private carregarCompras() {
    this.fornecedorService.listarCompras().subscribe(c => this.compras.set(c));
  }

  // ── Fornecedores ─────────────────────────────────────────────────
  abrirNovoFornecedor() {
    this.editingFornecedorId.set(null);
    this.error.set('');
    this.fornecedorForm.reset({ razaoSocial: '', cnpj: '', telefone: '', email: '', endereco: '', contato: '', observacoes: '' });
    this.showFornecedorForm.set(true);
  }

  editarFornecedor(f: Fornecedor) {
    this.editingFornecedorId.set(f.id);
    this.error.set('');
    this.fornecedorForm.reset({
      razaoSocial: f.razaoSocial, cnpj: f.cnpj ?? '', telefone: f.telefone ?? '',
      email: f.email ?? '', endereco: f.endereco ?? '', contato: f.contato ?? '', observacoes: f.observacoes ?? ''
    });
    this.showFornecedorForm.set(true);
  }

  fecharFornecedorForm() {
    this.showFornecedorForm.set(false);
  }

  salvarFornecedor() {
    if (this.fornecedorForm.invalid) { this.fornecedorForm.markAllAsTouched(); return; }
    this.saving.set(true);
    this.error.set('');
    const payload = this.fornecedorForm.getRawValue();
    const id = this.editingFornecedorId();
    const request$ = id ? this.fornecedorService.atualizarFornecedor(id, payload) : this.fornecedorService.criarFornecedor(payload);
    request$.subscribe({
      next: () => { this.saving.set(false); this.showFornecedorForm.set(false); this.carregarFornecedores(); },
      error: err => { this.saving.set(false); this.error.set(err.error?.message ?? 'Não foi possível salvar o fornecedor.'); }
    });
  }

  toggleAtivoFornecedor(f: Fornecedor) {
    this.fornecedorService.alterarAtivoFornecedor(f.id, !f.ativo).subscribe(() => this.carregarFornecedores());
  }

  // ── Compras ──────────────────────────────────────────────────────
  abrirNovaCompra() {
    this.error.set('');
    this.itensCompra.set([]);
    this.compraForm.reset({ fornecedorId: null, lojaId: this.lojas()[0]?.id ?? null, numeroNf: '', observacoes: '' });
    this.itemForm.reset({ variacaoId: null, quantidade: 1, precoCustoUnitario: 0 });
    this.showCompraForm.set(true);
  }

  fecharCompraForm() {
    this.showCompraForm.set(false);
  }

  adicionarItemCompra() {
    if (this.itemForm.invalid) { this.itemForm.markAllAsTouched(); return; }
    const valores = this.itemForm.getRawValue();
    this.itensCompra.update(lista => [...lista, {
      variacaoId: valores.variacaoId!,
      quantidade: valores.quantidade,
      precoCustoUnitario: valores.precoCustoUnitario
    }]);
    this.itemForm.reset({ variacaoId: null, quantidade: 1, precoCustoUnitario: 0 });
  }

  removerItemCompra(index: number) {
    this.itensCompra.update(lista => lista.filter((_, i) => i !== index));
  }

  totalCompra(): number {
    return this.itensCompra().reduce((soma, i) => soma + i.quantidade * i.precoCustoUnitario, 0);
  }

  nomeVariacao(id: number): string {
    const v = this.variacoes().find(v => v.id === id);
    if (!v) return '—';
    return [v.produtoNome, v.tamanhoValor, v.corNome, v.sku].filter(Boolean).join(' · ');
  }

  salvarCompra() {
    if (this.compraForm.invalid || this.itensCompra().length === 0) {
      this.compraForm.markAllAsTouched();
      if (this.itensCompra().length === 0) this.error.set('Adicione ao menos um item à compra.');
      return;
    }
    this.saving.set(true);
    this.error.set('');
    const valores = this.compraForm.getRawValue();
    this.fornecedorService.criarCompra({
      fornecedorId: valores.fornecedorId,
      lojaId: valores.lojaId!,
      numeroNf: valores.numeroNf,
      observacoes: valores.observacoes,
      itens: this.itensCompra()
    }).subscribe({
      next: () => { this.saving.set(false); this.showCompraForm.set(false); this.carregarCompras(); },
      error: err => { this.saving.set(false); this.error.set(err.error?.message ?? 'Não foi possível registrar a compra.'); }
    });
  }

  receberCompra(compra: Compra) {
    this.fornecedorService.receberCompra(compra.id).subscribe(() => this.carregarCompras());
  }

  cancelarCompra(compra: Compra) {
    this.fornecedorService.cancelarCompra(compra.id).subscribe(() => this.carregarCompras());
  }
}
