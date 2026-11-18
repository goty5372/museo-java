@echo off
title Sistema de Administracion de Museos - Quarkus
set "JAVA_HOME=C:\Program Files\Java\jdk-21.0.11"
set "PATH=%JAVA_HOME%\bin;C:\Users\Jeiss\.m2\wrapper\dists\apache-maven-3.9.9-bin\apache-maven-3.9.9\bin;%PATH%"
cd /d "%~dp0"
echo ==========================================================
echo   Iniciando Sistema de Administracion de Museos (Quarkus)
echo   Swagger UI: http://localhost:8080/swagger-ui
echo ==========================================================
start "" "http://localhost:8080/swagger-ui"
mvn quarkus:dev
pause
