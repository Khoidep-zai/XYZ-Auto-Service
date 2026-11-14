@echo off
cd /d "%~dp0"
set "SPRING_PROFILES_ACTIVE=postgres"
if not defined DB_URL set "DB_URL=jdbc:postgresql://localhost:5432/xyz_auto_service"
if not defined DB_USER set "DB_USER=postgres"
if not defined DB_PASSWORD set "DB_PASSWORD=postgres"
call mvnw.cmd spring-boot:run
pause
