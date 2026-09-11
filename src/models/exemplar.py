import uuid
from src.database.connection import get_connection, is_mysql

def get_ph():
    return "%s" if is_mysql() else "?"

class Exemplar:
    @classmethod
    def create(cls, id_livro, codigo_barras, status='disponivel'):
        conn = get_connection()
        cursor = conn.cursor()
        id_exemplar = str(uuid.uuid4())
        ph = get_ph()
        sql = f"""
            INSERT INTO exemplar (id_exemplar, id_livro, codigo_barras, status)
            VALUES ({ph}, {ph}, {ph}, {ph})
        """
        cursor.execute(sql, (id_exemplar, id_livro, codigo_barras, status))
        conn.commit()
        conn.close()
        return id_exemplar

    @classmethod
    def get_by_id(cls, id_exemplar):
        conn = get_connection()
        cursor = conn.cursor()
        ph = get_ph()
        sql = f"""
            SELECT e.*, l.titulo as titulo_livro
            FROM exemplar e
            JOIN livro l ON e.id_livro = l.id_livro
            WHERE e.id_exemplar = {ph}
        """
        cursor.execute(sql, (id_exemplar,))
        row = cursor.fetchone()
        conn.close()
        if not row:
            return None
        return dict(row) if isinstance(row, dict) or hasattr(row, 'keys') else cls._row_to_dict(cursor, row)

    @classmethod
    def get_by_codigo_barras(cls, codigo_barras):
        conn = get_connection()
        cursor = conn.cursor()
        ph = get_ph()
        sql = f"""
            SELECT e.*, l.titulo as titulo_livro
            FROM exemplar e
            JOIN livro l ON e.id_livro = l.id_livro
            WHERE e.codigo_barras = {ph}
        """
        cursor.execute(sql, (codigo_barras,))
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
        sql = f"SELECT * FROM exemplar WHERE id_livro = {ph} ORDER BY created_at"
        cursor.execute(sql, (id_livro,))
        rows = cursor.fetchall()
        conn.close()
        res = []
        for r in rows:
            res.append(dict(r) if isinstance(r, dict) or hasattr(r, 'keys') else cls._row_to_dict(cursor, r))
        return res

    @classmethod
    def update_status(cls, id_exemplar, novo_status):
        conn = get_connection()
        cursor = conn.cursor()
        ph = get_ph()
        sql = f"UPDATE exemplar SET status = {ph}, updated_at = CURRENT_TIMESTAMP WHERE id_exemplar = {ph}"
        cursor.execute(sql, (novo_status, id_exemplar))
        conn.commit()
        conn.close()
        return True

    @classmethod
    def delete(cls, id_exemplar):
        conn = get_connection()
        cursor = conn.cursor()
        ph = get_ph()
        sql = f"DELETE FROM exemplar WHERE id_exemplar = {ph}"
        cursor.execute(sql, (id_exemplar,))
        conn.commit()
        conn.close()
        return True

    @staticmethod
    def _row_to_dict(cursor, row):
        if not row:
            return None
        return {col[0]: row[idx] for idx, col in enumerate(cursor.description)}
