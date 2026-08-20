# Online Judge

College project. Submit code for problems and get AC/WA/TLE etc.

Stack: Spring Boot (API + judge worker), React, Postgres, RabbitMQ, Redis.

## Run

```bash
docker compose up -d
mvn -DskipTests package
scripts\run-api.bat
scripts\run-judge.bat
cd apps/web
npm install
npm run dev
```

- UI: http://localhost:5173
- API: http://localhost:8080

## Login (seeded)

- ashmit / ashmit123
- admin / admin123

## Folder layout

- `apps/web` – frontend
- `services/api` – REST + websocket
- `services/judge` – runs submissions
- `services/common` – shared entities
- `docs/` – my notes

Judge supports Java, Python, C++. War room is basic 1v1/small group race.

If timezone error on Windows, scripts already set UTC for Java.
