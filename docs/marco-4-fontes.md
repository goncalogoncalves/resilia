# Marco 4 — Fontes e propostas para validar

> Data de consulta: 2026-11-21 · Estado: **aprovado a 2026-11-21** (fontes, §4, §5 e §6, incluindo o bloqueio opcional da ficha médica)

O Marco 4 (proposta, §7) tem duas partes:

- **Plano familiar:** membros, pontos de encontro, contactos e ficha médica cifrada;
- **Papel:** PDF em A4, cartão de carteira e caderno de guias.

Este documento lista:

- as fontes sobre **o que deve ter um plano familiar**;
- as lacunas;
- as decisões técnicas que preciso que aproves antes de escrever código.

A ficha médica é escrita por cada pessoa e a app não dá conselhos médicos, por isso não precisa de fonte. Os **campos** e as **sugestões** do plano precisam.

---

## 1. Fontes propostas

| ID proposto | Entidade | Documento | Data | Usar para |
|---|---|---|---|---|
| `anepc-sismos-kit` | ANEPC + APS | (aprovada no Marco 2) | Nov. 2018 | Combinar um ponto de encontro; ensinar todos a desligar a eletricidade, a água e o gás; números de emergência à mão |
| `cvp-kit-familiar` | Cruz Vermelha Portuguesa | (aprovada no Marco 3) | 2024 | Plano de comunicação; ponto de encontro fora de casa e, se for preciso, fora do bairro; rotas de evacuação; responsabilidades de cada pessoa; exercícios regulares; cópias dos documentos |
| `eu-swd-72h` | Comissão Europeia | (aprovada no Marco 3) | 2026-10-30 | Plano familiar com o abrigo mais próximo e com a forma de contactar família e amigos sem rede móvel |
| `cm-matosinhos-plano` | Câmara Municipal de Matosinhos (Proteção Civil) | [Plano Familiar de Emergência](https://www.cm-matosinhos.pt/servicos/protecao-civil/prevencao-e-autoprotecao/medidas-de-autoprotecao/plano-familiar-de-emergencia) | Atualizado a 2026-10-08 | **Dois pontos de encontro** (perto de casa e fora da zona de residência); uma pessoa de contacto que todos conhecem (nome, morada, telefone); lista de contactos junto ao telefone; garantir que todos sabem ligar para o 112 |
| `cm-valongo-plano` | Câmara Municipal de Valongo (Proteção Civil) | [Plano de Emergência Familiar: um passo simples que pode salvar vidas](https://www.cm-valongo.pt/diretorio-servicos/comunicacao/noticias/noticia/plano-de-emergencia-familiar-um-passo-simples-que-pode-salvar-vidas) | 2026-01-23 | Saídas da casa; **responsabilidades** (quem ajuda crianças, idosos e animais, quem leva o kit, quem liga para o 112); contactos de família e vizinhos; explicar a todos, incluindo as crianças; **rever quando a família ou a casa mudam** |

---

## 2. O que cada fonte diz (resumo para conferires)

### ANEPC: folheto sobre sismos (2018)
- Combinar antes um ponto de encontro para a família.
- Ensinar todos a desligar a eletricidade, a água e o gás.
- Ter os números de emergência à mão.

### Cruz Vermelha Portuguesa: manual do kit (2024), secção "Plano de Emergência Familiar"
- **Comunicação:** um plano para todos saberem como se contactar numa emergência.
- **Ponto de encontro:** fora de casa e, se for preciso, fora do bairro.
- **Rotas de evacuação:** identificar e praticar com cada pessoa da casa, com rotas alternativas.
- **Responsabilidades:** dar uma tarefa a cada pessoa.
- **Prática:** fazer exercícios de simulação regularmente (sem intervalo definido).
- **Recursos locais:** conhecer os abrigos e os postos de saúde da zona.

### Comissão Europeia: orientações das 72 h (2026)
- O plano familiar inclui o **abrigo mais próximo** e **como contactar família e amigos se a rede móvel falhar**.
- Dá como exemplo um modelo de plano para imprimir (Centro de Crise da Bélgica).

### Câmara de Matosinhos: Plano Familiar de Emergência (2026)
- **Dois pontos de encontro:** um fora de casa, a uma distância segura; outro fora da zona de residência, para quando não for possível voltar a casa.
- **Pessoa de contacto:** se a família se separar, todos devem saber o nome, a morada e o telefone de um familiar ou amigo que sirva de ponto de contacto.
- **Contactos visíveis** junto ao telefone: 112, bombeiros, polícia, centro de saúde, CIAV, escola dos filhos, médico de família, seguradora e proteção civil municipal.
- Garantir que todos sabem usar o 112.
- Ensinar todos, incluindo as crianças, a desligar o gás, a eletricidade e a água.

### Câmara de Valongo: Plano de Emergência Familiar (2026)
- Identificar as saídas seguras da casa e garantir que todos sabem sair.
- Dois pontos de encontro: um perto de casa e outro fora da zona de risco.
- **Responsabilidades:** quem ajuda crianças, idosos ou animais; quem leva o kit; quem liga para os serviços de emergência.
- **Contactos:** 112, família, vizinhos ou apoio local.
- Explicar o plano a todos, incluindo as crianças, e testá-lo "periodicamente".
- **Rever sempre que a família ou a casa mudarem.**

---

## 3. O que procurei e **não** uso

| Fonte | Porque não uso |
|---|---|
| Página nacional da ANEPC sobre o plano familiar | Não existe. Procurei em prociv.gov.pt e só encontrei a secção do folheto de sismos. |
| Manual da Câmara de Albufeira (2012) | É de 2012 e foi feito num estágio curricular. Prefiro fontes atuais. |
| Guia de Évora (2018) e outras câmaras | Os resultados não permitiam confirmar o texto completo. |
| Modelo belga citado pela UE | É de outro país e tem números e serviços belgas. |

---

## 4. Lacunas e decisões que preciso que tomes

1. **Câmaras municipais como fonte.** As duas fontes mais concretas sobre o plano familiar são serviços municipais de proteção civil, que são autoridades de proteção civil, e a ANEPC não tem uma página própria sobre o tema.
   - **Proposta:** criar a entidade `MUNICIPAL`, que conta como **oficial**, com o nome da câmara em `publisher`. Uso-a só para o plano familiar, onde concorda com as fontes nacionais (ANEPC, Cruz Vermelha, UE).
2. **Revisão do plano.** Nenhuma fonte dá um intervalo. Valongo diz "rever quando a família ou a casa mudarem"; a Cruz Vermelha diz "exercícios regularmente".
   - **Proposta:** juntar o plano à **revisão semestral do kit** que já existe, com o texto "Revê também o plano familiar" e as duas fontes. Acrescento também a nota "Revê o plano sempre que a família ou a casa mudarem" (Valongo). Não invento outro intervalo.
3. **Abrigo mais próximo (UE).** A app ainda não tem locais; vêm no Marco 8.
   - **Proposta:** um campo de texto livre, "Zona de concentração ou abrigo indicado pela tua câmara (se souberes)", com uma nota a sugerir que perguntes ao serviço municipal de proteção civil.
4. **Contactos sem rede móvel (UE).** Nenhuma fonte diz como fazer.
   - **Proposta:** usar o que as fontes dizem: a pessoa de contacto (Matosinhos) e os pontos de encontro. O cartão de papel garante que os números existem fora do telemóvel. Não acrescento conselhos técnicos (SMS, rádio, etc.) sem fonte.

---

## 5. Como fica o plano familiar (proposta)

Tudo é opcional e em texto livre (D12). Cada secção indica a sua fonte.

| Secção | Campos | Fonte |
|---|---|---|
| **Pessoas da casa** | Nome, relação, telefone, notas. **Ficha médica** (cifrada): grupo sanguíneo, alergias, medicação, condições relevantes, contacto do médico | D12 (proposta aprovada) |
| **Pontos de encontro** | 1) Perto de casa · 2) Fora da zona de residência | ANEPC, Cruz Vermelha, Matosinhos, Valongo |
| **Pessoa de contacto** | Nome, telefone, morada. "Se a família se separar, todos ligam a esta pessoa." | Matosinhos, UE |
| **Responsabilidades** | Quem ajuda crianças, idosos ou animais · Quem leva o kit · Quem liga para o 112 | Valongo, Cruz Vermelha |
| **Como sair de casa** | Saída principal · Saída alternativa | Cruz Vermelha, Valongo |
| **Desligar em casa** | Onde se desliga a eletricidade · a água · o gás | ANEPC, Matosinhos |
| **Contactos úteis** | 112 e os contactos nacionais com fonte (Marco 2). A pessoa pode acrescentar escola, médico de família, seguradora, vizinhos e proteção civil municipal | Matosinhos, Valongo |
| **Abrigo** | Texto livre (ver §4.3) | UE |

