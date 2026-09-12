package br.com.keila.bootstrap;

import br.com.keila.modules.caixa.model.Caixa;
import br.com.keila.modules.caixa.repository.CaixaRepository;
import br.com.keila.modules.loja.model.Loja;
import br.com.keila.modules.loja.repository.LojaRepository;
import br.com.keila.modules.produto.model.Categoria;
import br.com.keila.modules.produto.model.Cor;
import br.com.keila.modules.produto.model.Marca;
import br.com.keila.modules.produto.model.Tamanho;
import br.com.keila.modules.produto.model.TipoTamanho;
import br.com.keila.modules.produto.repository.CategoriaRepository;
import br.com.keila.modules.produto.repository.CorRepository;
import br.com.keila.modules.produto.repository.MarcaRepository;
import br.com.keila.modules.produto.repository.TamanhoRepository;
import br.com.keila.modules.usuario.model.PerfilUsuario;
import br.com.keila.modules.usuario.model.Usuario;
import br.com.keila.modules.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Carga inicial de dados de exemplo — só roda quando o banco está vazio
 * (primeira execução), já que o H2 persiste em arquivo entre reinícios.
 * Substitui o seed que antes vinha de uma migration Flyway (V7).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final LojaRepository lojaRepository;
    private final CategoriaRepository categoriaRepository;
    private final MarcaRepository marcaRepository;
    private final TamanhoRepository tamanhoRepository;
    private final CorRepository corRepository;
    private final CaixaRepository caixaRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (usuarioRepository.count() > 0) {
            return;
        }

        log.info("Banco vazio — carregando dados iniciais (loja, admin, catálogo básico)...");

        Loja loja = lojaRepository.save(Loja.builder()
                .nome("Keila Calçados")
                .cnpj("00.000.000/0001-00")
                .telefone("(00) 00000-0000")
                .endereco("Rua das Flores, 100 - Centro")
                .ativo(true)
                .build());

        usuarioRepository.save(Usuario.builder()
                .nome("Administrador")
                .email("admin@keila.com.br")
                .senhaHash(passwordEncoder.encode("Admin@123"))
                .perfil(PerfilUsuario.ADMIN)
                .ativo(true)
                .build());

        Categoria calcados = categoriaRepository.save(
                Categoria.builder().nome("Calçados").descricao("Todos os tipos de calçados").ordem(1).ativo(true).build());
        Categoria acessorios = categoriaRepository.save(
                Categoria.builder().nome("Acessórios").descricao("Todos os acessórios").ordem(2).ativo(true).build());

        List.of("Tênis", "Sandália", "Bota", "Chinelo", "Sapato Social", "Sapatilha").forEach(nome ->
                categoriaRepository.save(Categoria.builder().nome(nome).categoriaPai(calcados).ordem(1).ativo(true).build()));

        List.of("Cinto", "Carteira", "Bolsa", "Meia", "Meião", "Caneleira", "Bola de Futebol", "Boné", "Mala de Viagem")
                .forEach(nome -> categoriaRepository.save(
                        Categoria.builder().nome(nome).categoriaPai(acessorios).ordem(1).ativo(true).build()));

        List.of("Kenner", "Nike", "Adidas", "Puma", "Havaianas", "Skechers", "Moleca", "Rider", "Fila", "Reserva")
                .forEach(nome -> marcaRepository.save(Marca.builder().nome(nome).ativo(true).build()));

        int ordem = 1;
        for (int numero = 22; numero <= 48; numero++) {
            tamanhoRepository.save(Tamanho.builder().valor(String.valueOf(numero)).tipo(TipoTamanho.NUMERO).ordem(ordem++).build());
        }
        for (String letra : List.of("P", "M", "G", "GG", "XG")) {
            tamanhoRepository.save(Tamanho.builder().valor(letra).tipo(TipoTamanho.LETRA).ordem(ordem++).build());
        }
        tamanhoRepository.save(Tamanho.builder().valor("ÚNICO").tipo(TipoTamanho.UNICO).ordem(99).build());

        List<String[]> cores = List.of(
                new String[]{"Preto", "#000000"}, new String[]{"Branco", "#FFFFFF"}, new String[]{"Vermelho", "#DC2626"},
                new String[]{"Azul", "#2563EB"}, new String[]{"Azul Marinho", "#1E3A8A"}, new String[]{"Verde", "#16A34A"},
                new String[]{"Amarelo", "#EAB308"}, new String[]{"Cinza", "#6B7280"}, new String[]{"Marrom", "#92400E"},
                new String[]{"Rosa", "#EC4899"}, new String[]{"Laranja", "#EA580C"}, new String[]{"Bege", "#D4A574"},
                new String[]{"Roxo", "#7C3AED"}, new String[]{"Vinho", "#9F1239"});
        cores.forEach(c -> corRepository.save(Cor.builder().nome(c[0]).hexCode(c[1]).build()));

        List.of("Caixa 1", "Caixa 2", "Caixa 3").forEach(nome ->
                caixaRepository.save(Caixa.builder().loja(loja).nome(nome).ativo(true).build()));

        log.info("Dados iniciais carregados. Login: admin@keila.com.br / Admin@123");
    }
}
