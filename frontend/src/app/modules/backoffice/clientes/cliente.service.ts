import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { Cliente, ClienteRequest } from './cliente.model';

@Injectable({ providedIn: 'root' })
export class ClienteService {
  private http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/clientes`;

  listar(busca?: string) {
    const params: Record<string, string> = busca ? { busca } : {};
    return this.http.get<Cliente[]>(this.base, { params });
  }

  criar(request: ClienteRequest) {
    return this.http.post<Cliente>(this.base, request);
  }

  atualizar(id: number, request: ClienteRequest) {
    return this.http.put<Cliente>(`${this.base}/${id}`, request);
  }

  alterarAtivo(id: number, ativo: boolean) {
    return this.http.patch<Cliente>(`${this.base}/${id}/ativo?valor=${ativo}`, {});
  }
}
