# Várzea: o Brasileirão dos bairros

MVP da CitizenTech da Global Solution FIAP 2026. Cada distrito de São Paulo é um time, e o problema da rua resolvido e confirmado por vizinhos vale gol.

## O que já funciona

- Mapa 3D de São Paulo com os 96 distritos (contornos do GeoSampa): o distrito sobe e fica vermelho conforme os gols.
- Letreiro com os últimos gols, pódio e classificação, no celular e no computador.
- Modo avaliação: entra sem cadastro, e um VAR de teste com dois vizinhos simulados permite marcar um gol sozinho.
- Conta por e-mail e escolha do time entre os 96 distritos.
- Abertura de chamado, com foto opcional.
- Marcação como resolvido e VAR com foto obrigatória.
- Gol automático com 2 confirmações, e chamado reaberto com 2 contestações.
- Tabela por zona, partida da rodada e lista de chamados por distrito.

## O que ainda não existe

- Comparação com o histórico do distrito, cartão vermelho, assistências e rebaixamento.
- Integração com o SP156: a Várzea não abre nem fecha pedidos oficiais.
- Painéis para comércio e prefeituras.
- Moderação de conteúdo e fotos.

## Como executar

```bash
npm install
```

```bash
cp .env.example .env
```

```bash
npm run dev
```

Abra `http://localhost:5173`. Precisa de Node 20 ou mais recente.

## Stack

React, TypeScript, Vite e Three.js no front. Supabase no banco, login e fotos. As regras do jogo estão em `supabase/migrations/` (`0001_esquema.sql` e `0003_modo_avaliacao.sql`), no banco, para o navegador não conseguir burlar.

O design segue a skill `varzea-design`: fundo `#0F0F0F`, papel `#EBE6E6`, vermelho `#A31923`, Bebas Neue e Montserrat.

## Dados

| Dado | Origem |
|---|---|
| Distritos | Os 96 distritos da capital. A divisão por zona foi feita pela equipe e deve ser conferida |
| Gols por serviço | Calculados com o SP156, 4º trimestre de 2025 |
| Chamados com `demo = true` | Fictícios |

Para limpar a demonstração antes de um uso real:

```sql
delete from public.chamados where demo;
```

## Antes de usar com pessoas reais

- Definir a Site URL e as Redirect URLs em Authentication > URL Configuration no Supabase.
- Configurar um serviço de e-mail próprio, pois o padrão do Supabase tem limite baixo de envios.
- Publicar uma política de privacidade, por causa da LGPD: o app guarda e-mail, apelido, distrito e fotos.
- Definir quem modera as fotos, que ficam acessíveis por URL pública.
- Reforçar a verificação de moradia: hoje, qualquer pessoa pode escolher um distrito, e 3 contas combinadas conseguem marcar um gol falso.
