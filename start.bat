@echo off
echo Starting JourneyHub...

:: Проверка установки Docker
docker --version >nul 2>&1
IF %ERRORLEVEL% NEQ 0 (
    echo Error: Docker is not installed or not in PATH. Please install Docker and try again.
    pause
    exit /b 1
)

:: Проверка наличия 7-Zip (или другой архиватор)
where 7z >nul 2>&1
IF %ERRORLEVEL% NEQ 0 (
    echo Warning: 7-Zip not found. Please install 7-Zip to unzip the database dump.
    echo Attempting to proceed without unzipping...
) ELSE (
    :: Разархивировать дамп
    echo Unzipping database dump...
    7z x "db\journeyhub_dump.zip" -o"db" -y >nul 2>&1
    IF %ERRORLEVEL% NEQ 0 (
        echo Error: Failed to unzip db/journeyhub_dump.zip
        pause
        exit /b 1
    )
)

:: Запуск Docker Compose
docker compose up -d

:: Ждём 10 секунд, чтобы контейнеры успели запуститься
echo Waiting for containers to start...
timeout /t 10 /nobreak >nul

:: Открываем страницу фронтенда в браузере по умолчанию
start http://localhost:5173

echo JourneyHub is running!
echo - Backend: http://localhost:8080
echo - Frontend: http://localhost:5173
echo - Redis: localhost:6379
echo Press any key to stop...
pause

:: Остановка сервисов
docker compose down

:: Удаление разархивированного файла (опционально)
del "db\journeyhub_dump.sql"