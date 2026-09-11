import unittest
from datetime import date
from src.database.setup import setup_database
from src.models.usuario import Usuario
from src.models.livro import Livro
from src.models.exemplar import Exemplar
from src.models.aluguel import Aluguel
from src.models.fila_espera import FilaEspera
from src.models.multa import Multa

class TestModels(unittest.TestCase):
    def setUp(self):
        setup_database()

    def test_usuario_crud(self):
        uid = Usuario.create("Maria Silva", "maria@test.com", "12345678901", "3A", "senha123", "aluno")
        self.assertIsNotNone(uid)

        user = Usuario.get_by_id(uid)
        self.assertEqual(user['nome'], "Maria Silva")
        self.assertTrue(Usuario.verify_password("senha123", user['senha_hash']))

        Usuario.update(uid, nome="Maria Souza")
        updated = Usuario.get_by_id(uid)
        self.assertEqual(updated['nome'], "Maria Souza")

        Usuario.delete(uid)
        self.assertIsNone(Usuario.get_by_id(uid))

    def test_livro_tamanho_automatico(self):
        l1_id = Livro.create("Livro Pequeno", "Autor A", "Ficção", 100, 29.90)
        l1 = Livro.get_by_id(l1_id)
        self.assertEqual(l1['tamanho'], 'pequeno')

        l2_id = Livro.create("Livro Grande", "Autor B", "Ficção", 200, 49.90)
        l2 = Livro.get_by_id(l2_id)
        self.assertEqual(l2['tamanho'], 'grande')

        Livro.update(l2_id, num_paginas=120)
        l2_updated = Livro.get_by_id(l2_id)
        self.assertEqual(l2_updated['tamanho'], 'pequeno')

    def test_exemplar_crud(self):
        lid = Livro.create("Livro Teste", "Autor C", "Gênero C", 100, 20.00)
        ex1 = Exemplar.create(lid, "EX001")
        ex2 = Exemplar.create(lid, "EX002")
        ex3 = Exemplar.create(lid, "EX003")

        exemplares = Exemplar.list_by_livro(lid)
        self.assertEqual(len(exemplares), 3)

    def test_fila_espera_posicao(self):
        lid = Livro.create("Livro Fila", "Autor D", "Gênero D", 100, 20.00)
        u1 = Usuario.create("U1", "u1@test.com", "111", "1A", "pass")
        u2 = Usuario.create("U2", "u2@test.com", "222", "1A", "pass")
        u3 = Usuario.create("U3", "u3@test.com", "333", "1A", "pass")

        f1 = FilaEspera.create(lid, u1)
        f2 = FilaEspera.create(lid, u2)
        f3 = FilaEspera.create(lid, u3)

        fila = FilaEspera.list_by_livro(lid)
        self.assertEqual(len(fila), 3)
        self.assertEqual(fila[0]['posicao'], 1)
        self.assertEqual(fila[1]['posicao'], 2)
        self.assertEqual(fila[2]['posicao'], 3)

if __name__ == '__main__':
    unittest.main()
