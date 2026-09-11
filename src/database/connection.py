import sqlite3
import mysql.connector
import config

def is_mysql():
    return config.DB_ENGINE == "mysql"

def get_connection():
    if config.DB_ENGINE == "sqlite":
        conn = sqlite3.connect(config.DB_FILE)
        conn.row_factory = sqlite3.Row
        conn.execute("PRAGMA foreign_keys = ON;")
        return conn
    elif config.DB_ENGINE == "mysql":
        conn = mysql.connector.connect(
            host=config.DB_HOST,
            user=config.DB_USER,
            password=config.DB_PASSWORD,
            database=config.DB_NAME,
            port=config.DB_PORT
        )
        return conn
    else:
        raise ValueError(f"Engine de banco não suportada: {config.DB_ENGINE}")
