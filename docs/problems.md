# problems

problems live in postgres. statement + limits + tags.
sample testcases shown on the problem page; hidden ones used only by judge.

## apis

- GET `/api/problems?difficulty=&tag=&company=&q=&page=&size=`
  paginated list
- GET `/api/problems/recent`
- GET `/api/problems/{idOrSlug}`
  detail + sample testcases only

admin (role ADMIN):
- POST `/api/admin/problems`
- PUT `/api/admin/problems/{id}`
- POST `/api/admin/testcases`

difficulty: EASY / MEDIUM / HARD
limits: timeLimitMs, memoryLimitMb

## seed problems

on empty db DataSeeder adds a few:
- two-sum
- a-plus-b
- palindrome-check
- fizzbuzz
- max-of-three

good ones to try first: `a-plus-b` and `max-of-three`.

## ui

`/problems` list with filters
`/problems/:idOrSlug` statement + monaco editor
admin page at `/admin` (admin login) to add problems/testcases
