import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  private fb     = inject(FormBuilder);
  private auth   = inject(AuthService);
  private router = inject(Router);

  form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    senha: ['', [Validators.required, Validators.minLength(6)]]
  });

  // Signals para estado da UI
  loading     = signal(false);
  showSenha   = signal(false);
  error       = signal('');
  activeTab   = signal<'email' | 'pin'>('email');

  // PIN rápido para troca de operador no caixa
  pinDigits   = signal<string[]>([]);
  PIN_LENGTH  = 6;

  currentYear = new Date().getFullYear();

  onSubmit() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading.set(true);
    this.error.set('');

    this.auth.login(this.form.getRawValue()).subscribe({
      next: () => this.redirectAfterLogin(),
      error: (err) => {
        this.loading.set(false);
        this.error.set(err.error?.message ?? 'E-mail ou senha incorretos.');
      }
    });
  }

  onPinDigit(digit: string) {
    const current = this.pinDigits();
    if (current.length >= this.PIN_LENGTH) return;
    const updated = [...current, digit];
    this.pinDigits.set(updated);

    if (updated.length === this.PIN_LENGTH) {
      this.submitPin(updated.join(''));
    }
  }

  onPinClear() {
    this.pinDigits.set([]);
    this.error.set('');
  }

  toggleShowSenha() {
    this.showSenha.update(v => !v);
  }

  private submitPin(pin: string) {
    this.loading.set(true);
    this.auth.loginWithPin({ pin }).subscribe({
      next: () => this.redirectAfterLogin(),
      error: () => {
        this.loading.set(false);
        this.error.set('PIN inválido. Tente novamente.');
        this.pinDigits.set([]);
      }
    });
  }

  private redirectAfterLogin() {
    const perfil = this.auth.perfil();
    if (perfil === 'CAIXA') {
      this.router.navigate(['/pdv']);
    } else {
      this.router.navigate(['/backoffice/dashboard']);
    }
  }

  get emailCtrl() { return this.form.controls.email; }
  get senhaCtrl() { return this.form.controls.senha; }
}
