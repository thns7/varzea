# Várzea: Escalação do dia

**Global Solution 2026.2 · Dynamic Programming · CitizenTech**

## Integrantes

| Nome | RM |
| --- | --- |
| Gabriel Santos | 562419 |
| Gustavo Rocha | 564152 |
| Thiago Nascimento | 561967 |
| Vinicius Herreira | 565662 |

## O problema

A **Várzea** é um campeonato entre distritos de São Paulo: problema da rua resolvido e confirmado por vizinhos vale gol.

Uma equipe da subprefeitura tem um dia de trabalho e mais chamados do que consegue atender. **Quais chamados ela deve atender, e em que ordem, para fazer mais gols sem passar do horário?** Gol difícil vale mais, então a escolha evita atender só o que é rápido.

Moradores não fazem os consertos. Eles relatam, confirmam e torcem.

## Funcionalidades

- Lê as ruas e os chamados de um distrito.
- Calcula o menor deslocamento entre quaisquer dois pontos.
- Mostra o máximo de gols possível se o deslocamento não existisse.
- Verifica se essa escolha realmente cabe no dia.
- Monta a melhor rota real, com o caminho rua a rua e os horários.
- Aceita qualquer duração de jornada com `--horas`.

## Os algoritmos

Cada técnica resolve uma parte do problema, e uma sozinha não bastaria:

| Técnica | Pergunta que responde | Onde | Tempo | Memória |
| --- | --- | --- | --- | --- |
| **Grafo (Dijkstra)** | Qual o menor tempo entre dois pontos? | `grafo.py` | O((V + E) log V) por origem | O(V) |
| **Tabulação (mochila 0/1)** | Sem deslocamento, quais chamados rendem mais gols? | `tabulacao.py` | O(n · minutos) | O(n · minutos) |
| **Memoização (Held-Karp)** | Com deslocamento, qual a melhor rota? | `memoizacao.py` | O(2ⁿ · n²) | O(2ⁿ · n) |

### Grafo: Dijkstra
Esquinas são vértices, e ruas são arestas com peso em minutos. Dijkstra serve porque os tempos nunca são negativos. Ele roda uma vez para cada ponto de interesse, e depois qualquer deslocamento é consultado em O(1).

### Tabulação: mochila 0/1
`tabela[i][m]` guarda o máximo de gols usando os `i` primeiros chamados com `m` minutos. Cada chamado é feito ou não:

```
tabela[i][m] = max(tabela[i-1][m], tabela[i-1][m - minutos_i] + gols_i)
```

O resultado é um **teto**: como deslocar só consome tempo, nenhuma rota real faz mais gols que isso. A tabulação é rápida e mostra quanto o deslocamento custa.

### Memoização: rota de Held-Karp
Para terminar um grupo de chamados, só importa **quais foram feitos** e **onde a equipe parou**. A ordem anterior não muda o que vem depois, e isso permite reaproveitar respostas:

```
menor_tempo(grupo, ultimo) = duração(ultimo)
    + min(menor_tempo(grupo sem ultimo, k) + deslocamento(k, ultimo))
```

O grupo é uma máscara de bits, e `functools.lru_cache` guarda as respostas. O programa avalia todos os grupos e fica com o de mais gols dentro do tempo. Em caso de empate, escolhe a rota mais curta.

## Resultado no exemplo

Com 12 chamados fictícios e 8 horas de trabalho:

| Etapa | Resultado |
| --- | --- |
| Tabulação, sem deslocamento | 28 gols em 7h55 |
| Essa mesma escolha na melhor ordem | 9h52: passa 1h52 do dia |
| **Memoização, com deslocamento** | **24 gols em 7h59** |

Olhar só para o tempo de execução geraria um plano impossível. O deslocamento custou 4 gols, e o plano real troca o chamado C09, que é longe, por chamados mais próximos.

## Eficiência

Medido com `python benchmark.py`. Os tempos variam conforme a máquina.

| Chamados | Rotas na força bruta | Força bruta | Estados memoizados | Memoização | Tabulação |
| ---: | ---: | ---: | ---: | ---: | ---: |
| 4 | 64 | 0,000 s | 32 | 0,000 s | 0,2 ms |
| 6 | 1.956 | 0,001 s | 192 | 0,000 s | 0,4 ms |
| 8 | 109.600 | 0,079 s | 1.024 | 0,003 s | 0,9 ms |
| 9 | 986.409 | 0,864 s | 2.304 | 0,008 s | 0,9 ms |
| 10 | 9.864.100 | não executada | 5.120 | 0,015 s | 0,7 ms |
| 12 | 1.302.061.344 | não executada | 24.576 | 0,089 s | 0,7 ms |
| 14 | 236.975.164.804 | não executada | 114.688 | 0,602 s | 0,9 ms |
| 15 | 3.554.627.472.075 | não executada | 245.760 | 1,605 s | 1,5 ms |

Com 9 chamados, a memoização foi cerca de 100 vezes mais rápida que a força bruta e chegou ao mesmo resultado.

**Limite:** a memoização continua exponencial. Como o custo dobra a cada chamado, o programa recusa mais de 15. Uma equipe atende poucos chamados por dia, mas uma lista maior precisaria de um filtro prévio, e o resultado deixaria de ser garantidamente ótimo.

## Como executar

Precisa de Python 3.10 ou mais recente, sem bibliotecas externas. Na pasta do projeto:

```bash
python main.py --horas 8
```

```bash
python -m unittest -v
```

```bash
python benchmark.py
```

O comando abaixo só é necessário para recalcular a pontuação, e exige a base da entrega de Data Science:

```bash
python gerar_pontuacao.py ../data-science/sp156_ruas_4t2025.csv
```

## Como sabemos que funciona

São 15 testes em `test_algoritmos.py`:

- Dijkstra é comparado com Floyd-Warshall em grafos aleatórios.
- A tabulação é comparada com a força bruta em 200 casos aleatórios.
- A memoização é comparada com a força bruta em 60 casos aleatórios.
- Também são verificados rotas válidas, chamados inalcançáveis, listas vazias e entradas inválidas.

## De onde vêm os dados

| Dado | Origem |
| --- | --- |
| **Gols por serviço** | Reais: `1 + arredondar(10 × (1 − chance))`, com a chance de finalizar em 30 dias no [SP156](https://dados.prefeitura.sp.gov.br/dataset/dados-do-sp156), 4º trimestre de 2025 |
| **Tempo de execução por serviço** | **Hipótese da equipe:** o SP156 não traz esse dado |
| **Ruas e chamados de Vila Exemplo** | **Fictícios:** nenhum endereço ou dado pessoal real |

## Limitações

- Os tempos de execução e deslocamento são estimativas, e o trânsito não muda ao longo do dia.
- O modelo considera uma equipe por vez, capaz de atender qualquer serviço. Na prática, os serviços exigem equipes e materiais diferentes.
- Não há urgência: um problema perigoso de 1 gol pode perder para outro de mais gols. No uso real, seria preciso um critério de segurança.
- A fórmula dos gols premia serviços lentos. Se a lentidão vier de outra causa que não a dificuldade, a pontuação fica distorcida.

## Arquivos

| Arquivo | Conteúdo |
| --- | --- |
| `main.py` | Programa principal |
| `grafo.py`, `tabulacao.py`, `memoizacao.py` | Os três algoritmos |
| `modelos.py`, `dados.py` | Estruturas e leitura dos dados |
| `test_algoritmos.py`, `benchmark.py` | Testes e medição |
| `gerar_pontuacao.py` | Gera `dados/pontuacao_servicos.csv` |
| `dados/` | Pontuação por serviço e distrito fictício |
