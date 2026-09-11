from src.models.livro import Livro
from src.models.exemplar import Exemplar
from src.models.aluguel import Aluguel
from src.services.devolucao_service import devolver_livro
from src.services.multa_service import aplicar_multa_dano, verificar_atrasos

def menu_bibliotecario(usuario):
    while True:
        print(f"\n=== MENU BIBLIOTECÁRIO ({usuario['nome']}) ===")
        print("1. Gerenciar Acervo (Livros e Exemplares)")
        print("2. Registrar Devolução")
        print("3. Aplicar Multa por Dano")
        print("4. Executar Verificação Diária de Atrasos")
        print("5. Sair")
        opcao = input("Escolha uma opção: ").strip()

        if opcao == '1':
            gerenciar_acervo()
        elif opcao == '2':
            registrar_devolucao()
        elif opcao == '3':
            aplicar_dano()
        elif opcao == '4':
            executar_job_atrasos()
        elif opcao == '5':
            break
        else:
            print("Opção inválida.")

def gerenciar_acervo():
    print("\n--- GERENCIAR ACERVO ---")
    print("1. Cadastrar Novo Livro")
    print("2. Adicionar Exemplar a Livro Existente")
    print("3. Listar Todos os Livros")
    op = input("Opção: ").strip()

    if op == '1':
        titulo = input("Título: ").strip()
        autor = input("Autor: ").strip()
        genero = input("Gênero: ").strip()
        num_paginas = int(input("Número de Páginas: ").strip())
        valor_livro = float(input("Valor do Livro (R$): ").strip())

        id_livro = Livro.create(titulo, autor, genero, num_paginas, valor_livro)
        livro = Livro.get_by_id(id_livro)
        print(f"Livro '{titulo}' cadastrado com sucesso! Tamanho automático: {livro['tamanho']}")

    elif op == '2':
        livros = Livro.list_all()
        if not livros:
            print("Nenhum livro cadastrado.")
            return

        for idx, l in enumerate(livros, 1):
            print(f"{idx}. {l['titulo']} (ID: {l['id_livro'][:8]})")

        es = input("Selecione o número do livro: ").strip()
        if not es.isdigit() or int(es) < 1 or int(es) > len(livros):
            return

        livro_sel = livros[int(es) - 1]
        codigo_barras = input("Código de Barras do Exemplar: ").strip()
        id_ex = Exemplar.create(livro_sel['id_livro'], codigo_barras)
        print(f"Exemplar '{codigo_barras}' adicionado com sucesso ao livro '{livro_sel['titulo']}'.")

    elif op == '3':
        livros = Livro.list_all()
        for l in livros:
            exemplares = Exemplar.list_by_livro(l['id_livro'])
            print(f"[{l['tamanho'].upper()}] {l['titulo']} - {l['autor']} | Total Exemplares: {len(exemplares)}")

def registrar_devolucao():
    print("\n--- REGISTRAR DEVOLUÇÃO ---")
    codigo_barras = input("Código de barras do exemplar (ou ID do aluguel): ").strip()

    # Tenta buscar por código de barras primeiro
    ex = Exemplar.get_by_codigo_barras(codigo_barras)
    id_aluguel = None

    if ex:
        # Busca aluguel ativo para este exemplar
        alugueis_ativos = Aluguel.list_ativos()
        for a in alugueis_ativos:
            if a['id_exemplar'] == ex['id_exemplar']:
                id_aluguel = a['id_aluguel']
                break

    if not id_aluguel:
        id_aluguel = codigo_barras

    res = devolver_livro(id_aluguel)
    if res['sucesso']:
        print("Devolução registrada com sucesso!")
        if res['valor_multa'] > 0:
            print(f"Multa de atraso de R$ {res['valor_multa']:.2f} gerada para o usuário.")

        dano = input("O exemplar apresenta algum dano? (S/N): ").strip().upper()
        if dano == 'S':
            print("Tipos de dano: capa_rasgada, paginas_faltantes, paginas_rasgadas, manchas, escrita, lombada_danificada, outros")
            tipo_dano = input("Tipo do dano: ").strip()
            res_dano = aplicar_multa_dano(id_aluguel, tipo_dano)
            if res_dano['sucesso']:
                print(f"Multa por dano no valor de R$ {res_dano['valor']:.2f} aplicada!")
    else:
        print(f"Erro ao registrar devolução: {res['erro']}")

def aplicar_dano():
    print("\n--- APLICAR MULTA POR DANO ---")
    id_aluguel = input("ID do Aluguel: ").strip()
    tipo_dano = input("Tipo do Dano: ").strip()
    res = aplicar_multa_dano(id_aluguel, tipo_dano)
    if res['sucesso']:
        print(f"Multa por dano de R$ {res['valor']:.2f} aplicada com sucesso!")
    else:
        print(f"Erro: {res['erro']}")

def executar_job_atrasos():
    print("\n--- PROCESSANDO VERIFICAÇÃO DIÁRIA DE ATRASOS ---")
    res = verificar_atrasos()
    print(f"Verificação concluída! Aluguéis em atraso processados: {res['alugueis_atrasados_processados']} | Multas geradas: {res['multas_geradas']}")
