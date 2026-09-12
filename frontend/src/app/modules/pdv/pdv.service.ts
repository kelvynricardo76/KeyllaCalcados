import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { CaixaOption, PagamentoInput, Sessao, TipoMovCaixa, Venda } from './pdv.model';

@Injectable({ providedIn: 'root' })
export class PdvService {
  private http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}`;

  listarCaixas() {
    return this.http.get<CaixaOption[]>(`${this.base}/caixas`);
  }

  abrirSessao(caixaId: number, valorAbertura: number) {
    return this.http.post<Sessao>(`${this.base}/caixas/${caixaId}/sessoes`, { valorAbertura });
  }

  buscarSessao(sessaoId: number) {
    return this.http.get<Sessao>(`${this.base}/sessoes/${sessaoId}`);
  }

  fecharSessao(sessaoId: number, valorFechamentoInformado: number, observacoes?: string) {
    return this.http.post<Sessao>(`${this.base}/sessoes/${sessaoId}/fechar`, { valorFechamentoInformado, observacoes });
  }

  lancarMovimentacaoCaixa(sessaoId: number, tipo: TipoMovCaixa, valor: number, descricao: string, categoria?: string) {
    return this.http.post(`${this.base}/sessoes/${sessaoId}/movimentacoes`, { tipo, valor, descricao, categoria });
  }

  abrirVenda(sessaoId: number, lojaId: number, clienteId?: number | null) {
    return this.http.post<Venda>(`${this.base}/vendas`, { sessaoId, lojaId, clienteId });
  }

  listarVendasPorSessao(sessaoId: number) {
    return this.http.get<Venda[]>(`${this.base}/vendas`, { params: { sessaoId: String(sessaoId) } });
  }

  buscarVenda(vendaId: number) {
    return this.http.get<Venda>(`${this.base}/vendas/${vendaId}`);
  }

  adicionarItem(vendaId: number, variacaoId: number, quantidade: number, descontoItem?: number) {
    return this.http.post<Venda>(`${this.base}/vendas/${vendaId}/itens`, { variacaoId, quantidade, descontoItem });
  }

  removerItem(vendaId: number, itemId: number) {
    return this.http.delete<Venda>(`${this.base}/vendas/${vendaId}/itens/${itemId}`);
  }

  finalizarVenda(vendaId: number, descontoGeral: number, pagamentos: PagamentoInput[], fiadoVencimentoDias?: number) {
    return this.http.post<Venda>(`${this.base}/vendas/${vendaId}/finalizar`, { descontoGeral, pagamentos, fiadoVencimentoDias });
  }

  cancelarVenda(vendaId: number) {
    return this.http.post<Venda>(`${this.base}/vendas/${vendaId}/cancelar`, {});
  }
}
