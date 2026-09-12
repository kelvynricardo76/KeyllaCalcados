import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { AjusteEstoqueRequest, EstoqueItem, Loja, Movimentacao } from './estoque.model';

@Injectable({ providedIn: 'root' })
export class EstoqueService {
  private http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}`;

  listar() {
    return this.http.get<EstoqueItem[]>(`${this.base}/estoque`);
  }

  listarMovimentacoes(estoqueId: number) {
    return this.http.get<Movimentacao[]>(`${this.base}/estoque/${estoqueId}/movimentacoes`);
  }

  ajustar(request: AjusteEstoqueRequest) {
    return this.http.post<EstoqueItem>(`${this.base}/estoque/ajuste`, request);
  }

  listarLojas() {
    return this.http.get<Loja[]>(`${this.base}/lojas`);
  }
}
