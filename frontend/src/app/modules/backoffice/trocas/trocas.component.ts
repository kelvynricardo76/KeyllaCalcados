import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PdvService } from '../../pdv/pdv.service';
import { Venda } from '../../pdv/pdv.model';
import { DevolucaoService } from './devolucao.service';
import { Devolucao, TipoDevolucao } from './devolucao.model';

interface LinhaDevolucao {
  variacaoId: number;
  nomeProduto: string;
  tamanho?: string | null;
  cor?: string | null;
  quantidadeVendida: number;
  quantidadeDevolver: number;
}

@Component({
  selector: 'app-trocas',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './trocas.component.html',
  styleUrl: './trocas.component.scss'
})
export class TrocasComponent {
  private pdvService = inject(PdvService);
  private devolucaoService = inject(DevolucaoService);

  vendaId: number | null = null;
  venda = signal<Venda | null>(null);
  linhas = signal<LinhaDevolucao[]>([]);
  historico = signal<Devolucao[]>([]);

  tipo: TipoDevolucao = 'TROCA';
  motivo = '';

  loading = signal(false);
  saving = signal(false);
  error = signal('');
  sucesso = signal('');

  buscarVenda() {
    if (!this.vendaId) return;
    this.error.set('');
    this.sucesso.set('');
    this.loading.set(true);
    this.venda.set(null);

    this.pdvService.buscarVenda(this.vendaId).subscribe({
      next: v => {
        this.venda.set(v);
        this.linhas.set(v.itens.map(i => ({
          variacaoId: i.variacaoId,
          nomeProduto: i.nomeProduto,
          tamanho: i.tamanho,
          cor: i.cor,
          quantidadeVendida: i.quantidade,
          quantidadeDevolver: 0
        })));
        this.loading.set(false);
        this.carregarHistorico(v.id);
      },
      error: err => {
        this.loading.set(false);
        this.error.set(err.error?.message ?? 'Venda não encontrada.');
      }
    });
  }

  private carregarHistorico(vendaId: number) {
    this.devolucaoService.listarPorVenda(vendaId).subscribe(h => this.historico.set(h));
  }

  precoUnitario(variacaoId: number): number {
    const item = this.venda()?.itens.find(i => i.variacaoId === variacaoId);
    return item?.precoUnitario ?? 0;
  }

  totalDevolucao(): number {
    const venda = this.venda();
    if (!venda) return 0;
    return this.linhas().reduce((soma, linha) => {
      const item = venda.itens.find(i => i.variacaoId === linha.variacaoId);
      return soma + (item ? item.precoUnitario * linha.quantidadeDevolver : 0);
    }, 0);
  }

  confirmarDevolucao() {
    const venda = this.venda();
    if (!venda) return;

    const itens = this.linhas()
      .filter(l => l.quantidadeDevolver > 0)
      .map(l => ({ variacaoId: l.variacaoId, quantidade: l.quantidadeDevolver }));

    if (itens.length === 0) {
      this.error.set('Informe a quantidade de ao menos um item para devolver.');
      return;
    }
    if (!this.motivo.trim()) {
      this.error.set('Informe o motivo da troca/devolução.');
      return;
    }

    this.saving.set(true);
    this.error.set('');
    this.sucesso.set('');

    this.devolucaoService.registrar(venda.id, { tipo: this.tipo, motivo: this.motivo, itens }).subscribe({
      next: d => {
        this.saving.set(false);
        this.sucesso.set(`${this.tipo === 'TROCA' ? 'Troca' : 'Devolução'} registrada. Valor: R$ ${d.valorDevolvido.toFixed(2)}`);
        this.motivo = '';
        this.linhas.update(lista => lista.map(l => ({ ...l, quantidadeDevolver: 0 })));
        this.carregarHistorico(venda.id);
      },
      error: err => {
        this.saving.set(false);
        this.error.set(err.error?.message ?? 'Não foi possível registrar a troca/devolução.');
      }
    });
  }
}
