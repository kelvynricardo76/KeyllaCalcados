import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CaixaService } from './caixa.service';
import { MovimentacaoCaixa, SessaoCaixa } from './caixa.model';
import { PdvService } from '../../pdv/pdv.service';
import { Venda } from '../../pdv/pdv.model';
import { ModalComponent } from '../../../shared/components/modal/modal.component';

const LABEL_FORMA_PAGAMENTO: Record<string, string> = {
  DINHEIRO: 'Dinheiro', DEBITO: 'Débito', CREDITO: 'Crédito', PIX: 'PIX',
  FIADO: 'Fiado', TROCA: 'Troca', OUTRO: 'Outro'
};

@Component({
  selector: 'app-caixa',
  standalone: true,
  imports: [CommonModule, ModalComponent],
  templateUrl: './caixa.component.html',
  styleUrl: './caixa.component.scss'
})
export class CaixaComponent implements OnInit {
  private caixaService = inject(CaixaService);
  private pdvService = inject(PdvService);

  sessoes = signal<SessaoCaixa[]>([]);
  loading = signal(true);
  error = signal('');

  showDetalheSessao = signal(false);
  sessaoSelecionada = signal<SessaoCaixa | null>(null);
  movimentacoes = signal<MovimentacaoCaixa[]>([]);
  vendasDaSessao = signal<Venda[]>([]);
  carregandoDetalhe = signal(false);

  vendaSelecionada = signal<Venda | null>(null);

  totalVendasSessao = computed(() =>
    this.vendasDaSessao().filter(v => v.status === 'FECHADA').reduce((soma, v) => soma + v.valorTotal, 0));

  totalEntradasSessao = computed(() =>
    this.movimentacoes().filter(m => m.tipo === 'SUPRIMENTO' || m.tipo === 'RECEITA_AVULSA')
      .reduce((soma, m) => soma + m.valor, 0));

  totalSaidasSessao = computed(() =>
    this.movimentacoes().filter(m => m.tipo === 'SANGRIA' || m.tipo === 'DESPESA')
      .reduce((soma, m) => soma + m.valor, 0));

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

  verDetalheSessao(sessao: SessaoCaixa) {
    this.sessaoSelecionada.set(sessao);
    this.movimentacoes.set([]);
    this.vendasDaSessao.set([]);
    this.showDetalheSessao.set(true);
    this.carregandoDetalhe.set(true);
    this.caixaService.listarMovimentacoes(sessao.id).subscribe(m => this.movimentacoes.set(m));
    this.pdvService.listarVendasPorSessao(sessao.id).subscribe({
      next: vendas => { this.vendasDaSessao.set(vendas); this.carregandoDetalhe.set(false); },
      error: () => this.carregandoDetalhe.set(false)
    });
  }

  fecharDetalheSessao() {
    this.showDetalheSessao.set(false);
  }

  abrirDetalheVenda(venda: Venda) {
    this.vendaSelecionada.set(venda);
  }

  fecharDetalheVenda() {
    this.vendaSelecionada.set(null);
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

  labelForma(forma: string): string {
    return LABEL_FORMA_PAGAMENTO[forma] ?? forma;
  }

  resumoItens(venda: Venda): string {
    return venda.itens.map(i => `${i.quantidade}x ${i.nomeProduto}`).join(', ');
  }

  resumoPagamentos(venda: Venda): string {
    return venda.pagamentos.map(p => this.labelForma(p.forma)).join(', ') || '—';
  }
}
