# auth

jwt based. register + login under `/api/auth`.

## endpoints

- POST `/api/auth/register`
  body: username, email, password, fullName (optional), dob (optional)
- POST `/api/auth/login`
  body: username, password
  returns: `{ token, user }`

frontend stores token in localStorage as `oj_token` and sends
`Authorization: Bearer ...` on later calls.

passwords hashed with bcrypt. roles: USER / ADMIN.

## seed users (first api boot)

| user | pass | role |
|------|------|------|
| ashmit | ashmit123 | USER |
| admin | admin123 | ADMIN |

seed runs only if users table is empty (see DataSeeder).

## notes to self

- jwt secret in application.yml is for local only, change before real deploy
- token expiry ~24h right now
- admin routes need role ADMIN
