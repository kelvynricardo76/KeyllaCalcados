import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { Fiado, FiadoRequest, Pagamento, PagamentoRequest } from './fiado.model';

@Injectable({ providedIn: 'root' })
export class FiadoService {
  private http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/fiados`;

  listar() {
    return this.http.get<Fiado[]>(this.base);
  }

  listarPagamentos(fiadoId: number) {
    return this.http.get<Pagamento[]>(`${this.base}/${fiadoId}/pagamentos`);
  }

  criar(request: FiadoRequest) {
    return this.http.post<Fiado>(this.base, request);
  }

  registrarPagamento(fiadoId: number, request: PagamentoRequest) {
    return this.http.post<Fiado>(`${this.base}/${fiadoId}/pagamentos`, request);
  }
}
