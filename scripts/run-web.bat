@echo off
REM start Vite frontend
cd /d "%~dp0..\apps\web"

if not exist "node_modules\" (
  echo Installing npm deps...
  call npm install
  if errorlevel 1 (
    echo npm install failed. Need Node 20+.
    exit /b 1
  )
)

echo Starting web on http://localhost:5173
call npm run dev
