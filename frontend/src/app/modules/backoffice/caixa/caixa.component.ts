import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CaixaService } from './caixa.service';
import { MovimentacaoCaixa, SessaoCaixa } from './caixa.model';
import { ModalComponent } from '../../../shared/components/modal/modal.component';

@Component({
  selector: 'app-caixa',
  standalone: true,
  imports: [CommonModule, ModalComponent],
  templateUrl: './caixa.component.html',
  styleUrl: './caixa.component.scss'
})
export class CaixaComponent implements OnInit {
  private caixaService = inject(CaixaService);

  sessoes = signal<SessaoCaixa[]>([]);
  loading = signal(true);
  error = signal('');

  showMovimentacoes = signal(false);
  sessaoSelecionada = signal<SessaoCaixa | null>(null);
  movimentacoes = signal<MovimentacaoCaixa[]>([]);

  ngOnInit() {
    this.carregar();
  }

  private carregar() {
    this.loading.set(true);
    this.caixaService.listarSessoes().subscribe({
      next: sessoes => { this.sessoes.set(sessoes); this.loading.set(false); },
      error: () => { this.error.set('Não foi possível carregar as sessões de caixa.'); this.loading.set(false); }
    });
  }

  verMovimentacoes(sessao: SessaoCaixa) {
    this.sessaoSelecionada.set(sessao);
    this.movimentacoes.set([]);
    this.showMovimentacoes.set(true);
    this.caixaService.listarMovimentacoes(sessao.id).subscribe(m => this.movimentacoes.set(m));
  }

  fecharMovimentacoes() {
    this.showMovimentacoes.set(false);
  }

  rotuloTipo(tipo: string): string {
    switch (tipo) {
      case 'SUPRIMENTO': return 'Entrada';
      case 'SANGRIA': return 'Saída';
      case 'DESPESA': return 'Despesa';
      case 'RECEITA_AVULSA': return 'Receita avulsa';
      default: return tipo;
    }
  }
}
