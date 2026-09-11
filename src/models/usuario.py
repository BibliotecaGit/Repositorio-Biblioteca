import uuid
import bcrypt
from src.database.connection import get_connection, is_mysql

def get_ph():
    return "%s" if is_mysql() else "?"

class Usuario:
    def __init__(self, id_usuario=None, nome=None, email=None, cpf=None, turma=None, senha_hash=None, tipo='aluno', bloqueado_ate=None, created_at=None, updated_at=None):
        self.id_usuario = id_usuario
        self.nome = nome
        self.email = email
        self.cpf = cpf
        self.turma = turma
        self.senha_hash = senha_hash
        self.tipo = tipo
        self.bloqueado_ate = bloqueado_ate
        self.created_at = created_at
        self.updated_at = updated_at

    @staticmethod
    def hash_password(password: str) -> str:
        return bcrypt.hashpw(password.encode('utf-8'), bcrypt.gensalt()).decode('utf-8')

    @staticmethod
    def verify_password(password: str, hashed_password: str) -> bool:
        return bcrypt.checkpw(password.encode('utf-8'), hashed_password.encode('utf-8'))

    @classmethod
    def create(cls, nome, email, cpf, turma, senha, tipo='aluno'):
        conn = get_connection()
        cursor = conn.cursor()
        id_usuario = str(uuid.uuid4())
        senha_hash = cls.hash_password(senha)
        ph = get_ph()

        sql = f"""
            INSERT INTO usuario (id_usuario, nome, email, cpf, turma, senha_hash, tipo)
            VALUES ({ph}, {ph}, {ph}, {ph}, {ph}, {ph}, {ph})
        """
        cursor.execute(sql, (id_usuario, nome, email, cpf, turma, senha_hash, tipo))
        conn.commit()
        conn.close()
        return id_usuario

    @classmethod
    def get_by_id(cls, id_usuario):
        conn = get_connection()
        cursor = conn.cursor()
        ph = get_ph()
        sql = f"SELECT * FROM usuario WHERE id_usuario = {ph}"
        cursor.execute(sql, (id_usuario,))
        row = cursor.fetchone()
        conn.close()
        if not row:
            return None
        return dict(row) if isinstance(row, dict) or hasattr(row, 'keys') else cls._row_to_dict(cursor, row)

    @classmethod
    def get_by_cpf(cls, cpf):
        conn = get_connection()
        cursor = conn.cursor()
        ph = get_ph()
        sql = f"SELECT * FROM usuario WHERE cpf = {ph}"
        cursor.execute(sql, (cpf,))
        row = cursor.fetchone()
        conn.close()
        if not row:
            return None
        return dict(row) if isinstance(row, dict) or hasattr(row, 'keys') else cls._row_to_dict(cursor, row)

    @classmethod
    def get_by_email(cls, email):
        conn = get_connection()
        cursor = conn.cursor()
        ph = get_ph()
        sql = f"SELECT * FROM usuario WHERE email = {ph}"
        cursor.execute(sql, (email,))
        row = cursor.fetchone()
        conn.close()
        if not row:
            return None
        return dict(row) if isinstance(row, dict) or hasattr(row, 'keys') else cls._row_to_dict(cursor, row)

    @classmethod
    def update(cls, id_usuario, **kwargs):
        if not kwargs:
            return False
        conn = get_connection()
        cursor = conn.cursor()
        ph = get_ph()
        fields = []
        values = []
        for key, val in kwargs.items():
            fields.append(f"{key} = {ph}")
            values.append(val)
        values.append(id_usuario)
        sql = f"UPDATE usuario SET {', '.join(fields)}, updated_at = CURRENT_TIMESTAMP WHERE id_usuario = {ph}"
        cursor.execute(sql, tuple(values))
        conn.commit()
        conn.close()
        return True

    @classmethod
    def delete(cls, id_usuario):
        conn = get_connection()
        cursor = conn.cursor()
        ph = get_ph()
        sql = f"DELETE FROM usuario WHERE id_usuario = {ph}"
        cursor.execute(sql, (id_usuario,))
        conn.commit()
        conn.close()
        return True

    @classmethod
    def list_all(cls):
        conn = get_connection()
        cursor = conn.cursor()
        sql = "SELECT * FROM usuario ORDER BY nome"
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
