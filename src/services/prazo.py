from datetime import date, timedelta
from src.models.livro import Livro

def calcular_prazo(id_livro: str) -> date:
    livro = Livro.get_by_id(id_livro)
    if not livro:
        raise ValueError("Livro não encontrado.")

    tamanho = livro.get('tamanho')
    if not tamanho:
        num_paginas = livro.get('num_paginas', 0)
        tamanho = Livro.calcular_tamanho(num_paginas)

    hoje = date.today()
    if tamanho == 'pequeno':
        return hoje + timedelta(days=15)
    else:
        return hoje + timedelta(days=30)
