package br.edu.pucminas.aluguel.service;

import br.edu.pucminas.aluguel.model.Modelos.Avaliacao;
import br.edu.pucminas.aluguel.model.Modelos.Pedido;
import br.edu.pucminas.aluguel.repository.AluguelRepository;
import br.edu.pucminas.aluguel.repository.AluguelRepository.Conta;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AluguelServiceTest {
    private AluguelRepository repo;
    private AluguelService service;

    @BeforeEach void setup() {
        repo = mock(AluguelRepository.class);
        service = new AluguelService(repo, mock(PasswordEncoder.class));
    }

    private Pedido pedido(String status) {
        return new Pedido(9, 2, 3, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3),
                status, null, null, "Cliente", "Carro");
    }

    @Test void clienteNaoVePedidoDeOutroCliente() {
        when(repo.conta("a@teste.com")).thenReturn(Optional.of(new Conta(1,"A","a@teste.com","CLIENTE","hash")));
        when(repo.pedido(9)).thenReturn(Optional.of(pedido("PENDENTE")));

        ResponseStatusException erro = assertThrows(ResponseStatusException.class,
                () -> service.pedido("a@teste.com", 9));
        assertEquals(HttpStatus.FORBIDDEN, erro.getStatusCode());
    }

    @Test void aprovacaoExigeParecerFinanceiro() {
        when(repo.conta("agente@teste.com")).thenReturn(Optional.of(new Conta(7,"Agente","agente@teste.com","EMPRESA","hash")));
        when(repo.pedido(9)).thenReturn(Optional.of(pedido("PENDENTE")));

        ResponseStatusException erro = assertThrows(ResponseStatusException.class,
                () -> service.avaliar("agente@teste.com", 9, new Avaliacao("APROVADO", "  ")));
        assertEquals(HttpStatus.BAD_REQUEST, erro.getStatusCode());
        verify(repo, never()).mudarStatus(anyLong(), anyString(), any(), any());
    }

    @Test void somentePedidoAprovadoPodeVirarContrato() {
        when(repo.conta("agente@teste.com")).thenReturn(Optional.of(new Conta(7,"Agente","agente@teste.com","EMPRESA","hash")));
        when(repo.pedido(9)).thenReturn(Optional.of(pedido("PENDENTE")));

        ResponseStatusException erro = assertThrows(ResponseStatusException.class,
                () -> service.avaliar("agente@teste.com", 9, new Avaliacao("CONTRATADO", null)));
        assertEquals(HttpStatus.CONFLICT, erro.getStatusCode());
        verify(repo, never()).mudarStatus(anyLong(), anyString(), any(), any());
    }
}
