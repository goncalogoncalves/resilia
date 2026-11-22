# Marco 7 — Relatório

> Data: 2026-11-21 · Estado: **aprovado a 2026-11-21**
> Fontes e propostas aprovadas em [marco-7-fontes.md](marco-7-fontes.md) (propostas A da §3 e decisões técnicas da §4).

## O que ficou feito

- **Avisos do IPMA na app.** A app lê dois ficheiros dos dados abertos do IPMA:
  - os avisos meteorológicos (`warnings_www.json`);
  - o risco de incêndio para hoje, amanhã e depois de amanhã (`rcm-d0/1/2.json`). Só descarrega o risco de incêndio se a pessoa escolheu um concelho.

  Descarrega sempre o **ficheiro nacional** e filtra a zona no telemóvel, por isso o IPMA não fica a saber a zona.
- **Escolha da zona**, em Definições → "Avisos meteorológicos" ou a partir da página inicial:
  - as 25 áreas de aviso, com os nomes oficiais;
  - no continente, um **concelho** opcional para o risco de incêndio (lista de 278 concelhos, ordenada em português).

  Não pede localização.
- **Verificação em segundo plano** de 1, 3 (por omissão) ou 6 em 6 horas, ou desligada. Só corre com rede, sem *wakelocks* nem alarmes exatos. Ao abrir os avisos, a app atualiza se os dados tiverem mais de 1 hora.
- **Notificações:**
  - por omissão, só avisos **laranja e vermelhos** e risco de incêndio **muito elevado ou máximo**; os amarelos ligam-se nas definições;
  - cada aviso só notifica uma vez;
  - no ecrã bloqueado aparece só "Aviso meteorológico", porque a área diz onde a pessoa vive;
  - têm o botão "O que fazer", que abre o cenário certo;
  - nos laranja e vermelhos têm também "Modo emergência", que **pede confirmação** antes de ativar;
  - usam um canal próprio, "Avisos meteorológicos".
- **Faixa na página inicial:**
  - "Escolhe a tua zona" quando não há zona;
  - o aviso mais grave, com a cor e o nível escritos por extenso;
  - ou "Sem avisos para …" quando não há nenhum.
- **Ecrã "Avisos IPMA":**
  - os avisos ativos, mais graves primeiro, com período em hora local, texto do IPMA e significado da cor;
  - o risco de incêndio para 3 dias;
  - a hora a que a app descarregou os dados, com "Desatualizado" a partir das 6 horas;
  - "Não foi possível obter os avisos", com a hora da última leitura que correu bem;
  - a nota "A ausência de aviso na app não significa que não haja perigo…" (sempre visível);
  - ligações para o IPMA e para os Avisos à população da ANEPC;
  - fontes e aviso legal.
- **Cenários sugeridos pelos avisos (`alertTriggers`), conforme a decisão 5:**

  | Aviso IPMA | Cenário |
  |---|---|
  | Vento, trovoada, agitação marítima | Tempestade |
  | Precipitação | Cheias |
  | Tempo quente | Calor |
  | Tempo frio | Frio |
  | Neve | Frio (por confirmar) |
  | Risco de incêndio 4 ou 5 | Incêndio rural |

  O CI recusa gatilhos para tipos de aviso que não existem e gatilhos repetidos.
- **Conteúdo novo:**
  - `content/alerts/alerts.json`: áreas, concelhos, nomes dos tipos de aviso, cores e significados parafraseados do guia do IPMA. Tudo "Por rever".
  - 5 fontes novas: `ipma-api`, `ipma-guia-avisos`, `ipma-avisos`, `ipma-risco-incendio` e `anepc-avisos-populacao`.
  - Conteúdo na versão 0.8.0.
- **Permissão `INTERNET`**, só HTTPS. O `network_security_config` proíbe tráfego sem cifra e não há bibliotecas de rede novas.
- **Base de dados na versão 3**, com as tabelas `alert_cache` e `alert_notified`. A migração 2 → 3 foi testada. Fica excluída dos backups, como o resto.
- **Novo módulo `feature:alerts`.**

