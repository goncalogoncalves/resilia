# Marco 5 — Fontes e propostas para validar

> Data de consulta: 2026-11-21 · Estado: **aprovado a 2026-11-21** (fontes, propostas A da §3, guia da §4 e decisões da §5)

O Marco 5 (proposta, §7) tem duas partes:

- **Ferramentas:** ecrã como luz, sinal SOS com luz, apito/sirene e poupança de bateria. A lanterna já existe desde o Marco 1.
- **Guia "Fazer a bateria durar".**

Antes de escrever código, preciso que decidas sobre:

- duas ferramentas para as quais **não encontrei fonte oficial** (SOS com luz e apito);
- os passos do guia.

---

## 1. Fontes propostas

| ID proposto | Entidade | Documento | Data | Usar para |
|---|---|---|---|---|
| `anepc-charles-2026` | ANEPC | [Depressão Charles: medidas preventivas](https://prociv.gov.pt/pt/noticias/depressao-charles-medidas-preventivas/) | 2026-11-10 | "Assegure que dispõe de meios alternativos de iluminação e de equipamentos de comunicação devidamente carregados" |
| `anepc-serra-estrela-2026` | ANEPC | [Dispositivo Serra da Estrela](https://prociv.gov.pt/pt/noticias/23022026-dispositivo-serra-da-estrela/) | 2026-02-23 | "Tenha a bateria do seu telemóvel carregada" (conselho para atividades de montanha) |
| `anepc-riscos-tecnologicos` | ANEPC | (aprovada no Marco 2) | © 2023 | "Use o telefone só em caso de emergência" e "para não sobrecarregar as linhas" (dito para cheias de barragens e emergências radiológicas) |
| `anepc-sismos-kit` | ANEPC + APS | (aprovada no Marco 2) | Nov. 2018 | Ter no kit rádio, lanterna, **apito** e **powerbank**. Depois de um sismo, não usar o telefone e preferir SMS ou redes sociais. |
| `eu-swd-72h` | Comissão Europeia | (aprovada no Marco 3) | 2026-10-30 | Rádio a pilhas e powerbanks carregados |
| `dgs-quebras-energeticas` | DGS | (aprovada no Marco 2) | — | Lanterna e rádio a pilhas, com pilhas extra; lanternas em vez de velas |
| `google-android-bateria` | Google (Ajuda do Android) | [Get the most life from your Android device's battery](https://support.google.com/android/answer/7664692?hl=en-GB) | Sem data | **Só definições do telemóvel:** poupança de bateria, brilho, modo de avião, Bluetooth e localização. Não é conselho de segurança. |
| `w3c-wcag22-flashes` | W3C (norma técnica) | [WCAG 2.2, critério 2.3.1 "Three Flashes or Below Threshold"](https://www.w3.org/TR/WCAG22/#three-flashes-or-below-threshold) | 2024-12-12 | **Só regra de desenho da app:** nada pisca mais de 3 vezes por segundo. Não aparece como conselho. |

As duas últimas usam a entidade `OTHER`, que **não conta como oficial**. Por isso não podem sustentar passos de segurança ou saúde, e o CI já garante isso.

---

## 2. O que procurei e **não** encontrei

| Pergunta | Resultado |
|---|---|
| Que sinal de luz pedir socorro (SOS em Morse, 3 sinais, etc.)? | **Nenhuma fonte oficial portuguesa.** Procurei na ANEPC, GNR, INEM, Cruz Vermelha e gov.pt. |
| Como usar um apito para pedir ajuda (quantos sinais, quando)? | **Nenhuma.** A ANEPC e a GNR mandam *ter* um apito, mas não dizem como usá-lo. O folheto de sismos não fala em ficar soterrado. |
| Que definições do telemóvel poupar numa emergência? | **Nenhuma fonte portuguesa.** A ANACOM falou das redes depois do apagão de 2025, não dos telemóveis. Uso a Ajuda do Android (Google), marcada como não oficial. |
| Luzes a piscar e epilepsia fotossensível | Não há fonte portuguesa. A Epilepsy Society (Reino Unido) e uma revisão científica apontam **3 a 60 flashes por segundo** como zona de risco. Uso a regra técnica do W3C (máximo de 3 por segundo) como limite. |

---

## 3. Decisões que preciso que tomes

1. **Sinal SOS com luz.** Não há fonte oficial que o recomende.
   - **Proposta A (recomendo):** manter a ferramenta como **ferramenta**, sem conselho:
     - pisca a lanterna (ou o ecrã, se não houver lanterna) em ··· ––– ···;
     - mostra "Sem fonte oficial que recomende este sinal. Se puderes, liga 112";
     - ritmo lento: cada ponto dura 0,25 s, o que dá no máximo 2 flashes por segundo, abaixo do limite de 3 (W3C);
     - tem um aviso para quem é sensível a luzes a piscar.
   - **Proposta B:** retirar a ferramenta da v1.
2. **Apito/sirene.** A ANEPC recomenda ter um apito no kit, mas não diz como usá-lo.
   - **Proposta A (recomendo):** uma ferramenta "**Apito**" que toca um som agudo e intermitente pelo altifalante, com o volume que o telemóvel permitir. A app mostra três notas:
     - "Não encostes o telemóvel ao ouvido";
     - "Um apito verdadeiro não gasta bateria e ouve-se mais longe" (ANEPC: ter um apito no kit);
     - "Sem fonte oficial sobre como usar sinais sonoros".
   - O nome passa de "Apito / sirene" para "**Apito**": uma sirene pode confundir-se com veículos de emergência.
   - **Proposta B:** retirar a ferramenta da v1 e deixar só a nota "Tem um apito no kit" (ANEPC).
3. **Ecrã como luz.** Ecrã branco ou âmbar, com o brilho no máximo e o ecrã sempre ligado enquanto estiver aberto. **Nunca pisca.** É uma ferramenta prática e não precisa de fonte. Aprovas?
4. **Poupança de bateria.** O ecrã mostra:
   - o nível de bateria;
   - um botão que **abre as definições de poupança de energia do Android**, sem pedir permissões. A app não muda definições sozinha;
   - o guia da §4.

---

## 4. Guia "Fazer a bateria durar" (proposta de passos)

**Com fonte oficial:**

| # | Passo (resumo) | Tipo | Fonte |
|---|---|---|---|
| 1 | Antes de um temporal ou de uma falha anunciada, carrega o telemóvel e os equipamentos de comunicação, e tem iluminação alternativa (lanterna) | prático | ANEPC (Charles, 2026) |
| 2 | Tem um powerbank carregado no kit | prático | ANEPC (sismos), UE |
| 3 | Usa lanternas e um rádio a pilhas, com pilhas extra, para não depender do telemóvel para luz e informação | prático | DGS, UE |
| 4 | Usa o telefone só quando for preciso, para não sobrecarregar as redes | segurança | ANEPC (riscos tecnológicos) |
| 5 | Depois de um sismo, prefere SMS ou redes sociais a chamadas para falar com a família | segurança | ANEPC (sismos) |

**Definições do telemóvel** (fonte: Ajuda do Android, não oficial; cada passo mostra "Fonte: Google (Ajuda do Android)"):

| # | Passo (resumo) |
|---|---|
| 6 | Liga a poupança de bateria do Android (o botão da app abre essa definição) |
| 7 | Baixa o brilho e encurta o tempo até o ecrã se desligar |
| 8 | Desliga o Bluetooth e a localização quando não precisares deles |
| 9 | Quando não precisares da rede móvel, usa o modo de avião |
| 10 | Evita vídeo, navegação GPS prolongada e partilha de internet (hotspot) |

**Notas:**
- O passo 9 vai com uma nota bem visível: "**Em modo de avião não recebes chamadas nem avisos. Desliga-o para ligar 112.**" A nota é prática e não tem fonte.
- Os passos 4 e 5 são de segurança, por isso precisam de fonte oficial, que têm (ANEPC). A ANEPC dá o passo 4 para cheias de barragens e emergências radiológicas e o 5 para depois de sismos. O guia diz isso em vez de os generalizar.

---

## 5. Decisões técnicas para aprovares

1. **O guia é conteúdo** (`content/tools/battery.json`), com o mesmo modelo de passos dos guias (fonte, "Por rever", "Sem fonte oficial"). O CI valida-o como os outros.
   - O cartão de cada passo (`StepCard`) passa da funcionalidade de guias para o design system, para servir às duas.
2. **Sem permissões novas:**
   - a lanterna já usa `CameraManager.setTorchMode`;
   - o apito usa o som do sistema (`ToneGenerator`/`AudioTrack`), sem ficheiros de áudio;
   - o ecrã-luz muda só o brilho da janela da app;
   - o nível de bateria lê-se sem permissão.
3. **Tudo desliga ao sair do ecrã:** lanterna, SOS, apito e brilho. Nada fica a correr em background.
4. **Modo emergência:** acrescento os atalhos "Ecrã como luz" e "Apito" ao modo emergência? Proponho que **não**, para não afastar o 112 e a lanterna do topo. Ficam nas Ferramentas.
