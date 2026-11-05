"""Compara cada algoritmo com uma solução lenta e óbvia, em instâncias aleatórias.

Uso: python -m unittest -v
"""
import random
import unittest
from itertools import combinations, permutations
from math import inf

from dados import carregar_distrito
from grafo import construir_grafo, dijkstra, matriz_deslocamento, remontar_caminho
from memoizacao import LIMITE_CHAMADOS, melhor_rota
from modelos import Chamado
from tabulacao import escalar


def chamados_aleatorios(gerador, n, nos):
    return [
        Chamado(f"C{i}", "teste", gerador.choice(nos), gerador.randint(5, 120), gerador.randint(1, 10))
        for i in range(n)
    ]


def grafo_aleatorio(gerador, quantidade_nos):
    nos = [f"N{i}" for i in range(quantidade_nos)]
    ruas = [[nos[i], nos[i + 1], gerador.randint(1, 30)] for i in range(quantidade_nos - 1)]
    for _ in range(quantidade_nos):
        a, b = gerador.sample(nos, 2)
        ruas.append([a, b, gerador.randint(1, 30)])
    return nos, construir_grafo(ruas)


def mochila_forca_bruta(chamados, minutos):
    melhor = 0
    for k in range(len(chamados) + 1):
        for grupo in combinations(chamados, k):
            if sum(c.minutos for c in grupo) <= minutos:
                melhor = max(melhor, sum(c.gols for c in grupo))
    return melhor


def rota_forca_bruta(chamados, base, desl, minutos):
    melhor = 0
    for k in range(1, len(chamados) + 1):
        for ordem in permutations(chamados, k):
            total, posicao = 0, base
            for c in ordem:
                total += desl[posicao][c.no] + c.minutos
                posicao = c.no
            total += desl[posicao][base]
            if total <= minutos:
                melhor = max(melhor, sum(c.gols for c in ordem))
    return melhor


class TestGrafo(unittest.TestCase):
    def test_dijkstra_igual_a_floyd_warshall(self):
        gerador = random.Random(1)
        for _ in range(30):
            nos, grafo = grafo_aleatorio(gerador, 9)
            floyd = {a: {b: (0 if a == b else inf) for b in nos} for a in nos}
            for a in nos:
                for b, minutos in grafo[a]:
                    floyd[a][b] = min(floyd[a][b], minutos)
            for k in nos:
                for a in nos:
                    for b in nos:
                        floyd[a][b] = min(floyd[a][b], floyd[a][k] + floyd[k][b])
            for a in nos:
                self.assertEqual(dijkstra(grafo, a)[0], floyd[a])

    def test_caminho_tem_o_custo_informado(self):
        grafo = construir_grafo([["A", "B", 5], ["B", "C", 5], ["A", "C", 20]])
        distancias, anterior = dijkstra(grafo, "A")
        self.assertEqual(distancias["C"], 10)
        self.assertEqual(remontar_caminho(anterior, "A", "C"), ["A", "B", "C"])

    def test_esquina_isolada_fica_inalcancavel(self):
        grafo = construir_grafo([["A", "B", 1], ["X", "Y", 1]])
        distancias, anterior = dijkstra(grafo, "A")
        self.assertEqual(distancias["X"], inf)
        self.assertEqual(remontar_caminho(anterior, "A", "X"), [])

    def test_recusa_tempo_negativo_e_esquina_desconhecida(self):
        with self.assertRaises(ValueError):
            construir_grafo([["A", "B", -1]])
        with self.assertRaises(ValueError):
            dijkstra(construir_grafo([["A", "B", 1]]), "Z")


class TestTabulacao(unittest.TestCase):
    def test_igual_a_forca_bruta(self):
        gerador = random.Random(2)
        for _ in range(200):
            chamados = chamados_aleatorios(gerador, gerador.randint(0, 10), ["X"])
            minutos = gerador.randint(0, 400)
            resultado = escalar(chamados, minutos)
            self.assertEqual(resultado.gols, mochila_forca_bruta(chamados, minutos))
            self.assertEqual(resultado.gols, sum(c.gols for c in resultado.chamados))
            self.assertLessEqual(resultado.minutos, minutos)

    def test_sem_tempo_nao_escolhe_nada(self):
        chamado = Chamado("C", "s", "X", 10, 5)
        self.assertEqual(escalar([chamado], 0).chamados, ())

    def test_nao_aceita_tempo_negativo(self):
        with self.assertRaises(ValueError):
            escalar([], -1)


