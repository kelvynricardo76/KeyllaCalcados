import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';

/**
 * Modal padrão do sistema: cabeçalho e rodapé fixos, corpo rolável.
 * Corrige o bug de formulários "cortados" quando o conteúdo era maior que
 * a tela — antes o botão de salvar ficava dentro da área de rolagem e podia
 * nunca aparecer visivelmente sem o usuário perceber que precisava rolar.
 */
@Component({
  selector: 'app-modal',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="modal-overlay" (click)="onOverlayClick()">
      <div class="modal-panel" [style.max-width.px]="maxWidth" (click)="$event.stopPropagation()">
        <div class="modal-header">
          <h3>{{ titulo }}</h3>
          <button type="button" class="btn btn-icon btn-outline" (click)="fechar.emit()" aria-label="Fechar">✕</button>
        </div>
        <div class="modal-body">
          <ng-content></ng-content>
        </div>
        <div class="modal-footer">
          <ng-content select="[modal-footer]"></ng-content>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .modal-overlay {
      position: fixed;
      inset: 0;
      background: rgba(13, 27, 75, 0.45);
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 24px 20px;
      z-index: 200;
    }

    .modal-panel {
      background: var(--surface-card);
      border-radius: var(--border-radius-lg);
      box-shadow: var(--shadow-lg);
      width: 100%;
      max-height: min(90vh, 800px);
      display: flex;
      flex-direction: column;
      overflow: hidden;
    }

    .modal-header {
      flex: 0 0 auto;
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 20px 24px 16px;
      border-bottom: 1px solid var(--border-color);

      h3 { margin: 0; font-size: 1.1rem; color: var(--text-primary); }
    }

    .modal-body {
      flex: 1 1 auto;
      overflow-y: auto;
      padding: 24px;
      min-height: 0;
    }

    .modal-footer {
      flex: 0 0 auto;
      display: flex;
      justify-content: flex-end;
      gap: 10px;
      padding: 16px 24px;
      border-top: 1px solid var(--border-color);
      background: var(--surface-bg);
    }

    .modal-footer:empty { display: none; }
  `]
})
export class ModalComponent {
  @Input() titulo = '';
  @Input() maxWidth = 640;
  @Input() fecharAoClicarFora = true;
  @Output() fechar = new EventEmitter<void>();

  onOverlayClick() {
    if (this.fecharAoClicarFora) {
      this.fechar.emit();
    }
  }
}
