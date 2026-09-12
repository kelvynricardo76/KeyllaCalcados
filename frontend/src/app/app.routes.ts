import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  // Rota raiz → redireciona para login ou PDV
  { path: '', redirectTo: '/auth/login', pathMatch: 'full' },

  // ── Autenticação ──────────────────────────────────────────────
  {
    path: 'auth',
    loadChildren: () => import('./modules/auth/auth.routes').then(m => m.AUTH_ROUTES)
  },

  // ── PDV (Ponto de Venda) — requer login ────────────────────────
  {
    path: 'pdv',
    canActivate: [authGuard],
    loadChildren: () => import('./modules/pdv/pdv.routes').then(m => m.PDV_ROUTES)
  },

  // ── Backoffice (Gestão) — requer login ─────────────────────────
  {
    path: 'backoffice',
    canActivate: [authGuard],
    loadChildren: () => import('./modules/backoffice/backoffice.routes').then(m => m.BACKOFFICE_ROUTES)
  },

  // Fallback
  { path: '**', redirectTo: '/auth/login' }
];
