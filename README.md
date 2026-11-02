# AlugaMais — Sistema de Aluguel de Carros

![Logo AlugaMais](docs/logo.svg)

Protótipo web para cadastro de clientes, automóveis e pedidos de aluguel, com análise financeira por agentes e registro opcional de crédito bancário. Desenvolvido para o Laboratório 02 da disciplina DIAW (Engenharia de Software). A organização deste README segue o [template indicado no enunciado](https://github.com/joaopauloaramuni/desenvolvimento-e-integracao-de-aplicacoes-web/blob/main/TEMPLATES/template_README.md).

## Status do projeto

Protótipo funcional preparado para execução local ou com Supabase PostgreSQL. A publicação e a configuração de um projeto Supabase dependem das credenciais do responsável. O backend requer JDK 25 e o frontend, Node.js com npm.

## Índice

- [Sobre o projeto](#sobre-o-projeto)
- [Funcionalidades principais](#funcionalidades-principais)
- [Tecnologias utilizadas](#tecnologias-utilizadas)
- [Arquitetura e modelos](#arquitetura-e-modelos)
- [Instalação e execução](#instalação-e-execução)
- [Demonstração](#demonstração)
- [Testes](#testes)
- [Documentação](#documentação)
- [Autores e contribuição](#autores-e-contribuição)

## Sobre o projeto

O AlugaMais centraliza pedidos de aluguel de automóveis. Clientes autenticados mantêm seus dados e acompanham o status de suas solicitações. Agentes de empresas e bancos analisam os pedidos e podem formalizar contratos; agentes bancários também associam um contrato de crédito. O projeto é um protótipo acadêmico e não processa pagamentos nem assinatura jurídica.

## Funcionalidades principais

- Cadastro e login de clientes; senhas armazenadas com BCrypt.
- Agentes de empresa e banco criados por configuração segura do servidor.
- CRUD do cadastro do cliente, com RG, CPF, endereço, profissão e até três vínculos de emprego/rendimento.
- Cadastro e consulta de automóveis com matrícula, ano, marca, modelo, placa e proprietário.
- Criação, consulta, alteração e cancelamento de pedidos pelo cliente proprietário.
- Análise, aprovação, recusa e contratação de pedidos por agentes, com parecer financeiro.
- Crédito opcional concedido por agente bancário para pedido aprovado.
- Permissões validadas no servidor e interface responsiva.

## Tecnologias utilizadas

| Camada | Tecnologia |
|---|---|
| Frontend | React 19, Vite 7, CSS |
| Backend | Java 25, Spring Boot 4.0.9, Spring MVC, Spring Security, Spring JDBC, Maven |
| Banco de dados | PostgreSQL 17 local ou Supabase PostgreSQL |
| Autenticação | HTTP Basic sobre HTTPS; BCrypt; sem senha persistida no navegador |

## Arquitetura e modelos

O frontend consome a API REST MVC. O `controller` recebe e valida dados; o `service` aplica permissões e transições; o `repository` executa SQL parametrizado no PostgreSQL. O esquema é criado por [`schema.sql`](backend/src/main/resources/schema.sql) no início da aplicação.

As versões dos modelos estão preservadas:

- [Modelo inicial v1](docs/modelos/v1-inicial.md): primeiras entidades e relacionamentos.
- [Modelo atualizado v2](docs/modelos/v2-atual.md): casos de uso, classes, pacotes e estados alinhados ao código.
- [Histórias do usuário](docs/historias-usuario.md): regras e critérios de aceite.
- [Referência da API](docs/api.md): rotas e exemplos.

As alterações do modelo foram motivadas pela separação do cadastro de cliente, pela representação explícita dos perfis e pelas regras de transição do pedido. O PostgreSQL registra os relacionamentos por chaves estrangeiras.

## Instalação e execução

### Pré-requisitos

- JDK 25 e Apache Maven 3.9+.
- Node.js 20+ e npm.
- PostgreSQL 17 local (opcionalmente via Docker) ou projeto Supabase com conexão PostgreSQL habilitada.

### Variáveis de ambiente

Configure as variáveis no terminal/ambiente do backend, seguindo [`.env.example`](.env.example). O Spring não carrega esse arquivo automaticamente.

| Variável | Finalidade |
|---|---|
| `DATABASE_URL` | URL JDBC, por exemplo `jdbc:postgresql://localhost:5432/aluguel_carros`. Para Supabase, use o host e porta da conexão PostgreSQL do seu projeto e `?sslmode=require`. |
| `DATABASE_USER` | Usuário PostgreSQL. |
| `DATABASE_PASSWORD` | Senha PostgreSQL. |
| `AGENTE_EMPRESA_EMAIL`, `AGENTE_EMPRESA_SENHA` | Cria o agente empresa no primeiro início. |
| `AGENTE_BANCO_EMAIL`, `AGENTE_BANCO_SENHA` | Cria o agente banco no primeiro início. |
| `PORT` | Porta da API, padrão `8080`. |

As senhas dos agentes devem ter no mínimo oito caracteres. O seed não altera a senha de agente já cadastrado. No Supabase, prefira a conexão direta ou Session Pooler compatível com JDBC. Não coloque a senha do banco nem credenciais de agente em variáveis `VITE_`, pois elas são expostas ao navegador.

### Banco de dados local

```bash
docker compose up -d
```

O `compose.yaml` usa credenciais de desenvolvimento. Para Supabase, crie o projeto na sua conta, obtenha os parâmetros de conexão em **Project Settings → Database**, configure `DATABASE_URL`, `DATABASE_USER` e `DATABASE_PASSWORD` e inicie o backend. O arquivo `schema.sql` criará as tabelas na primeira inicialização. O banco Supabase real não está provisionado neste repositório.

### Backend

```bash
cd backend
mvn spring-boot:run
```

### Frontend

Em outro terminal:

```bash
cd frontend
npm install
npm run dev
```

Acesse `http://localhost:5173`. O Vite encaminha `/api` para `http://localhost:8080`. Para hospedar separadamente, configure um proxy reverso de `/api` para o backend e obrigatoriamente HTTPS. A interface guarda a senha somente em memória até sair ou atualizar a página; atualizar a página exige novo login.

## Demonstração

1. Cadastre um cliente na tela inicial e entre automaticamente.
2. Em **Automóveis**, cadastre um veículo.
3. Em **Pedidos**, selecione o veículo e o período; acompanhe o status `PENDENTE`.
4. Entre com o agente empresa ou banco configurado no servidor para analisar e aprovar/recusar o pedido.
5. Após aprovação, marque como `CONTRATADO`; com um banco, registre o crédito opcional.

## Testes

```bash
cd frontend && npm run build
cd ../backend && mvn test
```

Para uma verificação manual da API, confira os exemplos em [docs/api.md](docs/api.md). As regras relevantes são CPF e email únicos, datas válidas, acesso do cliente apenas aos próprios pedidos, parecer obrigatório para decisão financeira e transições válidas do status.

## Documentação

- [Template do professor](https://github.com/joaopauloaramuni/desenvolvimento-e-integracao-de-aplicacoes-web/blob/main/TEMPLATES/template_README.md)
- [Spring Boot 4](https://docs.spring.io/spring-boot/4.0/reference/)
- [Spring Security](https://docs.spring.io/spring-security/reference/)
- [React](https://react.dev/)
- [Vite](https://vite.dev/)
- [Supabase PostgreSQL](https://supabase.com/docs/guides/database/connecting-to-postgres)

## Autores e contribuição

Projeto acadêmico de Engenharia de Software. Para colaborar, crie uma branch, mantenha diagramas e histórias alinhados às mudanças de código e abra uma revisão antes de integrar. Não envie arquivos `.env`, senhas, `node_modules` ou artefatos de build ao Git.

## Licença

Uso acadêmico. Uma licença de redistribuição ainda não foi definida pelo responsável pelo repositório.
