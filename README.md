# Online Judge

Practice coding problems, submit Java / Python / C++, get real verdicts (AC, WA, TLE, RE, CE).
Built as a college + resume project: full stack, async judge, and a small live War Room race.

**Problem:** Most “OJ demos” only mock submit. Judges that actually run code need queues, isolation, and clear API/worker split.

**Solution:** Spring Boot API + separate judge worker over RabbitMQ, Postgres for data, Redis for rate limits / war-room fanout, React SPA with Monaco.

## Features

- Auth (JWT), roles USER / ADMIN
- Problem set with samples + hidden tests
- Async judging (practice + priority war queue)
- Profile stats, submissions history, leaderboard
- Discussion + company tags
- War Room (create/join, live events over WebSocket)
- Docker Compose for local infra; optional full-stack containers

## Stack

| Layer | Tech |
|-------|------|
| Web | React, Vite, Monaco, STOMP |
| API | Spring Boot 3, Java 21, JWT, WebSocket |
| Judge | Spring Boot worker, process sandbox (+ experimental Docker) |
| Data | PostgreSQL, Redis, RabbitMQ |

## Architecture (one glance)

```
Browser → API → Postgres
           ↓
        RabbitMQ → Judge → verdict in DB
War events: Redis pub/sub → API STOMP → Browser
```

Details: [docs/architecture/ARCHITECTURE.md](docs/architecture/ARCHITECTURE.md)

## Run locally

Need: Java 21, Maven, Node 20+, Docker Desktop.

```bash
docker compose up -d
mvn -DskipTests package
scripts\run-api.bat
scripts\run-judge.bat
cd apps/web
npm install
npm run dev
```

| What | URL |
|------|-----|
| UI | http://localhost:5173 |
| API | http://localhost:8080 |
| Swagger | http://localhost:8080/swagger-ui.html |
| RabbitMQ UI | http://localhost:15672 (oj/oj) |

**Seed logins:** `ashmit` / `ashmit123` · `admin` / `admin123`

Copy [.env.example](.env.example) if you override defaults. Setup notes: [docs/setup/SETUP.md](docs/setup/SETUP.md)

## GitHub Pages

https://ash-2005.github.io/online-judge/ is **frontend only** (branch `gh-pages`).
Login/submit need API + judge running (local or a real host). See [docs/pages.md](docs/pages.md).

## Docs

| Doc | Path |
|-----|------|
| Status | [docs/PROJECT_STATUS.md](docs/PROJECT_STATUS.md) |
| Architecture | [docs/architecture/ARCHITECTURE.md](docs/architecture/ARCHITECTURE.md) |
| API | [docs/api/API.md](docs/api/API.md) |
| Database | [docs/database/SCHEMA.md](docs/database/SCHEMA.md) |
| Testing | [docs/testing/TESTING.md](docs/testing/TESTING.md) |
| Decisions | [docs/decisions/](docs/decisions/) |
| Milestones | [docs/milestones/](docs/milestones/) |
| 1-week push plan | [docs/COMMIT_PLAN.md](docs/COMMIT_PLAN.md) |

## Repo layout

```
apps/web/           React UI
services/api/       REST + JWT + WS
services/judge/     queue consumers + sandbox
services/common/    entities / messages
docs/               architecture, milestones, notes
scripts/            local run helpers
docker-compose.yml  Postgres, Redis, RabbitMQ
```

## Testing

See [docs/testing/TESTING.md](docs/testing/TESTING.md). Quick check: seed user → A+B Python → ACCEPTED.

## Future

- Host API/judge for a true public live link
- Stronger sandbox (isolate / cgroup)
- Timed contests

## License

Project for academic / portfolio use.
