"""Leitura dos arquivos de entrada."""
import csv
import json
from pathlib import Path

from modelos import Chamado

PASTA = Path(__file__).parent / "dados"


def carregar_pontuacao(caminho=PASTA / "pontuacao_servicos.csv"):
    with open(caminho, encoding="utf-8", newline="") as arquivo:
        return {linha["servico"]: linha for linha in csv.DictReader(arquivo)}


def carregar_distrito(caminho=PASTA / "distrito_exemplo.json", pontuacao=None):
    """Retorna (nome, base, ruas, chamados), combinando cada chamado com a pontuação do serviço."""
    pontuacao = pontuacao or carregar_pontuacao()
    with open(caminho, encoding="utf-8") as arquivo:
        distrito = json.load(arquivo)

    chamados = []
    for item in distrito["chamados"]:
        if item["servico"] not in pontuacao:
            raise ValueError(f"{item['id']}: serviço sem pontuação: {item['servico']}")
        dados_servico = pontuacao[item["servico"]]
        chamados.append(Chamado(
            id=item["id"],
            servico=item["servico"],
            no=item["no"],
            minutos=int(dados_servico["minutos_estimados"]),
            gols=int(dados_servico["gols"]),
        ))
    return distrito["distrito"], distrito["base"], distrito["ruas"], chamados
