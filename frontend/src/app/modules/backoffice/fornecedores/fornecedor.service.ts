import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { Compra, CompraRequest, Fornecedor, FornecedorRequest } from './fornecedor.model';

@Injectable({ providedIn: 'root' })
export class FornecedorService {
  private http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}`;

  listarFornecedores() {
    return this.http.get<Fornecedor[]>(`${this.base}/fornecedores`);
  }

  criarFornecedor(request: FornecedorRequest) {
    return this.http.post<Fornecedor>(`${this.base}/fornecedores`, request);
  }

  atualizarFornecedor(id: number, request: FornecedorRequest) {
    return this.http.put<Fornecedor>(`${this.base}/fornecedores/${id}`, request);
  }

  alterarAtivoFornecedor(id: number, ativo: boolean) {
    return this.http.patch<Fornecedor>(`${this.base}/fornecedores/${id}/ativo?valor=${ativo}`, {});
  }

  listarCompras() {
    return this.http.get<Compra[]>(`${this.base}/compras`);
  }

  criarCompra(request: CompraRequest) {
    return this.http.post<Compra>(`${this.base}/compras`, request);
  }

  receberCompra(id: number) {
    return this.http.post<Compra>(`${this.base}/compras/${id}/receber`, {});
  }

  cancelarCompra(id: number) {
    return this.http.post<Compra>(`${this.base}/compras/${id}/cancelar`, {});
  }
}
