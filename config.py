import os
from dotenv import load_dotenv

load_dotenv()

DB_ENGINE = os.getenv("DB_ENGINE", "sqlite")  # 'sqlite' or 'mysql'
DB_FILE = os.getenv("DB_FILE", "biblioteca.db")

DB_HOST = os.getenv("DB_HOST", "localhost")
DB_USER = os.getenv("DB_USER", "root")
DB_PASSWORD = os.getenv("DB_PASSWORD", "")
DB_NAME = os.getenv("DB_NAME", "biblioteca")
DB_PORT = int(os.getenv("DB_PORT", 3306))
