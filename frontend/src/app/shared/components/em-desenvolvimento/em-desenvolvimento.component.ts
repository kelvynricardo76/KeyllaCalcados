import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

/**
 * Placeholder reutilizável para módulos ainda não implementados.
 * Mantém a navegação e o layout funcionais enquanto cada módulo é construído.
 */
@Component({
  selector: 'app-em-desenvolvimento',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="card empty-state animate-fade-in">
      <div class="card-body text-center">
        <div class="empty-icon">{{ icon }}</div>
        <h2>{{ titulo }}</h2>
        <p class="text-muted">{{ descricao }}</p>
        <span class="badge badge-info">Em desenvolvimento</span>
      </div>
    </div>
  `,
  styles: [`
    .empty-state { max-width: 480px; margin: 48px auto; }
    .empty-icon { font-size: 3rem; margin-bottom: 12px; }
    h2 { margin-bottom: 8px; }
    p { margin-bottom: 16px; }
  `]
})
export class EmDesenvolvimentoComponent {
  @Input() icon = '🚧';
  @Input() titulo = 'Módulo em desenvolvimento';
  @Input() descricao = 'Esta funcionalidade ainda está sendo construída.';
}
