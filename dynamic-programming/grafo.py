"""GRAFO: as ruas do distrito e o menor caminho entre dois pontos (Dijkstra)."""
import heapq
from math import inf


def construir_grafo(ruas):
    """Cria uma lista de adjacência. Cada rua é [esquina_a, esquina_b, minutos] e vale nos dois sentidos."""
    grafo = {}
    for a, b, minutos in ruas:
        if minutos < 0:
            raise ValueError(f"Rua {a}-{b} tem tempo negativo: Dijkstra não funciona assim.")
        grafo.setdefault(a, []).append((b, minutos))
        grafo.setdefault(b, []).append((a, minutos))
    return grafo


def dijkstra(grafo, origem):
    """Menor tempo da origem até cada esquina. Custo: O((V + E) log V).

    Retorna (distancias, anterior). `anterior` permite remontar o caminho.
    """
    if origem not in grafo:
        raise ValueError(f"Esquina desconhecida: {origem}")
    distancias = {no: inf for no in grafo}
    anterior = {}
    distancias[origem] = 0
    fila = [(0, origem)]
    while fila:
        tempo, no = heapq.heappop(fila)
        if tempo > distancias[no]:
            continue  # registro antigo: já achamos um caminho melhor até aqui
        for vizinho, minutos in grafo[no]:
            novo_tempo = tempo + minutos
            if novo_tempo < distancias[vizinho]:
                distancias[vizinho] = novo_tempo
                anterior[vizinho] = no
                heapq.heappush(fila, (novo_tempo, vizinho))
    return distancias, anterior


def remontar_caminho(anterior, origem, destino):
    """Lista as esquinas de origem até destino, ou [] se não há caminho."""
    if origem == destino:
        return [origem]
    if destino not in anterior:
        return []
    caminho = [destino]
    while caminho[-1] != origem:
        caminho.append(anterior[caminho[-1]])
    return caminho[::-1]


def matriz_deslocamento(grafo, pontos):
    """Roda Dijkstra uma vez por ponto, para consultar qualquer deslocamento em O(1) depois."""
    return {ponto: dijkstra(grafo, ponto)[0] for ponto in set(pontos)}