## Medições

| Métrica | Valor |
|---|---|
| APK release (R8 + shrink, depois do zipalign) | 2,44 MB (Marco 6b: 2,47 MB). Não investiguei porque desceu 0,03 MB. |
| Testes | 120, todos a passar (+19): leitura das respostas reais do IPMA de hoje, regras dos avisos, repositório com rede simulada, migração da base de dados e ecrã. |
| Lint | 0 erros. 10 avisos, todos de versões mais recentes disponíveis. |
| Validador de conteúdo | "Content OK" |
| Rede | Por verificação: 35 KB (avisos) + 3 × 24 KB (risco de incêndio, só com concelho) |
| Bateria | Não medida. Corre uma verificação a cada 3 horas, só com rede. |

Verificado no emulador (Android 14, build de debug, **com os dados reais do IPMA de hoje**):
- página inicial sem zona;
- escolha da área Porto e do concelho Porto;
- aviso amarelo de chuva no Porto (18:00 às 21:00) na página inicial e no ecrã dos avisos;
- risco de incêndio "Reduzido" para os 3 dias;
- notificação real do aviso amarelo, com "O que fazer" a abrir o cenário das cheias;
- pedido de confirmação do modo emergência, com o botão "Ativar" a ativá-lo no cenário da tempestade.

**Não verifiquei:**
- **Uma notificação laranja ou vermelha real**, porque hoje só havia amarelos. O pedido de confirmação foi testado a simular a ação da notificação com o `adb`.
- **Os Açores e a Madeira no emulador.** Só estão cobertos pelos testes.
- **O horário de verão.**

A zona Porto ficou escolhida no emulador.

## Decisões tomadas durante o marco

1. **Hora do IPMA.** A decisão 6 dizia para mostrar "a hora a que o IPMA o atualizou", mas o ficheiro de avisos **não traz essa hora**. A app mostra a hora a que descarregou os dados e não inventa outra.
2. **Nomes das áreas das ilhas.** Escrevi por extenso as abreviaturas da página do IPMA: "Madeira-R. Montanhosas" passou a "Madeira – Regiões Montanhosas" e "Açores - G. Ocidental" a "Açores – Grupo Ocidental". Os nomes em inglês são traduções minhas.
3. **"Precipitação" aparece como "Chuva"** nos ecrãs. O identificador do IPMA não muda.
4. **Etiqueta de aviso vermelho:** texto branco em vez de preto, porque preto sobre o vermelho reservado dá cerca de 4,2:1 de contraste e branco dá cerca de 5:1.
5. **Risco de incêndio nas cores reservadas:** elevado a amarelo, muito elevado a laranja, máximo a vermelho; reduzido e moderado sem cor. **Esta correspondência é minha.** Não a encontrei no IPMA.
6. **Mudar as definições não volta a correr a verificação logo.** O WorkManager mantém o horário (política `UPDATE`). Só uma zona nova é que provoca uma atualização imediata.

## O que precisas de validar

1. **Os textos** da §4 de [marco-7-fontes.md](marco-7-fontes.md) e a nota sobre a ausência de aviso.
2. **As decisões 2, 3 e 5** acima.
3. **Enviar o email ao IPMA** (rascunho na §5 de [marco-7-fontes.md](marco-7-fontes.md)), com as duas perguntas: o logótipo e as horas em UTC no verão.
4. **Ver uma notificação laranja ou vermelha real** quando houver uma, e confirmar as horas no horário de verão.

Capturas de ecrã: [docs/screenshots/marco-7/](screenshots/marco-7/).

A captura 04 é anterior a três correções feitas no emulador:
- o concelho passou a aparecer como "Concelho: Porto";
- "há 0 min" passou a "agora mesmo";
- os concelhos passaram a ficar por ordem alfabética portuguesa.
