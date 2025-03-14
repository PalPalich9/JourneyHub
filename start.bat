@echo off
echo Starting JourneyHub...

mvn --version
IF %ERRORLEVEL% NEQ 0 (
    echo Error: Maven is not installed or not in PATH. Please install Maven and try again.
    pause
    exit /b 1
)

docker --version
IF %ERRORLEVEL% NEQ 0 (
    echo Error: Docker is not installed or not in PATH. Please install Docker and try again.
    pause
    exit /b 1
)

echo Building JourneyHub...
mvn clean package
IF %ERRORLEVEL% NEQ 0 (
    echo Error: Failed to build the project. Check Maven logs above.
    pause
    exit /b 1
)

where 7z
IF %ERRORLEVEL% NEQ 0 (
    echo Warning: 7-Zip not found. Please install 7-Zip to unzip the database dump.
    echo Attempting to proceed without unzipping...
) ELSE (
    echo Unzipping database dump...
    7z x "db\journeyhub_dump.zip" -o"db" -y
    IF %ERRORLEVEL% NEQ 0 (
        echo Error: Failed to unzip db/journeyhub_dump.zip
        pause
        exit /b 1
    )
)

docker compose up -d
IF %ERRORLEVEL% NEQ 0 (
    echo Error: Docker Compose failed. Check Docker logs above.
    pause
    exit /b 1
)

echo Waiting for containers to start...
timeout /t 10 /nobreak

start http://localhost:5173

echo JourneyHub is running!
echo - Backend: http://localhost:8080
echo - Frontend: http://localhost:5173
echo - Redis: localhost:6379
echo Press any key to stop...
pause

docker compose down

del "db\journeyhub_dump.sql"