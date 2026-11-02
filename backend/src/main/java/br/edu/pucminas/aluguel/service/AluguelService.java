package br.edu.pucminas.aluguel.service;

import br.edu.pucminas.aluguel.model.Modelos.*;
import br.edu.pucminas.aluguel.repository.AluguelRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AluguelService {
    private final AluguelRepository repo;
    private final PasswordEncoder encoder;
    public AluguelService(AluguelRepository repo, PasswordEncoder encoder) { this.repo=repo; this.encoder=encoder; }
    private ResponseStatusException erro(HttpStatus status, String mensagem) { return new ResponseStatusException(status, mensagem); }
    public AluguelRepository.Conta conta(String email) {
        return repo.conta(email).orElseThrow(() -> erro(HttpStatus.UNAUTHORIZED, "Conta não encontrada"));
    }
    public Usuario usuario(String email) {
        var c=conta(email); return new Usuario(c.id(),c.nome(),c.email(),c.perfil());
    }
    @Transactional public Usuario cadastrar(Cadastro c) {
        if (repo.conta(c.email()).isPresent()) throw erro(HttpStatus.CONFLICT,"Email já cadastrado");
        try {
            long id=repo.inserirUsuario(c.nome(),c.email(),encoder.encode(c.senha()),"CLIENTE");
            repo.inserirCliente(id,c.rg(),c.cpf(),c.endereco(),c.profissao());
            repo.substituirEmpregos(id,c.empregos());
            return usuario(c.email());
        } catch (DataIntegrityViolationException e) { throw erro(HttpStatus.CONFLICT,"Email ou CPF já cadastrado"); }
    }
    public Cliente cliente(String email) {
        return repo.cliente(conta(email).id()).orElseThrow(() -> erro(HttpStatus.NOT_FOUND,"Cliente não encontrado"));
    }
    public List<Cliente> clientes(String email) {
        exigirAgente(email); return repo.clientes();
    }
    @Transactional public Cliente editarCliente(String email, ClienteEdicao c) {
        long id=exigirCliente(email);
        try { repo.atualizarCliente(id,c); repo.substituirEmpregos(id,c.empregos()); }
        catch (DataIntegrityViolationException e) { throw erro(HttpStatus.CONFLICT,"CPF já cadastrado"); }
        return repo.cliente(id).orElseThrow();
    }
    @Transactional public void apagarCliente(String email) {
        long id=exigirCliente(email);
        if (repo.temPedidos(id) || repo.temAutomoveis(id))
            throw erro(HttpStatus.CONFLICT,"Cliente com pedidos ou automóveis não pode ser excluído");
        repo.apagarCliente(id);
    }
    public List<Automovel> automoveis() { return repo.automoveis(); }
    public Automovel cadastrarAutomovel(String email, AutomovelCadastro a) {
        try { return repo.inserirAutomovel(a,conta(email).id()); }
        catch (DataIntegrityViolationException e) { throw erro(HttpStatus.CONFLICT,"Matrícula ou placa já cadastrada"); }
    }
    public List<Pedido> pedidos(String email) {
        var c=conta(email);
        return repo.pedidos(c.perfil().equals("CLIENTE") ? c.id() : null);
    }
    public Pedido pedido(String email, long id) {
        Pedido p=repo.pedido(id).orElseThrow(() -> erro(HttpStatus.NOT_FOUND,"Pedido não encontrado"));
        var c=conta(email);
        if (c.perfil().equals("CLIENTE") && p.clienteId()!=c.id()) throw erro(HttpStatus.FORBIDDEN,"Acesso negado");
        return p;
    }
    private void validarPedido(PedidoCadastro p) {
        if (p.inicio().isBefore(LocalDate.now()) || p.fim().isBefore(p.inicio()))
            throw erro(HttpStatus.BAD_REQUEST,"Informe um período válido a partir de hoje");
        if (!repo.existeAutomovel(p.automovelId())) throw erro(HttpStatus.NOT_FOUND,"Automóvel não encontrado");
    }
    public Pedido criarPedido(String email, PedidoCadastro p) {
        long cliente=exigirCliente(email); validarPedido(p); return repo.inserirPedido(cliente,p);
    }
    public Pedido editarPedido(String email, long id, PedidoCadastro dados) {
        long cliente=exigirCliente(email); Pedido p=pedido(email,id);
        if (p.clienteId()!=cliente || !p.status().equals("PENDENTE"))
            throw erro(HttpStatus.CONFLICT,"Somente pedidos pendentes podem ser editados");
        validarPedido(dados); return repo.alterarPedido(id,dados);
    }
    public Pedido cancelarPedido(String email, long id) {
        long cliente=exigirCliente(email); Pedido p=pedido(email,id);
        if (p.clienteId()!=cliente || !(p.status().equals("PENDENTE") || p.status().equals("EM_ANALISE")))
            throw erro(HttpStatus.CONFLICT,"Este pedido não pode ser cancelado");
        return repo.mudarStatus(id,"CANCELADO",null,null);
    }
    public Pedido avaliar(String email, long id, Avaliacao a) {
        long agente=exigirAgente(email); Pedido p=pedido(email,id);
        boolean permitido = switch (a.status()) {
            case "EM_ANALISE", "APROVADO", "RECUSADO" -> p.status().equals("PENDENTE") || p.status().equals("EM_ANALISE");
            case "CONTRATADO" -> p.status().equals("APROVADO");
            default -> false;
        };
        if (!permitido) throw erro(HttpStatus.CONFLICT,"Transição de status inválida");
        if ((a.status().equals("APROVADO") || a.status().equals("RECUSADO")) &&
                (a.parecer()==null || a.parecer().isBlank()))
            throw erro(HttpStatus.BAD_REQUEST,"Informe o parecer financeiro");
        return repo.mudarStatus(id,a.status(),a.parecer(),agente);
    }
    public void concederCredito(String email, long id, CreditoCadastro c) {
        var banco=conta(email);
        if (!banco.perfil().equals("BANCO")) throw erro(HttpStatus.FORBIDDEN,"Apenas bancos concedem crédito");
        Pedido p=pedido(email,id);
        if (!(p.status().equals("APROVADO") || p.status().equals("CONTRATADO")))
            throw erro(HttpStatus.CONFLICT,"Pedido ainda não aprovado");
        try { repo.inserirCredito(id,banco.id(),c); }
        catch (DataIntegrityViolationException e) { throw erro(HttpStatus.CONFLICT,"Crédito já cadastrado para o pedido"); }
    }
    private long exigirCliente(String email) {
        var c=conta(email);
        if (!c.perfil().equals("CLIENTE")) throw erro(HttpStatus.FORBIDDEN,"Apenas clientes podem realizar esta ação");
        return c.id();
    }
    private long exigirAgente(String email) {
        var c=conta(email);
        if (c.perfil().equals("CLIENTE")) throw erro(HttpStatus.FORBIDDEN,"Apenas agentes podem realizar esta ação");
        return c.id();
    }
}
