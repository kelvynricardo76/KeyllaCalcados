import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { environment } from '../../../../environments/environment';
import { ModalComponent } from '../../../shared/components/modal/modal.component';

interface ItemVendaResumo {
  nomeProduto: string;
  tamanho?: string | null;
  cor?: string | null;
  quantidade: number;
}

interface PagamentoResumo {
  forma: string;
  valor: number;
  parcelas: number;
  referencia?: string | null;
}

interface VendaDetalhe {
  id: number;
  createdAt: string;
  clienteNome: string;
  clienteTelefone?: string | null;
  usuarioNome: string;
  lojaNome: string;
  subtotal: number;
  descontoGeral: number;
  valorTotal: number;
  troco?: number | null;
  observacoes?: string | null;
  itens: ItemVendaResumo[];
  pagamentos: PagamentoResumo[];
}

interface DashboardKpis {
  faturamentoHoje: number;
  faturamentoOntem: number;
  vendasHoje: number;
  ticketMedio: number;
  fiadosEmAberto: number;
  fiadosVencidos: number;
  estoqueBaixo: number;
  vendasPorHora: { hora: string; valor: number }[];
  ultimasVendas: VendaDetalhe[];
}

const LABEL_FORMA_PAGAMENTO: Record<string, string> = {
  DINHEIRO: 'Dinheiro', DEBITO: 'Débito', CREDITO: 'Crédito', PIX: 'PIX',
  FIADO: 'Fiado', TROCA: 'Troca', OUTRO: 'Outro'
};

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, ModalComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  private http = inject(HttpClient);

  kpis = signal<DashboardKpis | null>(null);
  loading = signal(true);
  currentDate = new Date();

  vendaSelecionada = signal<VendaDetalhe | null>(null);

  ngOnInit() {
    this.loadKpis();
    // Atualiza periodicamente para refletir vendas recém-fechadas no PDV.
    setInterval(() => this.loadKpis(), 20_000);
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

  descricaoItem(item: ItemVendaResumo): string {
    const detalhe = [item.tamanho, item.cor].filter(Boolean).join(' ');
    return `${item.quantidade}x ${item.nomeProduto}${detalhe ? ' (' + detalhe + ')' : ''}`;
  }

  labelForma(forma: string): string {
    return LABEL_FORMA_PAGAMENTO[forma] ?? forma;
  }

  abrirDetalheVenda(venda: VendaDetalhe) {
    this.vendaSelecionada.set(venda);
  }

  fecharDetalheVenda() {
    this.vendaSelecionada.set(null);
  }
}
