import unittest
from src.database.setup import setup_database
from src.models.usuario import Usuario
from src.models.livro import Livro
from src.models.exemplar import Exemplar
from src.models.aluguel import Aluguel
from src.models.fila_espera import FilaEspera

from src.services.aluguel_service import alugar_livro
from src.services.fila_service import entrar_fila, confirmar_aluguel_fila
from src.services.devolucao_service import devolver_livro

class TestFluxoCompleto(unittest.TestCase):
    def setUp(self):
        setup_database()

    def test_t4_1_fluxo_completo(self):
        # 1. Cadastrar usuário A e usuário B
        user_a = Usuario.create("Usuario A", "usera@test.com", "11111111111", "3A", "passA", "aluno")
        user_b = Usuario.create("Usuario B", "userb@test.com", "22222222222", "3A", "passB", "aluno")

        # 2. Cadastrar livro com 1 exemplar
        id_livro = Livro.create("Livro Unico", "Autor Unico", "Ficção", 120, 35.00)
        id_exemplar = Exemplar.create(id_livro, "EX_UNICO_1")

        # 3. Usuário A aluga o livro
        res_a = alugar_livro(user_a, id_livro)
        self.assertTrue(res_a['sucesso'])
        ex = Exemplar.get_by_id(id_exemplar)
        self.assertEqual(ex['status'], 'alugado')

        # 4. Usuário B entra na fila (posição 1)
        res_b_fila = entrar_fila(user_b, id_livro)
        self.assertTrue(res_b_fila['sucesso'])
        self.assertEqual(res_b_fila['posicao'], 1)

        # 5. Usuário A devolve o livro
        dev_a = devolver_livro(res_a['id_aluguel'])
        self.assertTrue(dev_a['sucesso'])
        self.assertTrue(dev_a['notificou_fila'])

        # 6. Verificar: exemplar fica 'reservado', usuário B é notificado
        ex = Exemplar.get_by_id(id_exemplar)
        self.assertEqual(ex['status'], 'reservado')

        fila_b = FilaEspera.get_by_usuario_livro(user_b, id_livro)
        self.assertEqual(fila_b['status'], 'notificado')

        # 7. Usuário B confirma aluguel
        conf_b = confirmar_aluguel_fila(fila_b['id_fila'])
        self.assertTrue(conf_b['sucesso'])

        # 8. Verificar: novo aluguel criado para B, exemplar 'alugado'
        ex = Exemplar.get_by_id(id_exemplar)
        self.assertEqual(ex['status'], 'alugado')

        alugueis_b = Aluguel.list_by_usuario(user_b)
        self.assertEqual(len(alugueis_b), 1)
        self.assertEqual(alugueis_b[0]['status'], 'ativo')

if __name__ == '__main__':
    unittest.main()
