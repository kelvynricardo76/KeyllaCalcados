import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import {
  Categoria, Cor, Marca, MarcaRequest, Produto, ProdutoRequest,
  Tamanho, Variacao, VariacaoRequest
} from './produto.model';

@Injectable({ providedIn: 'root' })
export class ProdutoService {
  private http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}`;

  listarProdutos(busca?: string) {
    const params: Record<string, string> = busca ? { busca } : {};
    return this.http.get<Produto[]>(`${this.base}/produtos`, { params });
  }

  buscarProduto(id: number) {
    return this.http.get<Produto>(`${this.base}/produtos/${id}`);
  }

  criarProduto(request: ProdutoRequest) {
    return this.http.post<Produto>(`${this.base}/produtos`, request);
  }

  atualizarProduto(id: number, request: ProdutoRequest) {
    return this.http.put<Produto>(`${this.base}/produtos/${id}`, request);
  }

  alterarAtivoProduto(id: number, ativo: boolean) {
    return this.http.patch<Produto>(`${this.base}/produtos/${id}/ativo?valor=${ativo}`, {});
  }

  listarMarcas() {
    return this.http.get<Marca[]>(`${this.base}/marcas`);
  }

  criarMarca(request: MarcaRequest) {
    return this.http.post<Marca>(`${this.base}/marcas`, request);
  }

  listarCategorias() {
    return this.http.get<Categoria[]>(`${this.base}/categorias`);
  }

  listarTamanhos() {
    return this.http.get<Tamanho[]>(`${this.base}/tamanhos`);
  }

  listarCores() {
    return this.http.get<Cor[]>(`${this.base}/cores`);
  }

  listarVariacoes(produtoId: number) {
    return this.http.get<Variacao[]>(`${this.base}/produtos/${produtoId}/variacoes`);
  }

  listarTodasVariacoes() {
    return this.http.get<Variacao[]>(`${this.base}/variacoes`);
  }

  criarVariacao(produtoId: number, request: VariacaoRequest) {
    return this.http.post<Variacao>(`${this.base}/produtos/${produtoId}/variacoes`, request);
  }

  alterarAtivoVariacao(id: number, ativo: boolean) {
    return this.http.patch<Variacao>(`${this.base}/variacoes/${id}/ativo?valor=${ativo}`, {});
  }

  excluirVariacao(id: number) {
    return this.http.delete<void>(`${this.base}/variacoes/${id}`);
  }
}
