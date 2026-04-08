# NexusMind

Monorepo **production-ready** para análise estratégica de **League of Legends** com IA (provedor plugável), backend **Java 21 + Spring Boot**, frontend **Next.js (App Router) + shadcn/ui**, banco **PostgreSQL** (Neon em produção), **Flyway**, exportação **PDF**, **sem autenticação** nesta versão.

Documentação de arquitetura: [ARCHITECTURE.md](./ARCHITECTURE.md).

## Stack e decisões (resumo)

| Área | Escolha | Motivo |
|------|---------|--------|
| Persistência | **Spring Data JPA** | Modelo de domínio e relatórios JSON evoluem melhor com ORM + Flyway; JDBC fino pode entrar depois na ingestão em lote. |
| Frontend | **Next.js App Router** | Formulários interativos, TanStack Query, futuro streaming; Astro seria melhor só para site estático. |
| IA | **HTTP OpenAI-compatible** | OpenRouter / Groq / OpenAI com a mesma integração; variáveis `AI_*`. |
| Rate limit | **Caffeine + janela 1 min** | Sem dependência extra instável; troque por Redis em escala horizontal. |

## Pré-requisitos

- Java **21**, Maven **3.9+**
- Node **20+** (recomendado **22**)
- Docker (opcional) para Postgres local

## Desenvolvimento local

### 1. Banco de dados

```bash
docker compose up -d
```

Isso sobe Postgres em `localhost:5432` (usuário/senha/db `nexusmind`).

### 2. Backend

```bash
cd backend
cp .env.example .env
# Edite .env — defina AI_API_KEY para respostas reais da IA (OpenRouter etc.)
mvn spring-boot:run
```

- API: `http://localhost:8080`
- Swagger: `http://localhost:8080/swagger-ui/index.html`
- Health: `GET http://localhost:8080/api/health`

### 3. Frontend

```bash
cd frontend
cp .env.example .env.local
npm install
npm run dev
```

- App: `http://localhost:3000`
- `NEXT_PUBLIC_API_URL` deve apontar para a API (padrão `http://localhost:8080`).

## Deploy sugerido

| Componente | Plataforma | Notas |
|------------|------------|--------|
| Frontend | **Vercel** | Root `frontend`, env `NEXT_PUBLIC_API_URL` = URL pública da API. |
| Backend | **Railway** | JAR Spring; env `DATABASE_URL` no formato JDBC ou variáveis separadas conforme plugin Postgres. |
| Banco | **Neon** | Connection string Postgres; Flyway roda no startup. |

**CORS:** defina `APP_CORS_ALLOWED_ORIGINS` com a origem do site (ex.: `https://app.seudominio.com`).

**Produção:** use `SPRING_PROFILES_ACTIVE=prod` (cookies seguros, menos detalhe em erros).

## Segurança e escalabilidade

- Validação Bean Validation nas entradas; corpo JSON limitado (~512KB).
- Rate limit em `POST /api/analysis/*` por IP (header `X-Forwarded-For` quando atrás de proxy).
- Rate limit separado para `GET/POST /api/leaderboard`, `/api/players`, `/api/matches` (módulo competitivo; `APP_COMPETITIVE_RATE_LIMIT_PER_MINUTE`).
- CORS explícito; headers `X-Frame-Options: DENY`.
- Logs sem vazar API keys; falhas de IA retornam fallback heurístico.
- Para múltiplas instâncias da API: substituir o rate limit em memória por Redis ou gateway.

## Módulo competitivo (Riot API)

Tela dedicada em `/competitive/leaderboard` e perfis em `/competitive/player/{REGION}/{PUUID}` (ex.: `BR1`). O backend integra a **Riot API** com camada desacoplada (`RiotApiClient`, serviços de cache, rate limit interno e retries), **Caffeine** (`spring-boot-starter-cache`) para leaderboard, summoner, partidas e entradas de liga, e **PostgreSQL** para cache persistente de análises IA (`competitive_ai_cache`) e cadastro manual de **jogadores profissionais** (`pro_player`).

**Variáveis:** `RIOT_API_KEY` (obrigatória para dados live), `APP_COMPETITIVE_RATE_LIMIT_PER_MINUTE` (proteção do endpoint), demais `RIOT_*` em `backend/.env.example`.

**Limitações da Riot API:** não existe um “leaderboard global” público; usamos ligas **Challenger + Grandmaster** por fila (Solo/Flex). **Peak elo histórico por season** não é exposto de forma completa pela API — o perfil mostra snapshot atual de liga e agrega estatísticas por **temporada aproximada** (cortes de data) a partir do histórico de partidas.

**Assets visuais:** ícones de campeão/itens/runas e splashes seguem o **Data Dragon** (`ddragon.leagueoflegends.com`); emblemas de elo usam o caminho público de tier do CDN. Atualize o patch no banco (`PATCH`) para alinhar versões do CDN com o app.

### Rotas REST (competitivo)

- `GET /api/leaderboard/regions`
- `GET /api/leaderboard?region=BR1&queue=RANKED_SOLO_5x5&page=0&size=20&sort=LP_DESC&prosOnly=false`
- `GET /api/players/search?gameName=&tagLine=&region=BR1`
- `GET /api/players/{region}/{puuid}` — perfil agregado
- `GET /api/players/{region}/{puuid}/seasons`
- `GET /api/players/{region}/{puuid}/matches?...` — filtros: `season`, `championId`, `queueId`, `outcome`
- `GET /api/players/{region}/{puuid}/champions` | `/peak-ranks`
- `GET /api/matches/{matchId}?region=&puuid=`
- `POST /api/matches/{matchId}/ai-analysis?region=&puuid=`
- `POST /api/players/{region}/{puuid}/ai-analysis`

Documentação interativa: **Swagger** em `/swagger-ui/index.html`.

## API (principais rotas)

- `GET /api/health`
- `GET /api/patch/current`
- `GET /api/champions` | `/api/items` | `/api/runes` | `/api/summoner-spells`
- `POST /api/analysis/casual`
- `POST /api/analysis/draft`
- `GET /api/reports?page=0&size=20`
- `GET /api/reports/{id}?kind=CASUAL|DRAFT`
- `GET /api/reports/{id}/pdf?kind=CASUAL|DRAFT`

## Testes

```bash
cd backend && mvn test
cd frontend && npm run build
```

## Convenções de commit (sugestão)

Formato **Conventional Commits**: `feat:`, `fix:`, `chore:`, `docs:`, `refactor:`.

## Licença e dados do jogo

NexusMind não é afiliado à Riot Games. Ícones e dados de campeões devem seguir os termos da Riot; use Data Dragon como fonte oficial para assets.
