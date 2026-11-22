# Resilia — Marco 0: proposta para aprovação

> Estado: **aprovado** (2026-11-20), com as decisões abaixo. Ajustes futuros são possíveis.
> Data: 2026-11-20

## 0. Decisões tomadas (resposta ao Marco 0)

| # | Decisão |
|---|---|
| D1 | Sem domínio próprio. applicationId: **`com.goncalogoncalves.resilia`** (com base em goncalogoncalves.com). |
| D2 | Repositório **privado** em github.com/goncalogoncalves. As licenças (Apache-2.0 / CC BY-SA 4.0) ficam desde já. Nota: em repositórios privados, o GitHub Actions tem um limite de minutos gratuitos, por isso o CI deve ser leve. |
| D3 | Distribuição só na **Google Play**. Mesmo assim evitamos Google Play Services e Firebase (princípio "sem trackers"). |
| D4 | **Mostrar todo o conteúdo**, incluindo o que está "por rever", sempre com essa etiqueta. A **exclusão de responsabilidade** tem de estar sempre muito clara: ecrã de aceitação na primeira abertura, rodapé permanente nos guias e no modo emergência, e secção própria em Sobre/Aviso legal. O texto jurídico fica "por rever" e deve ser validado por alguém com formação jurídica. |
| D5 | Acrescentar o cenário **frio extremo**. |
| D6 | **Açores e Madeira** incluídos. |
| D7 | Incêndios na v1: avisos IPMA + ligação para a página de avisos da ANEPC. Sem fogos.pt. |
| D8 | O conteúdo só é atualizado com novas versões da app. |
| D9 | Inglês: **en-GB**. |
| D10 | Os campos `reviewedBy`/`reviewedAt` ficam no JSON, mas **não aparecem** na UI. A UI mostra só "Revisto" / "Por rever" e a data da fonte. |
| D11 | Identidade visual criada de raiz. Tom **calmo e acolhedor**. Cor principal **azul-petróleo**; vermelho, laranja e amarelo ficam reservados para avisos. Símbolo: **escudo/abrigo**, desenhado com formas suaves (casa dentro do escudo?) para não parecer um antivírus. Tipografia: **Atkinson Hyperlegible** (OFL), incluída na app e no PDF. Modo Emergência: preto OLED com alto contraste. |
| D12 | Ficha médica com campos simples, opcionais e em texto livre. |
| §6 | Barra inferior com **5 destinos**: Início, Guias, Preparar, Calma, Ferramentas. |
| §3 | Exportação/importação manual de dados (inventário + plano) **na v1**, com cifra opcional por palavra-passe. |

---

## 1. O que entendi

**Resilia** é uma app Android, open source e de serviço público, que ajuda uma pessoa comum em Portugal (sobretudo em meio urbano) a **preparar-se**, **agir**, **recuperar** e **manter a moral** em emergências. O cenário-modelo é uma falha de eletricidade de 3 dias. A estrutura serve qualquer cenário.

O que a distingue da K72:
1. Guias antes/durante/depois por cenário, offline, pt-PT + en.
2. Inventário com validades e lembretes, sem conta.
3. Plano familiar e cartão em papel.
4. Moral e entretenimento sem ecrã.
5. Confiança: sem anúncios, paywall, trackers, conta ou backend.

Restrições que guiam todas as decisões:
- Offline-first; rede só para alertas (e, talvez, atualização de conteúdo).
- Dados só no dispositivo, excluídos de backups na cloud.
- Permissões mínimas: `POST_NOTIFICATIONS`, `INTERNET`. Lanterna via `CameraManager.setTorchMode` (não precisa de `CAMERA`). Chamadas via `ACTION_DIAL` (abre o marcador, não precisa de `CALL_PHONE`).
- Conteúdo de saúde/segurança **só** de fontes oficiais, com fonte e data por passo. O resto é marcado "por rever". A validação final é tua.
- Domain e data têm de poder migrar para KMP mais tarde.
- Lançamento só com todos os marcos concluídos.

