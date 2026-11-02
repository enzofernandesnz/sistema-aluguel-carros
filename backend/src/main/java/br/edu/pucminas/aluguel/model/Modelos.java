package br.edu.pucminas.aluguel.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class Modelos {
    private Modelos() {}

    public record Emprego(@NotBlank String empregador, @NotNull @DecimalMin("0.00") BigDecimal rendimento) {}
    public record Cadastro(@NotBlank String nome, @Email @NotBlank String email,
            @NotBlank @Size(min=8) String senha, @NotBlank String rg,
            @NotBlank @Pattern(regexp="\\d{11}", message="CPF deve ter 11 dígitos") String cpf,
            @NotBlank String endereco, @NotBlank String profissao,
            @Size(max=3) List<@Valid Emprego> empregos) {}
    public record ClienteEdicao(@NotBlank String nome, @NotBlank String rg,
            @NotBlank @Pattern(regexp="\\d{11}") String cpf, @NotBlank String endereco,
            @NotBlank String profissao, @Size(max=3) List<@Valid Emprego> empregos) {}
    public record Cliente(long id, String nome, String email, String rg, String cpf,
            String endereco, String profissao, List<Emprego> empregos) {}
    public record Usuario(long id, String nome, String email, String perfil) {}
    public record AutomovelCadastro(@NotBlank String matricula, @Min(1886) @Max(2100) int ano,
            @NotBlank String marca, @NotBlank String modelo, @NotBlank String placa) {}
    public record Automovel(long id, String matricula, int ano, String marca, String modelo,
            String placa, long proprietarioId) {}
    public record PedidoCadastro(@NotNull Long automovelId, @NotNull LocalDate inicio,
            @NotNull LocalDate fim) {}
    public record Avaliacao(@NotNull String status, String parecer) {}
    public record Pedido(long id, long clienteId, long automovelId, LocalDate inicio,
            LocalDate fim, String status, String parecer, Long agenteId,
            String clienteNome, String automovel) {}
    public record CreditoCadastro(@NotNull @DecimalMin("0.01") BigDecimal valor,
            @NotNull @Min(1) Integer parcelas) {}
}
