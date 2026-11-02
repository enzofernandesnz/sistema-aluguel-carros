package br.edu.pucminas.aluguel.repository;

import br.edu.pucminas.aluguel.model.Modelos.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class AluguelRepository {
    private final JdbcTemplate db;
    public AluguelRepository(JdbcTemplate db) { this.db = db; }

    public record Conta(long id, String nome, String email, String perfil, String senhaHash) {}
    private static final RowMapper<Conta> CONTA = (r, n) -> new Conta(r.getLong("id"), r.getString("nome"),
            r.getString("email"), r.getString("perfil"), r.getString("senha_hash"));

    public Optional<Conta> conta(String email) {
        return db.query("SELECT * FROM usuarios WHERE lower(email)=lower(?)", CONTA, email).stream().findFirst();
    }
    public Conta conta(long id) {
        return db.queryForObject("SELECT * FROM usuarios WHERE id=?", CONTA, id);
    }
    public long inserirUsuario(String nome, String email, String hash, String perfil) {
        return db.queryForObject("INSERT INTO usuarios(nome,email,senha_hash,perfil) VALUES (?,?,?,?) RETURNING id",
                Long.class, nome, email.toLowerCase(), hash, perfil);
    }
    public void inserirCliente(long id, String rg, String cpf, String endereco, String profissao) {
        db.update("INSERT INTO clientes(usuario_id,rg,cpf,endereco,profissao) VALUES (?,?,?,?,?)",
                id, rg, cpf, endereco, profissao);
    }
    public void atualizarCliente(long id, ClienteEdicao c) {
        db.update("UPDATE usuarios SET nome=? WHERE id=?", c.nome(), id);
        db.update("UPDATE clientes SET rg=?,cpf=?,endereco=?,profissao=? WHERE usuario_id=?",
                c.rg(), c.cpf(), c.endereco(), c.profissao(), id);
    }
    public void substituirEmpregos(long id, List<Emprego> empregos) {
        db.update("DELETE FROM empregos WHERE cliente_id=?", id);
        if (empregos != null) for (Emprego e : empregos)
            db.update("INSERT INTO empregos(cliente_id,empregador,rendimento) VALUES (?,?,?)",
                    id, e.empregador(), e.rendimento());
    }
    private final RowMapper<Cliente> clienteMapper = (r, n) -> new Cliente(r.getLong("usuario_id"),
            r.getString("nome"), r.getString("email"), r.getString("rg"), r.getString("cpf"),
            r.getString("endereco"), r.getString("profissao"), empregos(r.getLong("usuario_id")));
    public Optional<Cliente> cliente(long id) {
        return db.query("SELECT c.*,u.nome,u.email FROM clientes c JOIN usuarios u ON u.id=c.usuario_id WHERE c.usuario_id=?",
                clienteMapper, id).stream().findFirst();
    }
    public List<Cliente> clientes() {
        return db.query("SELECT c.*,u.nome,u.email FROM clientes c JOIN usuarios u ON u.id=c.usuario_id ORDER BY u.nome",
                clienteMapper);
    }
    public List<Emprego> empregos(long id) {
        return db.query("SELECT empregador,rendimento FROM empregos WHERE cliente_id=? ORDER BY id",
                (r,n) -> new Emprego(r.getString(1), r.getBigDecimal(2)), id);
    }
    public void apagarCliente(long id) { db.update("DELETE FROM usuarios WHERE id=?", id); }
    private static final RowMapper<Automovel> AUTO = (r,n) -> new Automovel(r.getLong("id"),
            r.getString("matricula"), r.getInt("ano"), r.getString("marca"), r.getString("modelo"),
            r.getString("placa"), r.getLong("proprietario_id"));
    public List<Automovel> automoveis() {
        return db.query("SELECT * FROM automoveis ORDER BY marca,modelo,ano", AUTO);
    }
    public boolean existeAutomovel(long id) {
        return Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM automoveis WHERE id=?)", Boolean.class, id));
    }
    public Automovel inserirAutomovel(AutomovelCadastro a, long dono) {
        long id = db.queryForObject("INSERT INTO automoveis(matricula,ano,marca,modelo,placa,proprietario_id) VALUES (?,?,?,?,?,?) RETURNING id",
                Long.class, a.matricula(), a.ano(), a.marca(), a.modelo(), a.placa().toUpperCase(), dono);
        return db.queryForObject("SELECT * FROM automoveis WHERE id=?", AUTO, id);
    }
    private static final String PEDIDOS = "SELECT p.*,u.nome AS cliente_nome,concat(a.marca,' ',a.modelo,' - ',a.placa) AS automovel FROM pedidos p JOIN usuarios u ON u.id=p.cliente_id JOIN automoveis a ON a.id=p.automovel_id";
    private static Pedido pedido(ResultSet r, int n) throws SQLException {
        long agente = r.getLong("agente_id");
        return new Pedido(r.getLong("id"), r.getLong("cliente_id"), r.getLong("automovel_id"),
                r.getDate("inicio").toLocalDate(), r.getDate("fim").toLocalDate(),
                r.getString("status"), r.getString("parecer"), r.wasNull() ? null : agente,
                r.getString("cliente_nome"), r.getString("automovel"));
    }
    public List<Pedido> pedidos(Long clienteId) {
        return clienteId == null ? db.query(PEDIDOS + " ORDER BY p.id DESC", AluguelRepository::pedido)
                : db.query(PEDIDOS + " WHERE p.cliente_id=? ORDER BY p.id DESC", AluguelRepository::pedido, clienteId);
    }
    public Optional<Pedido> pedido(long id) {
        return db.query(PEDIDOS + " WHERE p.id=?", AluguelRepository::pedido, id).stream().findFirst();
    }
    public Pedido inserirPedido(long cliente, PedidoCadastro p) {
        long id = db.queryForObject("INSERT INTO pedidos(cliente_id,automovel_id,inicio,fim) VALUES (?,?,?,?) RETURNING id",
                Long.class, cliente, p.automovelId(), p.inicio(), p.fim());
        return pedido(id).orElseThrow();
    }
    public Pedido alterarPedido(long id, PedidoCadastro p) {
        db.update("UPDATE pedidos SET automovel_id=?,inicio=?,fim=?,status='PENDENTE',parecer=NULL,agente_id=NULL WHERE id=?",
                p.automovelId(), p.inicio(), p.fim(), id);
        return pedido(id).orElseThrow();
    }
    public Pedido mudarStatus(long id, String status, String parecer, Long agente) {
        db.update("UPDATE pedidos SET status=?,parecer=?,agente_id=? WHERE id=?", status, parecer, agente, id);
        return pedido(id).orElseThrow();
    }
    public boolean temPedidos(long cliente) {
        return Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM pedidos WHERE cliente_id=?)", Boolean.class, cliente));
    }
    public boolean temAutomoveis(long proprietario) {
        return Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM automoveis WHERE proprietario_id=?)", Boolean.class, proprietario));
    }
    public void inserirCredito(long pedido, long banco, CreditoCadastro c) {
        db.update("INSERT INTO contratos_credito(pedido_id,banco_id,valor,parcelas) VALUES (?,?,?,?)",
                pedido, banco, c.valor(), c.parcelas());
    }
}