---

## 2. Dúvidas (preciso de resposta antes do Marco 1)

| # | Pergunta | A minha proposta por omissão |
|---|---|---|
| D1 | **applicationId / pacote.** Tens domínio? | `pt.resilia.app` só se controlares `resilia.pt`. Caso contrário, `io.github.<utilizador>.resilia`. |
| D2 | **Onde fica o repositório** (GitHub? conta pessoal ou organização? público desde o Marco 1?) | GitHub, público desde o início, CI em GitHub Actions. |
| D3 | **Distribuição**: Google Play, F-Droid ou ambos? | Ambos. Isto implica **zero dependências Google Play Services/Firebase** (WorkManager e Jetpack não têm esse problema). |
| D4 | **Conteúdo "draft" na versão de lançamento.** | Saúde/segurança em draft **nunca** aparece em release. Conteúdo geral em draft aparece com a etiqueta "Por rever". Em debug aparece tudo, sinalizado. |
| D5 | **Cenários extra?** Frio extremo, acidente industrial/químico (abrigo no local), tsunami (costa), falha de comunicações. | Ficam fora da v1, mas o modelo suporta-os. Proponho acrescentar pelo menos **frio extremo**, porque o IPMA emite avisos de "tempo frio" e a sugestão automática ficaria coerente. |
| D6 | **Açores e Madeira** incluídos? (Têm proteções civis regionais próprias.) | Incluídos nos alertas IPMA e na escolha de zona. Contactos regionais ficam como placeholders "por rever". |
| D7 | **Alertas de incêndio** (ver §8): a fonte mais útil (fogos.pt) vai exigir token individual. | Na v1: só avisos IPMA (e risco de incêndio IPMA, se confirmar que existe em dados abertos). Mais uma ligação externa para a página de avisos da ANEPC. Sem fogos.pt. |
| D8 | **Atualização de conteúdo pela rede**, sem backend. | Na v1 o conteúdo só muda com atualizações da app. Mais tarde pode ser descarregado das *releases* do GitHub com assinatura verificada. |
| D9 | **Inglês**: en-GB ou en-US? | en-GB. |
| D10 | **`reviewedBy`**: nome real público, pseudónimo ou só a entidade? | Identificador curto (ex.: iniciais ou handle do GitHub), definido por ti. |
| D11 | **Identidade visual**: cores, logótipo? | Proponho uma paleta calma (azul-petróleo + neutros quentes) e ícones Material Symbols (Apache-2.0). O logótipo fica para o Marco 9. |
| D12 | **Ficha médica**: que campos? | Grupo sanguíneo, alergias, medicação, condições relevantes, contacto do médico. Todos opcionais e em texto livre. Sem campos clínicos estruturados. |

---

## 3. Riscos

