# Várzea: Ficha da CitizenTech e plano até 19/11

> Rascunho. O que está entre [colchetes] a equipe precisa preencher, e o que está marcado como hipótese precisa ser confirmado nas conversas com moradores.

## Ficha da CitizenTech (vai no início do README)

| Campo | Conteúdo |
| --- | --- |
| **Nome** | Várzea: o campeonato dos bairros |
| **Cidade e bairro** | São Paulo, piloto com 4 distritos vizinhos, porque o SP156 traz distrito e não bairro: [distrito 1], [distrito 2], [distrito 3], [distrito 4] |
| **Cidadão beneficiado** | Moradores que convivem com buracos, entulho, iluminação ruim e calçadas quebradas, principalmente quem depende da rua a pé, como idosos e pessoas com mobilidade reduzida |
| **Problema, em uma frase** | Em [bairro], [nome e perfil do morador entrevistado] pede conserto de problemas da rua e não sabe quando nem se foi resolvido de verdade, porque cobrar a cidade é lento e solitário (hipótese a confirmar na entrevista) |
| **Como funciona** | Cada bairro é um time. O morador registra um problema, a Prefeitura resolve e os vizinhos confirmam com foto (o VAR). Só problema confirmado vira gol. Cada bairro compete contra o próprio histórico, com rodada, tabela e clássicos |
| **Link do repositório** | [link] |
| **Indicador de impacto** | Principal: % de problemas registrados e confirmados como resolvidos em até [N] dias, comparado ao histórico do bairro. Secundário: vizinhos ativos por rodada |
| **ODS conectados** | ODS 11: mantém ruas e calçadas seguras e acessíveis. ODS 10: a regra do histórico próprio compara cada distrito com ele mesmo, e não com os mais ricos ou mais bem atendidos. ODS 9: usa dados abertos para melhorar a infraestrutura urbana |

### Ecossistema
- **Moradores:** relatam, confirmam e torcem.
- **Prefeitura e subprefeituras:** resolvem os chamados.
- **Comércio local:** patrocina o time do bairro.
- **CEUs e associações de bairro:** recebem a sede da torcida.

### O que cada parte faz

| Disciplina | Parte da Várzea | Entrega |
| --- | --- | --- |
| Data Science | Prevê o "resultado do jogo" com dados do SP156. Linear: dias até resolver. Logística: chance de resolver em até N dias | ZIP com `.ipynb`, CSV ou link e TXT com nomes e RMs |
| Dynamic Programming | Escalação do dia da equipe da subprefeitura. Grafo (Dijkstra): deslocamentos. Tabulação: máximo de gols sem deslocamento. Memoização: melhor rota real | ZIP com código e README |
| Java/DDD | API REST de times, rodadas, jogadas, VAR e classificação, com Oracle da FIAP e deploy no Render | ZIP com PDF, link do GitHub e link do deploy |
| Database | Modelo lógico no Oracle Data Modeler e scripts DDL, DML e DQL | PDF e arquivos `.sql` |
| Redes | Sede da torcida em um CEU: VLANs Equipe, Cidadão e Servidores/Sensores, Wi-Fi WPA2, NAT e ACL | `.pkt` e PDF |
| AR/VR | O bairro em 3D vira estádio, e cada conserto acende o placar | Vídeo de 30 a 60 s e PDF com o link |
| Agile | Ideia, personas, casos de uso, backlog, 3 telas e pitch narrado como jogo | PDF único com link não listado do YouTube |

## MVP: o que entra e o que fica de fora

**Entra (Must):**
1. Registrar um problema, com foto e bairro (dados fictícios).
2. Confirmar a resolução pelo VAR.
3. Ver a tabela da rodada.
4. Ver a previsão de resolução de um chamado.

**Fica de fora nesta entrega:** patrocínio, figurinhas, integração real com o SP156, rebaixamento automático e Copa entre cidades. Podem aparecer no backlog como Could ou Won't.

### Regra do gol (primeira versão)
Um gol conta quando:
1. o problema foi registrado no jogo;
2. foi marcado como resolvido;
3. pelo menos 2 vizinhos diferentes confirmaram com foto.

Um relato sozinho não vale gol. Assim, o jogo não premia encher o SP156 de pedidos.

## Plano até 19/11

