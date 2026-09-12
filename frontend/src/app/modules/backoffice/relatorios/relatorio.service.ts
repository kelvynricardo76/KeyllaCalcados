import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { RelatorioVendas } from './relatorio.model';

@Injectable({ providedIn: 'root' })
export class RelatorioService {
  private http = inject(HttpClient);

  vendasPorPeriodo(inicio: string, fim: string) {
    return this.http.get<RelatorioVendas>(`${environment.apiUrl}/relatorios/vendas`, { params: { inicio, fim } });
  }
}
