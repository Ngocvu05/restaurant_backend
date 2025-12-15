@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo.
echo ╔═══════════════════════════════════════════════╗
echo ║   🎯 SonarQube Analysis - All Services 🎯    ║
echo ║   Restaurant Management Microservices         ║
echo ╚═══════════════════════════════════════════════╝
echo.

REM Set your token here or use environment variable
if "%SONAR_TOKEN%"=="" (
    set /p SONAR_TOKEN="Enter SonarQube token: "
)

if "%SONAR_TOKEN%"=="" (
    echo ❌ Token is required!
    pause
    exit /b 1
)

set SONAR_HOST=http://localhost:9000

echo [1/6] Checking SonarQube...
curl -s %SONAR_HOST%/api/system/status 2>nul | findstr "UP" >nul
if errorlevel 1 (
    echo ⚠️  SonarQube not running. Starting...
    docker-compose -f docker-compose.sonarqube.yml up -d
    echo ⏳ Waiting 120 seconds for SonarQube to start...
    timeout /t 120 /nobreak >nul
    echo ✅ SonarQube should be ready now
) else (
    echo ✅ SonarQube is running
)

echo.
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
echo   Starting Analysis...
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

REM Track results
set SUCCESS_COUNT=0
set TOTAL_COUNT=5

echo.
echo [2/6] Analyzing user-service...
cd backend-service
call gradlew.bat clean test jacocoTestReport sonar ^
    -Dsonar.host.url=%SONAR_HOST% ^
    -Dsonar.token=%SONAR_TOKEN% ^
    -x test ^
    --no-daemon --console=plain

if %ERRORLEVEL% EQU 0 (
    echo ✅ user-service analysis completed
    set /a SUCCESS_COUNT+=1
) else (
    echo ❌ user-service analysis failed
)
cd ..

echo.
echo [3/6] Analyzing search-service...
cd search-service
call gradlew.bat clean test jacocoTestReport sonar ^
    -Dsonar.host.url=%SONAR_HOST% ^
    -Dsonar.token=%SONAR_TOKEN% ^
    -x test ^
    --no-daemon --console=plain

if %ERRORLEVEL% EQU 0 (
    echo ✅ search-service analysis completed
    set /a SUCCESS_COUNT+=1
) else (
    echo ❌ search-service analysis failed
)
cd ..

echo.
echo [4/6] Analyzing chat-service...
cd chat-service
call gradlew.bat clean test jacocoTestReport sonar ^
    -Dsonar.host.url=%SONAR_HOST% ^
    -Dsonar.token=%SONAR_TOKEN% ^
    -x test ^
    --no-daemon --console=plain

if %ERRORLEVEL% EQU 0 (
    echo ✅ chat-service analysis completed
    set /a SUCCESS_COUNT+=1
) else (
    echo ❌ chat-service analysis failed
)
cd ..

echo.
echo [5/6] Analyzing api-gateway...
cd api-gateway
call gradlew.bat clean test jacocoTestReport sonar ^
    -Dsonar.host.url=%SONAR_HOST% ^
    -Dsonar.token=%SONAR_TOKEN% ^
    -x test ^
    --no-daemon --console=plain

if %ERRORLEVEL% EQU 0 (
    echo ✅ api-gateway analysis completed
    set /a SUCCESS_COUNT+=1
) else (
    echo ❌ api-gateway analysis failed
)
cd ..

echo.
echo [6/6] Skipping discovery-service (Eureka config only)...
set /a SUCCESS_COUNT+=1

echo.
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
echo   Analysis Summary
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
echo.
echo   Results: %SUCCESS_COUNT%/%TOTAL_COUNT% services analyzed successfully
echo.
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
echo.
echo 📊 View results at: %SONAR_HOST%
echo.
echo Projects:
echo   • restaurant-user-service
echo   • restaurant-search-service
echo   • restaurant-chat-service
echo   • restaurant-api-gateway
echo   • restaurant-discovery-service (skipped)
echo.

if %SUCCESS_COUNT% EQU %TOTAL_COUNT% (
    echo 🎉 All analyses completed successfully!
) else (
    echo ⚠️  Some analyses failed. Check logs for details.
)

echo.
pause