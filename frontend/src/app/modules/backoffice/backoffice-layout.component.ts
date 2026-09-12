import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';

interface NavItem {
  label: string;
  icon: string;
  route: string;
  perfis?: string[];  // Se vazio = todos podem ver
}

@Component({
  selector: 'app-backoffice-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './backoffice-layout.component.html',
  styleUrl: './backoffice-layout.component.scss'
})
export class BackofficeLayoutComponent {
  sidebarCollapsed = signal(false);
  currentYear = new Date().getFullYear();

  private readonly NAO_VENDEDOR = ['ADMIN', 'GERENTE', 'CAIXA', 'ESTOQUISTA'];

  navItems: NavItem[] = [
    { label: 'Dashboard',   icon: '📊', route: '/backoffice/dashboard', perfis: this.NAO_VENDEDOR },
    { label: 'PDV / Caixa', icon: '🏪', route: '/pdv', perfis: this.NAO_VENDEDOR },
    { label: 'Produtos',    icon: '👟', route: '/backoffice/produtos', perfis: this.NAO_VENDEDOR },
    { label: 'Estoque',     icon: '📦', route: '/backoffice/estoque', perfis: this.NAO_VENDEDOR },
    { label: 'Consulta de Estoque', icon: '🔍', route: '/backoffice/consulta-estoque' },
    { label: 'Clientes',    icon: '👥', route: '/backoffice/clientes', perfis: this.NAO_VENDEDOR },
    { label: 'Fornecedores', icon: '🚚', route: '/backoffice/fornecedores', perfis: ['ADMIN','GERENTE','ESTOQUISTA'] },
    { label: 'Trocas/Devoluções', icon: '🔄', route: '/backoffice/trocas', perfis: ['ADMIN','GERENTE','CAIXA'] },
    { label: 'Caixa',       icon: '💰', route: '/backoffice/caixa', perfis: ['ADMIN','GERENTE'] },
    { label: 'Fiado',       icon: '📋', route: '/backoffice/fiado', perfis: this.NAO_VENDEDOR },
    { label: 'Relatórios',  icon: '📈', route: '/backoffice/relatorios', perfis: ['ADMIN','GERENTE'] },
    { label: 'Funcionários', icon: '🧑‍💼', route: '/backoffice/funcionarios', perfis: ['ADMIN','GERENTE'] },
    { label: 'Usuários',    icon: '⚙️',  route: '/backoffice/usuarios', perfis: ['ADMIN'] },
  ];

  constructor(public auth: AuthService) {}

  visibleNavItems(): NavItem[] {
    const perfil = this.auth.perfil() ?? '';
    return this.navItems.filter(item =>
      !item.perfis || item.perfis.includes(perfil)
    );
  }

  toggleSidebar() {
    this.sidebarCollapsed.update(v => !v);
  }
}
