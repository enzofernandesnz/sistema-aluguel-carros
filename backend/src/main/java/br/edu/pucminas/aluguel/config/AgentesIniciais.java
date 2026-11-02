package br.edu.pucminas.aluguel.config;

import br.edu.pucminas.aluguel.repository.AluguelRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AgentesIniciais {
    @Bean CommandLineRunner agentes(Environment env, AluguelRepository repo, PasswordEncoder encoder) {
        return args -> {
            criar(env, repo, encoder, "EMPRESA");
            criar(env, repo, encoder, "BANCO");
        };
    }
    private void criar(Environment env, AluguelRepository repo, PasswordEncoder encoder, String tipo) {
        String email = env.getProperty("AGENTE_" + tipo + "_EMAIL");
        String senha = env.getProperty("AGENTE_" + tipo + "_SENHA");
        if (email != null && !email.isBlank() && senha != null && senha.length() >= 8 && repo.conta(email).isEmpty())
            repo.inserirUsuario("Agente " + tipo.toLowerCase(), email, encoder.encode(senha), tipo);
    }
}
