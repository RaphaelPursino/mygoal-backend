# 🎯 MyGoal — Backend

API REST do MyGoal, uma plataforma SaaS de gestão de metas pessoais com geração de missões diárias por Inteligência Artificial.

## 📋 Sobre o Projeto

O MyGoal permite que usuários cadastrem metas pessoais com prazo definido e recebam automaticamente **3 missões diárias geradas por IA** para ajudá-los a alcançar seus objetivos. A barra de progresso avança conforme as missões são concluídas.

## ✨ Funcionalidades

- 🔐 Autenticação com e-mail/senha e login social com Google OAuth2
- 🎯 CRUD completo de metas com data limite
- 🤖 Geração automática de missões diárias via IA (Groq API + LLaMA 3.1)
- 📊 Cálculo automático de progresso ao concluir missões
- 🔄 Agendamento diário de novas missões via Spring Scheduler
- 🛡️ Segurança com JWT stateless e BCrypt para senhas

## 🛠️ Stack Tecnológica

| Tecnologia | Versão | Uso |
|---|---|---|
| Java | 21 | Linguagem principal |
| Spring Boot | 3.2.5 | Framework principal |
| Spring Security | 6.x | Autenticação e autorização |
| Spring Data JPA | 3.x | ORM e acesso ao banco |
| Spring WebFlux | 3.x | Cliente HTTP reativo (Groq API) |
| PostgreSQL | 15+ | Banco de dados |
| Flyway | 9.x | Migrations do banco |
| JWT (JJWT) | 0.12.5 | Tokens de autenticação |
| Lombok | latest | Redução de boilerplate |
| Maven | 3.9+ | Gerenciamento de dependências |
| Docker | latest | Containerização para deploy |

## 🏗️ Arquitetura

```
src/main/java/com/mygoal/
├── controller/          # Endpoints REST da API
│   ├── AuthController.java
│   ├── GoalController.java
│   └── GlobalExceptionHandler.java
├── service/             # Lógica de negócio
│   ├── AuthService.java
│   ├── GoalService.java
│   ├── MissionService.java
│   └── AIService.java
├── repository/          # Acesso ao banco de dados
│   ├── UserRepository.java
│   ├── GoalRepository.java
│   └── MissionRepository.java
├── entity/              # Entidades JPA (tabelas do banco)
│   ├── User.java
│   ├── Goal.java
│   └── Mission.java
├── dto/                 # Objetos de transferência de dados
│   ├── auth/
│   └── goal/
├── security/            # Configurações de segurança
│   ├── SecurityConfig.java
│   ├── JwtService.java
│   ├── JwtAuthFilter.java
│   └── OAuth2SuccessHandler.java
└── scheduler/           # Tarefas agendadas
    └── MissionScheduler.java

src/main/resources/
├── application.yml      # Configurações da aplicação
└── db/migration/        # Scripts SQL do Flyway
    ├── V1__create_tables.sql
    └── V2__remove_notification_logs.sql
```

## 🔌 Endpoints da API

### Autenticação (público)
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/v1/auth/register` | Cadastro com e-mail e senha |
| POST | `/api/v1/auth/login` | Login com e-mail e senha |
| GET | `/api/v1/auth/health` | Health check |
| GET | `/oauth2/authorization/google` | Inicia fluxo OAuth2 Google |

### Metas (requer JWT)
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/v1/goals` | Lista todas as metas do usuário |
| POST | `/api/v1/goals` | Cria nova meta (dispara geração de missões) |
| GET | `/api/v1/goals/{id}` | Detalhe de uma meta com missões |
| DELETE | `/api/v1/goals/{id}` | Remove meta e suas missões |
| PATCH | `/api/v1/goals/missions/{id}/complete` | Marca missão como concluída |

## ⚙️ Variáveis de Ambiente

Crie um arquivo `.env` na raiz do projeto (nunca commite esse arquivo):

