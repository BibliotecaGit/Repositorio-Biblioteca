import unittest
from datetime import date, timedelta
from src.database.setup import setup_database
from src.models.usuario import Usuario
from src.models.livro import Livro
from src.models.exemplar import Exemplar
from src.models.aluguel import Aluguel
from src.models.fila_espera import FilaEspera
from src.models.multa import Multa

from src.services.aluguel_service import alugar_livro
from src.services.extensao_service import solicitar_extensao
from src.services.fila_service import entrar_fila
from src.services.multa_service import verificar_atrasos

class TestExtensaoInterrompida(unittest.TestCase):
    def setUp(self):
        setup_database()

    def test_t4_2_extensao_interrompida(self):
        user_a = Usuario.create("Usuario A", "usera@test.com", "11111111111", "3A", "passA", "aluno")
        user_b = Usuario.create("Usuario B", "userb@test.com", "22222222222", "3A", "passB", "aluno")

        id_livro = Livro.create("Livro Ext", "Autor Ext", "Drama", 200, 50.00)
        id_exemplar = Exemplar.create(id_livro, "EX_EXT_1")

        # 1. Usuário A aluga livro
        res_a = alugar_livro(user_a, id_livro)
        self.assertTrue(res_a['sucesso'])

        # 2. Usuário A solicita extensão (contador = 1)
        ext = solicitar_extensao(res_a['id_aluguel'])
        self.assertTrue(ext['sucesso'])
        self.assertEqual(ext['extensao_contador'], 1)

        # 3. Usuário B tenta entrar na fila -> OK
        res_b_fila = entrar_fila(user_b, id_livro)
        self.assertTrue(res_b_fila['sucesso'])

        # Se alguém está na fila, tenta solicitar nova extensão para A -> deve dar erro
        ext2 = solicitar_extensao(res_a['id_aluguel'])
        self.assertFalse(ext2['sucesso'])
        self.assertIn("Há pessoas na fila", ext2['erro'])

        # Simular que a data prevista de devolução venceu há 5 dias
        hoje = date.today()
        data_vencida = hoje - timedelta(days=5)
        Aluguel.update(res_a['id_aluguel'], data_devolucao_prevista=data_vencida)

        # Executa verificação de atraso
        res_job = verificar_atrasos()
        self.assertGreater(res_job['alugueis_atrasados_processados'], 0)

        # Verifica que multa de atraso foi criada
        multas_a = Multa.list_by_usuario(user_a)
        self.assertEqual(len(multas_a), 1)
        self.assertEqual(multas_a[0]['valor'], 15.00) # 5 * 3 = 15

if __name__ == '__main__':
    unittest.main()
