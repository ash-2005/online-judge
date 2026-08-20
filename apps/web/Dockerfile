# build from repo root: docker build -f apps/web/Dockerfile .
# Default: empty VITE_API_BASE so the browser calls same-origin /api and /ws
# (nginx proxies those to api:8080). Override if the API is on a public URL:
#   docker build -f apps/web/Dockerfile --build-arg VITE_API_BASE=https://api.example.com .
FROM node:20-alpine AS build
WORKDIR /app

COPY apps/web/package.json apps/web/package-lock.json ./
RUN npm ci

COPY apps/web/ ./
ARG VITE_API_BASE=
ENV VITE_API_BASE=$VITE_API_BASE
RUN npm run build

FROM nginx:1.27-alpine
COPY apps/web/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
