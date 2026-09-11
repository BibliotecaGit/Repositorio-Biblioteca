import os
import config
from src.database.connection import get_connection

def setup_database():
    if config.DB_ENGINE == "sqlite":
        if os.path.exists(config.DB_FILE):
            try:
                os.remove(config.DB_FILE)
            except OSError:
                pass

    schema_path = os.path.join(os.path.dirname(__file__), "../../sql/schema.sql")
    with open(schema_path, "r", encoding="utf-8") as f:
        schema_sql = f.read()

    conn = get_connection()
    cursor = conn.cursor()

    statements = [s.strip() for s in schema_sql.split(";") if s.strip()]
    for stmt in statements:
        cursor.execute(stmt)

    conn.commit()
    conn.close()

if __name__ == "__main__":
    setup_database()
    print("Banco de dados configurado com sucesso!")
