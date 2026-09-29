@echo off
chcp 65001 >nul
title Aurora Backend

echo ===================================================
echo           Iniciando Aurora Backend (Spring Boot)
echo ===================================================

cd /d "%~dp0aurora-backend"

:: 1. Verificar se JAVA_HOME ja aponta para um JDK valido
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\javac.exe" (
        goto :FOUND_JAVA
    )
)

:: 2. Procurar JDK instalado em Program Files
for /d %%D in ("C:\Program Files\Java\jdk*") do (
    if exist "%%D\bin\javac.exe" (
        set "JAVA_HOME=%%D"
        goto :FOUND_JAVA
    )
)

:: 3. Procurar outros distribuidores comuns (Adoptium, Microsoft, Corretto, etc.)
for /d %%D in ("C:\Program Files\Eclipse Adoptium\jdk*") do (
    if exist "%%D\bin\javac.exe" (
        set "JAVA_HOME=%%D"
        goto :FOUND_JAVA
    )
)
for /d %%D in ("C:\Program Files\Microsoft\jdk*") do (
    if exist "%%D\bin\javac.exe" (
        set "JAVA_HOME=%%D"
        goto :FOUND_JAVA
    )
)
for /d %%D in ("C:\Program Files\Amazon Corretto\jdk*") do (
    if exist "%%D\bin\javac.exe" (
        set "JAVA_HOME=%%D"
        goto :FOUND_JAVA
    )
)

:FOUND_JAVA
if defined JAVA_HOME (
    set "PATH=%JAVA_HOME%\bin;%PATH%"
    echo [OK] Usando JDK: %JAVA_HOME%
) else (
    echo [AVISO] JDK nao localizado automaticamente. Usando Java padrao do PATH...
)

echo.
echo Iniciando aplicacao Spring Boot na porta 8080...
echo.
call mvnw.cmd spring-boot:run

pause