class TestMemoizacao(unittest.TestCase):
    def test_igual_a_forca_bruta(self):
        gerador = random.Random(3)
        for _ in range(60):
            nos, grafo = grafo_aleatorio(gerador, 7)
            chamados = chamados_aleatorios(gerador, gerador.randint(1, 6), nos)
            base = gerador.choice(nos)
            desl = matriz_deslocamento(grafo, [base] + [c.no for c in chamados])
            minutos = gerador.randint(0, 500)
            rota = melhor_rota(chamados, base, desl, minutos)
            self.assertEqual(rota.gols, rota_forca_bruta(chamados, base, desl, minutos))

    def test_rota_informada_e_valida(self):
        gerador = random.Random(4)
        for _ in range(60):
            nos, grafo = grafo_aleatorio(gerador, 7)
            chamados = chamados_aleatorios(gerador, 6, nos)
            base = gerador.choice(nos)
            desl = matriz_deslocamento(grafo, [base] + [c.no for c in chamados])
            limite = gerador.randint(50, 500)
            rota = melhor_rota(chamados, base, desl, limite)
            total, posicao = 0, base
            for c in rota.chamados:
                total += desl[posicao][c.no] + c.minutos
                posicao = c.no
            total += desl[posicao][base]
            self.assertEqual(total, rota.minutos)
            self.assertLessEqual(total, limite)
            self.assertEqual(sum(c.gols for c in rota.chamados), rota.gols)
            self.assertEqual(len({c.id for c in rota.chamados}), len(rota.chamados))

    def test_nunca_supera_o_teto_da_tabulacao(self):
        gerador = random.Random(5)
        for _ in range(60):
            nos, grafo = grafo_aleatorio(gerador, 7)
            chamados = chamados_aleatorios(gerador, 7, nos)
            base = gerador.choice(nos)
            desl = matriz_deslocamento(grafo, [base] + [c.no for c in chamados])
            minutos = gerador.randint(0, 500)
            self.assertLessEqual(melhor_rota(chamados, base, desl, minutos).gols, escalar(chamados, minutos).gols)

    def test_nada_cabe_retorna_rota_vazia(self):
        desl = matriz_deslocamento(construir_grafo([["A", "B", 100]]), ["A", "B"])
        rota = melhor_rota([Chamado("C", "s", "B", 10, 5)], "A", desl, 50)
        self.assertEqual((rota.chamados, rota.gols, rota.minutos), ((), 0, 0))

    def test_ignora_chamado_inalcancavel(self):
        grafo = construir_grafo([["A", "B", 1], ["X", "Y", 1]])
        desl = matriz_deslocamento(grafo, ["A", "B", "X"])
        chamados = [Chamado("perto", "s", "B", 10, 1), Chamado("longe", "s", "X", 10, 100)]
        self.assertEqual(melhor_rota(chamados, "A", desl, 500).gols, 1)

    def test_lista_vazia(self):
        self.assertEqual(melhor_rota([], "A", {"A": {"A": 0}}, 100).gols, 0)

    def test_recusa_chamados_demais(self):
        chamados = [Chamado(str(i), "s", "A", 1, 1) for i in range(LIMITE_CHAMADOS + 1)]
        with self.assertRaises(ValueError):
            melhor_rota(chamados, "A", {"A": {"A": 0}}, 100)


class TestDadosDeExemplo(unittest.TestCase):
    def test_exemplo_carrega_e_todas_as_esquinas_existem(self):
        _, base, ruas, chamados = carregar_distrito()
        grafo = construir_grafo(ruas)
        self.assertIn(base, grafo)
        for chamado in chamados:
            self.assertIn(chamado.no, grafo)
            self.assertGreaterEqual(chamado.gols, 1)


if __name__ == "__main__":
    unittest.main()
