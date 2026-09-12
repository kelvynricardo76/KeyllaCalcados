import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { AtualizarUsuarioRequest, CriarUsuarioRequest, Usuario } from './usuario.model';

@Injectable({ providedIn: 'root' })
export class UsuarioService {
  private http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/usuarios`;

  listar() {
    return this.http.get<Usuario[]>(this.base);
  }

  criar(request: CriarUsuarioRequest) {
    return this.http.post<Usuario>(this.base, request);
  }

  atualizar(id: number, request: AtualizarUsuarioRequest) {
    return this.http.put<Usuario>(`${this.base}/${id}`, request);
  }

  alterarAtivo(id: number, ativo: boolean) {
    return this.http.patch<Usuario>(`${this.base}/${id}/ativo?valor=${ativo}`, {});
  }
}
