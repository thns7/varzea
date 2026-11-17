# Várzea: o campeonato dos bairros

CitizenTech da Global Solution FIAP 2026. Cada distrito de São Paulo é um time, e o problema da rua resolvido e confirmado por vizinhos vale gol.

**MVP publicado:** https://varzea-flame.vercel.app

## Ficha da CitizenTech

> Rascunho. O que está entre [colchetes] ainda precisa ser preenchido pela equipe.

| Campo | Conteúdo |
| --- | --- |
| **Nome** | Várzea: o campeonato dos bairros |
| **Cidade e bairro** | São Paulo, piloto com 4 distritos vizinhos: [distrito 1], [distrito 2], [distrito 3], [distrito 4] |
| **Cidadão beneficiado** | Moradores que convivem com buracos, entulho e calçadas quebradas, principalmente quem depende da rua a pé, como idosos e pessoas com mobilidade reduzida |
| **Problema, em uma frase** | Em [distrito], [perfil do morador entrevistado] pede conserto de problemas da rua e não sabe quando nem se foi resolvido de verdade, porque cobrar a cidade é lento e solitário (hipótese a confirmar nas entrevistas) |
| **Como funciona** | Um morador registra o problema, outro informa que foi resolvido, e dois vizinhos confirmam com foto. Só então o conserto vira gol para o distrito. Problemas mais difíceis valem mais gols |
| **Link do repositório** | https://github.com/thns7/varzea |
| **Indicador de impacto** | % de problemas confirmados como resolvidos em até [N] dias, comparado ao histórico do distrito, e vizinhos ativos por rodada |
| **ODS conectados** | ODS 11: ruas e calçadas mais seguras e acessíveis. ODS 10: cada distrito é comparado com o próprio histórico. ODS 9: dados abertos para melhorar a infraestrutura urbana |

## O que cada parte faz

| Pasta | Disciplina | Conteúdo |
| --- | --- | --- |
| [`varzea-app`](varzea-app/README.md) | Demonstração e pitch | MVP com mapa 3D da cidade, com React, Three.js e Supabase. No ar em https://varzea-flame.vercel.app |
| [`java-api`](java-api/documentacao.md) | Domain Driven Design - Java | API REST com Spring Boot e Oracle |
| [`database`](database/documentacao.md) | Database Design | Modelo de dados e scripts SQL para Oracle |
| [`data-science`](data-science/varzea_sp156.ipynb) | Data Science | Regressões linear e logística com dados do SP156 |
| [`dynamic-programming`](dynamic-programming/README.md) | Dynamic Programming | Grafo, tabulação e memoização para escalar a equipe do dia |

Agile, AR/VR e Redes ainda não estão neste repositório.

## Dados

- **SP156:** dados abertos da Prefeitura de São Paulo, 4º trimestre de 2025, sem endereços.
- **Chamados, moradores e gols de demonstração:** fictícios.
- **Credenciais:** ficam só em variáveis de ambiente, nunca no código.

## Uso de IA

O projeto usou IA como copiloto. Os pedidos estão em [`prompts.md`](prompts.md).

## Equipe

- Gabriel Santos - RM 562419
- Gustavo Rocha - RM 564152
- Thiago Nascimento - RM 561967
- Vinicius Herreira - RM 565662
