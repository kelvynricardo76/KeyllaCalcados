import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RelatorioService } from './relatorio.service';
import { ProdutoParado, ProdutoVencendo, RelatorioVendas } from './relatorio.model';

function formatarData(d: Date): string {
  return d.toISOString().substring(0, 10);
}

@Component({
  selector: 'app-relatorios',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './relatorios.component.html',
  styleUrl: './relatorios.component.scss'
})
export class RelatoriosComponent implements OnInit {
  private relatorioService = inject(RelatorioService);

  loading = signal(true);
  error = signal('');
  relatorio = signal<RelatorioVendas | null>(null);

  vencendo = signal<ProdutoVencendo[]>([]);
  diasVencimento = signal(30);

  parados = signal<ProdutoParado[]>([]);
  diasParados = signal(60);
  paradosLoading = signal(false);

  alertaVencendoCount = computed(() => this.vencendo().length);
  alertaVencidoCount = computed(() => this.vencendo().filter(v => v.vencido).length);

  inicio: string;
  fim: string;

  constructor() {
    const hoje = new Date();
    const primeiroDiaMes = new Date(hoje.getFullYear(), hoje.getMonth(), 1);
    this.inicio = formatarData(primeiroDiaMes);
    this.fim = formatarData(hoje);
  }

  ngOnInit() {
    this.buscar();
    this.buscarVencendo();
    this.buscarParados();
  }

  buscar() {
    this.loading.set(true);
    this.error.set('');
    this.relatorioService.vendasPorPeriodo(this.inicio, this.fim).subscribe({
      next: r => { this.relatorio.set(r); this.loading.set(false); },
      error: () => { this.error.set('Não foi possível carregar o relatório.'); this.loading.set(false); }
    });
  }

  buscarVencendo() {
    this.relatorioService.produtosVencendo(this.diasVencimento()).subscribe(lista => this.vencendo.set(lista));
  }

  buscarParados() {
    this.paradosLoading.set(true);
    this.relatorioService.produtosParados(this.diasParados()).subscribe({
      next: lista => { this.parados.set(lista); this.paradosLoading.set(false); },
      error: () => this.paradosLoading.set(false)
    });
  }
}
