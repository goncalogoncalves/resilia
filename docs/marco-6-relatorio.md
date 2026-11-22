# Marco 6a — Relatório

> Data: 2026-11-21 · Estado: **aprovado a 2026-11-21**
> Fontes e propostas aprovadas em [marco-6-fontes.md](marco-6-fontes.md) (propostas A).

## O que ficou feito

- **6 cenários novos**, com antes, durante e depois, em pt-PT e en-GB. Todos os passos ficam "Por rever".

  | Cenário | Passos (antes / durante / depois) | Fontes | Modo emergência |
  |---|---|---|---|
  | Tempestade: chuva e vento fortes | 6 / 6 / 2 | ANEPC (Depressão Charles, riscos tecnológicos) | 2 passos |
  | Cheias e inundações | 4 / 8 / 7 | ANEPC (aviso do Mondego, Charles, riscos tecnológicos) | 2 passos |
  | Sismo (inclui o tsunami no "Depois") | 8 / 6 / 12 | ANEPC (folheto de 2018, riscos naturais) | 6 passos |
  | Onda de calor | 2 / 11 / 0 | ANEPC (riscos naturais) | 4 passos |
  | Frio intenso | 2 / 11 / 0 | ANEPC (riscos naturais) | 3 passos |
  | Incêndio rural | 5 / 12 / 1 | AGIF (autoproteção, gestão de combustível) | 6 passos |

- **4 fontes novas:**
  - ANEPC "Riscos naturais";
  - ANEPC aviso de cheias do Mondego (10/02/2026);
  - AGIF "Medidas de autoproteção";
  - AGIF "Gestão de combustível".
- **Nova entidade `AGIF`, oficial.** Na app aparece "Fonte: AGIF".
- **Cheias:** os 12 passos que vêm da página das barragens levam a nota "A ANEPC dá este conselho para cheias causadas por barragens". Um teste garante que a nota está lá.
- **Sismo:** o conselho dos vãos de portas ficou de fora, como aprovado.
- **Fase sem passos** (o "Depois" do calor e do frio): mostra "Não encontrámos passos oficiais para esta fase".
- **"Ver também"** no fim de cada guia, com ligação aos cenários relacionados:
  - falha de eletricidade ↔ tempestade e sismo;
  - tempestade ↔ cheias.
- **Conteúdo na versão 0.6.0.** O Início e a lista de guias mostram os 7 cenários.

## Medições

| Métrica | Valor |
|---|---|
| APK release (R8 + shrink) | 2,40 MB (Marco 5: 2,39 MB) |
| Testes | 93, todos a passar (+2) |
| Lint | 0 erros. 10 avisos, todos de versões mais recentes disponíveis. |
| Validador de conteúdo | "Content OK" |
| Bateria | Não medida. Este marco só acrescenta texto, sem nada novo a correr em background. |

Verificado no emulador (Android 14, build de debug):
- Início com os 7 cenários;
- incêndio rural com "Fonte: AGIF";
- "Depois" do calor vazio, com a mensagem;
- "Depois" das cheias com a nota das barragens;
- "Ver também" a abrir a tempestade a partir das cheias;
- modo emergência do sismo com os 6 passos, incluindo o do tsunami;
- saída do modo emergência.

## Decisões tomadas durante o marco

1. **Incêndio: o contexto vai no próprio passo, sem passos-subtítulo.** Na §5 propus pôr "Fechado em casa", "Se ficares cercado", etc. como passos separados. Mudei porque o modo emergência mostra os passos soltos e perdia o contexto. Agora cada passo começa pela situação: "Se ficares fechado em casa, …".
2. **Tirei o passo "sabe onde fica o abrigo ou refúgio da tua aldeia".** As páginas da AGIF que li mandam ir para o abrigo, mas não dizem para o procurar antes. No lugar dele ficou "Informa-te sobre o perigo de incêndio e as ocorrências na tua zona", que está na página da gestão de combustível.
3. **Calor: "Não fiques dentro de um carro estacionado ao sol"** em vez de "nunca deixes ninguém", para ficar igual ao texto da ANEPC.
4. **O primeiro passo das cheias ("pergunta à câmara se vives numa zona que pode inundar") também vem da página das barragens**, por isso leva a mesma nota.
5. **Ligações ao kit (`relatedKitItemIds`) ficaram só nos dados.** A app ainda não as mostra; o sítio certo é o Marco 9 (acessibilidade e revisão da navegação).
6. **No modo emergência do sismo entram passos do "Depois"** (ver se estás ferido, não acender fósforos, tsunami), porque são os primeiros a fazer quando o tremor para.

## O que precisas de validar

1. **O texto dos 6 cenários**, em [`content/scenarios/`](../content/scenarios/): `storm`, `flood`, `earthquake`, `heat`, `cold` e `wildfire`.
2. **Os passos escolhidos para o modo emergência** de cada cenário (tabela acima; capturas [02](screenshots/marco-6/02-incendio.png) e [06](screenshots/marco-6/06-emergencia-sismo.png)).
3. **As decisões 1 a 3 acima**, porque mudam um pouco o que foi aprovado.

Capturas de ecrã: [docs/screenshots/marco-6/](screenshots/marco-6/).

## Próximo passo

O **Marco 6b (Calma)** começa por `docs/marco-6b-fontes.md`, que vai tratar de:
- atividades originais, de domínio público ou tradicionais, sem letras de canções nem textos protegidos;
- conteúdo de saúde mental só de fontes oficiais.
