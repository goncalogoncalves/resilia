# Marco 6b — Calma: fontes e propostas para validar

> Data de consulta: 2026-11-21 · Estado: **aprovado a 2026-11-21** (fontes, propostas A da §3, passos da §4, atividades da §5 e decisões da §6)

O separador **Calma** tem duas partes de natureza diferente:

1. **Apoio emocional** (saúde mental): como cuidar de nós, das crianças e das pessoas idosas depois de uma emergência. Aqui aplica-se a regra das fontes oficiais.
2. **Atividades sem ecrã:** jogos, histórias, sons, calma e desafios. Não são conselho de saúde nem de segurança, por isso não precisam de fonte oficial. Têm de ter uma **origem** declarada (original, tradicional ou domínio público) e **não podem ter letras de canções, poemas nem textos protegidos**.

---

## 1. Fontes propostas

| ID proposto | Entidade | Documento | Data | Usar para |
|---|---|---|---|---|
| `opp-dgs-anepc-tempestades` | DGS (com a Ordem dos Psicólogos e a ANEPC) | [Como recuperar emocionalmente de tempestades e inundações? Guia prático para a população (PDF)](https://www.ordemdospsicologos.pt/ficheiros/documentos/comorecuperaremocionalmentedetempestadeseinundacoes.pdf) | Versão 01, fev. 2026 | Todo o apoio emocional: recomendações gerais, para crianças, para pessoas idosas, o que fazer quando há flashbacks e sinais de alerta |
| `gov-sns24-psicologico` | GOV | [Linha de Aconselhamento Psicológico do SNS 24 disponível em inglês](https://www.gov.pt/noticias/linha-de-aconselhamento-psicologico-do-sns-24-disponivel-em-ingles) | 2022-04-22 | O aconselhamento psicológico está integrado na linha SNS 24 (808 24 24 24), também em inglês |
| `sns-linha-1411` | SNS (entidade `GOV`) | [Campanha de prevenção do suicídio](https://www.sns.gov.pt/noticias/2025/12/22/nova-campanha-de-prevencao-do-suicidio/) | 2025-12-22 | Linha 1411: prevenção do suicídio, 24 horas por dia, gratuita e confidencial |

**Sobre a autoria do guia:** o PDF diz "documento elaborado em articulação", mas os logótipos não se leem como texto. A notícia da RTP sobre o lançamento confirma que é um **guia conjunto da Ordem dos Psicólogos, da DGS e da ANEPC**. Por isso proponho a entidade `DGS`, que é oficial, com o nome das três entidades no campo `publisher`.

---

## 2. O que procurei e **não** encontrei

| Pergunta | Resultado |
|---|---|
| Que **opção do menu** escolher no 808 24 24 24 para falar com um psicólogo? | O PDF tem o número e a opção numa imagem que não consigo ler. A notícia do gov.pt não diz a opção, e as notícias de jornais dizem "opção 4". **Proponho não escrever a opção**: "liga 808 24 24 24 e pede aconselhamento psicológico". Podes confirmar a opção ligando tu. |
| Guia oficial sobre **apoio emocional na falta de eletricidade, nos sismos ou nos incêndios**? | A Ordem dos Psicólogos tem um PDF sobre incêndios, mas o ficheiro descarregado veio vazio e não o li. Não encontrei nada da DGS nem do SNS 24 sobre o apagão. O guia das tempestades é o único que li por inteiro. |
| "Como lidar com um desastre natural" (Ordem dos Psicólogos e DGS, 2021)? | As notícias falam dele, mas não encontrei o PDF. |
| Linha **SOS Criança (116 111)**? | É do Instituto de Apoio à Criança, uma IPSS, e não é uma entidade oficial. **Proponho não incluir.** |
| **Exercícios de respiração** com contagens (por exemplo 4-7-8)? | **Nenhuma fonte oficial.** O guia só diz "fazer pausas breves para respirar calmamente e alongar o corpo". Não ponho contagens. |

---

## 3. Decisões que preciso que tomes

1. **"Cantigas" sem letras.** O pedido inicial diz "nada de letras de canções, poemas ou textos protegidos".
   - **Proposta A (recomendada):** nenhuma letra, nem tradicional. A categoria "Histórias e cantigas" passa a **"Histórias e sons"**, com jogos de ritmo, palmas e sons escritos por nós.
   - **Proposta B:** incluir letras tradicionais de domínio público, com a origem indicada. É mais arriscado, porque muitas versões "tradicionais" que circulam têm autor.
2. **Histórias.**
   - **Proposta A (recomendada):** só atividades **para inventar histórias** (histórias em cadeia, três palavras, etc.), escritas por nós. Não há textos de histórias.
   - **Proposta B:** também 4 ou 5 histórias curtas **originais**, escritas por mim, marcadas "Por rever". Têm de ser revistas por alguém, porque são texto longo para crianças.
3. **O guia de apoio emocional foi escrito para tempestades e inundações.**
   - **Proposta A (recomendada):** usá-lo como apoio emocional geral, com a nota "Guia oficial escrito para tempestades e inundações" no topo, e não em cada passo. Quase todos os conselhos são gerais (falar, rotinas, notícias, sono).
   - **Proposta B:** mostrá-lo só nos guias da tempestade e das cheias.
4. **Contactos novos** em Contactos úteis e no apoio emocional:
   - **1411**, linha de prevenção do suicídio;
   - uma nota no SNS 24: "Também tem aconselhamento psicológico".

   Ambos com as fontes oficiais acima.
5. **Quantidade de atividades:** proponho **24** (lista na §5). Todas ficam "Por rever" até alguém as testar com crianças.

---

## 4. Apoio emocional (proposta de passos)

Fonte de todos os passos: `opp-dgs-anepc-tempestades`, que é paráfrase do guia. São 5 grupos, cada um num ecrã.

**Para ti (recomendações gerais)**
1. Sentir emoções fortes é natural. É melhor exprimi-las do que ignorá-las.
2. Fala sobre o que sentes com alguém de confiança. Também ajuda estar em silêncio com essa pessoa.
3. Não tentes resolver tudo sozinho e de uma vez. Começa por coisas pequenas e seguras e deixa para mais tarde o que é perigoso, como telhados, eletricidade, gás ou destroços pesados.
4. Combina um ponto de encontro com a família para quando não houver telefone nem internet e tem os contactos escritos em papel (com ligação ao plano familiar).
5. Fala com pessoas que passaram pelo mesmo e evita passar muito tempo sozinho.
6. Vê as notícias com conta: prefere fontes oficiais e, se te deixarem ansioso, vê-as só no início e no fim do dia.
7. Mantém horários para comer, a higiene e o descanso. Dorme quando puderes, mesmo que pouco. Toma a medicação habitual. Faz pausas para respirar com calma e alongar o corpo.
8. Não bebas álcool: pode piorar a ansiedade, a insónia e a tristeza.
9. Aos poucos, volta às atividades do dia a dia. Pede ajuda quando precisares.
10. Tem paciência: cada pessoa recupera ao seu ritmo.

**Crianças e jovens**
1. É natural ficarem mais assustadas, dormirem mal, terem dores de barriga ou de cabeça, ficarem irritadas ou voltarem a comportamentos de quando eram mais pequenas.
2. Os perigos continuam depois da tempestade. Fala com a criança sobre eles, verifica a casa antes de ela andar à vontade, define sítios seguros para brincar e mantém-na por perto.
3. Está disponível: colo, presença, tempo em família. Olhar nos olhos e dizer "estou contigo" pode bastar.
4. Aceita o que ela sente e diz-lhe que é natural. Lembra-lhe que a culpa não é dela. Evita "não te preocupes" e "já viste a sorte que tens?".
5. Responde às perguntas de forma direta e clara, sem mentir e sem dizer mais do que ela pergunta. Conversas, desenhos, jogos e histórias ajudam (com ligação às atividades).
6. Mantém as rotinas e as regras da casa. Dá-lhe pequenas escolhas, como o que jantar ou a que brincar.
7. Incentiva as brincadeiras e o convívio, e dá-lhe pequenas tarefas para se sentir útil.

**Pessoas idosas**
1. Combina um contacto diário (chamada, mensagem ou visita) e um plano para quando não houver rede: hora e sítio para se encontrarem, contactos escritos e alguém que passa lá se não houver resposta.
2. Com calma, façam juntos um plano simples: para onde ir, a quem ligar, como sair e o que levar (documentos, água, medicação, carregador). Repete-o se for preciso.
3. Garante uma rede de apoio para a comida, os cuidados e a conversa.
4. Atenção à medicação, aos óculos, às próteses e às consultas, que podem ter-se perdido ou ficado para trás.
5. Mantém a continuidade: falar do passado e do futuro, fotografias, tarefas com significado, como cozinhar ou tratar das plantas.

**Se reviveres o que aconteceu (flashbacks)**
1. Isto pode acontecer, e há coisas que ajudam:
   - tocar em objetos à tua volta;
   - descrever em voz alta o que vês;
   - beber um copo de água fria devagar;
   - ouvir uma música de que gostes;
   - fazer festas a um animal;
   - ligar a alguém de confiança.
2. Mãos em água fria com gelo; depois uma mão na testa e outra na nuca, durante 3 a 4 minutos, a sentir a respiração abrandar.

**Sinais de alerta**, sobretudo se duram há mais de um mês:
- perder o interesse pelo que antes dava prazer;
- álcool ou outras substâncias sem controlo;
- dificuldade em fazer as tarefas do dia a dia;
- dormir mal ou ter pesadelos;
- evitar a todo o custo lugares e pessoas que lembram o que aconteceu;
- estar sempre em alerta;
- irritação ou agressividade que antes não havia.

→ **Pede ajuda:** SNS 24 (808 24 24 24, aconselhamento psicológico) · 1411 · em perigo de vida, 112.

---

## 5. Atividades (proposta)

Todas foram **escritas por nós**. "Tradicional" quer dizer um jogo conhecido de todos, descrito pelas nossas palavras. Cada uma tem filtros para:
- idade e número de pessoas;
- materiais e duração;
- interior ou exterior;
- barulho (silencioso, moderado ou alto);
- **se dá para fazer às escuras**.

| Categoria | Atividade | Origem | Às escuras | Barulho |
|---|---|---|---|---|
| Jogos | Jogo do galo com pedras ou papel | tradicional | não | silencioso |
| Jogos | Eu vejo, eu vejo (adivinhar um objeto pela cor ou letra) | tradicional | não | moderado |
| Jogos | Quem sou eu? (20 perguntas de sim ou não) | tradicional | **sim** | moderado |
| Jogos | Telefone estragado | tradicional | **sim** | silencioso |
| Jogos | Stop (categorias com uma letra) | tradicional | não | moderado |
| Jogos | Forca | tradicional | não | silencioso |
| Jogos | Pedra, papel, tesoura com torneio | tradicional | não | moderado |
| Jogos | Batalha naval em papel quadriculado | tradicional | não | silencioso |
| Histórias e sons | História em cadeia (cada pessoa diz uma frase) | original | **sim** | moderado |
| Histórias e sons | Três palavras, uma história | original | **sim** | moderado |
| Histórias e sons | Sombras com as mãos (com lanterna) | tradicional | precisa de lanterna | silencioso |
| Histórias e sons | Adivinha o som (objetos da casa, olhos fechados) | original | **sim** | moderado |
| Histórias e sons | Eco de palmas (repetir ritmos) | original | **sim** | alto |
| Histórias e sons | Orquestra da cozinha (tachos, colheres, frascos) | original | não | alto |
| Calma | Pausa para respirar com calma e alongar (sem contagens) | baseada no guia oficial | **sim** | silencioso |
| Calma | O que está à minha volta? (descrever em voz alta o que se vê e ouve) | baseada no guia oficial | **sim** | silencioso |
| Calma | Desenho livre do dia (com conversa a seguir, se a criança quiser) | baseada no guia oficial | não | silencioso |
| Calma | Caixa das coisas boas (lembrar e escrever 3 coisas boas do dia) | original | não | silencioso |
| Desafios | Encontrar a lanterna às escuras (treino) | original | **sim** | silencioso |
| Desafios | Copiar os contactos importantes para papel | baseada no plano familiar | não | silencioso |
| Desafios | Construir a torre mais alta com o que houver | original | não | moderado |
| Desafios | Caça ao tesouro dentro de casa | original | não | moderado |
| Desafios | Inventar um jogo novo com 3 objetos | original | não | moderado |
| Desafios | Dar uma pequena tarefa útil a cada pessoa (com ligação ao guia) | baseada no guia oficial | **sim** | silencioso |

As atividades "baseadas no guia oficial" citam `opp-dgs-anepc-tempestades` e seguem a regra das fontes. As outras não citam fonte, porque não dão conselho de saúde.

---

## 6. Decisões técnicas para aprovares

1. **Ficheiros novos:**
   - `content/morale/activities.json` e `content/morale/materials.json` (lista fechada de materiais, com nomes em pt-PT e en);
   - `content/morale/support.json`, com os 5 grupos do apoio emocional.

   Cada um tem esquema e regras no CI.
2. **Regras novas no CI:**
   - toda a atividade tem `origin`;
   - uma atividade `public-domain` tem de ter `originNote`;
   - uma atividade que dá conselho (`kind: wellbeing`) tem de ter fonte oficial;
   - os materiais têm de existir na lista.
3. **Ecrã Calma:**
   - no topo, um cartão "Apoio emocional";
   - por baixo, as 4 categorias;
   - filtros em chips: idade, pessoas, **às escuras**, **silencioso**, duração e sem materiais;
   - os filtros ficam guardados no dispositivo.
4. **Ecrã da atividade:**
   - passos numerados, materiais, variantes e origem;
   - "Por rever";
   - aviso legal no rodapé.
5. **Modo emergência:** um botão "Algo para fazer" que abre só as atividades silenciosas que dá para fazer às escuras.
