@echo off
ECHO Starting JourneyHub...

ECHO Checking Docker...
call docker --version
IF %ERRORLEVEL% NEQ 0 (
    ECHO Error: Docker is not installed or not in PATH. Please install Docker and try again.
    PAUSE
    EXIT /B 1
)
ECHO Docker check passed.

ECHO Starting Docker Compose...
docker compose down  2>nul
docker compose up -d --build
IF %ERRORLEVEL% NEQ 0 (
    ECHO Error: Docker Compose failed. Check logs above.
    PAUSE
    EXIT /B 1
)
ECHO Docker Compose started.

ECHO Waiting for containers to start...
echo.
echo.

echo !!! On the first launch, please wait 2 minutes for the dump to load !!!

echo.
echo.
timeout /t 5 /nobreak

start http://localhost:5173

ECHO JourneyHub is running!
ECHO - Backend: http://localhost:8080
ECHO - Frontend: http://localhost:5173
ECHO - Redis: localhost:6379
ECHO Press any key to stop...
PAUSE

docker compose down