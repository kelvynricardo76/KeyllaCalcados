import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { EstoqueService } from './estoque.service';
import { EstoqueItem, Loja, Movimentacao, TipoMovEstoque } from './estoque.model';
import { ProdutoService } from '../produtos/produto.service';
import { Variacao } from '../produtos/produto.model';

@Component({
  selector: 'app-estoque',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './estoque.component.html',
  styleUrl: './estoque.component.scss'
})
export class EstoqueComponent implements OnInit {
  private estoqueService = inject(EstoqueService);
  private produtoService = inject(ProdutoService);
  private fb = inject(FormBuilder);

  itens = signal<EstoqueItem[]>([]);
  variacoes = signal<Variacao[]>([]);
  lojas = signal<Loja[]>([]);
  movimentacoes = signal<Movimentacao[]>([]);

  loading = signal(true);
  saving = signal(false);
  error = signal('');

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
    this.produtoService.listarTodasVariacoes().subscribe(v => this.variacoes.set(v));
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
    const valores = this.form.getRawValue();
    this.estoqueService.ajustar({
      variacaoId: valores.variacaoId!,
      lojaId: valores.lojaId!,
      tipo: valores.tipo,
      quantidade: valores.quantidade,
      motivo: valores.motivo
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
