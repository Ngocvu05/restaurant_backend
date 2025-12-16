@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo.
echo ╔═══════════════════════════════════════════════╗
echo ║   🎯 SonarQube Analysis - All Services 🎯    ║
echo ║   Restaurant Management Microservices         ║
echo ╚═══════════════════════════════════════════════╝
echo.

REM ==================================================
REM Sonar token
REM ==================================================
if "%SONAR_TOKEN%"=="" (
    set /p SONAR_TOKEN="🔑 Enter SonarQube token: "
)

if "%SONAR_TOKEN%"=="" (
    echo ❌ SonarQube token is required!
    pause
    exit /b 1
)

set SONAR_HOST=http://localhost:9000

REM ==================================================
REM Check SonarQube
REM ==================================================
echo [1/6] Checking SonarQube status...
curl -s %SONAR_HOST%/api/system/status 2>nul | findstr "UP" >nul

if errorlevel 1 (
    echo ⚠️  SonarQube not running. Starting...
    docker-compose -f docker-compose.sonarqube.yml up -d
    echo ⏳ Waiting 120 seconds...
    timeout /t 120 /nobreak >nul
) else (
    echo ✅ SonarQube is running
)

echo.
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
echo   🚀 Starting SonarQube Analysis (NO TESTS)
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

set SUCCESS_COUNT=0
set TOTAL_COUNT=5

REM ==================================================
REM user-service
REM ==================================================
echo.
echo [2/6] Analyzing user-service...
cd backend-service

call gradlew.bat sonarAnalyze ^
    -Dsonar.host.url=%SONAR_HOST% ^
    -Dsonar.token=%SONAR_TOKEN% ^
    --no-daemon --console=plain

if %ERRORLEVEL% EQU 0 (
    echo ✅ user-service analyzed
    set /a SUCCESS_COUNT+=1
) else (
    echo ❌ user-service failed
)
cd ..

REM ==================================================
REM search-service
REM ==================================================
echo.
echo [3/6] Analyzing search-service...
cd search-service

call gradlew.bat sonarAnalyze ^
    -Dsonar.host.url=%SONAR_HOST% ^
    -Dsonar.token=%SONAR_TOKEN% ^
    --no-daemon --console=plain

if %ERRORLEVEL% EQU 0 (
    echo ✅ search-service analyzed
    set /a SUCCESS_COUNT+=1
) else (
    echo ❌ search-service failed
)
cd ..

REM ==================================================
REM chat-service
REM ==================================================
echo.
echo [4/6] Analyzing chat-service...
cd chat-service

call gradlew.bat sonarAnalyze ^
    -Dsonar.host.url=%SONAR_HOST% ^
    -Dsonar.token=%SONAR_TOKEN% ^
    --no-daemon --console=plain

if %ERRORLEVEL% EQU 0 (
    echo ✅ chat-service analyzed
    set /a SUCCESS_COUNT+=1
) else (
    echo ❌ chat-service failed
)
cd ..

REM ==================================================
REM api-gateway
REM ==================================================
echo.
echo [5/6] Analyzing api-gateway...
cd api-gateway

call gradlew.bat sonarAnalyze ^
    -Dsonar.host.url=%SONAR_HOST% ^
    -Dsonar.token=%SONAR_TOKEN% ^
    --no-daemon --console=plain

if %ERRORLEVEL% EQU 0 (
    echo ✅ api-gateway analyzed
    set /a SUCCESS_COUNT+=1
) else (
    echo ❌ api-gateway failed
)
cd ..

REM ==================================================
REM discovery-service
REM ==================================================
echo.
echo [6/6] Skipping discovery-service (Eureka only)
set /a SUCCESS_COUNT+=1

echo.
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
echo   📊 Analysis Summary
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
echo.
echo   Results: %SUCCESS_COUNT% / %TOTAL_COUNT%
echo   SonarQube: %SONAR_HOST%
echo.

pause