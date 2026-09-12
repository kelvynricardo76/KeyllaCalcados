import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { Devolucao, DevolucaoRequest } from './devolucao.model';

@Injectable({ providedIn: 'root' })
export class DevolucaoService {
  private http = inject(HttpClient);

  listarPorVenda(vendaId: number) {
    return this.http.get<Devolucao[]>(`${environment.apiUrl}/vendas/${vendaId}/devolucoes`);
  }

  registrar(vendaId: number, request: DevolucaoRequest) {
    return this.http.post<Devolucao>(`${environment.apiUrl}/vendas/${vendaId}/devolucoes`, request);
  }
}
