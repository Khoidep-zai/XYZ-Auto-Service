@echo off
cd /d "%~dp0"
set "SPRING_PROFILES_ACTIVE=dev"
call mvnw.cmd spring-boot:run
pause
