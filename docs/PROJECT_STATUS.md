# Project status

Last updated: Aug 2026

## What this is

Online Judge: practice problems, submit code (Java/Python/C++), get verdicts.
Also has discussion, company tags, leaderboard, admin, and a basic War Room.

## Completed

- Auth (JWT register/login, roles USER/ADMIN)
- Problems + sample/hidden testcases + admin CRUD
- Async judging via RabbitMQ (practice + war queues)
- Process sandbox judge (Docker mode optional/experimental)
- React UI: list, editor, verdict poll, profile stats, submissions
- Discussion + company tags + companies browse
- War Room lobby/live + Redis → WebSocket events
- Leaderboard, rate limits, actuator health, swagger
- Docker Compose for Postgres/Redis/RabbitMQ
- Dockerfiles + optional full compose profile
- GitHub Pages UI deploy (frontend only)
- Seed users + sample problems

## Architecture (short)

Browser → Spring API → Postgres  
Submit → RabbitMQ → Judge worker → write verdict  
War Room events: Judge/API → Redis pub/sub → API STOMP → browser

## Tests

- Manual smoke: login, list problems, submit A+B → ACCEPTED
- `AuthSmokeIT` (Testcontainers) present; needs Docker to run
- Frontend `npm run build` succeeds
- Maven `package` succeeds

## Known issues / limits

- GitHub Pages cannot run API/judge (static UI only)
- Process sandbox is for demo, not a hard jail
- War Room is demo-grade, not contest-scale
- Windows needs `JAVA_TOOL_OPTIONS=-Duser.timezone=UTC` (scripts set this)
- Full cloud deploy of API+judge not finished yet

## Deployment state

- Code: https://github.com/ash-2005/online-judge
- Pages UI: https://ash-2005.github.io/online-judge/ (set branch to `gh-pages`)
- Local full stack via compose + jars/scripts

## Next (optional)

- Host API/judge on Render/Railway/VPS for a real live submit link
- More languages / isolate sandbox
- Timed contests
