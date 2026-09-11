from datetime import date
from src.services.elegibilidade import verificar_elegibilidade
from src.services.prazo import calcular_prazo
from src.models.exemplar import Exemplar
from src.models.fila_espera import FilaEspera
from src.models.aluguel import Aluguel

def alugar_livro(id_usuario: str, id_livro: str) -> dict:
    # 1. Verifica elegibilidade do usuário
    check = verificar_elegibilidade(id_usuario)
    if not check['elegivel']:
        return {'sucesso': False, 'erro': check['motivo']}

    # 2. Verifica se há fila para o livro
    fila = FilaEspera.list_by_livro(id_livro)
    if len(fila) > 0:
        return {'sucesso': False, 'erro': 'Livro possui fila de espera. Entre na fila.'}

    # 3. Busca um exemplar disponível
    exemplares = Exemplar.list_by_livro(id_livro)
    exemplar_disponivel = None
    for ex in exemplares:
        if ex['status'] == 'disponivel':
            exemplar_disponivel = ex
            break

    if not exemplar_disponivel:
        return {'sucesso': False, 'erro': 'Nenhum exemplar disponível no momento.'}

    # 4. Calcula prazo
    data_prevista = calcular_prazo(id_livro)
    hoje = date.today()

    # 5. Cria aluguel e altera exemplar para alugado
    id_aluguel = Aluguel.create(
        id_usuario=id_usuario,
        id_exemplar=exemplar_disponivel['id_exemplar'],
        data_aluguel=hoje,
        data_devolucao_prevista=data_prevista,
        status='ativo'
    )
    Exemplar.update_status(exemplar_disponivel['id_exemplar'], 'alugado')

    return {
        'sucesso': True,
        'id_aluguel': id_aluguel,
        'data_aluguel': hoje.strftime("%Y-%m-%d"),
        'data_devolucao_prevista': data_prevista.strftime("%Y-%m-%d")
    }
