import uuid
from src.database.connection import get_connection, is_mysql

def get_ph():
    return "%s" if is_mysql() else "?"

class Multa:
    @classmethod
    def create(cls, id_aluguel, id_usuario, tipo, valor, tipo_dano=None, dias_atraso=None, status='pendente'):
        conn = get_connection()
        cursor = conn.cursor()
        id_multa = str(uuid.uuid4())
        valor = float(valor)
        ph = get_ph()

        sql = f"""
            INSERT INTO multa (id_multa, id_aluguel, id_usuario, tipo, tipo_dano, valor, dias_atraso, status)
            VALUES ({ph}, {ph}, {ph}, {ph}, {ph}, {ph}, {ph}, {ph})
        """
        cursor.execute(sql, (id_multa, id_aluguel, id_usuario, tipo, tipo_dano, valor, dias_atraso, status))
        conn.commit()
        conn.close()
        return id_multa

    @classmethod
    def get_by_id(cls, id_multa):
        conn = get_connection()
        cursor = conn.cursor()
        ph = get_ph()
        sql = f"""
            SELECT m.*, u.nome as nome_usuario, l.titulo as titulo_livro
            FROM multa m
            JOIN usuario u ON m.id_usuario = u.id_usuario
            JOIN aluguel a ON m.id_aluguel = a.id_aluguel
            JOIN exemplar e ON a.id_exemplar = e.id_exemplar
            JOIN livro l ON e.id_livro = l.id_livro
            WHERE m.id_multa = {ph}
        """
        cursor.execute(sql, (id_multa,))
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
            SELECT m.*, l.titulo as titulo_livro
            FROM multa m
            JOIN aluguel a ON m.id_aluguel = a.id_aluguel
            JOIN exemplar e ON a.id_exemplar = e.id_exemplar
            JOIN livro l ON e.id_livro = l.id_livro
            WHERE m.id_usuario = {ph}
            ORDER BY m.created_at DESC
        """
        cursor.execute(sql, (id_usuario,))
        rows = cursor.fetchall()
        conn.close()
        res = []
        for r in rows:
            res.append(dict(r) if isinstance(r, dict) or hasattr(r, 'keys') else cls._row_to_dict(cursor, r))
        return res

    @classmethod
    def list_pendentes(cls):
        conn = get_connection()
        cursor = conn.cursor()
        sql = """
            SELECT m.*, u.nome as nome_usuario, l.titulo as titulo_livro
            FROM multa m
            JOIN usuario u ON m.id_usuario = u.id_usuario
            JOIN aluguel a ON m.id_aluguel = a.id_aluguel
            JOIN exemplar e ON a.id_exemplar = e.id_exemplar
            JOIN livro l ON e.id_livro = l.id_livro
            WHERE m.status = 'pendente'
            ORDER BY m.created_at DESC
        """
        cursor.execute(sql)
        rows = cursor.fetchall()
        conn.close()
        res = []
        for r in rows:
            res.append(dict(r) if isinstance(r, dict) or hasattr(r, 'keys') else cls._row_to_dict(cursor, r))
        return res

    @classmethod
    def pagar(cls, id_multa):
        conn = get_connection()
        cursor = conn.cursor()
        ph = get_ph()
        sql = f"UPDATE multa SET status = 'pago' WHERE id_multa = {ph}"
        cursor.execute(sql, (id_multa,))
        conn.commit()
        conn.close()
        return True

    @staticmethod
    def _row_to_dict(cursor, row):
        if not row:
            return None
        return {col[0]: row[idx] for idx, col in enumerate(cursor.description)}
