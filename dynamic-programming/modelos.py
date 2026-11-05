"""Estruturas de dados compartilhadas pelos algoritmos."""
from dataclasses import dataclass


@dataclass(frozen=True)
class Chamado:
    """Um problema da rua que pode entrar na escalação do dia."""

    id: str
    servico: str
    no: str  # esquina do grafo onde o problema está
    minutos: int  # tempo estimado de execução
    gols: int  # valor no jogo: gol difícil vale mais


@dataclass(frozen=True)
class Escalacao:
    """Resultado da tabulação: considera só o tempo de execução."""

    chamados: tuple
    gols: int
    minutos: int
    celulas: int  # tamanho da tabela, para medir o custo


@dataclass(frozen=True)
class Rota:
    """Resultado da memoização: considera execução e deslocamento."""

    chamados: tuple  # na ordem de atendimento
    gols: int
    minutos: float  # execução + deslocamento, com a volta à base
    estados: int  # subproblemas distintos resolvidos
