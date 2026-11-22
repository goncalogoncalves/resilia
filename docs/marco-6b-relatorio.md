# Marco 6b — Relatório

> Data: 2026-11-21 · Estado: **aprovado a 2026-11-21**
> Fontes e propostas aprovadas em [marco-6b-fontes.md](marco-6b-fontes.md) (propostas A).

## O que ficou feito

- **Separador Calma** com duas partes:
  - **Apoio emocional**, no topo, com 5 guias curtos:
    - para ti (10 passos);
    - crianças e jovens (7);
    - pessoas idosas (5);
    - se reviveres o que aconteceu (3);
    - sinais de alerta (8).

    Cada passo cita o guia da Ordem dos Psicólogos, da DGS e da ANEPC (fev. 2026). No topo está a nota "Estes conselhos vêm de um guia oficial escrito para depois de tempestades e inundações. Quase todos servem para qualquer emergência." No fim de cada guia aparece "Onde pedir ajuda", com SNS 24, 1411 e 112.
  - **Coisas para fazer:** as 24 atividades aprovadas, em pt-PT e en-GB, todas "Por rever":
    - 8 jogos;
    - 6 histórias e sons;
    - 4 de calma;
    - 6 desafios.
- **Filtros:**
  - tipo;
  - **às escuras**, **silencioso**, sem materiais, sozinho e até 10 min;
  - idade (4, 7, 10 ou 14 anos).

  Ficam guardados no telemóvel. Se nenhuma atividade passar nos filtros, aparece "Limpar filtros".
- **Ecrã da atividade:**
  - idade, pessoas, duração, materiais, luz, barulho e onde se faz;
  - passos numerados e variantes;
  - origem: "Criada para a Resilia", "Jogo tradicional, descrito por nós" ou "Baseada em conselhos oficiais", com a fonte.
- **Modo emergência:** o novo botão "Algo para fazer (em silêncio, às escuras)" mostra só as 5 atividades silenciosas que dá para fazer às escuras. Os passos abrem no mesmo ecrã, sem sair do modo emergência.
- **Contactos:**
  - novo: **1411**, Linha Nacional de Prevenção do Suicídio (24 h, gratuita e confidencial; fonte SNS);
  - o SNS 24 passa a dizer "Também tem aconselhamento psicológico" (fonte gov.pt). **Não escrevi a opção do menu**, como aprovado.
- **Nenhuma letra de canção nem texto de histórias.** A categoria chama-se "Histórias e sons".
- **CI:**
  - esquemas novos para atividades, materiais e apoio;
  - regras novas:
    - todas as atividades têm origem;
    - as de domínio público têm de ter nota;
    - as baseadas em conselhos têm fonte oficial;
    - os materiais existem na lista;
    - os passos do apoio emocional são "saúde" e têm fonte oficial.
- **Conteúdo na versão 0.7.0.**

## Medições

| Métrica | Valor |
|---|---|
| APK release (R8 + shrink) | 2,47 MB (Marco 6a: 2,40 MB) |
| Testes | 101, todos a passar (+8) |
| Lint | 0 erros. 10 avisos, todos de versões mais recentes disponíveis. |
| Validador de conteúdo | "Content OK" |
| Bateria | Não medida. Só texto e ecrãs, nada corre em background. |

Verificado no emulador (Android 14, build de debug):
- separador Calma com os filtros;
- filtro "às escuras" e "silencioso" a deixar 5 atividades;
- ecrã de uma atividade baseada no guia oficial, com a fonte;
- apoio emocional com os 5 guias;
- guia das crianças;
- "Onde pedir ajuda" com o 1411;
- "Algo para fazer" no modo emergência, com uma atividade aberta;
- saída do modo emergência.

## Decisões tomadas durante o marco

1. **Origem nova "baseada em conselhos oficiais" (`guidance`).** Corresponde às atividades "baseadas no guia oficial" da proposta. O CI obriga a que tenham fonte oficial.
2. **"Forca" passou a "Adivinha a palavra"** e o desenho passou a ser uma flor que perde pétalas. As regras são as mesmas. Mudei por causa do tom, numa app para momentos difíceis.
3. **"Encontrar a lanterna de olhos fechados"** treina-se com a luz acesa e com um adulto por perto, para não haver quedas. Como não precisa de luz, aparece na lista do modo emergência.
4. **Os passos do apoio emocional são do tipo "saúde"**, para o CI exigir fonte oficial em todos.
5. **A data do guia ficou 2026-02-01**, porque o PDF só diz "02.2026". É a mesma regra dos folhetos sem dia nos marcos anteriores.

## O que precisas de validar

1. **O texto dos 5 guias de apoio emocional**, em [`content/morale/support.json`](../content/morale/support.json). É conteúdo de saúde mental.
2. **As 24 atividades**, em [`content/morale/activities.json`](../content/morale/activities.json): regras, idades e duração. Idealmente, alguém deve testá-las com crianças.
3. **A opção do menu do SNS 24** para aconselhamento psicológico. Se ligares e confirmares, posso acrescentá-la.
4. **As decisões 2 e 3 acima.**

Capturas de ecrã: [docs/screenshots/marco-6b/](screenshots/marco-6b/).
