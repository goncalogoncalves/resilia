# Marco 7 — Avisos IPMA: fontes e propostas para validar

> Data de consulta: 2026-11-21 · Estado: **aprovado a 2026-11-21** (fontes, propostas A da §3, decisões técnicas da §4)

Neste marco a app passa a usar a rede pela primeira vez, mas só para uma coisa: ler os avisos meteorológicos e o risco de incêndio que o IPMA publica em dados abertos. Não tem conta nem servidor próprio, e não envia nada sobre a pessoa.

---

## 1. Fontes verificadas

| ID proposto | Entidade | Documento | Usar para |
|---|---|---|---|
| `ipma-api` | IPMA | [IPMA API (documentação e condições de utilização)](https://api.ipma.pt/) | Endpoints, campos, códigos e condições de uso |
| `ipma-guia-avisos` | IPMA | [Guia de utilização dos avisos meteorológicos](https://www.ipma.pt/pt/enciclopedia/otempo/sam/index.html) | O que significa cada cor; a escala é distrital; consultar a ANEPC e, no frio e no calor, a DGS |
| `ipma-avisos` | IPMA | [Avisos meteorológicos](https://www.ipma.pt/pt/otempo/prev-sam/) | Nomes oficiais das 25 áreas de aviso; "toda a informação em horas UTC"; ligação "ver no IPMA" |
| `ipma-risco-incendio` | IPMA | [Risco de incêndio (página do IPMA)](https://www.ipma.pt/pt/riscoincendio/rcm.pt/) | Lista dos 278 concelhos do continente (código DICO e nome) |
| `anepc-avisos-populacao` | ANEPC | [Avisos à população](https://prociv.gov.pt/pt/avisos-a-populacao/) | Só uma ligação externa (ver §2) |

### Endpoints documentados pelo IPMA

Li-os na página da API e descarreguei-os hoje para confirmar o formato:

| Endpoint | O que tem | O que confirmei hoje |
|---|---|---|
| `https://api.ipma.pt/open-data/forecast/warnings/warnings_www.json` | Avisos até 3 dias. Campos: `idAreaAviso`, `awarenessTypeName`, `awarenessLevelID` (green, yellow, orange, red), `startTime`, `endTime` e `text` (só preenchido quando o aviso não é verde) | 204 entradas, 25 áreas, 8 tipos. Havia 4 avisos amarelos de precipitação (Porto, Vila Real, Viana do Castelo e Braga). 35 KB. |
| `https://api.ipma.pt/open-data/forecast/meteorology/rcm/rcm-d{0,1,2}.json` | Risco de incêndio para hoje, amanhã e depois de amanhã, por concelho (DICO). Códigos: 1 reduzido, 2 moderado, 3 elevado, 4 muito elevado, 5 máximo. | 278 concelhos, **só do continente**. 24 KB. |
| `https://api.ipma.pt/open-data/distrits-islands.json` | Liga cada capital de distrito e cada ilha à sua `idAreaAviso` | Não tem as áreas `MCN` (Madeira, Costa Norte) nem `MRM` (Madeira, Regiões Montanhosas). Os nomes vêm da página dos avisos. |

Segundo a documentação, os avisos e o risco de incêndio atualizam-se de hora a hora.

### As 25 áreas de aviso (nomes da página do IPMA)

- **Continente (18):** Aveiro, Beja, Braga, Bragança, Castelo Branco, Coimbra, Évora, Faro, Guarda, Leiria, Lisboa, Portalegre, Porto, Santarém, Setúbal, Viana do Castelo, Vila Real e Viseu.
- **Madeira (4):** Costa Norte, Regiões Montanhosas, Costa Sul e Porto Santo.
- **Açores (3):** Grupo Ocidental, Grupo Central e Grupo Oriental.

### Condições de utilização do IPMA (página da API)

- O IPMA pede que se leiam as condições de utilização e que se **envie um email para webmaster@ipma.pt** a explicar o uso e o propósito, "para efeitos de estatística e melhoria do serviço".
- A informação pode ser usada gratuitamente, para uso pessoal ou público, **sem fins lucrativos**, e "deve referir, sempre, a fonte de informação". A Resilia é gratuita e sem anúncios.
- O IPMA não garante que o serviço esteja sempre disponível e não se responsabiliza por danos.
- Há uma frase sobre usar o **logótipo do IPMA** quando se inclui a informação em "páginas de hiper-documentos", com ligação ao site. Foi escrita para sites e não é claro se se aplica a uma app (ver decisão 6).

---

## 2. O que procurei e **não** encontrei

| Pergunta | Resultado |
|---|---|
| Risco de incêndio para a **Madeira** e os **Açores** em dados abertos | O endpoint só tem o continente. O IPMA tem uma página "Perigo de Incêndio Madeira", mas não está na API. **Nas ilhas não mostro o risco de incêndio**; mostro uma ligação para o site do IPMA. |
| Avisos à população da ANEPC num formato que a app consiga ler (RSS, API) | Não há RSS nem API na página. Hoje há lá um aviso ("Chuva persistente"), mas ler o HTML seria frágil. **Proponho só uma ligação.** |
| Fuso horário de `startTime` e `endTime` | Os campos não trazem o fuso. A página dos avisos diz "Toda a Informação em horas UTC" e a documentação diz que as previsões estão em UTC. Hoje não dá para confirmar, porque em novembro a hora de Lisboa é igual à UTC. **Proponho tratar como UTC e mostrar a hora local**, e voltar a confirmar no horário de verão (fica "por confirmar"). |
| Explicações oficiais das cores em inglês | Não as procurei. As traduções seriam minhas e ficam "Por rever". O texto do campo `text` do aviso só existe em português. |
| Lista oficial de concelhos com o código DICO (CAOP, da DGT) | Não precisei: a página do IPMA tem os mesmos 278 códigos do endpoint, com nome e distrito. |

---

## 3. Decisões que preciso que tomes

**1. Como se escolhe a zona**
- **A (proponho):** a pessoa escolhe a **área de aviso** numa lista com as 25. No continente pode escolher também o **concelho**, que serve para o risco de incêndio. Não pede localização.
- B: usar o GPS para descobrir a área. Isto obriga a pedir uma permissão nova e a guardar dados de localização.

**2. Com que frequência verifica**
- **A (proponho):** de 3 em 3 horas por omissão, com opções de 1, 3 ou 6 horas e "desligado". Só verifica quando há rede e a pessoa escolheu uma zona. Usa o WorkManager, sem *wakelocks*. Ao abrir a app atualiza logo se os dados tiverem mais de 1 hora.
- B: só atualizar quando a app é aberta. Gasta menos bateria, mas deixa de haver notificações.

**3. Quando notifica**
- **A (proponho):** notifica para avisos **laranja e vermelhos** e para risco de incêndio **muito elevado ou máximo**. Os amarelos ficam nas definições, desligados por omissão, e aparecem sempre na faixa da página inicial. A mesma combinação de aviso, área, nível e início só notifica uma vez.
- B: notificar também os amarelos por omissão. São frequentes; hoje havia quatro.

**4. Sugerir o modo emergência**
- **A (proponho):** a notificação de um aviso laranja ou vermelho diz "Aviso [nível] de [tipo] para [área]" e tem dois botões: "Ver o que fazer", que abre o cenário certo, e "Modo emergência". O modo emergência **só ativa depois de a pessoa confirmar** dentro da app, como está no Marco 0. Nunca ativa sozinho.
- B: só "Ver o que fazer", sem sugerir o modo emergência.

**5. Que cenário cada aviso sugere (`alertTriggers`)**

É uma decisão de produto, não um facto. Proponho:

| Aviso IPMA | Cenário sugerido | Porquê |
|---|---|---|
| Vento | Tempestade | O cenário vem do aviso da ANEPC para a depressão Charles, que fala de vento forte |
| Precipitação | Cheias | Também é o que o cenário da tempestade cobre; sugiro as cheias porque são o risco principal da chuva forte |
| Trovoada | Tempestade | |
| Agitação Marítima | Tempestade | O cenário já tem "Afasta-te de praias, arribas, molhes…" |
| Tempo Quente | Calor | O guia do IPMA remete para a DGS no frio e no calor |
| Tempo Frio | Frio | Idem |
| Neve | Frio | **Por confirmar.** O cenário do frio não fala de neve. A alternativa é não sugerir nenhum cenário. |
| Nevoeiro | Nenhum | Não há cenário |
| Risco de incêndio 4 ou 5 | Incêndio rural | |

**6. Como indicar a fonte**
- **A (proponho):** cada aviso mostra "Fonte: IPMA", a hora a que o IPMA o atualizou, a hora a que a app o descarregou e a ligação "Ver no IPMA". Não uso o logótipo até o IPMA responder ao email (decisão 7).
- B: pôr já o logótipo do IPMA. O logótipo é uma marca e precisa de autorização.

**7. Email ao IPMA:** o IPMA pede que lhe digam que se usa a API. **Tens de ser tu a enviá-lo.** Deixo um rascunho na §5, com a pergunta sobre o logótipo e a pergunta sobre o fuso horário.

**8. Dados desatualizados ou em falta**
- **A (proponho):**
  - Com mais de 6 horas, os avisos aparecem marcados "Desatualizado (há X horas)".
  - Sem rede ou com erro, aparece "Não foi possível obter os avisos" com a hora da última leitura que correu bem.
  - Está sempre escrito: "A ausência de aviso na app não significa que não haja perigo. Confirma no IPMA e segue as indicações da Proteção Civil."
- Também aparece a ligação para os Avisos à população da ANEPC.

---

## 4. Decisões técnicas para aprovares

1. **Permissão `INTERNET`** nova, que o Marco 0 já previa. Só HTTPS, com `network_security_config` a bloquear tráfego sem cifra.
2. **Sem bibliotecas de rede novas:** `HttpURLConnection` + kotlinx.serialization, que já usamos. O Marco 0 falava em Ktor, mas para três pedidos GET acrescentava tamanho sem necessidade. Funciona sem Google Play Services, o que permite o F-Droid.
3. **Privacidade:** a app descarrega o **ficheiro nacional inteiro** e filtra a zona no telemóvel. O IPMA nunca fica a saber a zona da pessoa. Os pedidos não levam identificadores; o User-Agent é só "Resilia/<versão>".
4. **Cache:** guarda só a última resposta que correu bem de cada endpoint (tabela `alert_cache` no Room) e o registo do que já foi notificado. Nada vai para os backups na cloud.
5. **Leitura tolerante:** campos desconhecidos são ignorados; um nível ou tipo desconhecido aparece como "Aviso do IPMA" sem cor. Testes com respostas reais guardadas hoje.
6. **Ficheiro de conteúdo `content/alerts/areas.json`:**
   - as 25 áreas com o nome oficial e a região;
   - os 278 concelhos do continente (DICO, nome, área de aviso);
   - os nomes dos 8 tipos de aviso em pt-PT e en;
   - as explicações das cores, parafraseadas do guia do IPMA com a fonte.

   A ligação concelho → área de aviso vem do `idDistrito` em `distrits-islands.json`. O validador confirma os códigos.
7. **Ecrãs:**
   - faixa na página inicial quando há aviso amarelo ou acima na zona;
   - ecrã "Avisos" com a lista, o risco de incêndio para 3 dias, a fonte e as horas;
   - escolha da zona e da frequência nas Definições.
8. **Canal de notificação próprio** ("Avisos meteorológicos"), separado do canal dos lembretes do kit, para a pessoa os poder silenciar à parte.

### Explicação das cores (paráfrase do guia do IPMA, para aprovares)

| Cor | pt-PT | en (tradução minha, Por rever) |
|---|---|---|
| Verde | Não se prevê tempo de risco. | No hazardous weather expected. |
| Amarelo | Risco para algumas atividades que dependem do tempo. Acompanha a evolução do tempo. | Risk for some weather-dependent activities. Keep an eye on the weather. |
| Laranja | Risco moderado a elevado. Mantém-te informado e segue as indicações da Proteção Civil. | Moderate to high risk. Stay informed and follow Civil Protection advice. |
| Vermelho | Risco extremo. Mantém-te informado com frequência e segue as indicações da Proteção Civil. | Extreme risk. Check for updates often and follow Civil Protection advice. |

---

## 5. Rascunho do email para o IPMA (para enviares tu)

> **Para:** webmaster@ipma.pt
> **Assunto:** Utilização da API de dados abertos — app Resilia
>
> Bom dia,
>
> Estou a desenvolver a Resilia, uma app Android gratuita, sem anúncios e de código aberto, de preparação para emergências em Portugal. Seguindo o pedido na página da API, informo que a app vai usar:
> - warnings_www.json (avisos meteorológicos);
> - rcm-d0/d1/d2.json (risco de incêndio);
> - distrits-islands.json.
>
> Cada telemóvel faz no máximo um pedido por hora (3 em 3 horas por omissão). A app indica sempre o IPMA como fonte e tem ligação para o site.
>
> Tenho duas dúvidas:
> 1. Numa app, devemos mostrar o logótipo do IPMA junto aos avisos? Se sim, há uma versão e regras de utilização?
> 2. Os campos startTime e endTime de warnings_www.json estão em UTC também no horário de verão?
>
> Obrigado,
> [nome]

---

## 6. O que fica fora deste marco

- Avisos à população da ANEPC dentro da app: só se a ANEPC publicar um formato legível.
- Sismos: o IPMA tem um endpoint de sismos recentes. Não o proponho, porque um sismo não se avisa antes e a lista não ajuda a agir. Pode entrar mais tarde, se quiseres.
- Notificações *push*: sem servidor não há; a app verifica sozinha.
