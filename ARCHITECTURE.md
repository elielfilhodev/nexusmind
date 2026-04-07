# NexusMind — Arquitetura

## Visão geral

Monorepo com **dois deployáveis independentes**:

| Camada | Tecnologia | Deploy alvo |
|--------|------------|-------------|
| API | Spring Boot 3.x (Java 21) | Railway |
| Web | Next.js App Router (TypeScript) | Vercel |
| Dados | PostgreSQL | Neon |

Comunicação: **REST + JSON** (`NEXT_PUBLIC_API_URL` no browser, URL interna em SSR quando necessário). Sem autenticação nesta versão; a API permanece **stateless** e pronta para futuro `Authorization` sem reescrever domínio.

## Justificativa de stack

### Backend: Spring Data **JPA** (e não JDBC puro)

- **Domínio rico** (relatórios, seções, cache, patch): mapeamento objeto-relacional, `@Embeddable`/`@ElementCollection` e evolução de schema com Flyway combinam melhor com JPA.
- **Produtividade e ecossistema**: validação (`jakarta.validation`), transações declarativas, integração nativa com OpenAPI.
- **Trade-off**: JDBC seria mais fino para leituras massivas; quando houver ingestão em lote do Data Dragon, podemos acrescentar **projeções JDBC ou batch** na camada de infraestrutura sem trocar o modelo de domínio.

### Frontend: **Next.js App Router** (e não Astro)

- **Interatividade densa**: formulários (React Hook Form + Zod), TanStack Query, geração de relatório com estados de loading/erro/toast e possível streaming futuro.
- **RSC onde ajuda**: layouts estáticos e partes de marketing; client components nas telas de análise.

### IA: provedor plugável

- Porta `AiProvider` na aplicação; adaptador HTTP (OpenAI-compatible) para **OpenRouter**, **Groq** ou qualquer endpoint compatível.
- **Prompt engineering** isolado (`PromptCatalog`, builders por caso de uso).
- Resposta preferencialmente **JSON estruturado** + validação; fallback com mensagem segura e log sem conteúdo sensível.

### PDF

- Geração no **backend** (OpenPDF) a partir do JSON persistido — fonte única da verdade e exportação consistente.

## Árvore de pastas (alvo)

```
NexusMind/
├── ARCHITECTURE.md
├── README.md
├── docker-compose.yml
├── .github/workflows/ci.yml
├── backend/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/com/nexusmind/
│       ├── NexusMindApplication.java
│       ├── domain/model/              # entidades, enums
│       ├── application/
│       │   ├── dto/                   # DTOs de API / uso interno
│       │   ├── service/               # orquestração, casos de uso
│       │   ├── ai/                    # portas, prompts, parser
│       │   └── pdf/                   # porta de exportação
│       ├── infrastructure/
│       │   ├── persistence/           # Spring Data JPA
│       │   ├── ai/                    # cliente HTTP, retry, fallback
│       │   ├── pdf/                   # OpenPDF
│       │   ├── datadragon/            # stubs / sync futura
│       │   └── security/              # CORS, headers, rate limit
│       └── web/
│           ├── api/                   # REST controllers
│           └── error/                 # problem details, handlers
│   └── src/main/resources/
│       ├── application.yml
│       ├── application-dev.yml
│       ├── application-prod.yml
│       └── db/migration/              # Flyway
└── frontend/
    ├── Dockerfile
    ├── package.json
    ├── src/
    │   ├── app/                       # rotas App Router
    │   ├── features/                  # módulos por fluxo (casual, draft, reports, runes)
    │   ├── entities/                # tipos de domínio TS, mappers leves
    │   ├── shared/                  # ui (shadcn), lib, api client, hooks
    │   └── widgets/                 # composições reutilizáveis
    └── public/game-assets/            # estrutura para ícones (ver README)
```

## Camadas e responsabilidades

1. **domain**: regras e modelo persistente coerente; sem dependência de framework.
2. **application**: serviços que orquestram repositórios, IA e PDF; DTOs estáveis para a API.
3. **infrastructure**: adaptadores (DB, HTTP IA, PDF, futuro Data Dragon).
4. **web**: HTTP, validação de entrada (`@Valid`), mapeamento request/response.

## Segurança (sem login)

- Validação e limites de tamanho de body.
- **Rate limiting** por IP (Bucket4j + Caffeine).
- CORS explícito por perfil; headers de segurança (Spring Security em modo API).
- Erros sem stack trace vazado em produção; logs estruturados sem segredos.

## Dados de jogo (MVP)

- **Fonte atual**: seed Flyway + tabelas versionadas por `patch_versions`.
- **Extensão**: `GameDataProvider` (interface) com implementação `DatabaseGameDataProvider`; futuro `DataDragonGameDataProvider` / provedores de estatísticas documentados em código.

## Plano de implementação (fases)

1. Contrato OpenAPI implícito via controllers + springdoc; migrations + seeds.
2. Serviços de análise casual/draft persistindo JSON + seções.
3. Camada IA com JSON schema-like e fallback.
4. PDF e listagem de relatórios.
5. Frontend: home, casual, profissional, relatórios, runas.
6. CI, Docker, documentação de deploy Vercel/Railway/Neon.

## Observabilidade (preparado)

- Actuator: `health`, `info` (habilitado por perfil).
- Métricas e tracing podem ser adicionados via Micrometer/OpenTelemetry sem alterar domínio.
