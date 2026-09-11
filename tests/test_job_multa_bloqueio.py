import unittest
from datetime import date, timedelta
from src.database.setup import setup_database
from src.models.usuario import Usuario
from src.models.livro import Livro
from src.models.exemplar import Exemplar
from src.models.aluguel import Aluguel
from src.models.multa import Multa

from src.services.aluguel_service import alugar_livro
from src.services.multa_service import verificar_atrasos
from src.services.elegibilidade import verificar_elegibilidade

class TestJobMultaBloqueio(unittest.TestCase):
    def setUp(self):
        setup_database()

    def test_t4_3_job_multa_bloqueio(self):
        user_a = Usuario.create("Usuario A", "usera@test.com", "11111111111", "3A", "passA", "aluno")

        id_livro = Livro.create("Livro Barato", "Autor X", "Aventura", 100, 30.00)
        id_exemplar = Exemplar.create(id_livro, "EX_BAR_1")

        # 1. Criar aluguel com data_prevista = hoje - 20 dias
        res_a = alugar_livro(user_a, id_livro)
        data_atrasada = date.today() - timedelta(days=20)
        Aluguel.update(res_a['id_aluguel'], data_devolucao_prevista=data_atrasada)

        # 2. Executar verificar_atrasos()
        verificar_atrasos()

        # 3. Verificar multa criada (20 dias * R$ 3 = R$ 60, mas teto = R$ 30,00)
        multas = Multa.list_by_usuario(user_a)
        self.assertEqual(len(multas), 1)
        self.assertEqual(multas[0]['valor'], 30.00)

        # 4. Verificar usuário bloqueado e inelegível para novos aluguéis
        usr = Usuario.get_by_id(user_a)
        self.assertIsNotNone(usr['bloqueado_ate'])

        eleg = verificar_elegibilidade(user_a)
        self.assertFalse(eleg['elegivel'])

if __name__ == '__main__':
    unittest.main()