| Risco | Impacto | Mitigação |
|---|---|---|
| **Exatidão do conteúdo** de saúde/segurança | Alto (pode causar dano) | Regra no validador: um passo `safety`/`health` sem `sourceIds` de entidade oficial faz o CI falhar. Os drafts são escondidos em release. Aviso "não substitui o 112" visível. |
| **Direitos sobre textos oficiais.** ANEPC, DGS e outras não publicam necessariamente sob licença compatível com CC BY-SA. | Médio (legal) | Parafrasear e citar, nunca copiar texto integral. O ficheiro `sources.json` regista a licença conhecida de cada fonte. Deves confirmar este ponto. |
| **Nome "Resilia"** pode já estar em uso (marcas, apps) | Médio | Pesquisar no INPI/EUIPO e na Play Store antes de publicar. O nome está num único sítio, por isso é fácil mudar. |
| **Fontes de incêndio** sem API oficial aberta | Médio (funcionalidade) | Ver D7. Não embutir tokens na app (num repositório open source ficariam públicos). |
| **Esquema do JSON do IPMA** não está documentado oficialmente (só encontrei uso por terceiros) | Médio | Parser tolerante, testes com respostas guardadas e aviso de "dados indisponíveis". Confirmar termos de uso com o IPMA antes do Marco 7. |
| **Bateria**: polling de alertas | Médio | WorkManager periódico (≥ 1 h, sugiro 3 h por omissão, configurável), só com rede, sem *wakelocks*. Desligado se não houver zona escolhida. |
| **Restrições do Android** a trabalho em background (Doze, fabricantes agressivos) | Médio | Nunca prometer "tempo real". Mostrar sempre a hora da última atualização. |
| **Lanterna** inexistente nalguns dispositivos; **sirene** perigosa para a audição | Baixo | Usar o ecrã como luz em alternativa. Avisar antes de ativar a sirene e não tocar em volume máximo de forma automática. |
| **PDF**: acentos, tamanhos A4 vs Letter, impressoras | Baixo | Usar `PdfDocument` com fontes do sistema, tamanho A4 fixo e teste de render. |
| **Perda de dados**: sem backup na cloud, trocar de telemóvel apaga tudo | Médio (UX) | Exportação/importação manual de ficheiro (inventário + plano), opcionalmente cifrada com palavra-passe. Pergunta: queres isto na v1? |
| **Âmbito grande** para um lançamento único | Alto (prazo) | Marcos curtos e verificáveis. O conteúdo (Marcos 2 e 6) é o caminho crítico, porque depende da tua revisão. |

---

## 4. Arquitetura e estrutura do repositório

### 4.1 Módulos Gradle

```
resilia/
├── app/                      # Application, MainActivity, NavHost, Hilt graph, manifest
├── core/
│   ├── domain/               # Kotlin/JVM puro: modelos, casos de uso, interfaces de repositório
│   ├── content/              # Parser do JSON de conteúdo (kotlinx.serialization) — KMP-ready
│   ├── data/                 # Room, DataStore, WorkManager workers, cliente IPMA (Ktor? ver nota), cifra
│   ├── designsystem/         # Tema Normal + Emergência, tipografia, componentes acessíveis
│   └── ui-common/            # Strings partilhadas, utilitários Compose
├── feature/
│   ├── home/
│   ├── guides/               # lista, pesquisa, favoritos, detalhe do cenário
│   ├── emergency/            # modo emergência
│   ├── kit/                  # calculadora + inventário
│   ├── family/               # plano familiar
│   ├── paper/                # geração PDF + impressão
│   ├── tools/                # lanterna, ecrã-luz, SOS, sirene, poupança de bateria
│   ├── alerts/
│   ├── morale/
│   ├── places/
│   └── settings/             # zona, idioma, sobre, licenças, fontes, aviso legal
├── content/                  # FONTE ÚNICA do conteúdo (copiada para assets no build)
├── tools/
│   └── content-validator/    # JVM CLI + testes; corre no CI e no build
├── docs/                     # decisões (ADR), esta proposta, guia de conteúdo
├── .github/workflows/ci.yml
├── README.md · CONTRIBUTING.md · CODE_OF_CONDUCT.md
├── LICENSE (Apache-2.0) · LICENSE-CONTENT (CC BY-SA 4.0)
└── gradle/libs.versions.toml
```

Decisões para KMP:
- `core:domain` e `core:content` sem dependências Android. Usam **kotlinx.serialization** (não Gson nem Moshi), **kotlinx-datetime** e coroutines.
- **Room** e **DataStore** já têm suporte KMP, por isso `core:data` pode migrar.
- **Hilt** fica apenas em `app` e `feature/*`. O domain usa construtores simples, sem anotações de DI.
- Rede: **Ktor client** (KMP) em vez de Retrofit/OkHttp-only. A diferença de tamanho é pequena.
- Cifra atrás de uma interface `SecretBox` no domain. Em Android: AES-GCM com chave no **Android Keystore**, sem bibliotecas (a `security-crypto` foi descontinuada). No futuro, em iOS: Keychain.
- Strings de UI em `strings.xml` (`values/` = en, `values-pt-rPT/` = pt-PT). Em KMP migram para Compose/Moko resources. O conteúdo educativo fica no JSON.

