import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { FiadoService } from './fiado.service';
import { Fiado, Pagamento } from './fiado.model';
import { ClienteService } from '../clientes/cliente.service';
import { Cliente } from '../clientes/cliente.model';
import { LojaService, Loja } from '../../../core/services/loja.service';
import { ModalComponent } from '../../../shared/components/modal/modal.component';

@Component({
  selector: 'app-fiado',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, ModalComponent],
  templateUrl: './fiado.component.html',
  styleUrl: './fiado.component.scss'
})
export class FiadoComponent implements OnInit {
  private fiadoService = inject(FiadoService);
  private clienteService = inject(ClienteService);
  private lojaService = inject(LojaService);
  private fb = inject(FormBuilder);

  fiados = signal<Fiado[]>([]);
  clientes = signal<Cliente[]>([]);
  lojas = signal<Loja[]>([]);
  pagamentos = signal<Pagamento[]>([]);

  loading = signal(true);
  saving = signal(false);
  error = signal('');
  filtro = signal<'TODOS' | 'ABERTOS' | 'VENCIDOS'>('ABERTOS');

  showForm = signal(false);
  showPagamento = signal(false);
  fiadoSelecionado = signal<Fiado | null>(null);

  form = this.fb.nonNullable.group({
    clienteId: [null as number | null, Validators.required],
    lojaId: [null as number | null, Validators.required],
    valorTotal: [0, [Validators.required, Validators.min(0.01)]],
    dataVencimento: ['', Validators.required],
    observacoes: ['']
  });

  pagamentoForm = this.fb.nonNullable.group({
    valor: [0, [Validators.required, Validators.min(0.01)]],
    formaPagamento: ['DINHEIRO', Validators.required],
    observacoes: ['']
  });

  formasPagamento = ['DINHEIRO', 'PIX', 'CARTAO', 'OUTRO'];

  ngOnInit() {
    this.clienteService.listar().subscribe(c => this.clientes.set(c));
    this.lojaService.listar().subscribe(lojas => {
      this.lojas.set(lojas);
      if (lojas.length > 0) this.form.controls.lojaId.setValue(lojas[0].id);
    });
    this.carregar();
  }

  private carregar() {
    this.loading.set(true);
    this.fiadoService.listar().subscribe({
      next: fiados => { this.fiados.set(fiados); this.loading.set(false); },
      error: () => { this.error.set('Não foi possível carregar os fiados.'); this.loading.set(false); }
    });
  }

  fiadosFiltrados(): Fiado[] {
    const filtro = this.filtro();
    return this.fiados().filter(f => {
      if (filtro === 'TODOS') return true;
      if (filtro === 'VENCIDOS') return f.vencido;
      return f.status !== 'PAGO';
    });
  }

  abrirNovo() {
    this.error.set('');
    this.form.reset({
      clienteId: null,
      lojaId: this.lojas()[0]?.id ?? null,
      valorTotal: 0,
      dataVencimento: '',
      observacoes: ''
    });
    this.showForm.set(true);
  }

  fecharForm() {
    this.showForm.set(false);
  }

  salvar() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving.set(true);
    this.error.set('');
    const valores = this.form.getRawValue();
    this.fiadoService.criar({
      clienteId: valores.clienteId!,
      lojaId: valores.lojaId!,
      valorTotal: valores.valorTotal,
      dataVencimento: valores.dataVencimento,
      observacoes: valores.observacoes
    }).subscribe({
      next: () => { this.saving.set(false); this.showForm.set(false); this.carregar(); },
      error: err => { this.saving.set(false); this.error.set(err.error?.message ?? 'Não foi possível lançar o fiado.'); }
    });
  }

  abrirPagamento(fiado: Fiado) {
    this.error.set('');
    this.fiadoSelecionado.set(fiado);
    this.pagamentos.set([]);
    this.pagamentoForm.reset({ valor: fiado.valorRestante, formaPagamento: 'DINHEIRO', observacoes: '' });
    this.showPagamento.set(true);
    this.fiadoService.listarPagamentos(fiado.id).subscribe(p => this.pagamentos.set(p));
  }

  fecharPagamento() {
    this.showPagamento.set(false);
  }

  confirmarPagamento() {
    const fiado = this.fiadoSelecionado();
    if (!fiado || this.pagamentoForm.invalid) { this.pagamentoForm.markAllAsTouched(); return; }
    this.saving.set(true);
    this.error.set('');
    this.fiadoService.registrarPagamento(fiado.id, this.pagamentoForm.getRawValue()).subscribe({
      next: () => { this.saving.set(false); this.showPagamento.set(false); this.carregar(); },
      error: err => { this.saving.set(false); this.error.set(err.error?.message ?? 'Não foi possível registrar o pagamento.'); }
    });
  }
}
