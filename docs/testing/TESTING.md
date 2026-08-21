# Testing

## Manual (main path)

1. Start compose + api + judge + web
2. Login ashmit/ashmit123
3. Open problem `a-plus-b`
4. Submit Python:
```python
a,b=map(int,input().split())
print(a+b)
```
5. Expect ACCEPTED

Also tried: bad code → WRONG_ANSWER / CE as expected.

## Automated

```bash
# backend unit/IT (Testcontainers needs Docker)
mvn -pl services/api test

# frontend build
cd apps/web && npm run build
```

`AuthSmokeIT` boots API against temporary Postgres/Redis/Rabbit.

## CI

`.github/workflows/ci.yml` exists locally; push needs GitHub `workflow` token scope.
Until then CI file may stay untracked or be added later.
