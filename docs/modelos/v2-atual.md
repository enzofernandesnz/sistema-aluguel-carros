# Modelo atualizado — versão 2

## Casos de uso

```mermaid
flowchart LR
  C[Cliente] --> L([Autenticar])
  C --> CC([Gerir próprio cadastro])
  C --> AC([Cadastrar automóvel])
  C --> P([Criar, consultar, alterar e cancelar pedido])
  E[Agente empresa] --> L
  E --> AC
  E --> A([Analisar e avaliar pedido])
  E --> CO([Formalizar contrato])
  B[Agente banco] --> L
  B --> A
  B --> CO
  B --> CR([Conceder crédito])
```

## Diagrama de classes / dados

```mermaid
classDiagram
  class Usuario { +long id; +String nome; +String email; +String senhaHash; +Perfil perfil }
  class Cliente { +long usuarioId; +String rg; +String cpf; +String endereco; +String profissao }
  class Emprego { +long id; +String empregador; +Decimal rendimento }
  class Automovel { +long id; +String matricula; +int ano; +String marca; +String modelo; +String placa; +long proprietarioId }
  class Pedido { +long id; +LocalDate inicio; +LocalDate fim; +Status status; +String parecer }
  class ContratoCredito { +long id; +Decimal valor; +int parcelas }
  Usuario "1" --> "0..1" Cliente : cadastro
  Cliente "1" --> "0..3" Emprego : vínculos
  Cliente "1" --> "0..*" Pedido : solicita
  Usuario "1" --> "0..*" Automovel : possui
  Automovel "1" --> "0..*" Pedido : solicitado
  Usuario "0..1" --> "0..*" Pedido : avalia
  Pedido "1" --> "0..1" ContratoCredito : crédito
  Usuario "1" --> "0..*" ContratoCredito : banco
```

## Diagrama de pacotes / visão lógica

```mermaid
flowchart LR
  F[frontend / React + Vite] --> C[controller / API MVC]
  C --> S[service / regras e permissões]
  S --> R[repository / SQL]
  R --> D[(Supabase PostgreSQL)]
  S --> M[model / DTOs]
  C --> M
  SEC[config / Spring Security] --> C
  SEC --> R
```

## Estados do pedido

```mermaid
stateDiagram-v2
  [*] --> PENDENTE
  PENDENTE --> EM_ANALISE
  PENDENTE --> APROVADO
  PENDENTE --> RECUSADO
  PENDENTE --> CANCELADO
  EM_ANALISE --> APROVADO
  EM_ANALISE --> RECUSADO
  EM_ANALISE --> CANCELADO
  APROVADO --> CONTRATADO
```

O código usa `usuarios.perfil` para CLIENTE, EMPRESA e BANCO. O backend valida cada transição; o frontend apenas oferece os botões correspondentes.