Nome da app: `app_name` em `strings.xml` mais `AppInfo.NAME = "Resilia"` em `core:domain`. Nenhum outro sítio pode ter o nome escrito diretamente (há uma verificação no lint/CI).

### 4.2 Dados do utilizador vs. conteúdo
- **Conteúdo** (só leitura): JSON em `assets/content`, carregado para memória com índice de pesquisa. Não vai para Room. São poucos ficheiros e não há necessidade de base de dados.
- **Dados do utilizador**: Room (inventário, plano, locais, favoritos, kits guardados, cache de alertas). DataStore guarda as preferências.
- `android:allowBackup="false"` e `dataExtractionRules` a excluir tudo (cloud e transferência entre dispositivos).

---

## 5. Modelo de dados (conteúdo JSON)

### 5.1 Convenções
- Texto localizado: `{"pt-PT": "...", "en": "..."}`. O validador exige as duas chaves e que nenhuma esteja vazia.
- IDs em `kebab-case`, únicos por tipo (ex.: `power-outage`, `power-outage.during.03`).
- Datas em ISO-8601 (`2026-11-20`).
- `content/manifest.json` tem `contentVersion` (semver), `schemaVersion` e a lista de ficheiros.
- JSON Schema (draft 2020-12) em `content/schema/`.

### 5.2 Entidades (proposta ajustada à tua secção 6)

**Source**
```json
{
  "id": "anepc-kit-72h",
  "entity": "ANEPC",              // enum: ANEPC|INEM|CVP|DGS|IPMA|EU|SNS24|OTHER
  "official": true,               // derivado da entidade; OTHER => false
  "title": {"pt-PT": "...", "en": "..."},
  "url": "https://...",
  "accessedAt": "2026-11-20",
  "license": "unknown"            // NOVO: para controlar direitos (ver riscos)
}
```

**Scenario**
```json
{
  "id": "power-outage",
  "title": {...}, "summary": {...},
  "icon": "power_off",            // nome de Material Symbol
  "typicalSeverity": "high",      // low|medium|high|critical
  "typicalDuration": {"pt-PT": "horas a dias", "en": "..."},
  "keywords": {"pt-PT": ["apagão","luz"], "en": [...]},   // NOVO: pesquisa
  "phases": {
    "before": [Step], "during": [Step], "after": [Step]
  },
  "emergencyNow": ["power-outage.during.01", "..."],       // NOVO: passos "AGORA" no modo emergência
  "alertTriggers": ["ipma:Vento", "ipma:Precipitação"],    // NOVO: para sugestão automática (Marco 7)
  "relatedScenarioIds": ["water-outage"],
  "sourceIds": ["..."],
  "reviewStatus": "draft", "reviewedAt": null, "reviewedBy": null
}
```

**Step** (com fonte e revisão **por passo**, como pediste)
```json
{
  "id": "power-outage.during.03",
  "text": {...},
  "detail": {...},                // opcional, texto expandido
  "kind": "safety",               // NOVO: safety|health|practical|wellbeing
  "urgency": "now",               // now|soon|later|info
  "relatedKitItemIds": ["torch"],
  "note": {...},                  // opcional
  "sourceIds": ["anepc-..."],
  "reviewStatus": "draft", "reviewedAt": null, "reviewedBy": null
}
```
Regras do validador:
- `kind` ∈ {safety, health} ⇒ `sourceIds` não vazio e pelo menos uma fonte `official: true`.
- `reviewStatus: reviewed` ⇒ `reviewedAt` e `reviewedBy` preenchidos.
- O estado de um cenário é o **pior** estado dos seus passos (calculado, não escrito à mão).

