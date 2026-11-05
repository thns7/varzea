"""MEMOIZAÇÃO: a melhor rota real do dia, somando execução e deslocamento.

Testar todas as ordens de todos os grupos de chamados cresce com o fatorial. Mas o tempo mínimo
para terminar um grupo só depende de QUAIS chamados foram feitos e de ONDE a equipe parou, e não da
ordem anterior. Essa é a recursão de Held-Karp, e guardar cada resposta evita refazer contas.
"""
from functools import lru_cache
from math import inf

from modelos import Rota

LIMITE_CHAMADOS = 15  # o custo dobra a cada chamado a mais


def melhor_rota(chamados, base, deslocamento, minutos_disponiveis):
    """Maximiza os gols de uma rota que sai da base e volta a ela dentro do tempo.

    Custo: O(2^n * n^2) em tempo e O(2^n * n) em memória. No empate de gols, escolhe a rota mais curta.
    """
    n = len(chamados)
    if n > LIMITE_CHAMADOS:
        raise ValueError(f"Máximo de {LIMITE_CHAMADOS} chamados: com {n}, o custo ficaria alto demais.")

    def tempo(origem, destino):
        return deslocamento[origem].get(destino, inf)

    @lru_cache(maxsize=None)
    def menor_tempo(grupo, ultimo):
        """Menor tempo para sair da base e concluir o `grupo`, terminando em `ultimo`.

        `grupo` é uma máscara de bits: o bit i ligado significa que o chamado i foi feito.
        """
        atual = chamados[ultimo]
        restante = grupo ^ (1 << ultimo)
        if restante == 0:
            return tempo(base, atual.no) + atual.minutos
        melhor = inf
        for anterior in range(n):
            if restante & (1 << anterior):
                candidato = menor_tempo(restante, anterior) + tempo(chamados[anterior].no, atual.no)
                melhor = min(melhor, candidato)
        return melhor + atual.minutos

    melhor_gols, melhor_total, melhor_grupo, melhor_ultimo = 0, 0, 0, None
    for grupo in range(1, 1 << n):
        gols = sum(chamados[i].gols for i in range(n) if grupo & (1 << i))
        for ultimo in range(n):
            if not grupo & (1 << ultimo):
                continue
            total = menor_tempo(grupo, ultimo) + tempo(chamados[ultimo].no, base)
            if total > minutos_disponiveis:
                continue
            if gols > melhor_gols or (gols == melhor_gols and total < melhor_total):
                melhor_gols, melhor_total, melhor_grupo, melhor_ultimo = gols, total, grupo, ultimo

    # Remonta a ordem: procura qual chamado anterior explica o tempo mínimo guardado.
    ordem = []
    grupo, ultimo = melhor_grupo, melhor_ultimo
    while grupo:
        ordem.append(chamados[ultimo])
        restante = grupo ^ (1 << ultimo)
        if restante:
            esperado = menor_tempo(grupo, ultimo) - chamados[ultimo].minutos
            ultimo = next(
                k for k in range(n)
                if restante & (1 << k)
                and menor_tempo(restante, k) + tempo(chamados[k].no, chamados[ultimo].no) == esperado
            )
        grupo = restante
    ordem.reverse()

    return Rota(
        chamados=tuple(ordem),
        gols=melhor_gols,
        minutos=melhor_total,
        estados=menor_tempo.cache_info().currsize,
    )
