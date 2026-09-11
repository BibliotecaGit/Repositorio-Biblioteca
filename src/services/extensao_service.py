from datetime import date, datetime, timedelta
from src.models.aluguel import Aluguel
from src.models.fila_espera import FilaEspera

def solicitar_extensao(id_aluguel: str) -> dict:
    aluguel = Aluguel.get_by_id(id_aluguel)
    if not aluguel:
        return {'sucesso': False, 'erro': 'Aluguel não encontrado.'}

    if aluguel['status'] not in ('ativo', 'extensao'):
        return {'sucesso': False, 'erro': f"Não é possível estender aluguel com status '{aluguel['status']}'."}

    contador = aluguel.get('extensao_contador', 0)
    if contador >= 2:
        return {'sucesso': False, 'erro': 'Limite de extensões atingido'}

    # Verifica se há pessoas na fila do livro
    id_livro = aluguel['id_livro']
    fila = FilaEspera.list_by_livro(id_livro)
    if len(fila) > 0:
        return {'sucesso': False, 'erro': 'Não é possível estender. Há pessoas na fila.'}

    hoje = date.today()
    data_prevista_atual = aluguel['data_devolucao_prevista']
    if isinstance(data_prevista_atual, str):
        dt_base = datetime.strptime(data_prevista_atual[:10], "%Y-%m-%d").date()
    elif isinstance(data_prevista_atual, datetime):
        dt_base = data_prevista_atual.date()
    else:
        dt_base = data_prevista_atual

    # Se a data prevista for menor que hoje, considera hoje como base (ou erro)
    if dt_base < hoje:
        dt_base = hoje

    if contador == 0:
        # 1ª extensão: + 60 dias (2 meses)
        nova_data = dt_base + timedelta(days=60)
        novo_contador = 1
    else:
        # 2ª extensão: + 30 dias (1 mês)
        nova_data = dt_base + timedelta(days=30)
        novo_contador = 2

    Aluguel.update(
        id_aluguel,
        data_devolucao_prevista=nova_data,
        extensao_contador=novo_contador,
        data_extensao=hoje,
        status='extensao'
    )

    return {
        'sucesso': True,
        'nova_data_devolucao': nova_data.strftime("%Y-%m-%d"),
        'extensao_contador': novo_contador
    }