**KitItem**
```json
{
  "id": "water-drinking",
  "category": "water",            // water|food|light|power|health|hygiene|docs|comms|tools|children|pets|other
  "name": {...},
  "unit": "L",                    // L|unit|pack|kg|...
  "perAdultPerDay": 0,            // quantidades SEMPRE ligadas a sourceIds; valores ainda não definidos
  "perChildPerDay": 0,
  "perPetPerDay": 0,
  "perHousehold": 0,              // itens fixos (rádio, lanterna)
  "typicalShelfLifeMonths": null,
  "essential": true,
  "notes": {...},
  "sourceIds": [...],
  "reviewStatus": "draft", ...
}
```
A calculadora (caso de uso no domain, puro e testável) recebe `{adultos, crianças, animais, dias}` e devolve uma lista com quantidades arredondadas para cima. Nenhum número entra sem fonte.

**Activity**
```json
{
  "id": "stone-tic-tac-toe",
  "type": "game",                 // game|story|song|calm|challenge
  "title": {...},
  "ageMin": 4, "ageMax": 99,
  "peopleMin": 2, "peopleMax": 2,
  "materials": ["stones","paper"],   // vocabulário controlado em materials.json
  "durationMin": 10,
  "setting": "both",              // indoor|outdoor|both
  "noise": "quiet",               // NOVO: quiet|moderate|loud (útil à noite / prédio)
  "needsLight": false,            // NOVO: dá para fazer às escuras?
  "instructions": {...},          // passos numerados (array localizado)
  "variants": {...},              // "como inventar variantes"
  "origin": "original",           // original|public-domain|traditional
  "originNote": {...},            // fonte se domínio público
  "reviewStatus": "draft", ...
}
```
Para atividades "calma" com conteúdo de saúde mental (ex.: falar com crianças sobre o que está a acontecer), aplica-se a mesma regra de fontes oficiais.

**Outros ficheiros**
- `contacts/national.json`: números nacionais (112, SNS 24, etc.). **Cada número com fonte oficial.** Não escrevo nenhum número sem fonte verificada no Marco 2.
- `tools/battery-saving.json`: checklist de poupança de bateria (estrutura igual à de Step).
- `geo/districts.json`, `geo/municipalities.json`: distritos e concelhos (fonte: CAOP/DGT, a confirmar). Inclui o mapeamento para as áreas de aviso do IPMA (`idAreaAviso`).
- `materials.json`: vocabulário de materiais com nomes localizados.

### 5.3 Dados do utilizador (Room)
| Tabela | Campos principais |
|---|---|
| `inventory_item` | id, kitItemId?, name, quantity, unit, location, expiresAt?, notes, updatedAt |
| `saved_kit` | id, label, adults, children, pets, days, createdAt (o resultado é recalculado) |
| `family_member` | id, name, relation, phone, notes, **medical (cifrado)** |
| `place` | id, type (home/meeting/hospital/pharmacy/firefighters/other), name, address, notes, lat?/lng? |
| `favorite` | contentId, type, addedAt |
| `alert_cache` | source, fetchedAt, payload, httpStatus |
| `checklist_state` | scenarioId, stepId, checkedAt (checklist do modo emergência) |

DataStore: zona (distrito/concelho), idioma (sistema/pt-PT/en), modo emergência ativo e cenário ativo, mostrar drafts, antecedência dos lembretes, data da última revisão semestral, frequência dos alertas.

---

## 6. Mapa de ecrãs

