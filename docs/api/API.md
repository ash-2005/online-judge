# API overview

Base: `http://localhost:8080`

Auth header: `Authorization: Bearer <token>`

## Public / light auth

| Method | Path | Notes |
|--------|------|-------|
| POST | `/api/auth/register` | create user |
| POST | `/api/auth/login` | returns token + user |
| GET | `/api/problems` | filters: difficulty, tag, company, q, page |
| GET | `/api/problems/recent` | |
| GET | `/api/problems/{idOrSlug}` | samples only |
| GET | `/api/stats/summary` | |
| GET | `/api/companies` | |
| GET | `/api/companies/{name}/problems` | |
| GET | `/api/leaderboard` | |
| GET | `/actuator/health` | |
| GET | `/swagger-ui.html` | |

## Logged in

| Method | Path |
|--------|------|
| GET/PATCH | `/api/users/me` |
| GET | `/api/users/me/submissions` |
| GET | `/api/users/me/stats` |
| POST | `/api/submissions` |
| GET | `/api/submissions/{id}` |
| GET/POST | `/api/problems/{id}/discussions` |
| POST | `/api/discussions/{id}/upvote` |
| POST | `/api/problems/{id}/company-tags` |
| POST | `/api/warrooms` |
| GET | `/api/warrooms` |
| GET | `/api/warrooms/code/{code}` |
| POST | `/api/warrooms/{code}/join` |

## Admin

| Method | Path |
|--------|------|
| POST | `/api/admin/problems` |
| PUT | `/api/admin/problems/{id}` |
| POST | `/api/admin/testcases` |

## Submit body

```json
{ "problemId": 1, "language": "PYTHON", "code": "...", "warRoomId": null }
```

Languages: `JAVA` | `PYTHON` | `CPP`

More detail: `docs/auth.md`, `docs/problems.md`
