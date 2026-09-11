from datetime import date, datetime
from src.models.aluguel import Aluguel
from src.models.exemplar import Exemplar
from src.models.multa import Multa
from src.models.livro import Livro
from src.services.fila_service import notificar_proximo_da_fila
from src.database.connection import get_connection, is_mysql

def devolver_livro(id_aluguel: str) -> dict:
    aluguel = Aluguel.get_by_id(id_aluguel)
    if not aluguel:
        return {'sucesso': False, 'erro': 'Aluguel não encontrado.'}

    if aluguel['status'] not in ('ativo', 'extensao', 'atrasado'):
        return {'sucesso': False, 'erro': f"Livro não está emprestado (status atual: {aluguel['status']})."}

    hoje = date.today()
    data_prevista = aluguel['data_devolucao_prevista']
    if isinstance(data_prevista, str):
        data_prevista = datetime.strptime(data_prevista[:10], "%Y-%m-%d").date()
    elif isinstance(data_prevista, datetime):
        data_prevista = data_prevista.date()

    dias_atraso = (hoje - data_prevista).days
    valor_multa = 0.0
    multa_id = None

    if dias_atraso > 0:
        valor_livro = float(aluguel.get('valor_livro', 0.0))
        valor_multa = min(dias_atraso * 3.0, valor_livro)

        # Verificar se já existe multa por atraso para este aluguel
        multas_existentes = Multa.list_by_usuario(aluguel['id_usuario'])
        multa_atraso = None
        for m in multas_existentes:
            if m['id_aluguel'] == id_aluguel and m['tipo'] == 'atraso':
                multa_atraso = m
                break

        if multa_atraso:
            multa_id = multa_atraso['id_multa']
            conn = get_connection()
            cursor = conn.cursor()
            ph = "%s" if is_mysql() else "?"
            cursor.execute(
                f"UPDATE multa SET valor = {ph}, dias_atraso = {ph} WHERE id_multa = {ph}",
                (valor_multa, dias_atraso, multa_id)
            )
            conn.commit()
            conn.close()
        else:
            multa_id = Multa.create(
                id_aluguel=id_aluguel,
                id_usuario=aluguel['id_usuario'],
                tipo='atraso',
                valor=valor_multa,
                dias_atraso=dias_atraso,
                status='pendente'
            )

    # Atualiza aluguel
    Aluguel.update(id_aluguel, status='devolvido', data_devolucao_real=hoje)

    id_exemplar = aluguel['id_exemplar']
    id_livro = aluguel['id_livro']

    # Libera exemplar
    Exemplar.update_status(id_exemplar, 'disponivel')

    # Processa fila
    notificado = notificar_proximo_da_fila(id_livro)

    return {
        'sucesso': True,
        'dias_atraso': max(0, dias_atraso),
        'valor_multa': valor_multa,
        'multa_id': multa_id,
        'notificou_fila': notificado
    }