### Modo Normal — barra inferior com 4 destinos
```
Início ─────────────────────────────────────────────
│ [ATIVAR MODO EMERGÊNCIA]  (sempre visível no topo)
│ Faixa de alerta (se houver) · fonte · hora · "desatualizado"
│ Grelha de cenários  ──► Cenário (1 toque)
│ Atalhos: Lanterna · Cartão de papel · Contactos
│ Aviso "Não substitui o 112" (rodapé fixo, discreto)
│
Guias ───────────────────────────────────────────────
│ Pesquisa · Favoritos · Lista de cenários
│   └ Cenário: separadores Antes | Durante | Depois
│       └ Passo (expandir detalhe, fonte, data de revisão, "por rever")
│
Preparar ────────────────────────────────────────────
│ Calculadora de kit ──► Resultado ──► [Guardar] [Adicionar ao inventário]
│ Inventário (por validade / local / categoria) ──► Item
│ Plano familiar: Membros · Pontos de encontro · Serviços · Ficha médica
│ Papel: Cartão A4 · Cartão de carteira · Caderno de guias ──► Pré-visualizar / Imprimir / Partilhar PDF
│ Locais (Marco 8)
│
Calma ───────────────────────────────────────────────
│ Filtros: idade · pessoas · materiais · duração · interior/exterior · silencioso · às escuras
│ Jogos · Histórias e cantigas · Calma · Desafios ──► Atividade
│
Menu (ícone no topo) ────────────────────────────────
  Ferramentas: Lanterna · Ecrã-luz · SOS luz · Sirene · Poupança de bateria
  Alertas: zona, último estado, fonte, hora
  Definições · Fontes · Sobre e licenças · Aviso legal
```
Ferramentas: proponho que fiquem **também** na barra inferior, como 5.º destino, porque são usadas sob stress. Queres 4 ou 5 destinos?

Regra dos 2 toques: qualquer guia está a 1 toque a partir do Início (grelha) e a 2 a partir de qualquer outro separador (Guias → cenário).

### Modo Emergência — ecrã único, fundo preto
```
┌────────────────────────────────────┐
│ ⚠ Falha de eletricidade   [mudar]  │
│ AGORA                              │
│ 1. ...  (texto grande, numerado)   │
│ 2. ...                             │
│ [Checklist ▸]                      │
│ ┌──────────┐ ┌──────────┐          │
│ │LANTERNA  │ │  SOS     │          │
│ └──────────┘ └──────────┘          │
│ ┌──────────┐ ┌──────────┐          │
│ │LIGAR 112 │ │CONTACTOS │          │
│ └──────────┘ └──────────┘          │
│ [CARTÃO DE PAPEL]                  │
│ Sair do modo emergência            │
└────────────────────────────────────┘
```
- Botões ≥ 72 dp, sem animações, sem imagens, fundo `#000000` (bom para ecrãs OLED).
- "Ligar 112" abre o marcador (`ACTION_DIAL`) já preenchido. A pessoa confirma a chamada.
- O estado persiste: se a app reabrir, volta ao modo emergência.
- Sugestão automática (Marco 7): notificação "Aviso [nível] para [zona]. Ativar modo emergência?". Só ativa com confirmação.

---

## 7. Ordem dos marcos (com ajustes propostos)

| Marco | Conteúdo | Ajuste que proponho |
|---|---|---|
| 1 | Scaffold, módulos, tema Normal/Emergência, navegação, esqueleto dos dois modos, CI (lint, testes, validador), licenças, README/CONTRIBUTING base | Incluir já o **validador de conteúdo + schemas** e um cenário de exemplo mínimo, para o CI ter algo a validar. |
| 2 | Motor de conteúdo (carregamento, pesquisa, favoritos, draft/reviewed) + falha de eletricidade completa + contactos nacionais com fontes | Começo por uma **recolha de fontes** (lista de URLs oficiais para validares) antes de redigir os passos. |
| 3 | Calculadora de kit, inventário, lembretes de validade e revisão semestral | — |
| 4 | Plano familiar (com cifra) + PDF (A4, carteira, caderno) | — |
| 5 | Ferramentas + guia de poupança de bateria | Pode trocar com o 4 sem impacto. |
| 6 | Restantes cenários + módulo Calma | Dividir em **6a** (cenários) e **6b** (moral), porque cada um tem muito conteúdo para rever. |
| 7 | Alertas IPMA + sugestão de modo emergência | Antes de começar: confirmar os termos do IPMA (ver §8). |
| 8 | Locais manuais + lista por concelho; mapa offline após aprovação | Proposta de mapa entregue como ADR no início do marco. |
| 9 | Acessibilidade, otimização, docs, publicação | A acessibilidade é testada em **todos** os marcos. O 9 é a auditoria final. |

