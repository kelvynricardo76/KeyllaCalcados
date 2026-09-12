package br.com.keila.modules.fiado.service;

import br.com.keila.modules.cliente.model.Cliente;
import br.com.keila.modules.cliente.repository.ClienteRepository;
import br.com.keila.modules.fiado.dto.FiadoResponse;
import br.com.keila.modules.fiado.dto.PagamentoRequest;
import br.com.keila.modules.fiado.model.Fiado;
import br.com.keila.modules.fiado.model.StatusFiado;
import br.com.keila.modules.fiado.repository.FiadoRepository;
import br.com.keila.modules.fiado.repository.PagamentoFiadoRepository;
import br.com.keila.modules.loja.model.Loja;
import br.com.keila.modules.loja.repository.LojaRepository;
import br.com.keila.modules.usuario.model.PerfilUsuario;
import br.com.keila.modules.usuario.model.Usuario;
import br.com.keila.shared.exception.RegraNegocioException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FiadoServiceTest {

    @Mock private FiadoRepository fiadoRepository;
    @Mock private PagamentoFiadoRepository pagamentoRepository;
    @Mock private ClienteRepository clienteRepository;
    @Mock private LojaRepository lojaRepository;

    private FiadoService fiadoService;

    @BeforeEach
    void setUp() {
        fiadoService = new FiadoService(fiadoRepository, pagamentoRepository, clienteRepository, lojaRepository);

        Usuario usuario = Usuario.builder().id(1L).nome("Caixa 1").email("caixa@keila.com.br").perfil(PerfilUsuario.CAIXA).ativo(true).build();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(usuario, null));

        org.mockito.Mockito.lenient().when(fiadoRepository.save(org.mockito.ArgumentMatchers.any(Fiado.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private Fiado fiadoAberto(BigDecimal total, BigDecimal pago) {
        Cliente cliente = Cliente.builder().id(1L).nome("Maria Silva").ativo(true).build();
        Loja loja = Loja.builder().id(1L).nome("Loja Centro").build();
        return Fiado.builder()
                .id(50L).cliente(cliente).loja(loja)
                .valorTotal(total).valorPago(pago).valorRestante(total.subtract(pago))
                .dataLancamento(LocalDate.now()).dataVencimento(LocalDate.now().plusDays(30))
                .status(pago.compareTo(BigDecimal.ZERO) > 0 ? StatusFiado.PARCIAL : StatusFiado.ABERTO)
                .build();
    }

    @Test
    void registrarPagamento_comValorMaiorQueSaldoDevedor_lancaRegraNegocio() {
        Fiado fiado = fiadoAberto(new BigDecimal("100.00"), BigDecimal.ZERO);
        when(fiadoRepository.findById(50L)).thenReturn(Optional.of(fiado));

        assertThatThrownBy(() -> fiadoService.registrarPagamento(50L,
                new PagamentoRequest(new BigDecimal("150.00"), "DINHEIRO", null)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("maior que o saldo devedor");
    }

    @Test
    void registrarPagamento_pagamentoParcial_atualizaStatusParaParcial() {
        Fiado fiado = fiadoAberto(new BigDecimal("100.00"), BigDecimal.ZERO);
        when(fiadoRepository.findById(50L)).thenReturn(Optional.of(fiado));

        FiadoResponse resposta = fiadoService.registrarPagamento(50L,
                new PagamentoRequest(new BigDecimal("40.00"), "PIX", null));

        assertThat(resposta.status()).isEqualTo(StatusFiado.PARCIAL);
        assertThat(resposta.valorRestante()).isEqualByComparingTo("60.00");
    }

    @Test
    void registrarPagamento_quitandoSaldoTotal_atualizaStatusParaPago() {
        Fiado fiado = fiadoAberto(new BigDecimal("100.00"), new BigDecimal("60.00"));
        when(fiadoRepository.findById(50L)).thenReturn(Optional.of(fiado));

        FiadoResponse resposta = fiadoService.registrarPagamento(50L,
                new PagamentoRequest(new BigDecimal("40.00"), "DINHEIRO", null));

        assertThat(resposta.status()).isEqualTo(StatusFiado.PAGO);
        assertThat(resposta.valorRestante()).isEqualByComparingTo("0.00");
    }

    @Test
    void registrarPagamento_emFiadoJaQuitado_lancaRegraNegocio() {
        Fiado fiado = fiadoAberto(new BigDecimal("100.00"), new BigDecimal("100.00"));
        fiado.setStatus(StatusFiado.PAGO);
        when(fiadoRepository.findById(50L)).thenReturn(Optional.of(fiado));

        assertThatThrownBy(() -> fiadoService.registrarPagamento(50L,
                new PagamentoRequest(new BigDecimal("10.00"), "DINHEIRO", null)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("já está totalmente quitado");
    }
}
