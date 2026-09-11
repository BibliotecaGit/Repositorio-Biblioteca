from src.views.auth import realizar_login
from src.views.aluno_view import menu_aluno
from src.views.bibliotecario_view import menu_bibliotecario
from src.views.admin_view import menu_admin

def main():
    print("==================================================")
    print("   SISTEMA DE BIBLIOTECA COM ALUGUEL DE LIVROS    ")
    print("==================================================")

    usuario = realizar_login()
    if not usuario:
        print("Saindo do sistema...")
        return

    tipo = usuario.get('tipo', 'aluno')
    if tipo == 'aluno':
        menu_aluno(usuario)
    elif tipo == 'bibliotecario':
        menu_bibliotecario(usuario)
    elif tipo == 'administrador':
        menu_admin(usuario)
    else:
        print(f"Tipo de usuário desconhecido: {tipo}")

if __name__ == "__main__":
    main()
