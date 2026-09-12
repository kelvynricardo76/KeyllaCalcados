import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { UsuarioService } from './usuario.service';
import { PerfilUsuario, Usuario } from './usuario.model';

@Component({
  selector: 'app-usuarios',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './usuarios.component.html',
  styleUrl: './usuarios.component.scss'
})
export class UsuariosComponent implements OnInit {
  private usuarioService = inject(UsuarioService);
  private fb = inject(FormBuilder);

  usuarios = signal<Usuario[]>([]);
  loading = signal(true);
  saving = signal(false);
  error = signal('');

  showForm = signal(false);
  editingId = signal<number | null>(null);

  perfis: PerfilUsuario[] = ['ADMIN', 'GERENTE', 'CAIXA', 'ESTOQUISTA'];

  form = this.fb.nonNullable.group({
    nome: ['', [Validators.required, Validators.maxLength(120)]],
    email: ['', [Validators.required, Validators.email]],
    perfil: ['CAIXA' as PerfilUsuario, Validators.required],
    senha: ['']
  });

  ngOnInit() {
    this.carregar();
  }

  private carregar() {
    this.loading.set(true);
    this.usuarioService.listar().subscribe({
      next: usuarios => { this.usuarios.set(usuarios); this.loading.set(false); },
      error: () => { this.error.set('Não foi possível carregar os usuários.'); this.loading.set(false); }
    });
  }

  abrirNovo() {
    this.editingId.set(null);
    this.error.set('');
    this.form.reset({ nome: '', email: '', perfil: 'CAIXA', senha: '' });
    this.form.controls.senha.setValidators([Validators.required, Validators.minLength(6)]);
    this.form.controls.senha.updateValueAndValidity();
    this.showForm.set(true);
  }

  editar(usuario: Usuario) {
    this.editingId.set(usuario.id);
    this.error.set('');
    this.form.reset({ nome: usuario.nome, email: usuario.email, perfil: usuario.perfil, senha: '' });
    this.form.controls.senha.setValidators([Validators.minLength(6)]);
    this.form.controls.senha.updateValueAndValidity();
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
    const id = this.editingId();

    const request$ = id
      ? this.usuarioService.atualizar(id, { nome: valores.nome, email: valores.email, perfil: valores.perfil, novaSenha: valores.senha || null })
      : this.usuarioService.criar({ nome: valores.nome, email: valores.email, perfil: valores.perfil, senha: valores.senha });

    request$.subscribe({
      next: () => { this.saving.set(false); this.showForm.set(false); this.carregar(); },
      error: err => { this.saving.set(false); this.error.set(err.error?.message ?? 'Não foi possível salvar o usuário.'); }
    });
  }

  toggleAtivo(usuario: Usuario) {
    this.usuarioService.alterarAtivo(usuario.id, !usuario.ativo).subscribe(() => this.carregar());
  }

  get nomeCtrl() { return this.form.controls.nome; }
  get emailCtrl() { return this.form.controls.email; }
  get senhaCtrl() { return this.form.controls.senha; }
}
