import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';

export interface LoginRequest { email: string; senha: string; }
export interface PinLoginRequest { pin: string; }
export interface AuthResponse {
  token: string;
  nome: string;
  perfil: string;
  userId: number;
}

/**
 * AuthService: gerencia o ciclo de vida da autenticação.
 * Usa Angular Signals para estado reativo sem necessidade de BehaviorSubject.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly TOKEN_KEY = 'keylla_token';
  private readonly USER_KEY  = 'keylla_user';

  private _user = signal<AuthResponse | null>(this.loadUserFromStorage());
  readonly user = this._user.asReadonly();
  readonly isLoggedIn = computed(() => this._user() !== null);
  readonly perfil     = computed(() => this._user()?.perfil ?? null);

  constructor(private http: HttpClient, private router: Router) {}

  login(request: LoginRequest) {
    return this.http.post<AuthResponse>(`${environment.apiUrl}/auth/login`, request)
      .pipe(tap(resp => this.handleAuthResponse(resp)));
  }

  loginWithPin(request: PinLoginRequest) {
    return this.http.post<AuthResponse>(`${environment.apiUrl}/auth/pin`, request)
      .pipe(tap(resp => this.handleAuthResponse(resp)));
  }

  /**
   * MODO DEMONSTRAÇÃO — apenas para desenvolvimento local sem backend/banco no ar.
   * Não faz nenhuma chamada de rede; simula uma sessão ADMIN só para navegar pela UI.
   * REMOVER antes de qualquer deploy real (ver uso em LoginComponent, atrás de !environment.production).
   */
  entrarModoDemo(): void {
    this.handleAuthResponse({
      token: 'demo-token-sem-backend',
      nome: 'Administrador (Demo)',
      perfil: 'ADMIN',
      userId: 0
    });
  }

  logout() {
    // Chama o backend para adicionar o token à blacklist Redis
    const token = this.getToken();
    if (token && !token.startsWith('demo-token')) {
      this.http.post(`${environment.apiUrl}/auth/logout`, {}).subscribe();
    }
    this.clearSession();
    this.router.navigate(['/auth/login']);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  hasPermission(permissao: string): boolean {
    // Permissões são verificadas no backend; aqui fazemos checagem básica por perfil
    const perfil = this._user()?.perfil;
    if (perfil === 'ADMIN') return true;
    return false; // Refinado conforme permissões carregadas do backend
  }

  private handleAuthResponse(resp: AuthResponse): void {
    localStorage.setItem(this.TOKEN_KEY, resp.token);
    localStorage.setItem(this.USER_KEY, JSON.stringify(resp));
    this._user.set(resp);
  }

  private clearSession(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this._user.set(null);
  }

  private loadUserFromStorage(): AuthResponse | null {
    try {
      const raw = localStorage.getItem(this.USER_KEY);
      return raw ? JSON.parse(raw) : null;
    } catch { return null; }
  }
}
