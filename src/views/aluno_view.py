from src.models.livro import Livro
from src.models.exemplar import Exemplar
from src.models.fila_espera import FilaEspera
from src.models.aluguel import Aluguel
from src.models.multa import Multa
from src.services.aluguel_service import alugar_livro
from src.services.fila_service import entrar_fila, confirmar_aluguel_fila, desistir_fila
from src.services.extensao_service import solicitar_extensao
from src.services.devolucao_service import devolver_livro

def menu_aluno(usuario):
    while True:
        print(f"\n=== MENU ALUNO ({usuario['nome']}) ===")
        print("1. Buscar e Alugar Livros")
        print("2. Meus Aluguéis")
        print("3. Minha Fila de Espera / Notificações")
        print("4. Minhas Multas")
        print("5. Sair")
        opcao = input("Escolha uma opção: ").strip()

        if opcao == '1':
            buscar_e_alugar(usuario)
        elif opcao == '2':
            meus_alugueis(usuario)
        elif opcao == '3':
            minha_fila(usuario)
        elif opcao == '4':
            minhas_multas(usuario)
        elif opcao == '5':
            break
        else:
            print("Opção inválida.")

def buscar_e_alugar(usuario):
    print("\n--- BUSCA DE LIVROS ---")
    query = input("Digite o título, autor ou gênero (deixe em branco para listar todos): ").strip()
    livros = Livro.search(query) if query else Livro.list_all()

    if not livros:
        print("Nenhum livro encontrado.")
        return

    print("\nLivros Encontrados:")
    for idx, l in enumerate(livros, 1):
        exemplares = Exemplar.list_by_livro(l['id_livro'])
        disponiveis = sum(1 for ex in exemplares if ex['status'] == 'disponivel')
        fila = FilaEspera.list_by_livro(l['id_livro'])
        num_fila = len(fila)

        print(f"{idx}. {l['titulo']} - {l['autor']} | Gênero: {l['genero']} | Páginas: {l['num_paginas']} ({l['tamanho']})")
        print(f"   Cópias disponíveis: {disponiveis} | Na fila de espera: {num_fila}/5")

    escolha = input("\nDigite o número do livro para selecionar (ou 0 para voltar): ").strip()
    if not escolha.isdigit() or int(escolha) < 1 or int(escolha) > len(livros):
        return

    livro_sel = livros[int(escolha) - 1]
    exemplares = Exemplar.list_by_livro(livro_sel['id_livro'])
    disponiveis = sum(1 for ex in exemplares if ex['status'] == 'disponivel')
    fila = FilaEspera.list_by_livro(livro_sel['id_livro'])
    num_fila = len(fila)

    if disponiveis > 0 and num_fila == 0:
        acao = input("Deseja (R)eservar/Alugar este livro? (S/N): ").strip().upper()
        if acao == 'S':
            res = alugar_livro(usuario['id_usuario'], livro_sel['id_livro'])
            if res['sucesso']:
                print(f"Aluguel realizado com sucesso! Prazo de devolução: {res['data_devolucao_prevista']}")
            else:
                print(f"Erro ao alugar: {res['erro']}")
    elif num_fila < 5:
        acao = input("Livro sem exemplares disponíveis ou com fila. Deseja (E)ntrar na fila de espera? (S/N): ").strip().upper()
        if acao == 'S':
            res = entrar_fila(usuario['id_usuario'], livro_sel['id_livro'])
            if res['sucesso']:
                print(f"Você entrou na fila! Sua posição é: {res['posicao']}")
            else:
                print(f"Erro ao entrar na fila: {res['erro']}")
    else:
        print("A fila para este livro está cheia (5/5 pessoas). Tente novamente mais tarde.")

def meus_alugueis(usuario):
    print("\n--- MEUS ALUGUÉIS ---")
    alugueis = Aluguel.list_by_usuario(usuario['id_usuario'])
    ativos = [a for a in alugueis if a['status'] in ('ativo', 'extensao', 'atrasado')]

    if not ativos:
        print("Você não possui aluguéis ativos no momento.")
        return

    for idx, a in enumerate(ativos, 1):
        ext_str = f" ({a['extensao_contador']}ª extensão ativa)" if a['extensao_contador'] > 0 else ""
        status_str = f"STATUS: {a['status'].upper()}{ext_str}"
        print(f"{idx}. Livro: {a['titulo_livro']} | Data Prevista Devolução: {a['data_devolucao_prevista']} | {status_str}")

    escolha = input("\nSelecione um aluguel para solicitar extensão ou devolução (ou 0 para voltar): ").strip()
    if not escolha.isdigit() or int(escolha) < 1 or int(escolha) > len(ativos):
        return

    aluguel_sel = ativos[int(escolha) - 1]
    print("\nOpções:")
    print("1. Solicitar Extensão de Prazo")
    print("2. Devolver Livro")
    op = input("Opção: ").strip()

    if op == '1':
        res = solicitar_extensao(aluguel_sel['id_aluguel'])
        if res['sucesso']:
            print(f"Extensão concedida com sucesso! Nova data de devolução: {res['nova_data_devolucao']}")
        else:
            print(f"Erro na extensão: {res['erro']}")
    elif op == '2':
        res = devolver_livro(aluguel_sel['id_aluguel'])
        if res['sucesso']:
            print("Livro devolvido com sucesso!")
            if res['valor_multa'] > 0:
                print(f"Atenção: Foi gerada uma multa de atraso no valor de R$ {res['valor_multa']:.2f}")
        else:
            print(f"Erro na devolução: {res['erro']}")

def minha_fila(usuario):
    print("\n--- MINHA FILA DE ESPERA / NOTIFICAÇÕES ---")
    filas = FilaEspera.list_by_usuario(usuario['id_usuario'])
    if not filas:
        print("Você não está em nenhuma fila de espera.")
        return

    for f in filas:
        print(f"Livro: {f['titulo_livro']} | Posição: {f['posicao']} | Status: {f['status'].upper()}")
        if f['status'] == 'notificado':
            print(f"  --> É A SUA VEZ! Responda até: {f['data_limite_resposta']}")
            op = input("  Confirmar aluguel (C) ou Desistir (D)? ").strip().upper()
            if op == 'C':
                res = confirmar_aluguel_fila(f['id_fila'])
                if res['sucesso']:
                    print(f"Aluguel confirmado! Prazo de devolução: {res['data_devolucao_prevista']}")
                else:
                    print(f"Erro: {res['erro']}")
            elif op == 'D':
                res = desistir_fila(f['id_fila'])
                if res['sucesso']:
                    print("Você desistiu da fila.")

def minhas_multas(usuario):
    print("\n--- MINHAS MULTAS ---")
    multas = Multa.list_by_usuario(usuario['id_usuario'])
    if not multas:
        print("Você não possui multas.")
        return

    for m in multas:
        print(f"Multa ID: {m['id_multa'][:8]} | Livro: {m['titulo_livro']} | Tipo: {m['tipo']} | Valor: R$ {m['valor']:.2f} | Status: {m['status'].upper()}")
