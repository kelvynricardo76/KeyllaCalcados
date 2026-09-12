package br.com.keila.bootstrap;

import br.com.keila.modules.caixa.model.Caixa;
import br.com.keila.modules.caixa.repository.CaixaRepository;
import br.com.keila.modules.cliente.model.Cliente;
import br.com.keila.modules.cliente.repository.ClienteRepository;
import br.com.keila.modules.estoque.model.Estoque;
import br.com.keila.modules.estoque.repository.EstoqueRepository;
import br.com.keila.modules.fornecedor.model.Fornecedor;
import br.com.keila.modules.fornecedor.repository.FornecedorRepository;
import br.com.keila.modules.loja.model.Loja;
import br.com.keila.modules.loja.repository.LojaRepository;
import br.com.keila.modules.produto.model.Categoria;
import br.com.keila.modules.produto.model.Cor;
import br.com.keila.modules.produto.model.Marca;
import br.com.keila.modules.produto.model.Produto;
import br.com.keila.modules.produto.model.Tamanho;
import br.com.keila.modules.produto.model.TipoTamanho;
import br.com.keila.modules.produto.model.VariacaoProduto;
import br.com.keila.modules.produto.repository.CategoriaRepository;
import br.com.keila.modules.produto.repository.CorRepository;
import br.com.keila.modules.produto.repository.MarcaRepository;
import br.com.keila.modules.produto.repository.ProdutoRepository;
import br.com.keila.modules.produto.repository.TamanhoRepository;
import br.com.keila.modules.produto.repository.VariacaoProdutoRepository;
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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Carga inicial de dados de exemplo — só roda quando o banco está vazio
 * (primeira execução), já que o H2 persiste em arquivo entre reinícios.
 * Substitui o seed que antes vinha de uma migration Flyway (V7), e inclui
 * produtos, variações, estoque, um cliente e um fornecedor de exemplo para
 * que o sistema já nasça testável (igual ao que o VivaMais faz).
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
    private final ProdutoRepository produtoRepository;
    private final VariacaoProdutoRepository variacaoRepository;
    private final EstoqueRepository estoqueRepository;
    private final ClienteRepository clienteRepository;
    private final FornecedorRepository fornecedorRepository;
    private final PasswordEncoder passwordEncoder;

    private final Map<String, Categoria> categoriasPorNome = new HashMap<>();
    private final Map<String, Marca> marcasPorNome = new HashMap<>();
    private final Map<String, Tamanho> tamanhosPorValor = new HashMap<>();
    private final Map<String, Cor> coresPorNome = new HashMap<>();

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (usuarioRepository.count() > 0) {
            return;
        }

        log.info("Banco vazio — carregando dados iniciais (loja, admin, catálogo, produtos de teste)...");

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

        seedCategorias();
        seedMarcas();
        seedTamanhos();
        seedCores();

        List.of("Caixa 1", "Caixa 2", "Caixa 3").forEach(nome ->
                caixaRepository.save(Caixa.builder().loja(loja).nome(nome).ativo(true).build()));

        seedProdutos(loja);
        seedCliente();
        seedFornecedor();

        log.info("Dados iniciais carregados. Login: admin@keila.com.br / Admin@123");
    }

    private void seedCategorias() {
        Categoria calcados = categoriaRepository.save(
                Categoria.builder().nome("Calçados").descricao("Todos os tipos de calçados").ordem(1).ativo(true).build());
        Categoria acessorios = categoriaRepository.save(
                Categoria.builder().nome("Acessórios").descricao("Todos os acessórios").ordem(2).ativo(true).build());

        List.of("Tênis", "Sandália", "Bota", "Chinelo", "Sapato Social", "Sapatilha").forEach(nome ->
                categoriasPorNome.put(nome, categoriaRepository.save(
                        Categoria.builder().nome(nome).categoriaPai(calcados).ordem(1).ativo(true).build())));

        List.of("Cinto", "Carteira", "Bolsa", "Meia", "Meião", "Caneleira", "Bola de Futebol", "Boné", "Mala de Viagem")
                .forEach(nome -> categoriasPorNome.put(nome, categoriaRepository.save(
                        Categoria.builder().nome(nome).categoriaPai(acessorios).ordem(1).ativo(true).build())));
    }

    private void seedMarcas() {
        List.of("Kenner", "Nike", "Adidas", "Puma", "Havaianas", "Skechers", "Moleca", "Rider", "Fila", "Reserva")
                .forEach(nome -> marcasPorNome.put(nome, marcaRepository.save(Marca.builder().nome(nome).ativo(true).build())));
    }

    private void seedTamanhos() {
        int ordem = 1;
        for (int numero = 22; numero <= 48; numero++) {
            Tamanho tamanho = tamanhoRepository.save(
                    Tamanho.builder().valor(String.valueOf(numero)).tipo(TipoTamanho.NUMERO).ordem(ordem++).build());
            tamanhosPorValor.put(tamanho.getValor(), tamanho);
        }
        for (String letra : List.of("P", "M", "G", "GG", "XG")) {
            Tamanho tamanho = tamanhoRepository.save(Tamanho.builder().valor(letra).tipo(TipoTamanho.LETRA).ordem(ordem++).build());
            tamanhosPorValor.put(tamanho.getValor(), tamanho);
        }
        Tamanho unico = tamanhoRepository.save(Tamanho.builder().valor("ÚNICO").tipo(TipoTamanho.UNICO).ordem(99).build());
        tamanhosPorValor.put(unico.getValor(), unico);
    }

    private void seedCores() {
        List<String[]> cores = List.of(
                new String[]{"Preto", "#000000"}, new String[]{"Branco", "#FFFFFF"}, new String[]{"Vermelho", "#DC2626"},
                new String[]{"Azul", "#2563EB"}, new String[]{"Azul Marinho", "#1E3A8A"}, new String[]{"Verde", "#16A34A"},
                new String[]{"Amarelo", "#EAB308"}, new String[]{"Cinza", "#6B7280"}, new String[]{"Marrom", "#92400E"},
                new String[]{"Rosa", "#EC4899"}, new String[]{"Laranja", "#EA580C"}, new String[]{"Bege", "#D4A574"},
                new String[]{"Roxo", "#7C3AED"}, new String[]{"Vinho", "#9F1239"});
        cores.forEach(c -> coresPorNome.put(c[0], corRepository.save(Cor.builder().nome(c[0]).hexCode(c[1]).build())));
    }

    /** Cria produtos de exemplo com grade (tamanho × cor) e saldo de estoque, para o sistema já nascer testável. */
    private void seedProdutos(Loja loja) {
        criarProdutoComGrade(loja, "Tênis Esportivo Runner", "Nike", "Tênis", "TEN-RUN",
                new BigDecimal("120.00"), new BigDecimal("249.90"),
                List.of("38", "39", "40", "41", "42"), List.of("Preto", "Branco"), 12);

        criarProdutoComGrade(loja, "Tênis Casual Street", "Adidas", "Tênis", "TEN-STR",
                new BigDecimal("95.00"), new BigDecimal("219.90"),
                List.of("37", "38", "39", "40"), List.of("Branco", "Cinza", "Azul Marinho"), 8);

        criarProdutoComGrade(loja, "Sandália Conforto", "Havaianas", "Sandália", "SAN-CFT",
                new BigDecimal("25.00"), new BigDecimal("59.90"),
                List.of("35", "36", "37", "38", "39"), List.of("Preto", "Marrom", "Bege"), 20);

        criarProdutoComGrade(loja, "Bota Coturno Adventure", "Rider", "Bota", "BOT-ADV",
                new BigDecimal("90.00"), new BigDecimal("189.90"),
                List.of("39", "40", "41", "42", "43"), List.of("Preto"), 6);

        criarProdutoComGrade(loja, "Chinelo Slide", "Puma", "Chinelo", "CHI-SLD",
                new BigDecimal("18.00"), new BigDecimal("44.90"),
                List.of("37", "38", "39", "40", "41", "42"), List.of("Preto", "Branco", "Vermelho"), 15);

        criarProdutoSimples(loja, "Cinto de Couro Legítimo", "Reserva", "Cinto", "CIN-CRO",
                new BigDecimal("15.00"), new BigDecimal("49.90"), 25);

        criarProdutoSimples(loja, "Boné Trucker", "Puma", "Boné", "BON-TRK",
                new BigDecimal("12.00"), new BigDecimal("39.90"), 30);

        criarProdutoSimples(loja, "Meião Esportivo (par)", "Kenner", "Meião", "MEI-ESP",
                new BigDecimal("5.00"), new BigDecimal("14.90"), 40);
    }

    private void criarProdutoComGrade(Loja loja, String nome, String marca, String categoria, String skuBase,
                                       BigDecimal precoCusto, BigDecimal precoVenda,
                                       List<String> tamanhos, List<String> cores, int quantidadePorVariacao) {
        Produto produto = produtoRepository.save(Produto.builder()
                .nome(nome)
                .marca(marcasPorNome.get(marca))
                .categoria(categoriasPorNome.get(categoria))
                .sku(skuBase)
                .precoCusto(precoCusto)
                .precoVenda(precoVenda)
                .temGrade(true)
                .ativo(true)
                .build());

        for (String tamanho : tamanhos) {
            for (String cor : cores) {
                Tamanho tamanhoEntity = tamanhosPorValor.get(tamanho);
                Cor corEntity = coresPorNome.get(cor);
                String sku = skuBase + "-" + tamanho + "-" + cor.substring(0, Math.min(3, cor.length())).toUpperCase();

                VariacaoProduto variacao = variacaoRepository.save(VariacaoProduto.builder()
                        .produto(produto)
                        .tamanho(tamanhoEntity)
                        .cor(corEntity)
                        .sku(sku)
                        .ativo(true)
                        .build());

                estoqueRepository.save(Estoque.builder()
                        .variacao(variacao)
                        .loja(loja)
                        .quantidade(quantidadePorVariacao)
                        .estoqueMinimo(3)
                        .build());
            }
        }
    }

    private void criarProdutoSimples(Loja loja, String nome, String marca, String categoria, String sku,
                                      BigDecimal precoCusto, BigDecimal precoVenda, int quantidade) {
        Produto produto = produtoRepository.save(Produto.builder()
                .nome(nome)
                .marca(marcasPorNome.get(marca))
                .categoria(categoriasPorNome.get(categoria))
                .sku(sku)
                .precoCusto(precoCusto)
                .precoVenda(precoVenda)
                .temGrade(false)
                .ativo(true)
                .build());

        VariacaoProduto variacao = variacaoRepository.save(VariacaoProduto.builder()
                .produto(produto)
                .tamanho(tamanhosPorValor.get("ÚNICO"))
                .sku(sku + "-UNICO")
                .ativo(true)
                .build());

        estoqueRepository.save(Estoque.builder()
                .variacao(variacao)
                .loja(loja)
                .quantidade(quantidade)
                .estoqueMinimo(5)
                .build());
    }

    private void seedCliente() {
        clienteRepository.save(Cliente.builder()
                .nome("João da Silva")
                .cpf("111.222.333-44")
                .telefone("(11) 98765-4321")
                .email("joao.silva@exemplo.com.br")
                .cidade("São Paulo")
                .uf("SP")
                .limiteFiado(new BigDecimal("500.00"))
                .ativo(true)
                .build());
    }

    private void seedFornecedor() {
        fornecedorRepository.save(Fornecedor.builder()
                .razaoSocial("Distribuidora ABC Calçados Ltda")
                .cnpj("11.222.333/0001-44")
                .telefone("(11) 3333-4444")
                .email("vendas@abccalcados.com.br")
                .contato("Carlos Mendes")
                .ativo(true)
                .build());
    }
}
