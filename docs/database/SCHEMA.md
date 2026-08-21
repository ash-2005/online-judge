# Database

Postgres. Hibernate `ddl-auto=update` for local (tables created on boot).

## Main tables

- `users` – username, email, password_hash, role
- `problems` – title, slug, statement, difficulty, limits
- `problem_tags` – element collection
- `problem_company_tag_counts` – denormalized company counts
- `testcases` – input, expected_output, is_sample
- `submissions` – code, language, status, runtime, war_room_id
- `discussions` – threaded via parent_id
- `problem_company_tags` – unique (problem, user, company)
- `war_rooms` / `war_room_participants`

## Status values (submissions)

PENDING, RUNNING, ACCEPTED, WRONG_ANSWER, TIME_LIMIT_EXCEEDED,
MEMORY_LIMIT_EXCEEDED, RUNTIME_ERROR, COMPILATION_ERROR

## Indexes / constraints

- unique username, email, problem slug
- unique company tag per user/problem
- unique war room participant

## Seed

See DataSeeder: admin/ashmit users + 5 problems when DB empty.