```env
# Banco de dados
DB_HOST=localhost
DB_PORT=5432
DB_NAME=mygoal
DB_USER=mygoal_user
DB_PASSWORD=sua_senha

# JWT
JWT_SECRET=sua-chave-secreta-longa-com-minimo-256-bits

# Google OAuth2 (console.cloud.google.com)
GOOGLE_CLIENT_ID=seu-client-id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=seu-client-secret

# Groq API (console.groq.com)
GROQ_API_KEY=sua-chave-groq

# URLs
FRONTEND_URL=http://localhost:4200
APP_BASE_URL=http://localhost:8080
```

## 🚀 Como Rodar Localmente

### Pré-requisitos
- Java 21+
- Maven 3.9+
- PostgreSQL 15+

### 1. Clone o repositório
```bash
git clone https://github.com/seu-usuario/mygoal-backend.git
cd mygoal-backend
```

### 2. Crie o banco de dados
```sql
CREATE DATABASE mygoal;
CREATE USER mygoal_user WITH PASSWORD 'sua_senha';
GRANT ALL PRIVILEGES ON DATABASE mygoal TO mygoal_user;

\c mygoal
GRANT ALL ON SCHEMA public TO mygoal_user;
```

### 3. Configure as variáveis de ambiente no IntelliJ
Vá em **Run → Edit Configurations → Environment Variables** e adicione todas as variáveis do `.env`.

### 4. Rode o projeto
```bash
mvn spring-boot:run
```

A API estará disponível em `http://localhost:8080`

## 🐳 Docker

```bash
# Build da imagem
docker build -t mygoal-backend .

# Rodar o container
docker run -p 8080:8080 \
  -e DB_HOST=host.docker.internal \
  -e DB_PORT=5432 \
  -e DB_NAME=mygoal \
  -e DB_USER=mygoal_user \
  -e DB_PASSWORD=sua_senha \
  -e JWT_SECRET=sua-chave-secreta \
  -e GROQ_API_KEY=sua-chave-groq \
  -e GOOGLE_CLIENT_ID=seu-client-id \
  -e GOOGLE_CLIENT_SECRET=seu-client-secret \
  -e FRONTEND_URL=http://localhost:4200 \
  -e APP_BASE_URL=http://localhost:8080 \
  mygoal-backend
```

## 🗄️ Modelo do Banco de Dados

```
users
├── id (UUID, PK)
├── email (VARCHAR, UNIQUE)
├── name (VARCHAR)
├── password_hash (VARCHAR, nullable para OAuth2)
├── provider (LOCAL | GOOGLE)
├── google_id (VARCHAR, nullable)
├── avatar_url (TEXT, nullable)
└── notifications_enabled (BOOLEAN)

goals
├── id (UUID, PK)
├── user_id (UUID, FK → users)
├── title (VARCHAR)
├── notes (TEXT, nullable)
├── target_date (DATE)
├── status (ACTIVE | COMPLETED | ABANDONED)
├── progress_percentage (INTEGER, 0-100)
├── total_missions (INTEGER)
└── completed_missions (INTEGER)

missions
├── id (UUID, PK)
├── goal_id (UUID, FK → goals)
├── title (VARCHAR)
├── description (TEXT)
├── mission_date (DATE)
├── completed (BOOLEAN)
└── completed_at (TIMESTAMP, nullable)
```

## 🔒 Segurança

- Senhas criptografadas com **BCrypt** (strength 12)
- Autenticação via **JWT** com validade de 24h
- API **stateless** — sem sessão no servidor
- **CORS** configurado para permitir apenas o frontend autorizado
- Todas as rotas protegidas exceto `/api/v1/auth/**`

## 📦 Deploy

O backend está configurado para deploy no **Render.com** via Docker.

Configure as variáveis de ambiente no painel do Render e conecte o repositório GitHub para deploy automático a cada push na branch `main`.

> **Importante:** No plano gratuito do Render, o servidor dorme após 15 minutos sem uso. Configure o [cron-job.org](https://cron-job.org) para fazer uma requisição ao `/api/v1/auth/health` a cada 10 minutos.

## 📄 Licença

Este projeto está sob a licença MIT.