---

## 6. Decisões técnicas para aprovares

1. **Cifra da ficha médica:**
   - os campos médicos ficam cifrados (AES-256-GCM) com uma chave guardada no **Android Keystore**, que não sai do telemóvel e não se pode exportar;
   - os outros campos ficam na base de dados `resilia.db`, como o inventário, fora dos backups;
   - **limite, a dizer com clareza:** isto protege os ficheiros se alguém os copiar do telemóvel, mas **não** protege de quem tem o telemóvel desbloqueado na mão;
   - alternativa: cifrar a base de dados inteira com SQLCipher. Acrescenta cerca de 3 a 4 MB à APK e uma biblioteca nativa. Não a proponho.
2. **Bloqueio da ficha médica (opcional, desligado por defeito):** pedir o PIN, padrão ou impressão digital do telemóvel antes de mostrar a ficha (androidx.biometric).
   - Não pede permissões em tempo de execução; só declara `USE_BIOMETRIC`, que o Android concede sem perguntar.
   - **Queres isto já, ou fica para depois?**
3. **PDF sem bibliotecas externas:**
   - gerado com `PdfDocument` do Android e a letra Atkinson Hyperlegible, que já vem na app;
   - **Imprimir** usa o serviço de impressão do Android;
   - **Partilhar** usa um ficheiro temporário via `FileProvider`;
   - **Guardar** deixa a pessoa escolher a pasta.
   - Nenhum destes caminhos pede permissões de armazenamento.
4. **Três documentos:**
   - **Cartão A4:** o plano completo (pessoas, pontos de encontro, pessoa de contacto, responsabilidades, saídas, desligar em casa, contactos úteis com fonte) e a lista do kit da calculadora;
   - **Cartão de carteira:** tamanho de cartão bancário (85,6 × 54 mm), frente e verso lado a lado numa folha A4, com linhas de corte e de dobra. Leva 112, a pessoa de contacto, os pontos de encontro e os 2 ou 3 contactos que a pessoa escolher;
   - **Caderno de guias:** todos os guias (antes, durante, depois), com a fonte de cada passo, "Por rever" quando for o caso e a data de geração.
   - **Todos os documentos** levam o aviso de responsabilidade e "Em emergência, liga 112".
5. **Dados médicos no papel:** **desligados por defeito.** Escolhe-se por pessoa, com um aviso: "Quem encontrar este papel pode ler estes dados."
6. **Atalhos já existentes que passam a funcionar:**
   - "Cartão de emergência em papel" no Início;
   - "Cartão em papel" no Preparar;
   - o botão do modo emergência (proposta, §6).
