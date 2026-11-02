# Modelo inicial — versão 1

Esta versão registra a análise de requisitos antes das decisões de implementação.

```mermaid
classDiagram
  class Usuario { nome; email; senha; perfil }
  class Cliente { rg; cpf; endereco; profissao }
  class Emprego { empregador; rendimento }
  class Agente { tipo }
  class Automovel { matricula; ano; marca; modelo; placa }
  class Pedido { inicio; fim; status; parecer }
  class ContratoCredito { valor; parcelas }
  Usuario <|-- Cliente
  Usuario <|-- Agente
  Cliente "1" --> "0..3" Emprego
  Cliente "1" --> "0..*" Pedido
  Pedido "*" --> "1" Automovel
  Pedido "1" --> "0..1" ContratoCredito
  Agente "1" --> "0..*" Pedido : avalia
```

## Ajustes necessários após a implementação

- Os perfis foram representados por um campo em `usuarios`, evitando herança entre tabelas.
- O cadastro de clientes foi separado em `clientes` e `empregos`.
- O status ganhou transições explícitas e o crédito ficou associado a banco e pedido.
