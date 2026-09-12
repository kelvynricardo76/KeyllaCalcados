import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { LojaService } from '../../core/services/loja.service';
import { PdvService } from './pdv.service';
import { CaixaOption, FormaPagamento, PagamentoInput, Sessao, Venda } from './pdv.model';
import { ProdutoService } from '../backoffice/produtos/produto.service';
import { Variacao } from '../backoffice/produtos/produto.model';
import { ClienteService } from '../backoffice/clientes/cliente.service';
import { Cliente } from '../backoffice/clientes/cliente.model';

@Component({
  selector: 'app-pdv',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './pdv.component.html',
  styleUrl: './pdv.component.scss'
})
export class PdvComponent implements OnInit {
  private pdvService = inject(PdvService);
  private lojaService = inject(LojaService);
  private produtoService = inject(ProdutoService);
  private clienteService = inject(ClienteService);
  auth = inject(AuthService);

  loading = signal(true);
  error = signal('');

  caixas = signal<CaixaOption[]>([]);
  caixaSelecionado = signal<CaixaOption | null>(null);
  valorAbertura = 0;

  sessao = signal<Sessao | null>(null);
  venda = signal<Venda | null>(null);

  variacoes = signal<Variacao[]>([]);
  clientes = signal<Cliente[]>([]);
  buscaProduto = signal('');

  showFechamento = signal(false);
  valorFechamentoInformado = 0;
  observacoesFechamento = '';

  showSangria = signal(false);
  sangriaTipo: 'SANGRIA' | 'SUPRIMENTO' = 'SANGRIA';
  sangriaValor = 0;
  sangriaDescricao = '';

  showPagamento = signal(false);
  descontoGeral = 0;
  pagamentos = signal<PagamentoInput[]>([]);
  novoPagamentoForma: FormaPagamento = 'DINHEIRO';
  novoPagamentoValor = 0;

  ngOnInit() {
    this.produtoService.listarTodasVariacoes().subscribe(v => this.variacoes.set(v));
    this.clienteService.listar().subscribe(c => this.clientes.set(c));
    this.carregarCaixas();
  }

  private carregarCaixas() {
    this.loading.set(true);
    this.pdvService.listarCaixas().subscribe({
      next: caixas => { this.caixas.set(caixas); this.loading.set(false); },
      error: () => { this.error.set('Não foi possível carregar os caixas.'); this.loading.set(false); }
    });
  }

  variacoesFiltradas(): Variacao[] {
    const termo = this.buscaProduto().trim().toLowerCase();
    if (!termo) return this.variacoes().slice(0, 30);
    return this.variacoes().filter(v =>
      (v.produtoNome ?? '').toLowerCase().includes(termo) ||
      (v.sku ?? '').toLowerCase().includes(termo)
    ).slice(0, 30);
  }

  // ── Seleção de caixa / abertura de sessão ───────────────────────
  selecionarCaixa(caixa: CaixaOption) {
    this.error.set('');
    if (caixa.sessaoAbertaId) {
      this.caixaSelecionado.set(caixa);
      this.pdvService.buscarSessao(caixa.sessaoAbertaId).subscribe(sessao => {
        this.sessao.set(sessao);
        this.retomarOuCriarVenda();
      });
    } else {
      this.caixaSelecionado.set(caixa);
      this.valorAbertura = 0;
    }
  }

  cancelarSelecaoCaixa() {
    this.caixaSelecionado.set(null);
  }

  confirmarAberturaCaixa() {
    const caixa = this.caixaSelecionado();
    if (!caixa) return;
    this.error.set('');
    this.pdvService.abrirSessao(caixa.id, this.valorAbertura).subscribe({
      next: sessao => { this.sessao.set(sessao); this.retomarOuCriarVenda(); },
      error: err => this.error.set(err.error?.message ?? 'Não foi possível abrir o caixa.')
    });
  }

  private retomarOuCriarVenda() {
    const sessao = this.sessao();
    const caixa = this.caixaSelecionado();
    if (!sessao || !caixa) return;

    this.pdvService.listarVendasPorSessao(sessao.id).subscribe(vendas => {
      const aberta = vendas.find(v => v.status === 'ABERTA');
      if (aberta) {
        this.venda.set(aberta);
      } else {
        this.pdvService.abrirVenda(sessao.id, caixa.lojaId, null).subscribe(v => this.venda.set(v));
      }
    });
  }

  // ── Carrinho ─────────────────────────────────────────────────────
  adicionarAoCarrinho(v: Variacao) {
    const venda = this.venda();
    if (!venda) return;
    this.error.set('');
    this.pdvService.adicionarItem(venda.id, v.id, 1).subscribe({
      next: atualizada => this.venda.set(atualizada),
      error: err => this.error.set(err.error?.message ?? 'Não foi possível adicionar o item.')
    });
  }

  removerItem(itemId: number) {
    const venda = this.venda();
    if (!venda) return;
    this.pdvService.removerItem(venda.id, itemId).subscribe(atualizada => this.venda.set(atualizada));
  }

  selecionarCliente(clienteId: string) {
    const venda = this.venda();
    if (!venda) return;
    const id = clienteId ? Number(clienteId) : null;
    // Cliente é vinculado apenas ao finalizar (necessário para fiado); aqui guardamos localmente.
    this.clienteSelecionadoId.set(id);
  }

  clienteSelecionadoId = signal<number | null>(null);

