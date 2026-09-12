import { Routes } from '@angular/router';

export const BACKOFFICE_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./backoffice-layout.component').then(m => m.BackofficeLayoutComponent),
    children: [
      { path: '',            redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard',  loadComponent: () => import('./dashboard/dashboard.component').then(m => m.DashboardComponent) },
      { path: 'produtos',   loadComponent: () => import('./produtos/produtos.component').then(m => m.ProdutosComponent) },
      { path: 'estoque',    loadComponent: () => import('./estoque/estoque.component').then(m => m.EstoqueComponent) },
      { path: 'clientes',   loadComponent: () => import('./clientes/clientes.component').then(m => m.ClientesComponent) },
      { path: 'fornecedores', loadComponent: () => import('./fornecedores/fornecedores.component').then(m => m.FornecedoresComponent) },
      { path: 'trocas',     loadComponent: () => import('./trocas/trocas.component').then(m => m.TrocasComponent) },
      { path: 'caixa',      loadComponent: () => import('./caixa/caixa.component').then(m => m.CaixaComponent) },
      { path: 'fiado',      loadComponent: () => import('./fiado/fiado.component').then(m => m.FiadoComponent) },
      { path: 'relatorios', loadComponent: () => import('./relatorios/relatorios.component').then(m => m.RelatoriosComponent) },
      { path: 'usuarios',   loadComponent: () => import('./usuarios/usuarios.component').then(m => m.UsuariosComponent) },
    ]
  }
];
