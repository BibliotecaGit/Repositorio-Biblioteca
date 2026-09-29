# Guia de Configuração do Supabase — Aurora

Este guia explica como configurar o banco de dados e armazenamento do Aurora no **Supabase** em menos de 5 minutos.

---

## 1. Criar o Projeto no Supabase
1. Acesse [https://supabase.com](https://supabase.com) e entre na sua conta.
2. Clique em **"New Project"**.
3. Defina o nome (ex.: `aurora-db`), escolha a região mais próxima (ex.: `sa-east-1` / São Paulo) e defina uma senha forte para o banco de dados. **Guarde essa senha!**

---

## 2. Executar o Schema do Banco
1. No menu lateral esquerdo do painel do Supabase, clique em **SQL Editor** (ícone de terminal/código).
2. Clique em **"New query"**.
3. Copie todo o conteúdo do arquivo [`schema-jules.sql`](schema-jules.sql) deste repositório e cole no editor.
4. Clique no botão **"Run"** (ou aperte `Ctrl + Enter`).
5. Verifique a mensagem `Success. No rows returned`. Todas as tabelas, enums, views e triggers estão criadas.

---

## 3. Executar o Seed (Dados Iniciais)
1. No **SQL Editor**, abra uma nova aba de query (**"New query"**).
2. Copie todo o conteúdo do arquivo [`seed.sql`](seed.sql) deste repositório e cole no editor.
3. Clique em **"Run"**.
4. Pronto! O banco terá:
   - **Administrador:** `admin@aurora.local` | Senha: `admin123`
   - **Bibliotecário:** `biblio@aurora.local` | Senha: `biblio123`
   - **Aluno Teste:** `aluno@aurora.local` | Senha: `aluno123`
   - **14 Livros** distribuídos nas 4 faixas de tamanho (`pequeno`, `medio_pequeno`, `medio_padrao`, `grande`).
   - **42 Exemplares** (exatamente 3 cópias físicas disponíveis por título).

---

## 4. Criar o Bucket de Armazenamento (`capas`)
1. No menu lateral esquerdo, clique em **Storage**.
2. Clique em **"New bucket"**.
3. No campo **Name**, digite exatamente: `capas`.
4. Marque a opção **"Public bucket"** (para que as capas de livros possam ser visualizadas pelos leitores sem token de autenticação).
5. Clique em **"Save"**.

---

## 5. Conectar o Backend ao Supabase
1. No Supabase, vá em **Project Settings** (ícone de engrenagem) > **Database**.
2. Na seção **Connection string**, selecione a aba **URI** ou **JDBC**.
   * O formato da URL JDBC é:
     ```
     jdbc:postgresql://db.[SEU-PROJECT-REF].supabase.co:5432/postgres?sslmode=require
     ```
3. No seu ambiente local ou servidor:
   * Copie o arquivo `.env.example` para `.env`:
     ```bash
     cp .env.example .env
     ```
   * Preencha as variáveis no `.env`:
     ```env
     SPRING_DATASOURCE_URL=jdbc:postgresql://db.[SEU-PROJECT-REF].supabase.co:5432/postgres?sslmode=require
     SPRING_DATASOURCE_USERNAME=postgres
     SPRING_DATASOURCE_PASSWORD=SUA_SENHA_DO_BANCO
     ```
4. Para iniciar o backend com o perfil de produção conectado ao Supabase:
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
   ```
