"""Mede como cada abordagem cresce. Uso: python benchmark.py"""
import random
import sys
import time
from itertools import permutations
from math import comb, factorial

from grafo import construir_grafo, matriz_deslocamento
from memoizacao import melhor_rota
from modelos import Chamado
from tabulacao import escalar


def instancia(n, semente=42):
    gerador = random.Random(semente)
    nos = [f"N{i}" for i in range(20)]
    ruas = [[nos[i], nos[i + 1], gerador.randint(3, 25)] for i in range(19)]
    ruas += [[*gerador.sample(nos, 2), gerador.randint(3, 25)] for _ in range(20)]
    chamados = [Chamado(f"C{i}", "t", gerador.choice(nos), gerador.randint(15, 240), gerador.randint(1, 8)) for i in range(n)]
    return chamados, matriz_deslocamento(construir_grafo(ruas), ["N0"] + [c.no for c in chamados])


def rotas_possiveis(n):
    """Quantas rotas a força bruta teria de testar: toda ordem de todo grupo de chamados."""
    return sum(comb(n, k) * factorial(k) for k in range(1, n + 1))


def forca_bruta(chamados, desl, limite):
    melhor = 0
    for k in range(1, len(chamados) + 1):
        for ordem in permutations(chamados, k):
            total, posicao = 0, "N0"
            for c in ordem:
                total += desl[posicao][c.no] + c.minutos
                posicao = c.no
            if total + desl[posicao]["N0"] <= limite:
                melhor = max(melhor, sum(c.gols for c in ordem))
    return melhor


def main():
    sys.stdout.reconfigure(encoding="utf-8")
    print("| Chamados | Rotas na força bruta | Força bruta | Estados memoizados | Memoização | Tabulação |")
    print("|---:|---:|---:|---:|---:|---:|")
    for n in (4, 6, 8, 9, 10, 12, 14, 15):
        chamados, desl = instancia(n)
        inicio = time.perf_counter()
        rota = melhor_rota(chamados, "N0", desl, 480)
        t_memo = time.perf_counter() - inicio
        inicio = time.perf_counter()
        escalar(chamados, 480)
        t_tab = time.perf_counter() - inicio
        if n <= 9:
            inicio = time.perf_counter()
            assert forca_bruta(chamados, desl, 480) == rota.gols
            bruta = f"{time.perf_counter() - inicio:.3f} s"
        else:
            bruta = "não executada"
        print(f"| {n} | {rotas_possiveis(n):,} | {bruta} | {rota.estados:,} | {t_memo:.3f} s | {t_tab * 1000:.1f} ms |".replace(",", "."))


if __name__ == "__main__":
    main()