Em cada marco reporto: tamanho da APK (release, via `apkanalyzer`), consumo de bateria (Android Studio Power Profiler + `dumpsys batterystats` em cenário definido: 1 h de modo emergência com o ecrã ligado e 24 h em repouso com alertas ativos), testes que passaram e o que tens de validar.

---

## 8. Fontes de alertas — o que verifiquei até agora

- **IPMA (avisos meteorológicos)**: existe um ficheiro JSON público em `https://api.ipma.pt/open-data/forecast/warnings/warnings_www.json`, muito usado por projetos de terceiros. Campos observados nesses projetos: `awarenessLevelID`, `awarenessTypeName`, `idAreaAviso`, `startTime`, `endTime`, `text`. **Não encontrei documentação oficial do esquema nem os termos de uso** nas páginas que consultei. Antes do Marco 7 confirmo isto na página de dados abertos do IPMA e, se for preciso, peço-te para contactar o IPMA. Nota: as áreas de aviso são **por distrito/região**, não por concelho.
- **Incêndios / ANEPC**: **não encontrei uma API pública oficial da ANEPC.** O fogos.pt usa dados da ANEPC, mas anunciou que a API vai passar a exigir **autorização e token individual**. Numa app open source sem backend, um token embutido ficaria público, por isso não é uma solução adequada. Alternativas:
  1. usar só os avisos IPMA (e o risco de incêndio rural do IPMA, se existir em dados abertos — por verificar);
  2. ligação externa para a página de avisos à população da ANEPC (prociv.gov.pt);
  3. pedir à ANEPC um feed público (contacto institucional), o que fica fora do âmbito técnico.
  Proponho 1 + 2 na v1.
- **Avisos à população da ANEPC**: existe uma página pública. Ainda não verifiquei se tem RSS ou outro formato legível por máquina. Fica para o Marco 7.

Fontes consultadas:
- [IPMA — Avisos meteorológicos (guia)](https://www.ipma.pt/pt/enciclopedia/otempo/sam/index.html)
- [Exemplo de uso do endpoint IPMA por terceiros](https://glama.ai/mcp/servers/@gabriel20vieira/ipma-mcp-server/tools/get_weather_warnings)
- [Fogos.pt — regras de acesso à API](https://fogos.pt/en/api)
- [Fogos.pt — informações sobre os dados](https://fogos.pt/en/informacoes)
- [dados.gov.pt — "Ocorrências Portugal - ANEPC" (reutilização, não API)](https://dados.gov.pt/en/reuses/ocorrencias-portugal-anepc)
- [ANEPC — Avisos à população](https://prociv.gov.pt/en/warnings-to-the-population/)

---

## 9. Mapa offline (só para referência; decisão no Marco 8)
Abordagem provável: **MapLibre Native** + tiles vetoriais OSM (formato PMTiles/MBTiles) descarregáveis por distrito, com atribuição "© OpenStreetMap contributors" (ODbL). Trade-offs a quantificar: tamanho por distrito (estimativa a medir, não a adivinhar), alojamento dos ficheiros sem backend próprio (GitHub Releases tem limites) e aumento da APK com a biblioteca MapLibre (vários MB por ABI). Alternativa leve: sem mapa, só moradas e coordenadas com "abrir na app de mapas".

---

## 10. O que preciso de ti
1. Respostas às dúvidas D1–D12 (§2), mais a dos 4 ou 5 destinos (§6) e a da exportação/importação manual (§3).
2. Aprovação, ou correções, ao modelo de dados (§5).
3. Aprovação do mapa de ecrãs (§6) e da ordem dos marcos (§7).
