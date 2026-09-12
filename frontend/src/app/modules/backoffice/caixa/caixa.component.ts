import { Component } from '@angular/core';
import { EmDesenvolvimentoComponent } from '../../../shared/components/em-desenvolvimento/em-desenvolvimento.component';

@Component({
  selector: 'app-caixa',
  standalone: true,
  imports: [EmDesenvolvimentoComponent],
  template: `<app-em-desenvolvimento icon="💰" titulo="Caixa"
    descricao="Abertura e fechamento de sessão, sangrias, suprimentos e conferência de valores." />`
})
export class CaixaComponent {}
