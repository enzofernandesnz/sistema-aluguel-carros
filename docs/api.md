# API HTTP

Todas as rotas, exceto `POST /api/auth/cadastro`, exigem HTTP Basic com email e senha. Use HTTPS ao publicar. Respostas são JSON, salvo operações sem corpo. `401` indica ausência ou falha de autenticação, `403` falta de permissão, `404` recurso inexistente e `409` conflito de regra de negócio.

| Método | Rota | Perfil | Função |
|---|---|---|---|
| POST | `/api/auth/cadastro` | público | Criar cliente. |
| GET | `/api/auth/me` | todos | Identificar usuário autenticado. |
| GET, PUT, DELETE | `/api/clientes/me` | cliente | Consultar, atualizar ou excluir o próprio cadastro. |
| GET | `/api/clientes` | agente | Listar clientes. |
| GET, POST | `/api/automoveis` | todos | Listar ou cadastrar automóvel. |
| GET, POST | `/api/pedidos` | cliente (POST), todos (GET) | Criar ou listar pedidos visíveis. |
| GET, PUT | `/api/pedidos/{id}` | cliente proprietário (PUT), usuário autorizado (GET) | Consultar ou alterar pedido pendente. |
| POST | `/api/pedidos/{id}/cancelamento` | cliente proprietário | Cancelar pedido. |
| PATCH | `/api/pedidos/{id}/avaliacao` | agente | Analisar, aprovar, recusar ou contratar. |
| POST | `/api/pedidos/{id}/credito` | banco | Registrar crédito. |

Exemplo de cadastro:

```json
{"nome":"Maria Silva","email":"maria@example.com","senha":"senha-forte-123","rg":"MG123456","cpf":"12345678901","endereco":"Rua A, 10","profissao":"Analista","empregos":[{"empregador":"Empresa X","rendimento":5000.00}]}
```

Exemplo de criação de pedido:

```json
{"automovelId":1,"inicio":"2026-11-10","fim":"2026-11-15"}
```

Exemplo de avaliação:

```json
{"status":"APROVADO","parecer":"Renda compatível com o contrato."}
```