  nomeClienteSelecionado(): string {
    const id = this.clienteSelecionadoId();
    return this.clientes().find(c => c.id === id)?.nome ?? 'Consumidor final';
  }

  // ── Pagamento / finalização ──────────────────────────────────────
  abrirPagamento() {
    this.error.set('');
    this.descontoGeral = 0;
    this.pagamentos.set([]);
    this.novoPagamentoForma = 'DINHEIRO';
    this.novoPagamentoValor = this.totalAPagar();
    this.showPagamento.set(true);
  }

  fecharPagamento() {
    this.showPagamento.set(false);
  }

  totalAPagar(): number {
    const venda = this.venda();
    if (!venda) return 0;
    return Math.max(0, venda.subtotal - this.descontoGeral);
  }

  totalPago(): number {
    return this.pagamentos().reduce((soma, p) => soma + p.valor, 0);
  }

  faltaPagar(): number {
    return Math.max(0, this.totalAPagar() - this.totalPago());
  }

  trocoPrevisto(): number {
    return Math.max(0, this.totalPago() - this.totalAPagar());
  }

  adicionarPagamento() {
    if (this.novoPagamentoValor <= 0) return;
    this.pagamentos.update(lista => [...lista, {
      forma: this.novoPagamentoForma,
      valor: this.novoPagamentoValor
    }]);
    this.novoPagamentoValor = this.faltaPagar();
  }

  removerPagamento(index: number) {
    this.pagamentos.update(lista => lista.filter((_, i) => i !== index));
  }

  confirmarFinalizacao() {
    const venda = this.venda();
    if (!venda) return;
    this.error.set('');

    const temFiado = this.pagamentos().some(p => p.forma === 'FIADO');
    if (temFiado && !this.clienteSelecionadoId()) {
      this.error.set('Selecione um cliente para vendas com fiado.');
      return;
    }

    const finalizar = () => {
      this.pdvService.finalizarVenda(venda.id, this.descontoGeral, this.pagamentos()).subscribe({
        next: fechada => {
          this.venda.set(fechada);
          this.showPagamento.set(false);
          setTimeout(() => this.iniciarNovaVenda(), 1500);
        },
        error: err => this.error.set(err.error?.message ?? 'Não foi possível finalizar a venda.')
      });
    };

    if (this.clienteSelecionadoId() && venda.clienteId !== this.clienteSelecionadoId()) {
      // Reabre a venda associando o cliente escolhido antes de finalizar (fiado exige cliente).
      this.pdvService.abrirVenda(venda.sessaoId, venda.lojaId, this.clienteSelecionadoId()).subscribe(nova => {
        this.migrarItensEFinalizar(venda, nova, finalizar);
      });
    } else {
      finalizar();
    }
  }

  private migrarItensEFinalizar(vendaAntiga: Venda, vendaNova: Venda, callback: () => void) {
    // Como a venda antiga não tinha cliente, recriamos os itens na nova venda (já com cliente) e cancelamos a antiga.
    const itens = [...vendaAntiga.itens];
    const adicionarProximo = (index: number) => {
      if (index >= itens.length) {
        this.venda.set(vendaNova);
        this.pdvService.cancelarVenda(vendaAntiga.id).subscribe();
        this.pdvService.buscarVenda(vendaNova.id).subscribe(atualizada => {
          this.venda.set(atualizada);
          callback();
        });
        return;
      }
      const item = itens[index];
      this.pdvService.adicionarItem(vendaNova.id, item.variacaoId, item.quantidade, item.descontoItem).subscribe(() => {
        adicionarProximo(index + 1);
      });
    };
    adicionarProximo(0);
  }

  iniciarNovaVenda() {
    const sessao = this.sessao();
    const caixa = this.caixaSelecionado();
    if (!sessao || !caixa) return;
    this.clienteSelecionadoId.set(null);
    this.pdvService.abrirVenda(sessao.id, caixa.lojaId, null).subscribe(v => this.venda.set(v));
  }

  // ── Sangria / suprimento ─────────────────────────────────────────
  abrirSangria(tipo: 'SANGRIA' | 'SUPRIMENTO') {
    this.sangriaTipo = tipo;
    this.sangriaValor = 0;
    this.sangriaDescricao = '';
    this.showSangria.set(true);
  }

  fecharSangria() {
    this.showSangria.set(false);
  }

  confirmarSangria() {
    const sessao = this.sessao();
    if (!sessao || this.sangriaValor <= 0 || !this.sangriaDescricao) return;
    this.pdvService.lancarMovimentacaoCaixa(sessao.id, this.sangriaTipo, this.sangriaValor, this.sangriaDescricao)
      .subscribe(() => this.showSangria.set(false));
  }

  // ── Fechamento de caixa ──────────────────────────────────────────
  abrirFechamento() {
    this.valorFechamentoInformado = 0;
    this.observacoesFechamento = '';
    this.showFechamento.set(true);
  }

  fecharModalFechamento() {
    this.showFechamento.set(false);
  }

  confirmarFechamentoCaixa() {
    const sessao = this.sessao();
    if (!sessao) return;
    this.pdvService.fecharSessao(sessao.id, this.valorFechamentoInformado, this.observacoesFechamento).subscribe(fechada => {
      this.sessao.set(null);
      this.venda.set(null);
      this.caixaSelecionado.set(null);
      this.showFechamento.set(false);
      this.carregarCaixas();
    });
  }
}
