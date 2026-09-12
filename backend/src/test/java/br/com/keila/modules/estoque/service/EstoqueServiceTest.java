package br.com.keila.modules.estoque.service;

import br.com.keila.modules.estoque.dto.AjusteEstoqueRequest;
import br.com.keila.modules.estoque.dto.EstoqueResponse;
import br.com.keila.modules.estoque.model.Estoque;
import br.com.keila.modules.estoque.model.TipoMovEstoque;
import br.com.keila.modules.estoque.repository.EstoqueRepository;
import br.com.keila.modules.estoque.repository.MovimentacaoEstoqueRepository;
import br.com.keila.modules.loja.model.Loja;
import br.com.keila.modules.loja.repository.LojaRepository;
import br.com.keila.modules.produto.model.Produto;
import br.com.keila.modules.produto.model.VariacaoProduto;
import br.com.keila.modules.produto.repository.VariacaoProdutoRepository;
import br.com.keila.modules.usuario.model.PerfilUsuario;
import br.com.keila.modules.usuario.model.Usuario;
import br.com.keila.shared.exception.RegraNegocioException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EstoqueServiceTest {

    @Mock private EstoqueRepository estoqueRepository;
    @Mock private MovimentacaoEstoqueRepository movimentacaoRepository;
    @Mock private VariacaoProdutoRepository variacaoRepository;
    @Mock private LojaRepository lojaRepository;

    @InjectMocks
    private EstoqueService estoqueService;

    private VariacaoProduto variacao;
    private Loja loja;

    @BeforeEach
    void setUp() {
        Produto produto = Produto.builder().id(1L).nome("Tênis Esportivo").precoVenda(new BigDecimal("199.90")).build();
        variacao = VariacaoProduto.builder().id(10L).produto(produto).sku("TEN-38-PRETO").ativo(true).build();
        loja = Loja.builder().id(1L).nome("Loja Centro").ativo(true).build();

        Usuario usuario = Usuario.builder().id(1L).nome("Admin").email("admin@keila.com.br").perfil(PerfilUsuario.ADMIN).ativo(true).build();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(usuario, null));

        lenient().when(variacaoRepository.findById(10L)).thenReturn(Optional.of(variacao));
        lenient().when(lojaRepository.findById(1L)).thenReturn(Optional.of(loja));
        lenient().when(estoqueRepository.save(any(Estoque.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void ajustar_comEntradaEmVariacaoSemSaldoPrevio_criaRegistroComQuantidadeCorreta() {
        when(estoqueRepository.findByVariacaoIdAndLojaId(10L, 1L)).thenReturn(Optional.empty());

        EstoqueResponse resposta = estoqueService.ajustar(
                new AjusteEstoqueRequest(10L, 1L, TipoMovEstoque.ENTRADA, 15, "Recebimento inicial"));

        assertThat(resposta.quantidade()).isEqualTo(15);
        assertThat(resposta.statusEstoque()).isEqualTo("OK");
    }

    @Test
    void ajustar_comSaidaMaiorQueSaldoDisponivel_lancaRegraNegocio() {
        Estoque saldoAtual = Estoque.builder().id(5L).variacao(variacao).loja(loja).quantidade(3).estoqueMinimo(1).build();
        when(estoqueRepository.findByVariacaoIdAndLojaId(10L, 1L)).thenReturn(Optional.of(saldoAtual));

        assertThatThrownBy(() -> estoqueService.ajustar(
                new AjusteEstoqueRequest(10L, 1L, TipoMovEstoque.SAIDA, 5, "Venda")))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Estoque insuficiente");
    }

    @Test
    void ajustar_comSaidaDeixandoSaldoAbaixoDoMinimo_marcaStatusBaixo() {
        Estoque saldoAtual = Estoque.builder().id(5L).variacao(variacao).loja(loja).quantidade(5).estoqueMinimo(3).build();
        when(estoqueRepository.findByVariacaoIdAndLojaId(10L, 1L)).thenReturn(Optional.of(saldoAtual));

        EstoqueResponse resposta = estoqueService.ajustar(
                new AjusteEstoqueRequest(10L, 1L, TipoMovEstoque.SAIDA, 3, "Venda"));

        assertThat(resposta.quantidade()).isEqualTo(2);
        assertThat(resposta.statusEstoque()).isEqualTo("BAIXO");
    }

    @Test
    void saldoDisponivel_semRegistroDeEstoque_retornaZero() {
        when(estoqueRepository.findByVariacaoIdAndLojaId(10L, 1L)).thenReturn(Optional.empty());

        assertThat(estoqueService.saldoDisponivel(10L, 1L)).isZero();
    }
}
