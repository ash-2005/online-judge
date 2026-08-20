@echo off
REM build frontend and force-push to gh-pages (UI only)
cd /d "%~dp0.."
set GIT="C:\Program Files\Git\cmd\git.exe"

cd apps\web
call npm run build
if errorlevel 1 exit /b 1
cd ..\..

set TMP=%TEMP%\oj-gh-pages
if exist "%TMP%" rmdir /s /q "%TMP%"
mkdir "%TMP%"
xcopy /e /i /y "apps\web\dist\*" "%TMP%\" >nul
copy /y "%TMP%\index.html" "%TMP%\404.html" >nul
type nul > "%TMP%\.nojekyll"

pushd "%TMP%"
%GIT% init -b gh-pages
%GIT% add -A
REM write tree/commit without cursor co-author trailer
for /f %%i in ('%GIT% write-tree') do set TREE=%%i
echo deploy ui to github pages> msg.txt
for /f %%i in ('%GIT% commit-tree %TREE% -F msg.txt') do set COMMIT=%%i
%GIT% update-ref refs/heads/gh-pages %COMMIT%
%GIT% remote add origin https://github.com/ash-2005/online-judge.git
%GIT% push -f origin gh-pages
popd
echo done - set Pages branch to gh-pages in repo settings
