package br.edu.pucminas.aluguel.controller;

import br.edu.pucminas.aluguel.model.Modelos.*;
import br.edu.pucminas.aluguel.service.AluguelService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AluguelController {
    private final AluguelService service;
    public AluguelController(AluguelService service) { this.service=service; }
    @PostMapping("/auth/cadastro") @ResponseStatus(HttpStatus.CREATED)
    public Usuario cadastro(@Valid @RequestBody Cadastro c) { return service.cadastrar(c); }
    @GetMapping("/auth/me") public Usuario me(Principal p) { return service.usuario(p.getName()); }
    @GetMapping("/clientes/me") public Cliente cliente(Principal p) { return service.cliente(p.getName()); }
    @PutMapping("/clientes/me") public Cliente editarCliente(Principal p, @Valid @RequestBody ClienteEdicao c) { return service.editarCliente(p.getName(),c); }
    @DeleteMapping("/clientes/me") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void apagarCliente(Principal p) { service.apagarCliente(p.getName()); }
    @GetMapping("/clientes") public List<Cliente> clientes(Principal p) { return service.clientes(p.getName()); }
    @GetMapping("/automoveis") public List<Automovel> automoveis() { return service.automoveis(); }
    @PostMapping("/automoveis") @ResponseStatus(HttpStatus.CREATED)
    public Automovel automovel(Principal p, @Valid @RequestBody AutomovelCadastro a) { return service.cadastrarAutomovel(p.getName(),a); }
    @GetMapping("/pedidos") public List<Pedido> pedidos(Principal p) { return service.pedidos(p.getName()); }
    @GetMapping("/pedidos/{id}") public Pedido pedido(Principal p, @PathVariable long id) { return service.pedido(p.getName(),id); }
    @PostMapping("/pedidos") @ResponseStatus(HttpStatus.CREATED)
    public Pedido criarPedido(Principal p, @Valid @RequestBody PedidoCadastro dados) { return service.criarPedido(p.getName(),dados); }
    @PutMapping("/pedidos/{id}") public Pedido editarPedido(Principal p, @PathVariable long id, @Valid @RequestBody PedidoCadastro dados) { return service.editarPedido(p.getName(),id,dados); }
    @PostMapping("/pedidos/{id}/cancelamento") public Pedido cancelar(Principal p, @PathVariable long id) { return service.cancelarPedido(p.getName(),id); }
    @PatchMapping("/pedidos/{id}/avaliacao") public Pedido avaliar(Principal p, @PathVariable long id, @Valid @RequestBody Avaliacao dados) { return service.avaliar(p.getName(),id,dados); }
    @PostMapping("/pedidos/{id}/credito") @ResponseStatus(HttpStatus.CREATED)
    public void credito(Principal p, @PathVariable long id, @Valid @RequestBody CreditoCadastro dados) { service.concederCredito(p.getName(),id,dados); }
}
