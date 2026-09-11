from datetime import date, timedelta, datetime
from src.services.elegibilidade import verificar_elegibilidade
from src.services.prazo import calcular_prazo
from src.models.fila_espera import FilaEspera
from src.models.exemplar import Exemplar
from src.models.aluguel import Aluguel

def entrar_fila(id_usuario: str, id_livro: str) -> dict:
    check = verificar_elegibilidade(id_usuario)
    if not check['elegivel']:
        return {'sucesso': False, 'erro': check['motivo']}

    # Verifica se já está na fila
    existente = FilaEspera.get_by_usuario_livro(id_usuario, id_livro)
    if existente:
        return {'sucesso': False, 'erro': 'Usuário já está na fila de espera para este livro.'}

    fila = FilaEspera.list_by_livro(id_livro)
    if len(fila) >= 5:
        return {'sucesso': False, 'erro': 'Fila cheia'}

    id_fila = FilaEspera.create(id_livro, id_usuario)
    fila_item = FilaEspera.get_by_id(id_fila)

    return {
        'sucesso': True,
        'id_fila': id_fila,
        'posicao': fila_item['posicao']
    }

def confirmar_aluguel_fila(id_fila: str) -> dict:
    item = FilaEspera.get_by_id(id_fila)
    if not item:
        return {'sucesso': False, 'erro': 'Registro de fila não encontrado.'}

    if item['status'] != 'notificado':
        return {'sucesso': False, 'erro': 'Item da fila não está aguardando confirmação.'}

    # Verificar se expirou
    data_limite = item.get('data_limite_resposta')
    if data_limite:
        if isinstance(data_limite, str):
            dt_limite = datetime.strptime(data_limite[:10], "%Y-%m-%d").date()
        elif isinstance(data_limite, datetime):
            dt_limite = data_limite.date()
        else:
            dt_limite = data_limite

        if dt_limite < date.today():
            FilaEspera.update_status(id_fila, 'expirado')
            notificar_proximo_da_fila(item['id_livro'])
            return {'sucesso': False, 'erro': 'Prazo para resposta expirado.'}

    # Buscar exemplar reservado para este livro
    exemplares = Exemplar.list_by_livro(item['id_livro'])
    exemplar_reservado = None
    for ex in exemplares:
        if ex['status'] in ('reservado', 'disponivel'):
            exemplar_reservado = ex
            break

    if not exemplar_reservado:
        return {'sucesso': False, 'erro': 'Nenhum exemplar reservado/disponível encontrado.'}

    data_prevista = calcular_prazo(item['id_livro'])
    hoje = date.today()

    id_aluguel = Aluguel.create(
        id_usuario=item['id_usuario'],
        id_exemplar=exemplar_reservado['id_exemplar'],
        data_aluguel=hoje,
        data_devolucao_prevista=data_prevista,
        status='ativo'
    )
    Exemplar.update_status(exemplar_reservado['id_exemplar'], 'alugado')
    FilaEspera.update_status(id_fila, 'confirmado')

    return {
        'sucesso': True,
        'id_aluguel': id_aluguel,
        'data_devolucao_prevista': data_prevista.strftime("%Y-%m-%d")
    }

def desistir_fila(id_fila: str) -> dict:
    item = FilaEspera.get_by_id(id_fila)
    if not item:
        return {'sucesso': False, 'erro': 'Registro de fila não encontrado.'}

    FilaEspera.update_status(id_fila, 'desistiu')
    if item['status'] == 'notificado':
        notificar_proximo_da_fila(item['id_livro'])

    return {'sucesso': True}

def notificar_proximo_da_fila(id_livro: str) -> bool:
    fila = FilaEspera.list_by_livro(id_livro)
    for item in fila:
        if item['status'] == 'aguardando':
            hoje = date.today()
            data_limite = hoje + timedelta(days=3)
            FilaEspera.update_status(
                item['id_fila'],
                'notificado',
                data_notificacao=datetime.now(),
                data_limite_resposta=data_limite
            )
            # Garantir exemplar reservado
            exemplares = Exemplar.list_by_livro(id_livro)
            for ex in exemplares:
                if ex['status'] == 'disponivel':
                    Exemplar.update_status(ex['id_exemplar'], 'reservado')
                    break
            return True
    return False
