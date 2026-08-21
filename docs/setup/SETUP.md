# Setup

## Need

- Java 21+
- Maven 3.9+ (or `tools/apache-maven-3.9.6` if present locally)
- Node 20+
- Docker Desktop

## Steps

1. Infra
```bash
docker compose up -d
```

2. Build
```bash
mvn -DskipTests package
```

3. API + judge (Windows scripts set UTC)
```bash
scripts\run-api.bat
scripts\run-judge.bat
```

4. Web
```bash
cd apps/web
npm install
npm run dev
```

Open http://localhost:5173

## Env

Copy ideas from root `.env.example`.
Defaults in `services/*/src/main/resources/application.yml` work with compose.

## Seed login

- ashmit / ashmit123
- admin / admin123
