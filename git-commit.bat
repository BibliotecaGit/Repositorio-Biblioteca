@echo off
chcp 65001 >nul
title Aurora - Git Commit e Push

echo ===================================================
echo               Aurora - Enviar ao Git
echo ===================================================

cd /d "%~dp0"

:: 1. Garantir que o Git esta no PATH caso o terminal seja antigo
if exist "%LOCALAPPDATA%\Programs\Git\cmd\git.exe" (
    set "PATH=%LOCALAPPDATA%\Programs\Git\cmd;%PATH%"
)
if exist "C:\Program Files\Git\cmd\git.exe" (
    set "PATH=C:\Program Files\Git\cmd;%PATH%"
)

where git >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERRO] O comando git nao foi encontrado neste computador.
    pause
    exit /b 1
)

echo Status atual dos arquivos:
echo ---------------------------------------------------
git status -s
echo ---------------------------------------------------

set /p MSG="Digite a mensagem do commit: "
if "%MSG%"=="" (
    echo Mensagem vazia. Operacao cancelada.
    pause
    exit /b 0
)

echo.
echo Adicionando alteracoes...
git add .

echo Criando commit...
git commit -m "%MSG%"

echo.
echo Sincronizando com o repositorio remoto (GitHub)...
git pull --rebase origin main

echo Enviando commits (git push)...
git push origin main

echo.
if %errorlevel% equ 0 (
    echo [SUCESSO] Alteracoes enviadas para o GitHub com sucesso!
) else (
    echo [ERRO] Ocorreu uma falha no envio. Verifique as mensagens acima.
)

echo.
pause
