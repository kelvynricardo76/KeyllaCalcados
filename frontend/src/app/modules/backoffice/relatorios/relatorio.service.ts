import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { FuncionarioRanking, ProdutoVencendo, RelatorioVendas } from './relatorio.model';

@Injectable({ providedIn: 'root' })
export class RelatorioService {
  private http = inject(HttpClient);

  vendasPorPeriodo(inicio: string, fim: string) {
    return this.http.get<RelatorioVendas>(`${environment.apiUrl}/relatorios/vendas`, { params: { inicio, fim } });
  }

  funcionarios(inicio: string, fim: string) {
    return this.http.get<FuncionarioRanking[]>(`${environment.apiUrl}/relatorios/funcionarios`, { params: { inicio, fim } });
  }

  produtosVencendo(dias = 30) {
    return this.http.get<ProdutoVencendo[]>(`${environment.apiUrl}/relatorios/produtos-vencendo`, { params: { dias } });
  }
}
