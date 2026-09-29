# Como Executar a Aplicação Aurora

Esta aplicação foi configurada para funcionar com **Java 17 ou superior** (17, 21, 22, etc.).

---

## 🚀 Forma Mais Rápida (1 Clique)

Na pasta raiz do projeto, você encontrará scripts prontos:

- **`iniciar-tudo.bat`**: Inicia tanto o Backend quanto o Frontend em janelas separadas e já abre o navegador na tela de login.
- **`iniciar-backend.bat`**: Inicia apenas a API Spring Boot (detecta automaticamente o JDK instalado e roda em `http://localhost:8080`).
- **`iniciar-frontend.bat`**: Inicia o servidor local do frontend (em `http://localhost:3000`) sem precisar de Node.js ou Python.

---

## 🛠️ Execução Manual pelo Terminal (PowerShell)

### Passo 1: Iniciar o Backend

```powershell
# 1. Configurar o JAVA_HOME (caso não esteja nas variáveis de ambiente do sistema)
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17.0.3.1"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

# 2. Entrar na pasta do backend
cd aurora-backend

# 3. Executar com Maven Wrapper
.\mvnw.cmd spring-boot:run
```

Aguarde a mensagem:
```text
Tomcat started on port 8080 (http) with context path ''
Started AuroraApplication in X.XXX seconds
```

---

### Passo 2: Iniciar o Frontend

Você pode:
- Executar `.\iniciar-frontend.bat`
- Ou no **VS Code**, clicar com botão direito em `aurora-frontend/login.html` -> **"Open with Live Server"**.

---

## 🔑 Usuários para Teste (Banco Local H2)

O banco de dados local já vem alimentado com os seguintes usuários:

| Perfil | E-mail | Senha |
|---|---|---|
| **Leitor / Aluno** | `aluno@aurora.local` | `aluno123` |
| **Bibliotecário** | `biblio@aurora.local` | `biblio123` |
| **Administrador** | `admin@aurora.local` | `admin123` |
