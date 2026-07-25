@echo off
title FoodBridge - Fast Full Stack Runner
echo ============================================================
echo           FoodBridge - Fast Backend & Frontend Launcher
echo ============================================================
echo.

if not exist "%~dp0target\foodbridge-0.0.1-SNAPSHOT.jar" (
    echo [1/2] Building backend JAR (one-time setup)...
    call "%~dp0mvnw.cmd" package -DskipTests
)

echo [1/2] Launching Pre-compiled Fast Backend (Starts in 2 sec)...
start "FoodBridge Backend" cmd /k "cd /d "%~dp0" && java -jar target\foodbridge-0.0.1-SNAPSHOT.jar"

echo.
echo [2/2] Launching React Frontend (Port 5173)...
start "FoodBridge Frontend" cmd /k "cd /d "%~dp0foodbridge-frontend" && npm run dev"

echo.
echo ============================================================
echo Fast servers launching in separate windows!
echo Backend:  http://localhost:8080
echo Frontend: http://localhost:5173
echo ============================================================
pause
