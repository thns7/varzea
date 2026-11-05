"""Gera a base da Várzea a partir do CSV oficial do SP156.

Uso: python preparar_dados.py arquivo-final-4-trim-25.csv sp156_ruas_4t2025.csv

Fonte: Portal de Dados Abertos da Prefeitura de São Paulo, conjunto "Dados do SP156",
recurso "Dados do SP156 - 4º TRI 2025".
https://dados.prefeitura.sp.gov.br/dataset/dados-do-sp156

O recorte é feito só pelo tipo de serviço, nunca pelo resultado do chamado.
Logradouro, número, CEP, latitude e longitude são descartados aqui, antes de qualquer análise.
"""
import sys

import pandas as pd

# Serviços em que o morador consegue ver o resultado na rua, separados por categoria do jogo.
# Ficam de fora fiscalização (PSIU, ambulantes, terrenos particulares) e pedidos de avaliação
# (árvore, obstáculo na calçada), porque neles o fechamento não significa conserto visível.
CATEGORIAS = {
    "Tapa-buraco": "Pavimento",
    "Tapa-buraco em faixa exclusiva ou corredor de ônibus": "Pavimento",
    "Recolocação e ajuste de paralelepípedo": "Pavimento",
    "Guias, sarjetas e sarjetões - solicitar manutenção": "Pavimento",
    "Calçada pública - solicitar manutenção": "Calçada",
    "Vielas e escadarias - solicitar manutenção": "Calçada",
    "Solicitar limpeza de bueiros, boca de lobo e poços de visita": "Drenagem",
    "Reformar galerias de águas de chuvas (pluviais)": "Drenagem",
    "Drenagem de água da chuva - Bueiros e bocas de lobo": "Drenagem",
    "Microdrenagem": "Drenagem",
    "Córrego - solicitar limpeza": "Drenagem",
    "Entulho e grandes objetos em via pública - solicitar remoção": "Entulho e lixo",
    "Denunciar descarte irregular de entulho e outros resíduos em área pública": "Entulho e lixo",
    "Cata Bagulho - Fazer reclamação de não retirada de material": "Entulho e lixo",
    "Lixeiras públicas - solicitar instalação, manutenção ou limpeza": "Entulho e lixo",
    "Fazer reclamação de falta de varrição": "Limpeza e mato",
    "Sacos de varrição - Fazer reclamação de falta de remoção": "Limpeza e mato",
    "Guias e sarjetas - Solicitar capinação": "Limpeza e mato",
    "Praça e canteiro central - solicitar roçada": "Limpeza e mato",
    "Praças - Solicitar manutenção de equipamentos (mesas, bancos, brinquedos e equipamentos de ginástica)": "Praças",
}

COLUNAS = {
    "Data de Abertura": "data_abertura",
    "Data de Finalização": "data_finalizacao",
    "Meio de Comunicação": "canal",
    "Serviço": "servico",
    "Status": "status",
    "Distrito": "distrito",
}


def main(origem, destino):
    bruto = pd.read_csv(origem, sep=";", encoding="latin-1", dtype=str, on_bad_lines="skip", low_memory=False)
    base = bruto[bruto["Serviço"].isin(CATEGORIAS)]
    # Alguns registros trazem um número no lugar do nome do distrito.
    base = base[base["Distrito"].notna() & ~base["Distrito"].fillna("").str.fullmatch(r"\d+")]
    base = base[list(COLUNAS)].rename(columns=COLUNAS)
    base.insert(3, "categoria", base["servico"].map(CATEGORIAS))
    base.to_csv(destino, index=False, encoding="utf-8")
    print(f"{len(bruto)} chamados no original, {len(base)} na base final, {base['distrito'].nunique()} distritos")
    print(base["categoria"].value_counts().to_dict())
    faltando = set(CATEGORIAS) - set(base["servico"])
    print("Serviços sem registros:", faltando or "nenhum")


if __name__ == "__main__":
    main(sys.argv[1], sys.argv[2])
