import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

/**
 * Restringe uma rota a perfis específicos. O perfil VENDEDOR só enxerga a
 * consulta de estoque — qualquer outra rota o redireciona para lá em vez do
 * dashboard, já que ele não tem acesso às demais telas de gestão.
 */
export function perfilGuard(perfisPermitidos: string[]): CanActivateFn {
  return () => {
    const auth = inject(AuthService);
    const router = inject(Router);
    const perfil = auth.perfil();

    if (perfil && perfisPermitidos.includes(perfil)) return true;

    const destino = perfil === 'VENDEDOR' ? '/backoffice/consulta-estoque' : '/backoffice/dashboard';
    return router.createUrlTree([destino]);
  };
}
