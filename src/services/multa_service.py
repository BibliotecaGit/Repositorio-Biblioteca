from datetime import date, datetime, timedelta
from src.models.aluguel import Aluguel
from src.models.multa import Multa
from src.models.usuario import Usuario
from src.models.exemplar import Exemplar
from src.database.connection import get_connection, is_mysql

def verificar_atrasos() -> dict:
    alugueis_ativos = Aluguel.list_ativos()
    hoje = date.today()
    processados = 0
    multas_geradas = 0

    for a in alugueis_ativos:
        data_prevista = a['data_devolucao_prevista']
        if isinstance(data_prevista, str):
            dt_prevista = datetime.strptime(data_prevista[:10], "%Y-%m-%d").date()
        elif isinstance(data_prevista, datetime):
            dt_prevista = data_prevista.date()
        else:
            dt_prevista = data_prevista

        if dt_prevista < hoje:
            processados += 1
            dias_atraso = (hoje - dt_prevista).days
            Aluguel.update_status(a['id_aluguel'], 'atrasado')

            valor_livro = float(a.get('valor_livro', 0.0))
            valor_multa = min(dias_atraso * 3.0, valor_livro)

            # Verificar se já existe multa por atraso para este aluguel
            multas_existentes = Multa.list_by_usuario(a['id_usuario'])
            multa_atraso = None
            for m in multas_existentes:
                if m['id_aluguel'] == a['id_aluguel'] and m['tipo'] == 'atraso':
                    multa_atraso = m
                    break

            if multa_atraso:
                conn = get_connection()
                cursor = conn.cursor()
                ph = "%s" if is_mysql() else "?"
                cursor.execute(
                    f"UPDATE multa SET valor = {ph}, dias_atraso = {ph} WHERE id_multa = {ph}",
                    (valor_multa, dias_atraso, multa_atraso['id_multa'])
                )
                conn.commit()
                conn.close()
            else:
                Multa.create(
                    id_aluguel=a['id_aluguel'],
                    id_usuario=a['id_usuario'],
                    tipo='atraso',
                    valor=valor_multa,
                    dias_atraso=dias_atraso,
                    status='pendente'
                )
                multas_geradas += 1

            # Calcular bloqueio do usuário se dias_atraso > 15
            if dias_atraso > 15:
                dias_para_teto = int(valor_livro / 3.0) if valor_livro > 0 else 0
                extras = max(0, dias_atraso - dias_para_teto) if valor_multa >= valor_livro else 0
                dias_bloqueio = 7 + extras
                bloqueado_ate = hoje + timedelta(days=dias_bloqueio)
                Usuario.update(a['id_usuario'], bloqueado_ate=bloqueado_ate)

    return {
        'sucesso': True,
        'alugueis_atrasados_processados': processados,
        'multas_geradas': multas_geradas
    }

def aplicar_multa_dano(id_aluguel: str, tipo_dano: str) -> dict:
    aluguel = Aluguel.get_by_id(id_aluguel)
    if not aluguel:
        return {'sucesso': False, 'erro': 'Aluguel não encontrado.'}

    # Valor fixo de R$ 20.00
    valor_dano = 20.00
    id_multa = Multa.create(
        id_aluguel=id_aluguel,
        id_usuario=aluguel['id_usuario'],
        tipo='dano',
        tipo_dano=tipo_dano,
        valor=valor_dano,
        status='pendente'
    )

    # Atualizar exemplar
    Exemplar.update_status(aluguel['id_exemplar'], 'danificado')

    return {
        'sucesso': True,
        'id_multa': id_multa,
        'valor': valor_dano
    }
