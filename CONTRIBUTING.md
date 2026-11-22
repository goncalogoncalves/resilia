# Como contribuir

Obrigado por quereres ajudar! Há dois tipos de contribuição: **código** e **conteúdo**.

Ao contribuir aceitas que o código fica sob [Apache-2.0](LICENSE) e o conteúdo sob [CC BY-SA 4.0](LICENSE-CONTENT).

## Regras que não se negociam

1. **Nunca inventes** procedimentos de saúde ou segurança, números de telefone, dados oficiais ou endpoints.
2. Passos do tipo `safety` ou `health` **têm de** citar pelo menos uma fonte oficial
   (ANEPC, INEM, Cruz Vermelha Portuguesa, DGS, IPMA, SNS 24, Governo de Portugal, União Europeia,
   serviços municipais de proteção civil).
   Operadores (`UTILITY`, ex.: E-REDES) não contam como oficiais para estes passos. O CI falha se não citarem.
3. **Nenhum número de telefone sem fonte.** Cada contacto em `contacts/national.json` cita pelo menos uma fonte.
   O mesmo vale para o kit: cada item e cada quantidade em `kit/kit.json` cita uma fonte oficial.
4. **Parafraseia e cita**, não copies textos oficiais na íntegra.
5. Nada de letras de canções, poemas ou textos protegidos. Histórias e cantigas só originais ou de domínio público, com a origem indicada.
6. Todo o texto existe em **pt-PT** e **en** (inglês britânico). O CI falha se faltar um idioma.
7. Sem trackers, analytics, anúncios ou novas permissões sem discussão prévia.

## Contribuir conteúdo

O conteúdo está em [`content/`](content/), em JSON, e é validado por schemas em [`content/schema/`](content/schema/).

```
content/
  manifest.json          versão do conteúdo e lista de ficheiros
  sources.json           fontes (entidade, título, URL, data do documento, data de consulta)
  contacts/national.json contactos úteis (cada número com pelo menos uma fonte)
  kit/kit.json           itens do kit (reserva em casa e mochila), dias mínimos e intervalo de revisão
  plan/plan.json         sugestões de cada secção do plano familiar (cada sugestão com fonte oficial)
  tools/battery.json     guia "Fazer a bateria durar" (mesmas regras dos passos dos guias)
  scenarios/*.json       um ficheiro por cenário (antes / durante / depois)
  morale/support.json    apoio emocional (passos de saúde: só com fonte oficial)
  morale/activities.json atividades sem ecrã (origem obrigatória; sem letras de canções nem textos protegidos)
  morale/materials.json  lista fechada de materiais das atividades
  alerts/alerts.json     áreas de aviso e concelhos do IPMA, nomes dos tipos e das cores (só dados oficiais)
  places/sns-urgencias.json  hospitais com urgência (gerado: python tools/data/sns_urgencias.py; não editar à mão)
  schema/                JSON Schemas
```

### Um passo

```json
{
  "id": "power-outage.during.03",
  "text": { "pt-PT": "Frase curta, na 2.ª pessoa.", "en": "Short sentence, second person." },
  "kind": "safety",
  "urgency": "now",
  "sourceIds": ["anepc-exemplo"],
  "review": { "status": "draft" }
}
```

- `id`: `<id-do-cenário>.<fase>.<nn>`, em minúsculas.
- `kind`: `safety` | `health` (exigem fonte oficial) | `practical` | `wellbeing`.
- `urgency`: `now` | `soon` | `later` | `info`.
- `review.status`: novo conteúdo entra **sempre** como `draft`. Só o responsável pela revisão passa a `reviewed`
  (preenchendo `reviewedAt` e `reviewedBy`). A app não mostra este estado em cada passo: o aviso legal diz que os
  textos resumem fontes oficiais e não foram revistos por profissionais.
- Para o passo aparecer no modo emergência ("Faz isto agora"), acrescenta o `id` a `emergencyNow` do cenário.
- `alertTriggers` liga um cenário a avisos do IPMA: `ipma:<awarenessTypeName>` (ex.: `ipma:Vento`) ou `rcm:<1-5>` (risco de incêndio). Cada gatilho só pode estar num cenário.

### Estilo

- Frases curtas, uma ação por passo, verbo no início ("Desliga…", "Guarda…").
- Tratamento por "tu" em pt-PT; inglês britânico ("torch", "neighbours").
- Sem jargão. Pensa em alguém assustado, às escuras, a ler num telemóvel.

### Validar antes do pull request

```
./gradlew :tools:content-validator:validateContent
```

## Contribuir código

- Kotlin + Jetpack Compose. `core:domain` e `core:content` são Kotlin puro (sem Android) para poderem migrar para Kotlin Multiplatform.
- Módulos `feature:*` não dependem uns dos outros.
- O nome da app só aparece em `app_name` (strings.xml) e `AppInfo.NAME` — o CI verifica.
- Acessibilidade: alvos de toque ≥ 48dp, textos em `sp`, `contentDescription` em ícones com significado, testar com TalkBack e letra grande.
- Antes do pull request:

```
./gradlew test testDebugUnitTest lintDebug :tools:content-validator:validateContent
```
