@echo off
chcp 936 >nul
title WMS Start
echo ========================================
echo  Start All Services
echo ========================================
echo.
echo Port Assignments:
echo   WMS Backend      -^> http://localhost:8002
echo   Box Backend      -^> http://localhost:8083
echo   WMS Frontend     -^> http://localhost:8080
echo   Box Frontend     -^> http://localhost:5173
echo.
if not exist "G:\bishe\xiangMu\Warehouse-System-master" (echo [ERROR] WMS backend dir not found & pause & exit /b 1)
if not exist "G:\bishe\xiangMu\box-optimization-develop\backend\box-optimizer-api" (echo [ERROR] BoxOpt backend dir not found & pause & exit /b 1)
if not exist "G:\bishe\xiangMu\Warehouse-System-Web-master" (echo [ERROR] WMS frontend dir not found & pause & exit /b 1)
if not exist "G:\bishe\xiangMu\box-optimization-develop\frontend" (echo [ERROR] Box frontend dir not found & pause & exit /b 1)
echo [1/4] Starting WMS Backend (Spring Boot :8002)...
start "WMS-Backend" cmd /k "cd /d G:\bishe\xiangMu\Warehouse-System-master && mvn spring-boot:run"
echo Waiting 10 seconds for WMS backend to start...
ping -n 11 127.0.0.1 >nul
echo [2/4] Starting Box Optimizer Backend (Spring Boot :8083)...
start "BoxOpt-Backend" cmd /k "cd /d G:\bishe\xiangMu\box-optimization-develop\backend\box-optimizer-api && mvn spring-boot:run"
echo Waiting 10 seconds for BoxOpt backend to start...
ping -n 11 127.0.0.1 >nul
echo [3/4] Starting WMS Frontend (Vue :8080)...
start "WMS-Frontend" cmd /k "cd /d G:\bishe\xiangMu\Warehouse-System-Web-master && set NODE_OPTIONS=--openssl-legacy-provider && npm run serve"
echo Waiting 5 seconds...
ping -n 6 127.0.0.1 >nul
echo [4/4] Starting Box Frontend (React :5173)...
start "BoxOpt-Frontend" cmd /k "cd /d G:\bishe\xiangMu\box-optimization-develop\frontend && npm run dev"
echo.
echo ========================================
echo  All services started in separate windows
echo  Wait 30-60 seconds for initialization
echo ========================================
echo.
echo  Access:
echo    WMS:  http://localhost:8080
echo    Box:  http://localhost:5173
echo.
pause