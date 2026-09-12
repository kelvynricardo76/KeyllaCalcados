import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

export interface Loja {
  id: number;
  nome: string;
  ativo: boolean;
}

@Injectable({ providedIn: 'root' })
export class LojaService {
  private http = inject(HttpClient);

  listar() {
    return this.http.get<Loja[]>(`${environment.apiUrl}/lojas`);
  }
}
