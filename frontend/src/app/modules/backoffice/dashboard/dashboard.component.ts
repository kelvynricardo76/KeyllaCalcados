import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';

interface DashboardKpis {
  faturamentoHoje: number;
  faturamentoOntem: number;
  vendasHoje: number;
  ticketMedio: number;
  fiadosEmAberto: number;
  fiadosVencidos: number;
  estoqueBaixo: number;
  vendasPorHora: { hora: string; valor: number }[];
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  private http = inject(HttpClient);

  kpis = signal<DashboardKpis | null>(null);
  loading = signal(true);
  currentDate = new Date();

  ngOnInit() {
    this.loadKpis();
    // Atualiza a cada 60 segundos (tempo real)
    setInterval(() => this.loadKpis(), 60_000);
  }

  private loadKpis() {
    this.http.get<DashboardKpis>(`${environment.apiUrl}/relatorios/dashboard`)
      .subscribe({
        next: data => { this.kpis.set(data); this.loading.set(false); },
        error: () => this.loading.set(false)
      });
  }

  get variacaoFaturamento(): number {
    const k = this.kpis();
    if (!k || k.faturamentoOntem === 0) return 0;
    return ((k.faturamentoHoje - k.faturamentoOntem) / k.faturamentoOntem) * 100;
  }

  getBarHeight(valor: number): number {
    const pontos = this.kpis()?.vendasPorHora ?? [];
    const max = Math.max(1, ...pontos.map(p => p.valor));
    return (valor / max) * 100;
  }
}
