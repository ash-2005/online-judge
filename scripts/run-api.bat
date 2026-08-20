@echo off
cd /d "%~dp0.."
set JAVA_TOOL_OPTIONS=-Duser.timezone=UTC
if exist "services\api\target\api-0.0.1-SNAPSHOT.jar" (
  echo Starting API from jar...
  java -jar "services\api\target\api-0.0.1-SNAPSHOT.jar"
  exit /b %ERRORLEVEL%
)
set "MVN=mvn"
if exist "tools\apache-maven-3.9.6\bin\mvn.cmd" set "MVN=tools\apache-maven-3.9.6\bin\mvn.cmd"
echo Building then running API...
call %MVN% -f services\api\pom.xml spring-boot:run
