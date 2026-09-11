from src.models.usuario import Usuario

def realizar_login():
    print("\n--- LOGIN ---")
    cpf = input("CPF: ").strip()
    senha = input("Senha: ").strip()

    usuario = Usuario.get_by_cpf(cpf)
    if not usuario:
        print("Credenciais inválidas.")
        return None

    if not Usuario.verify_password(senha, usuario['senha_hash']):
        print("Credenciais inválidas.")
        return None

    print(f"Bem-vindo(a), {usuario['nome']}! ({usuario['tipo'].capitalize()})")
    return usuario
