import uuid
from datetime import date, datetime
from src.database.connection import get_connection, is_mysql

def get_ph():
    return "%s" if is_mysql() else "?"

class FilaEspera:
    @classmethod
    def create(cls, id_livro, id_usuario, status='aguardando'):
        conn = get_connection()
        cursor = conn.cursor()
        id_fila = str(uuid.uuid4())
        ph = get_ph()

        # Obter a última posição na fila do livro
        sql_pos = f"SELECT MAX(posicao) FROM fila_espera WHERE id_livro = {ph}"
        cursor.execute(sql_pos, (id_livro,))
        row = cursor.fetchone()
        max_pos = 0
        if row and row[0] is not None:
            max_pos = row[0]
        posicao = max_pos + 1

        sql = f"""
            INSERT INTO fila_espera (id_fila, id_livro, id_usuario, posicao, status)
            VALUES ({ph}, {ph}, {ph}, {ph}, {ph})
        """
        cursor.execute(sql, (id_fila, id_livro, id_usuario, posicao, status))
        conn.commit()
        conn.close()
        return id_fila

    @classmethod
    def get_by_id(cls, id_fila):
        conn = get_connection()
        cursor = conn.cursor()
        ph = get_ph()
        sql = f"""
            SELECT f.*, u.nome as nome_usuario, u.email as email_usuario, l.titulo as titulo_livro
            FROM fila_espera f
            JOIN usuario u ON f.id_usuario = u.id_usuario
            JOIN livro l ON f.id_livro = l.id_livro
            WHERE f.id_fila = {ph}
        """
        cursor.execute(sql, (id_fila,))
        row = cursor.fetchone()
        conn.close()
        if not row:
            return None
        return dict(row) if isinstance(row, dict) or hasattr(row, 'keys') else cls._row_to_dict(cursor, row)

    @classmethod
    def get_by_usuario_livro(cls, id_usuario, id_livro):
        conn = get_connection()
        cursor = conn.cursor()
        ph = get_ph()
        sql = f"SELECT * FROM fila_espera WHERE id_usuario = {ph} AND id_livro = {ph} AND status IN ('aguardando', 'notificado')"
        cursor.execute(sql, (id_usuario, id_livro))
        row = cursor.fetchone()
        conn.close()
        if not row:
            return None
        return dict(row) if isinstance(row, dict) or hasattr(row, 'keys') else cls._row_to_dict(cursor, row)

    @classmethod
    def list_by_livro(cls, id_livro):
        conn = get_connection()
        cursor = conn.cursor()
        ph = get_ph()
        sql = f"""
            SELECT f.*, u.nome as nome_usuario, u.email as email_usuario
            FROM fila_espera f
            JOIN usuario u ON f.id_usuario = u.id_usuario
            WHERE f.id_livro = {ph} AND f.status IN ('aguardando', 'notificado')
            ORDER BY f.posicao ASC
        """
        cursor.execute(sql, (id_livro,))
        rows = cursor.fetchall()
        conn.close()
        res = []
        for r in rows:
            res.append(dict(r) if isinstance(r, dict) or hasattr(r, 'keys') else cls._row_to_dict(cursor, r))
        return res

    @classmethod
    def list_by_usuario(cls, id_usuario):
        conn = get_connection()
        cursor = conn.cursor()
        ph = get_ph()
        sql = f"""
            SELECT f.*, l.titulo as titulo_livro
            FROM fila_espera f
            JOIN livro l ON f.id_livro = l.id_livro
            WHERE f.id_usuario = {ph}
            ORDER BY f.data_entrada DESC
        """
        cursor.execute(sql, (id_usuario,))
        rows = cursor.fetchall()
        conn.close()
        res = []
        for r in rows:
            res.append(dict(r) if isinstance(r, dict) or hasattr(r, 'keys') else cls._row_to_dict(cursor, r))
        return res

    @classmethod
    def update_status(cls, id_fila, novo_status, **kwargs):
        conn = get_connection()
        cursor = conn.cursor()
        ph = get_ph()

        fields = [f"status = {ph}"]
        values = [novo_status]
        for key, val in kwargs.items():
            fields.append(f"{key} = {ph}")
            if isinstance(val, (date, datetime)):
                val = val.strftime("%Y-%m-%d %H:%M:%S")
            values.append(val)
        values.append(id_fila)

        sql = f"UPDATE fila_espera SET {', '.join(fields)}, updated_at = CURRENT_TIMESTAMP WHERE id_fila = {ph}"
        cursor.execute(sql, tuple(values))
        conn.commit()
        conn.close()
        return True

    @classmethod
    def delete(cls, id_fila):
        conn = get_connection()
        cursor = conn.cursor()
        ph = get_ph()
        sql = f"DELETE FROM fila_espera WHERE id_fila = {ph}"
        cursor.execute(sql, (id_fila,))
        conn.commit()
        conn.close()
        return True

    @staticmethod
    def _row_to_dict(cursor, row):
        if not row:
            return None
        return {col[0]: row[idx] for idx, col in enumerate(cursor.description)}
