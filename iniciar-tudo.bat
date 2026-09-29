@echo off
chcp 65001 >nul
title Aurora - Iniciar Tudo

echo ===================================================
echo           Iniciando Aurora (Backend + Frontend)
echo ===================================================

echo 1. Abrindo o Backend em uma janela separada...
start "Aurora Backend" "%~dp0iniciar-backend.bat"

echo 2. Aguardando inicializacao...
timeout /t 5 /nobreak >nul

echo 3. Abrindo o Frontend em uma janela separada...
start "Aurora Frontend" "%~dp0iniciar-frontend.bat"

echo.
echo [Sucesso] Janelas do backend e frontend iniciadas!
echo Voce pode fechar esta janela agora.
timeout /t 3 >nul
exit
