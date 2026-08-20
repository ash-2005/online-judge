# Online Judge

College project. Submit code for problems and get AC/WA/TLE etc.

Stack: Spring Boot (API + judge worker), React, Postgres, RabbitMQ, Redis.

## Important about the GitHub Pages link

`https://ash-2005.github.io/online-judge/` can only host the **frontend** (static files).
The API + judge + database need a real server (your PC with docker, or Render/Railway later).
So the Pages site is the UI; login/submit work only when the API is running somewhere.

## Run locally (full app)

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
