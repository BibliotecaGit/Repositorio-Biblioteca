@echo off
chcp 65001 >nul
title Aurora Frontend

echo ===================================================
echo           Iniciando Aurora Frontend
echo ===================================================

cd /d "%~dp0"

echo Servidor local ativo em http://localhost:3000
echo Abrindo o navegador automaticamente...
echo.
echo Para encerrar o servidor, feche esta janela ou aperte Ctrl+C.
echo.

powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "Start-Process 'http://localhost:3000/login.html';" ^
  "Add-Type -AssemblyName System.Net.HttpListener;" ^
  "$listener = New-Object System.Net.HttpListener;" ^
  "$listener.Prefixes.Add('http://localhost:3000/');" ^
  "$listener.Start();" ^
  "while ($listener.IsListening) {" ^
  "  $ctx = $listener.GetContext();" ^
  "  $path = [System.Uri]::UnescapeDataString($ctx.Request.Url.LocalPath.TrimStart('/'));" ^
  "  if ([string]::IsNullOrEmpty($path)) { $path = 'login.html'; }" ^
  "  $file = Join-Path (Get-Location) $path;" ^
  "  if (Test-Path $file -PathType Leaf) {" ^
  "    $bytes = [System.IO.File]::ReadAllBytes($file);" ^
  "    $ext = [System.IO.Path]::GetExtension($file).ToLower();" ^
  "    switch ($ext) {" ^
  "      '.html' { $ctx.Response.ContentType = 'text/html; charset=utf-8' }" ^
  "      '.js'   { $ctx.Response.ContentType = 'application/javascript; charset=utf-8' }" ^
  "      '.css'  { $ctx.Response.ContentType = 'text/css; charset=utf-8' }" ^
  "      '.json' { $ctx.Response.ContentType = 'application/json; charset=utf-8' }" ^
  "      '.png'  { $ctx.Response.ContentType = 'image/png' }" ^
  "      '.jpg'  { $ctx.Response.ContentType = 'image/jpeg' }" ^
  "      '.jpeg' { $ctx.Response.ContentType = 'image/jpeg' }" ^
  "      '.svg'  { $ctx.Response.ContentType = 'image/svg+xml' }" ^
  "      default { $ctx.Response.ContentType = 'application/octet-stream' }" ^
  "    }" ^
  "    $ctx.Response.ContentLength64 = $bytes.Length;" ^
  "    $ctx.Response.OutputStream.Write($bytes, 0, $bytes.Length);" ^
  "  } else {" ^
  "    $ctx.Response.StatusCode = 404;" ^
  "  }" ^
  "  $ctx.Response.OutputStream.Close();" ^
  "}"

pause
