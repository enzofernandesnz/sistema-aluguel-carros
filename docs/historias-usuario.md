# Histórias do usuário

| ID | Como | Quero | Para | Critério de aceite |
|---|---|---|---|---|
| HU01 | Visitante | criar minha conta de cliente | acessar o sistema | Nome, email, senha, RG, CPF, endereço e profissão obrigatórios; CPF e email únicos. |
| HU02 | Usuário cadastrado | entrar com email e senha | usar as funções do meu perfil | Rotas protegidas retornam 401 sem credenciais. |
| HU03 | Cliente | consultar e alterar meu cadastro | manter dados atuais | O cliente acessa apenas o próprio cadastro e pode informar até três empregadores. |
| HU04 | Cliente | excluir meu cadastro | encerrar minha conta | A exclusão é bloqueada quando há pedidos ou automóveis associados. |
| HU05 | Usuário | cadastrar e consultar automóveis | escolher um veículo | Matrícula e placa únicas; proprietário é o usuário autenticado. |
| HU06 | Cliente | criar e consultar meus pedidos | acompanhar o aluguel | Datas válidas; pedido começa PENDENTE; lista mostra status. |
| HU07 | Cliente | alterar ou cancelar pedido | corrigir ou desistir | Alteração apenas quando PENDENTE; cancelamento quando PENDENTE ou EM_ANALISE. |
| HU08 | Agente | consultar e avaliar pedidos | emitir parecer financeiro | Aprovar e recusar exigem parecer; transições inválidas são rejeitadas. |
| HU09 | Agente | marcar pedido aprovado como contratado | registrar execução | Somente APROVADO pode virar CONTRATADO. |
| HU10 | Banco | associar crédito a pedido aprovado | financiar o aluguel | Valor e parcelas positivos; um crédito por pedido. |

## Critérios transversais

- O frontend usa React e Vite; a API usa Spring Boot MVC e JDBC.
- Os dados persistem em PostgreSQL, incluindo o PostgreSQL do Supabase.
- A API identifica o usuário autenticado em cada requisição e aplica as permissões no servidor.
