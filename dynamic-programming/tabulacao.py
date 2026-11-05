"""TABULAÇÃO: quais chamados rendem mais gols no tempo disponível (mochila 0/1).

Considera só o tempo de execução. Por isso, o resultado é um teto: nenhuma rota real faz
mais gols do que isso, porque o deslocamento só consome tempo.
"""
from modelos import Escalacao


def escalar(chamados, minutos_disponiveis):
    """Preenche a tabela de baixo para cima. Custo: O(n * minutos) em tempo e memória.

    tabela[i][m] = máximo de gols usando os i primeiros chamados com m minutos.
    """
    if minutos_disponiveis < 0:
        raise ValueError("O tempo disponível não pode ser negativo.")
    n = len(chamados)
    tabela = [[0] * (minutos_disponiveis + 1) for _ in range(n + 1)]

    for i in range(1, n + 1):
        chamado = chamados[i - 1]
        for m in range(minutos_disponiveis + 1):
            sem_ele = tabela[i - 1][m]
            com_ele = -1
            if chamado.minutos <= m:
                com_ele = tabela[i - 1][m - chamado.minutos] + chamado.gols
            tabela[i][m] = max(sem_ele, com_ele)

    # Volta pela tabela para descobrir quais chamados foram escolhidos.
    escolhidos = []
    m = minutos_disponiveis
    for i in range(n, 0, -1):
        if tabela[i][m] != tabela[i - 1][m]:
            escolhidos.append(chamados[i - 1])
            m -= chamados[i - 1].minutos
    escolhidos.reverse()

    return Escalacao(
        chamados=tuple(escolhidos),
        gols=tabela[n][minutos_disponiveis],
        minutos=sum(c.minutos for c in escolhidos),
        celulas=(n + 1) * (minutos_disponiveis + 1),
    )
