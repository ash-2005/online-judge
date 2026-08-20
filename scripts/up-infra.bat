@echo off
REM start infra only (postgres / redis / rabbitmq)
cd /d "%~dp0.."
docker compose up -d
echo Infra is up. Run scripts\run-api.bat , run-judge.bat , run-web.bat
echo Or full stack: docker compose --profile full up -d --build
