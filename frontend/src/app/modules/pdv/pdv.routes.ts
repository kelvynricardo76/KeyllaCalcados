import { Routes } from '@angular/router';

export const PDV_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./pdv.component').then(m => m.PdvComponent)
  }
];
