import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { MovimentacaoCaixa, SessaoCaixa } from './caixa.model';

@Injectable({ providedIn: 'root' })
export class CaixaService {
  private http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}`;

  listarSessoes() {
    return this.http.get<SessaoCaixa[]>(`${this.base}/sessoes`);
  }

  listarMovimentacoes(sessaoId: number) {
    return this.http.get<MovimentacaoCaixa[]>(`${this.base}/sessoes/${sessaoId}/movimentacoes`);
  }
}
