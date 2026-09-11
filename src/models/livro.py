import uuid
from src.database.connection import get_connection, is_mysql

def get_ph():
    return "%s" if is_mysql() else "?"

class Livro:
    @staticmethod
    def calcular_tamanho(num_paginas: int) -> str:
        return 'pequeno' if num_paginas <= 150 else 'grande'

    @classmethod
    def create(cls, titulo, autor, genero, num_paginas, valor_livro, tamanho=None):
        num_paginas = int(num_paginas)
        valor_livro = float(valor_livro)
        if not tamanho:
            tamanho = cls.calcular_tamanho(num_paginas)

        conn = get_connection()
        cursor = conn.cursor()
        id_livro = str(uuid.uuid4())
        ph = get_ph()

        sql = f"""
            INSERT INTO livro (id_livro, titulo, autor, genero, tamanho, num_paginas, valor_livro)
            VALUES ({ph}, {ph}, {ph}, {ph}, {ph}, {ph}, {ph})
        """
        cursor.execute(sql, (id_livro, titulo, autor, genero, tamanho, num_paginas, valor_livro))
        conn.commit()
        conn.close()
        return id_livro

    @classmethod
    def get_by_id(cls, id_livro):
        conn = get_connection()
        cursor = conn.cursor()
        ph = get_ph()
        sql = f"SELECT * FROM livro WHERE id_livro = {ph} AND deleted_at IS NULL"
        cursor.execute(sql, (id_livro,))
        row = cursor.fetchone()
        conn.close()
        if not row:
            return None
        return dict(row) if isinstance(row, dict) or hasattr(row, 'keys') else cls._row_to_dict(cursor, row)

    @classmethod
    def search(cls, query):
        conn = get_connection()
        cursor = conn.cursor()
        ph = get_ph()
        q = f"%{query}%"
        sql = f"""
            SELECT * FROM livro
            WHERE (titulo LIKE {ph} OR autor LIKE {ph} OR genero LIKE {ph})
              AND deleted_at IS NULL
            ORDER BY titulo
        """
        cursor.execute(sql, (q, q, q))
        rows = cursor.fetchall()
        conn.close()
        res = []
        for r in rows:
            res.append(dict(r) if isinstance(r, dict) or hasattr(r, 'keys') else cls._row_to_dict(cursor, r))
        return res

    @classmethod
    def update(cls, id_livro, **kwargs):
        if not kwargs:
            return False
        if 'num_paginas' in kwargs and 'tamanho' not in kwargs:
            kwargs['tamanho'] = cls.calcular_tamanho(int(kwargs['num_paginas']))

        conn = get_connection()
        cursor = conn.cursor()
        ph = get_ph()
        fields = []
        values = []
        for key, val in kwargs.items():
            fields.append(f"{key} = {ph}")
            values.append(val)
        values.append(id_livro)
        sql = f"UPDATE livro SET {', '.join(fields)}, updated_at = CURRENT_TIMESTAMP WHERE id_livro = {ph}"
        cursor.execute(sql, tuple(values))
        conn.commit()
        conn.close()
        return True

    @classmethod
    def delete(cls, id_livro):
        conn = get_connection()
        cursor = conn.cursor()
        ph = get_ph()
        sql = f"UPDATE livro SET deleted_at = CURRENT_TIMESTAMP WHERE id_livro = {ph}"
        cursor.execute(sql, (id_livro,))
        conn.commit()
        conn.close()
        return True

    @classmethod
    def list_all(cls):
        conn = get_connection()
        cursor = conn.cursor()
        sql = "SELECT * FROM livro WHERE deleted_at IS NULL ORDER BY titulo"
        cursor.execute(sql)
        rows = cursor.fetchall()
        conn.close()
        res = []
        for r in rows:
            res.append(dict(r) if isinstance(r, dict) or hasattr(r, 'keys') else cls._row_to_dict(cursor, r))
        return res

    @staticmethod
    def _row_to_dict(cursor, row):
        if not row:
            return None
        return {col[0]: row[idx] for idx, col in enumerate(cursor.description)}
