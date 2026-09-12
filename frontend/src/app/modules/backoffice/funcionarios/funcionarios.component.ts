import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RelatorioService } from '../relatorios/relatorio.service';
import { FuncionarioRanking } from '../relatorios/relatorio.model';

function formatarData(d: Date): string {
  return d.toISOString().substring(0, 10);
}

@Component({
  selector: 'app-funcionarios',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './funcionarios.component.html',
  styleUrl: './funcionarios.component.scss'
})
export class FuncionariosComponent implements OnInit {
  private relatorioService = inject(RelatorioService);

  loading = signal(true);
  error = signal('');
  ranking = signal<FuncionarioRanking[]>([]);

  inicio: string;
  fim: string;

  totalVendido = computed(() => this.ranking().reduce((acc, f) => acc + f.valorTotal, 0));
  totalVendas = computed(() => this.ranking().reduce((acc, f) => acc + f.quantidadeVendas, 0));

  constructor() {
    const hoje = new Date();
    const primeiroDiaMes = new Date(hoje.getFullYear(), hoje.getMonth(), 1);
    this.inicio = formatarData(primeiroDiaMes);
    this.fim = formatarData(hoje);
  }

  ngOnInit() {
    this.buscar();
  }

  buscar() {
    this.loading.set(true);
    this.error.set('');
    this.relatorioService.funcionarios(this.inicio, this.fim).subscribe({
      next: lista => {
        this.ranking.set([...lista].sort((a, b) => b.valorTotal - a.valorTotal));
        this.loading.set(false);
      },
      error: () => { this.error.set('Não foi possível carregar o desempenho dos funcionários.'); this.loading.set(false); }
    });
  }

  percentualDoTotal(funcionario: FuncionarioRanking): number {
    const total = this.totalVendido();
    return total > 0 ? (funcionario.valorTotal / total) * 100 : 0;
  }
}
