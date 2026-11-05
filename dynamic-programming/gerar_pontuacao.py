"""Gera dados/pontuacao_servicos.csv a partir da base usada em Data Science.

Uso: python gerar_pontuacao.py ../data-science/sp156_ruas_4t2025.csv

- chance_30d: parcela real de chamados do serviço finalizados, sem cancelamento, em até 30 dias.
- gols: quanto mais difícil o serviço, mais gols vale. Fórmula: 1 + arredondar(10 * (1 - chance)).
- minutos_estimados: HIPÓTESE da equipe para o tempo de execução, pois o SP156 não traz esse dado.
"""
import csv
import sys
from datetime import datetime

MINUTOS_ESTIMADOS = {
    "Tapa-buraco": 45,
    "Tapa-buraco em faixa exclusiva ou corredor de ônibus": 60,
    "Recolocação e ajuste de paralelepípedo": 120,
    "Guias, sarjetas e sarjetões - solicitar manutenção": 120,
    "Calçada pública - solicitar manutenção": 150,
    "Vielas e escadarias - solicitar manutenção": 120,
    "Solicitar limpeza de bueiros, boca de lobo e poços de visita": 40,
    "Reformar galerias de águas de chuvas (pluviais)": 240,
    "Drenagem de água da chuva - Bueiros e bocas de lobo": 90,
    "Microdrenagem": 180,
    "Córrego - solicitar limpeza": 180,
    "Entulho e grandes objetos em via pública - solicitar remoção": 30,
    "Denunciar descarte irregular de entulho e outros resíduos em área pública": 45,
    "Cata Bagulho - Fazer reclamação de não retirada de material": 30,
    "Lixeiras públicas - solicitar instalação, manutenção ou limpeza": 20,
    "Fazer reclamação de falta de varrição": 45,
    "Sacos de varrição - Fazer reclamação de falta de remoção": 15,
    "Guias e sarjetas - Solicitar capinação": 60,
    "Praça e canteiro central - solicitar roçada": 90,
    "Praças - Solicitar manutenção de equipamentos (mesas, bancos, brinquedos e equipamentos de ginástica)": 120,
}


def dias_ate_finalizar(linha):
    if not linha["data_finalizacao"]:
        return None
    abertura = datetime.strptime(linha["data_abertura"], "%d/%m/%Y")
    finalizacao = datetime.strptime(linha["data_finalizacao"], "%d/%m/%Y")
    return (finalizacao - abertura).days


def main(origem, destino="dados/pontuacao_servicos.csv"):
    total, resolvidos = {}, {}
    with open(origem, encoding="utf-8", newline="") as arquivo:
        for linha in csv.DictReader(arquivo):
            servico = linha["servico"]
            dias = dias_ate_finalizar(linha)
            no_prazo = dias is not None and dias <= 30 and linha["status"] != "Cancelada"
            total[servico] = total.get(servico, 0) + 1
            resolvidos[servico] = resolvidos.get(servico, 0) + int(no_prazo)

    with open(destino, "w", encoding="utf-8", newline="") as arquivo:
        saida = csv.writer(arquivo)
        saida.writerow(["servico", "chamados", "chance_30d", "gols", "minutos_estimados"])
        for servico in sorted(total, key=lambda s: resolvidos[s] / total[s]):
            chance = resolvidos[servico] / total[servico]
            gols = 1 + round(10 * (1 - chance))
            saida.writerow([servico, total[servico], f"{chance:.3f}", gols, MINUTOS_ESTIMADOS[servico]])
            print(f"{gols:3d} gols | {chance:6.1%} | {servico}")


if __name__ == "__main__":
    main(*sys.argv[1:])
