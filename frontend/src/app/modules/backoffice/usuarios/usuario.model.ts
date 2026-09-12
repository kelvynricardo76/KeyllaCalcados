export type PerfilUsuario = 'ADMIN' | 'GERENTE' | 'CAIXA' | 'ESTOQUISTA' | 'VENDEDOR';

export interface Usuario {
  id: number;
  nome: string;
  email: string;
  perfil: PerfilUsuario;
  ativo: boolean;
}

export interface CriarUsuarioRequest {
  nome: string;
  email: string;
  senha: string;
  perfil: PerfilUsuario;
}

export interface AtualizarUsuarioRequest {
  nome: string;
  email: string;
  perfil: PerfilUsuario;
  novaSenha?: string | null;
}
