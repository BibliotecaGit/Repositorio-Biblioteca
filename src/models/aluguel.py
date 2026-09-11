import uuid
from datetime import date, datetime
from src.database.connection import get_connection, is_mysql

def get_ph():
    return "%s" if is_mysql() else "?"

class Aluguel:
    @classmethod
    def create(cls, id_usuario, id_exemplar, data_aluguel, data_devolucao_prevista, status='ativo'):
        conn = get_connection()
        cursor = conn.cursor()
        id_aluguel = str(uuid.uuid4())

        if isinstance(data_aluguel, (date, datetime)):
            data_aluguel = data_aluguel.strftime("%Y-%m-%d")
        if isinstance(data_devolucao_prevista, (date, datetime)):
            data_devolucao_prevista = data_devolucao_prevista.strftime("%Y-%m-%d")

        ph = get_ph()
        sql = f"""
            INSERT INTO aluguel (id_aluguel, id_usuario, id_exemplar, data_aluguel, data_devolucao_prevista, status)
            VALUES ({ph}, {ph}, {ph}, {ph}, {ph}, {ph})
        """
        cursor.execute(sql, (id_aluguel, id_usuario, id_exemplar, data_aluguel, data_devolucao_prevista, status))
        conn.commit()
        conn.close()
        return id_aluguel

    @classmethod
    def get_by_id(cls, id_aluguel):
        conn = get_connection()
        cursor = conn.cursor()
        ph = get_ph()
        sql = f"""
            SELECT a.*, u.nome as nome_usuario, u.email as email_usuario,
                   e.codigo_barras, l.id_livro, l.titulo as titulo_livro, l.valor_livro
            FROM aluguel a
            JOIN usuario u ON a.id_usuario = u.id_usuario
            JOIN exemplar e ON a.id_exemplar = e.id_exemplar
            JOIN livro l ON e.id_livro = l.id_livro
            WHERE a.id_aluguel = {ph}
        """
        cursor.execute(sql, (id_aluguel,))
        row = cursor.fetchone()
        conn.close()
        if not row:
            return None
        return dict(row) if isinstance(row, dict) or hasattr(row, 'keys') else cls._row_to_dict(cursor, row)

    @classmethod
    def list_by_usuario(cls, id_usuario):
        conn = get_connection()
        cursor = conn.cursor()
        ph = get_ph()
        sql = f"""
            SELECT a.*, e.codigo_barras, l.id_livro, l.titulo as titulo_livro, l.valor_livro
            FROM aluguel a
            JOIN exemplar e ON a.id_exemplar = e.id_exemplar
            JOIN livro l ON e.id_livro = l.id_livro
            WHERE a.id_usuario = {ph}
            ORDER BY a.data_aluguel DESC
        """
        cursor.execute(sql, (id_usuario,))
        rows = cursor.fetchall()
        conn.close()
        res = []
        for r in rows:
            res.append(dict(r) if isinstance(r, dict) or hasattr(r, 'keys') else cls._row_to_dict(cursor, r))
        return res

    @classmethod
    def list_ativos(cls):
        conn = get_connection()
        cursor = conn.cursor()
        sql = """
            SELECT a.*, u.nome as nome_usuario, e.codigo_barras, l.id_livro, l.titulo as titulo_livro, l.valor_livro
            FROM aluguel a
            JOIN usuario u ON a.id_usuario = u.id_usuario
            JOIN exemplar e ON a.id_exemplar = e.id_exemplar
            JOIN livro l ON e.id_livro = l.id_livro
            WHERE a.status IN ('ativo', 'extensao', 'atrasado')
            ORDER BY a.data_devolucao_prevista ASC
        """
        cursor.execute(sql)
        rows = cursor.fetchall()
        conn.close()
        res = []
        for r in rows:
            res.append(dict(r) if isinstance(r, dict) or hasattr(r, 'keys') else cls._row_to_dict(cursor, r))
        return res

    @classmethod
    def update_status(cls, id_aluguel, novo_status):
        conn = get_connection()
        cursor = conn.cursor()
        ph = get_ph()
        sql = f"UPDATE aluguel SET status = {ph}, updated_at = CURRENT_TIMESTAMP WHERE id_aluguel = {ph}"
        cursor.execute(sql, (novo_status, id_aluguel))
        conn.commit()
        conn.close()
        return True

    @classmethod
    def update(cls, id_aluguel, **kwargs):
        if not kwargs:
            return False
        conn = get_connection()
        cursor = conn.cursor()
        ph = get_ph()
        fields = []
        values = []
        for key, val in kwargs.items():
            fields.append(f"{key} = {ph}")
            if isinstance(val, (date, datetime)):
                val = val.strftime("%Y-%m-%d")
            values.append(val)
        values.append(id_aluguel)
        sql = f"UPDATE aluguel SET {', '.join(fields)}, updated_at = CURRENT_TIMESTAMP WHERE id_aluguel = {ph}"
        cursor.execute(sql, tuple(values))
        conn.commit()
        conn.close()
        return True

    @staticmethod
    def _row_to_dict(cursor, row):
        if not row:
            return None
        return {col[0]: row[idx] for idx, col in enumerate(cursor.description)}
