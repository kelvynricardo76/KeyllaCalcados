import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ClienteService } from './cliente.service';
import { Cliente } from './cliente.model';
import { ModalComponent } from '../../../shared/components/modal/modal.component';
import { RelatorioService } from '../relatorios/relatorio.service';
import { ClienteRanking } from '../relatorios/relatorio.model';

function formatarData(d: Date): string {
  return d.toISOString().substring(0, 10);
}

@Component({
  selector: 'app-clientes',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, ModalComponent],
  templateUrl: './clientes.component.html',
  styleUrl: './clientes.component.scss'
})
export class ClientesComponent implements OnInit {
  private clienteService = inject(ClienteService);
  private relatorioService = inject(RelatorioService);
  private fb = inject(FormBuilder);

  clientes = signal<Cliente[]>([]);
  loading = signal(true);
  saving = signal(false);
  error = signal('');
  busca = signal('');

  showForm = signal(false);
  editingId = signal<number | null>(null);

  // ── Ranking de clientes ────────────────────────────────────────────
  aba = signal<'LISTA' | 'RANKING'>('LISTA');
  ranking = signal<ClienteRanking[]>([]);
  rankingLoading = signal(false);
  ordenacaoRanking = signal<'QUANTIDADE' | 'TICKET'>('QUANTIDADE');
  rankingInicio: string;
  rankingFim: string;

  private buscaTimeout: ReturnType<typeof setTimeout> | undefined;

  constructor() {
    const hoje = new Date();
    const primeiroDiaMes = new Date(hoje.getFullYear(), hoje.getMonth(), 1);
    this.rankingInicio = formatarData(primeiroDiaMes);
    this.rankingFim = formatarData(hoje);
  }

  form = this.fb.nonNullable.group({
    nome: ['', [Validators.required, Validators.maxLength(120)]],
    cpf: [''],
    cnpj: [''],
    telefone: [''],
    email: ['', Validators.email],
    dataNascimento: [''],
    cep: [''],
    logradouro: [''],
    numero: [''],
    complemento: [''],
    bairro: [''],
    cidade: [''],
    uf: [''],
    limiteFiado: [null as number | null],
    observacoes: ['']
  });

  ngOnInit() {
    this.buscarClientes();
  }

  buscarClientes() {
    this.loading.set(true);
    this.clienteService.listar(this.busca() || undefined).subscribe({
      next: clientes => { this.clientes.set(clientes); this.loading.set(false); },
      error: () => { this.error.set('Não foi possível carregar os clientes.'); this.loading.set(false); }
    });
  }

  onBuscaChange(valor: string) {
    this.busca.set(valor);
    clearTimeout(this.buscaTimeout);
    this.buscaTimeout = setTimeout(() => this.buscarClientes(), 350);
  }

  abrirNovo() {
    this.editingId.set(null);
    this.error.set('');
    this.form.reset({
      nome: '', cpf: '', cnpj: '', telefone: '', email: '', dataNascimento: '',
      cep: '', logradouro: '', numero: '', complemento: '', bairro: '', cidade: '', uf: '',
      limiteFiado: null, observacoes: ''
    });
    this.showForm.set(true);
  }

  editar(cliente: Cliente) {
    this.editingId.set(cliente.id);
    this.error.set('');
    this.form.reset({
      nome: cliente.nome,
      cpf: cliente.cpf ?? '',
      cnpj: cliente.cnpj ?? '',
      telefone: cliente.telefone ?? '',
      email: cliente.email ?? '',
      dataNascimento: cliente.dataNascimento ?? '',
      cep: cliente.cep ?? '',
      logradouro: cliente.logradouro ?? '',
      numero: cliente.numero ?? '',
      complemento: cliente.complemento ?? '',
      bairro: cliente.bairro ?? '',
      cidade: cliente.cidade ?? '',
      uf: cliente.uf ?? '',
      limiteFiado: cliente.limiteFiado ?? null,
      observacoes: cliente.observacoes ?? ''
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
    const payload = this.form.getRawValue();
    const id = this.editingId();

    const request$ = id ? this.clienteService.atualizar(id, payload) : this.clienteService.criar(payload);
    request$.subscribe({
      next: () => {
        this.saving.set(false);
        this.showForm.set(false);
        this.buscarClientes();
      },
      error: err => {
        this.saving.set(false);
        this.error.set(err.error?.message ?? 'Não foi possível salvar o cliente.');
      }
    });
  }

  toggleAtivo(cliente: Cliente) {
    this.clienteService.alterarAtivo(cliente.id, !cliente.ativo).subscribe(() => this.buscarClientes());
  }

  get nomeCtrl() { return this.form.controls.nome; }

  // ── Ranking de clientes ────────────────────────────────────────────
  mudarAba(aba: 'LISTA' | 'RANKING') {
    this.aba.set(aba);
    if (aba === 'RANKING' && this.ranking().length === 0) {
      this.buscarRanking();
    }
  }

  buscarRanking() {
    this.rankingLoading.set(true);
    this.relatorioService.clientesRanking(this.rankingInicio, this.rankingFim).subscribe({
      next: lista => { this.ranking.set(lista); this.rankingLoading.set(false); },
      error: () => { this.rankingLoading.set(false); }
    });
  }

  rankingOrdenado(): ClienteRanking[] {
    const criterio = this.ordenacaoRanking();
    return [...this.ranking()].sort((a, b) =>
      criterio === 'QUANTIDADE' ? b.quantidadeCompras - a.quantidadeCompras : b.ticketMedio - a.ticketMedio);
  }
}
