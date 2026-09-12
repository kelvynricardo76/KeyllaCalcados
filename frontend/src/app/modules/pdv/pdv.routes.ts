import { Routes } from '@angular/router';
import { perfilGuard } from '../../core/auth/perfil.guard';

export const PDV_ROUTES: Routes = [
  {
    path: '',
    canActivate: [perfilGuard(['ADMIN', 'GERENTE', 'CAIXA', 'ESTOQUISTA'])],
    loadComponent: () => import('./pdv.component').then(m => m.PdvComponent)
  }
];
