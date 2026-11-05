"""Escalação da Várzea: decide o que a equipe da subprefeitura faz no dia.

Uso: python main.py --horas 8
"""
import argparse
import sys

from dados import carregar_distrito
from grafo import construir_grafo, dijkstra, matriz_deslocamento, remontar_caminho
from memoizacao import melhor_rota
from tabulacao import escalar


def horario(minutos):
    return f"{int(minutos // 60)}h{int(minutos % 60):02d}"


def imprimir_chamados(chamados):
    print(f"\n{'ID':4} {'Gols':>4} {'Exec.':>6} {'Esquina':8} Serviço")
    for c in sorted(chamados, key=lambda c: -c.gols):
        print(f"{c.id:4} {c.gols:4d} {horario(c.minutos):>6} {c.no:8} {c.servico[:55]}")


def imprimir_rota(rota, base, grafo):
    posicao = base
    relogio = 0
    print(f"\n  Saída da base em {base}")
    for chamado in rota.chamados:
        distancias, anterior = dijkstra(grafo, posicao)
        caminho = remontar_caminho(anterior, posicao, chamado.no)
        relogio += distancias[chamado.no] + chamado.minutos
        print(f"  {' > '.join(caminho):22} {chamado.id} ({chamado.gols} gols), termina em {horario(relogio)}")
        posicao = chamado.no
    distancias, anterior = dijkstra(grafo, posicao)
    print(f"  {' > '.join(remontar_caminho(anterior, posicao, base)):22} volta à base em {horario(relogio + distancias[base])}")


def main():
    sys.stdout.reconfigure(encoding="utf-8")
    parser = argparse.ArgumentParser(description="Escalação do dia na Várzea")
    parser.add_argument("--horas", type=float, default=8, help="horas de trabalho da equipe (padrão: 8)")
    args = parser.parse_args()
    if args.horas <= 0:
        parser.error("--horas precisa ser maior que zero")
    minutos = int(args.horas * 60)

    distrito, base, ruas, chamados = carregar_distrito()
    grafo = construir_grafo(ruas)
    deslocamento = matriz_deslocamento(grafo, [base] + [c.no for c in chamados])

    print(f"VÁRZEA | {distrito} (dados fictícios) | {len(chamados)} chamados | {horario(minutos)} de trabalho")
    imprimir_chamados(chamados)

    print("\n1. TABULAÇÃO: melhor escolha ignorando o deslocamento")
    escalacao = escalar(chamados, minutos)
    print(f"  {', '.join(c.id for c in escalacao.chamados) or 'nenhum'}")
    print(f"  {escalacao.gols} gols em {horario(escalacao.minutos)} de execução ({escalacao.celulas} células na tabela)")

    print("\n2. GRAFO + MEMOIZAÇÃO: essa escolha cabe no dia?")
    so_escolhidos = melhor_rota(list(escalacao.chamados), base, deslocamento, 10 ** 9)
    if so_escolhidos.minutos <= minutos:
        print(f"  Sim: a melhor rota leva {horario(so_escolhidos.minutos)}.")
    else:
        excesso = so_escolhidos.minutos - minutos
        print(f"  Não: mesmo na melhor ordem, leva {horario(so_escolhidos.minutos)}, {horario(excesso)} além do dia.")

    print("\n3. MEMOIZAÇÃO: melhor plano real, com deslocamento")
    rota = melhor_rota(chamados, base, deslocamento, minutos)
    imprimir_rota(rota, base, grafo)
    print(f"\n  RESULTADO: {rota.gols} gols em {horario(rota.minutos)} ({rota.estados} estados memoizados)")
    print(f"  Teto da tabulação: {escalacao.gols} gols. Diferença causada pelo deslocamento: {escalacao.gols - rota.gols}.")


if __name__ == "__main__":
    main()
