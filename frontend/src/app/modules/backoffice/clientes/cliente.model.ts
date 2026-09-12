export interface Cliente {
  id: number;
  nome: string;
  cpf?: string | null;
  cnpj?: string | null;
  telefone?: string | null;
  email?: string | null;
  dataNascimento?: string | null;
  cep?: string | null;
  logradouro?: string | null;
  numero?: string | null;
  complemento?: string | null;
  bairro?: string | null;
  cidade?: string | null;
  uf?: string | null;
  limiteFiado?: number | null;
  observacoes?: string | null;
  ativo: boolean;
}

export interface ClienteRequest {
  nome: string;
  cpf?: string | null;
  cnpj?: string | null;
  telefone?: string | null;
  email?: string | null;
  dataNascimento?: string | null;
  cep?: string | null;
  logradouro?: string | null;
  numero?: string | null;
  complemento?: string | null;
  bairro?: string | null;
  cidade?: string | null;
  uf?: string | null;
  limiteFiado?: number | null;
  observacoes?: string | null;
}
