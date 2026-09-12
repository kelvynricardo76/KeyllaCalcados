import { Routes } from '@angular/router';
import { perfilGuard } from '../../core/auth/perfil.guard';

const NAO_VENDEDOR = ['ADMIN', 'GERENTE', 'CAIXA', 'ESTOQUISTA'];

export const BACKOFFICE_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./backoffice-layout.component').then(m => m.BackofficeLayoutComponent),
    children: [
      { path: '',            redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard',  canActivate: [perfilGuard(NAO_VENDEDOR)],
        loadComponent: () => import('./dashboard/dashboard.component').then(m => m.DashboardComponent) },
      { path: 'produtos',   canActivate: [perfilGuard(NAO_VENDEDOR)],
        loadComponent: () => import('./produtos/produtos.component').then(m => m.ProdutosComponent) },
      { path: 'estoque',    canActivate: [perfilGuard(NAO_VENDEDOR)],
        loadComponent: () => import('./estoque/estoque.component').then(m => m.EstoqueComponent) },
      { path: 'consulta-estoque',
        loadComponent: () => import('./consulta-estoque/consulta-estoque.component').then(m => m.ConsultaEstoqueComponent) },
      { path: 'clientes',   canActivate: [perfilGuard(NAO_VENDEDOR)],
        loadComponent: () => import('./clientes/clientes.component').then(m => m.ClientesComponent) },
      { path: 'fornecedores', canActivate: [perfilGuard(['ADMIN', 'GERENTE', 'ESTOQUISTA'])],
        loadComponent: () => import('./fornecedores/fornecedores.component').then(m => m.FornecedoresComponent) },
      { path: 'trocas',     canActivate: [perfilGuard(['ADMIN', 'GERENTE', 'CAIXA'])],
        loadComponent: () => import('./trocas/trocas.component').then(m => m.TrocasComponent) },
      { path: 'caixa',      canActivate: [perfilGuard(['ADMIN', 'GERENTE'])],
        loadComponent: () => import('./caixa/caixa.component').then(m => m.CaixaComponent) },
      { path: 'fiado',      canActivate: [perfilGuard(NAO_VENDEDOR)],
        loadComponent: () => import('./fiado/fiado.component').then(m => m.FiadoComponent) },
      { path: 'relatorios', canActivate: [perfilGuard(['ADMIN', 'GERENTE'])],
        loadComponent: () => import('./relatorios/relatorios.component').then(m => m.RelatoriosComponent) },
      { path: 'funcionarios', canActivate: [perfilGuard(['ADMIN', 'GERENTE'])],
        loadComponent: () => import('./funcionarios/funcionarios.component').then(m => m.FuncionariosComponent) },
      { path: 'usuarios',   canActivate: [perfilGuard(['ADMIN'])],
        loadComponent: () => import('./usuarios/usuarios.component').then(m => m.UsuariosComponent) },
    ]
  }
];
