from datetime import date, datetime
from src.models.usuario import Usuario
from src.models.aluguel import Aluguel

def verificar_elegibilidade(id_usuario: str) -> dict:
    usuario = Usuario.get_by_id(id_usuario)
    if not usuario:
        return {'elegivel': False, 'motivo': 'Usuário não encontrado.'}

    # Verificar se está bloqueado
    bloqueado_ate = usuario.get('bloqueado_ate')
    if bloqueado_ate:
        if isinstance(bloqueado_ate, str):
            bloqueado_ate_dt = datetime.strptime(bloqueado_ate[:10], "%Y-%m-%d").date()
        elif isinstance(bloqueado_ate, datetime):
            bloqueado_ate_dt = bloqueado_ate.date()
        else:
            bloqueado_ate_dt = bloqueado_ate

        if bloqueado_ate_dt >= date.today():
            data_str = bloqueado_ate_dt.strftime("%d/%m/%Y")
            return {'elegivel': False, 'motivo': f'Usuário bloqueado até {data_str}'}

    # Verificar aluguéis ativos/atrasados
    alugueis = Aluguel.list_by_usuario(id_usuario)
    ativos = 0
    for a in alugueis:
        status = a.get('status')
        if status in ('ativo', 'extensao'):
            ativos += 1
        elif status == 'atrasado':
            return {'elegivel': False, 'motivo': 'Possui atrasos pendentes'}

    if ativos >= 3:
        return {'elegivel': False, 'motivo': 'Limite de 3 livros atingido'}

    return {'elegivel': True}
