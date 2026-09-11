import unittest
from datetime import date, timedelta
from src.database.setup import setup_database
from src.models.usuario import Usuario
from src.models.livro import Livro
from src.models.exemplar import Exemplar
from src.models.aluguel import Aluguel
from src.models.fila_espera import FilaEspera
from src.models.multa import Multa

from src.services.elegibilidade import verificar_elegibilidade
from src.services.prazo import calcular_prazo
from src.services.aluguel_service import alugar_livro
from src.services.fila_service import entrar_fila, confirmar_aluguel_fila, desistir_fila
from src.services.devolucao_service import devolver_livro
from src.services.extensao_service import solicitar_extensao
from src.services.multa_service import verificar_atrasos, aplicar_multa_dano

class TestServices(unittest.TestCase):
    def setUp(self):
        setup_database()
        self.u1 = Usuario.create("Aluno 1", "a1@test.com", "11111111111", "3A", "senha")
        self.u2 = Usuario.create("Aluno 2", "a2@test.com", "22222222222", "3A", "senha")
        self.l_peq = Livro.create("Livro Pequeno", "Autor P", "Gênero P", 100, 30.00)
        self.l_gra = Livro.create("Livro Grande", "Autor G", "Gênero G", 200, 60.00)
        self.ex_peq = Exemplar.create(self.l_peq, "EX_PEQ_1")

    def test_elegibilidade_e_prazo(self):
        self.assertTrue(verificar_elegibilidade(self.u1)['elegivel'])
        prazo_peq = calcular_prazo(self.l_peq)
        prazo_gra = calcular_prazo(self.l_gra)
        self.assertEqual((prazo_peq - date.today()).days, 15)
        self.assertEqual((prazo_gra - date.today()).days, 30)

    def test_alugar_e_devolver(self):
        res = alugar_livro(self.u1, self.l_peq)
        self.assertTrue(res['sucesso'])

        ex = Exemplar.get_by_id(self.ex_peq)
        self.assertEqual(ex['status'], 'alugado')

        dev = devolver_livro(res['id_aluguel'])
        self.assertTrue(dev['sucesso'])
        ex = Exemplar.get_by_id(self.ex_peq)
        self.assertEqual(ex['status'], 'disponivel')

    def test_extensao(self):
        res = alugar_livro(self.u1, self.l_peq)
        ext1 = solicitar_extensao(res['id_aluguel'])
        self.assertTrue(ext1['sucesso'])
        self.assertEqual(ext1['extensao_contador'], 1)

        ext2 = solicitar_extensao(res['id_aluguel'])
        self.assertTrue(ext2['sucesso'])
        self.assertEqual(ext2['extensao_contador'], 2)

        ext3 = solicitar_extensao(res['id_aluguel'])
        self.assertFalse(ext3['sucesso'])

    def test_multa_dano(self):
        res = alugar_livro(self.u1, self.l_peq)
        devolver_livro(res['id_aluguel'])
        m_res = aplicar_multa_dano(res['id_aluguel'], 'capa_rasgada')
        self.assertTrue(m_res['sucesso'])
        self.assertEqual(m_res['valor'], 20.0)

if __name__ == '__main__':
    unittest.main()