| Quando | O que precisa estar pronto |
| --- | --- |
| **05 a 07/11** | Equipe e bairros definidos. Conversas com pelo menos 3 moradores. Ficha preenchida. API de teste no Render conectada ao Oracle da FIAP. CSV do SP156 aberto e conferido |
| **08 a 10/11** | Notebook com os dois modelos. Modelo de dados e scripts SQL. Algoritmo em Python |
| **11 a 14/11** | API completa no ar, com tratamento de exceções. Rede no Packet Tracer com todos os testes |
| **15 a 16/11** | Telas, diagramas de casos de uso e de classes, backlog e vídeo 3D |
| **17 a 18/11** | Pitch gravado. PDFs revisados. Conferência de cada entrega contra os critérios do documento |
| **19/11** | Último commit e envios com folga, antes das 23h55 |

## Armadilhas que tiram nota

- **Java:** commit depois da data de entrega zera toda a parte técnica. O deploy vale 30 de 100 pontos.
- **Agile:** mais de um PDF ou link externo tira 10 pontos. Vídeo inacessível zera o item.
- **Database:** não incluir links para diagramas em nuvem. Mínimo de 5 linhas por tabela, com exceções justificadas.
- **Redes:** a faixa é 192.168.X.0/24, com X igual ao número da equipe.
- **Data Science:** separar treino e teste sem vazamento de dados. As recomendações valem 3 dos 10 pontos.
- **Dados:** retirar logradouro e número do SP156 antes de publicar qualquer CSV. Cadastros do app devem ser fictícios.
- **IA:** registrar os prompts em `prompts.md`, e todos precisam saber explicar cada linha.
- **Envio:** só um integrante envia cada disciplina, identificando todos os membros.

## Teste dos dados do SP156 (4º trimestre de 2025)

Arquivo original: `arquivo-final-4-trim-25.csv`, 99,4 MB, 479.701 chamados abertos entre 01/10 e 31/12/2025, com finalizações até 07/08/2026. Fonte: portal de dados abertos da Prefeitura de São Paulo. A análise final está em `data-science/varzea_sp156.ipynb`.

**Recorte do jogo:** 104.604 chamados, 20 serviços visíveis na rua e 96 distritos. Treino com outubro, teste com dezembro. Entradas: serviço, distrito, canal e dia da semana.

| Modelo | Alvo | Resultado | Comparação simples |
| --- | --- | --- | --- |
| Linear | Dias até finalizar | MAE 6,6 dias, RMSE 19,6, R² 0,18 | Chutar a mediana: MAE 7,6, RMSE 22,5 |
| Logística | Finalizado em até 30 dias | Precisão 0,92, recall 0,97, F1 0,94, AUC 0,84 | Chutar sempre "sim": acurácia 0,88, F1 0,94 |
| Alerta de atraso | Mesma logística, limiar 0,82 | Marca 14,5% dos chamados e pega 61,4% dos atrasos | Taxa geral de atraso: 11,9% |

**Conclusões:**
- Guias e sarjetas e tapa-buraco somam 53,1% dos atrasos de dezembro.
- Entulho é resolvido em 98,1% dos casos em 30 dias, e calçadas, em 60,2%. Por isso, gol difícil deve valer mais.
- O placar deve usar o saldo contra o esperado, e não a taxa bruta. Tatuapé (+0,2) e Mooca (-5,2) têm taxas parecidas, mas saldos bem diferentes.
- Iluminação pública não aparece neste arquivo, então não pode ser medida pelo SP156.
- Logradouro, número, CEP, latitude e longitude ficam fora do CSV entregue.

**Limites:** é um único trimestre. "Finalizado" não prova conserto, e o volume de chamados também reflete quem costuma reclamar.

## O que ainda precisa ser provado

1. **Moradores querem isso?** Pergunta central: "Você cobraria o conserto da sua rua para seu bairro ganhar do bairro vizinho?"
2. **"Finalizado" é resolvido?** O SP156 só informa que o chamado foi fechado. Alguns tempos são curtos a ponto de levantar dúvida, e é isso que o VAR quer medir.
3. **Rivalidade ajuda?** Não há estudo sobre ligas entre bairros. O pitch deve apresentar isso como aposta, não como fato.
4. **Concorrência:** a busca foi curta. Antes de dizer "ninguém faz", testem o Colab e procurem de novo.
5. **Render + Oracle da FIAP:** ainda não testado. É o maior risco técnico.
