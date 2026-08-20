# github pages (frontend only)

the pages site can only host the react build, not spring/docker.

## publish UI

1. build:
```bash
cd apps/web
npm install
npm run build
```
vite `base` is already `/online-judge/`

2. put `apps/web/dist` on the `gh-pages` branch (root), include `.nojekyll` and copy `index.html` to `404.html` for refresh routes.

3. repo Settings → Pages:
   - source: Deploy from a branch
   - branch: **gh-pages** / (root)
   - not `main` (main shows the readme)

site: https://ash-2005.github.io/online-judge/

login/submit need the API running somewhere (local docker or later render/railway).
