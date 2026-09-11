from src.models.livro import Livro
from src.models.usuario import Usuario

def menu_admin(usuario):
    while True:
        print(f"\n=== MENU ADMINISTRADOR ({usuario['nome']}) ===")
        print("1. Categorizar / Alterar Livros")
        print("2. Excluir Título")
        print("3. Gerenciar Usuários")
        print("4. Sair")
        opcao = input("Escolha uma opção: ").strip()

        if opcao == '1':
            categorizar_livros()
        elif opcao == '2':
            excluir_titulo()
        elif opcao == '3':
            gerenciar_usuarios()
        elif opcao == '4':
            break
        else:
            print("Opção inválida.")

def categorizar_livros():
    print("\n--- CATEGORIZAR LIVROS ---")
    livros = Livro.list_all()
    if not livros:
        print("Nenhum livro cadastrado.")
        return

    for idx, l in enumerate(livros, 1):
        print(f"{idx}. {l['titulo']} | Gênero: {l['genero']} | Tamanho: {l['tamanho']} ({l['num_paginas']} pgs)")

    es = input("Selecione o livro para editar (ou 0 para voltar): ").strip()
    if not es.isdigit() or int(es) < 1 or int(es) > len(livros):
        return

    livro_sel = livros[int(es) - 1]
    novo_genero = input(f"Novo gênero [{livro_sel['genero']}]: ").strip()
    novo_tamanho = input(f"Novo tamanho (pequeno/grande) [{livro_sel['tamanho']}]: ").strip()

    kwargs = {}
    if novo_genero:
        kwargs['genero'] = novo_genero
    if novo_tamanho in ('pequeno', 'grande'):
        kwargs['tamanho'] = novo_tamanho

    if kwargs:
        Livro.update(livro_sel['id_livro'], **kwargs)
        print("Livro atualizado com sucesso!")

def excluir_titulo():
    print("\n--- EXCLUIR TÍTULO ---")
    livros = Livro.list_all()
    for idx, l in enumerate(livros, 1):
        print(f"{idx}. {l['titulo']} (ID: {l['id_livro'][:8]})")

    es = input("Selecione o livro para excluir (ou 0 para voltar): ").strip()
    if not es.isdigit() or int(es) < 1 or int(es) > len(livros):
        return

    livro_sel = livros[int(es) - 1]
    conf = input(f"Tem certeza que deseja excluir '{livro_sel['titulo']}'? (S/N): ").strip().upper()
    if conf == 'S':
        Livro.delete(livro_sel['id_livro'])
        print("Livro excluído (soft delete) com sucesso!")

def gerenciar_usuarios():
    print("\n--- GERENCIAR USUÁRIOS ---")
    print("1. Cadastrar Novo Usuário")
    print("2. Listar Todos os Usuários")
    op = input("Opção: ").strip()

    if op == '1':
        nome = input("Nome: ").strip()
        email = input("E-mail: ").strip()
        cpf = input("CPF (11 dígitos): ").strip()
        turma = input("Turma: ").strip()
        senha = input("Senha: ").strip()
        tipo = input("Tipo (aluno / bibliotecario / administrador): ").strip().lower()

        if tipo not in ('aluno', 'bibliotecario', 'administrador'):
            tipo = 'aluno'

        try:
            uid = Usuario.create(nome, email, cpf, turma, senha, tipo)
            print(f"Usuário '{nome}' ({tipo}) cadastrado com sucesso! ID: {uid[:8]}")
        except Exception as e:
            print(f"Erro ao cadastrar usuário: {e}")

    elif op == '2':
        usuarios = Usuario.list_all()
        for u in usuarios:
            print(f"[{u['tipo'].upper()}] {u['nome']} | CPF: {u['cpf']} | Email: {u['email']}")
