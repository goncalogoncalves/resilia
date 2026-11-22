# Marco 3 — Fontes e propostas para validar

> Data de consulta: 2026-11-20 · Estado: **aprovado a 2026-11-20** (fontes, propostas da §4 e decisões da §5)

Este documento lista as fontes oficiais que encontrei para:

- a **calculadora do kit** (o que ter e em que quantidade);
- o **inventário** e os **lembretes de validade**;
- a **revisão semestral**.

No fim há as decisões técnicas que preciso que aproves antes de escrever código. Nenhuma quantidade entra na app sem fonte.

---

## 1. Fontes propostas

| ID proposto | Entidade | Documento | Data do documento | Usar para |
|---|---|---|---|---|
| `dgs-quebras-energeticas` | DGS | (já aprovada no Marco 2) | Sem data | **Água: cerca de 2 L por pessoa e por dia, pelo menos 3 dias.** Comida não perecível. Lanterna, rádio e pilhas extra. |
| `anepc-sismos-kit` | ANEPC + APS | (já aprovada no Marco 2) | Nov. 2018 | Lista do kit (mochila). Reserva em casa para 2 a 3 dias. "Rever regularmente." |
| `cvp-kit-familiar` | Cruz Vermelha Portuguesa | [Como Elaborar um Kit de Emergência Familiar: Manual prático (PDF)](https://www.cruzvermelha.pt/files/uploads/2024/12/CVP_ElaborarUmKitDeEmergencia_BOOK_.pdf) | 2024 (ficheiro de ago. 2024, publicado em dez. 2024) | Lista do kit por categorias. **Rever o kit a cada seis meses.** Animais, bebés e necessidades individuais. |
| `eu-swd-72h` | Comissão Europeia | [SWD(2026) 826 final: Implementation of Population Preparedness Guidelines on Reaching a Population Self Sufficiency of at least 72 Hours (PDF)](https://ec.europa.eu/echo/files/policies/preparedness/guidelines_preparedness_selfsufficiency.pdf) | 2026-10-30 | 72 h como **mínimo**. Lista de bens essenciais. Separar a **reserva de casa** da **mochila de evacuação**. Dinheiro em notas pequenas e moedas. |

A fonte `eu-preparedness` (estratégia de 2025, já aprovada) continua válida como contexto. A nova (`eu-swd-72h`) é mais concreta e substitui-a nos passos do kit.

---

## 2. O que cada fonte diz (resumo para conferires)

### Cruz Vermelha Portuguesa: manual do kit familiar (2024)
- **Água:** "pelo menos uma garrafa de água por pessoa, por dia, para um mínimo de três dias". Guardar em recipientes próprios, em local fresco e escuro. **Não diz o volume da garrafa.**
- **Comida:** alimentos de longa validade (enlatados, barras de cereais, frutos secos) para pelo menos três dias. Abre-latas manual e utensílios descartáveis.
- **Luz:** lanternas e pilhas extra; considerar lanternas de manivela. Verificar regularmente as lanternas e a validade das pilhas.
- **Medicamentos e primeiros socorros:** medicamentos essenciais e kit básico. Verificar validades e repor.
- **Documentos:** cópias (identificação, documentos financeiros, seguros) em recipiente à prova de água; cópia digital numa pen ou na nuvem.
- **Itens pessoais:** óculos, fraldas, leite para bebés, higiene pessoal, adaptados a cada pessoa.
- **Comunicação:** rádio a pilhas ou de manivela; carregadores a pilhas ou solares.
- **Roupa e cobertores** para cada pessoa, guardados em sacos de plástico.
- **Dinheiro** em notas pequenas e moedas.
- **Ferramentas:** ferramenta multifunções, fita adesiva, sacos de plástico.
- **Mapas** da zona, caso o GPS não funcione.
- **Animais:** ração, água, medicamentos e objetos de conforto. Verificar a validade da ração.
- **Manutenção:** "Reveja o conteúdo do kit de emergência a cada seis meses". Atualizar sobretudo alimentos e medicamentos. Guardar em local acessível, longe de humidade e calor.

### Comissão Europeia: orientações para as 72 h (30/10/2026)
- As 72 h são um **mínimo**, não um limite.
- **Bens essenciais:**
  - comida que se guarde à temperatura ambiente e se coma sem cozinhar ou quase sem preparação;
  - água guardada para beber, cozinhar o básico e higiene;
  - alternativa para aquecer ou arrefecer e para iluminar;
  - medicamentos e primeiros socorros;
  - higiene e saneamento, incluindo como usar a sanita e tratar o lixo;
  - rádio a pilhas e powerbanks carregados;
  - dinheiro, com notas pequenas e moedas.
- **A considerar:** animais; plano familiar (abrigo mais próximo, como contactar a família sem rede móvel).
- **Separar** a **reserva para ficar em casa** da **mochila para sair de casa** (documentos, dinheiro, básico). Evitar chamar "kit" à reserva de casa.
- Pessoas com deficiência: medicação extra, energia de reserva para dispositivos de apoio.
- **Não dá nenhuma quantidade** (nem litros nem calorias).

---

## 3. O que procurei e **não** uso

| Fonte | Porque não uso |
|---|---|
| "Cerca de 4 L por pessoa e por dia, durante 5 dias" (atribuído à Cruz Vermelha em resultados de pesquisa) | Aparece em páginas antigas de delegações que hoje redirecionam. Não consegui ler o original. O manual nacional de 2024 não repete este número. |
| App "K72" (Montijo) | É um projeto municipal, não uma entidade oficial nacional. Não publica quantidades. |
| Cartaz "Kit de Emergência" do SGIFR | O PDF não tem texto legível (é uma imagem) e não indica quem o publica. |
| Proteção Civil dos Açores (folheto de autoproteção) | O PDF tem mais de 10 MB e não o consegui abrir. Se quiseres, procuro outra versão. |
| Artigos de bancos, seguradoras e jornais | Não são fontes oficiais. |
| Kit à venda na loja da Cruz Vermelha | É um produto. Não o uso como recomendação. |

---

## 4. Lacunas e conflitos, com proposta

1. **Litros de água por pessoa.** A DGS diz cerca de 2 L por dia. A Cruz Vermelha diz "uma garrafa", sem volume. A UE não dá número.
   - **Proposta:** usar **2 L por pessoa por dia (DGS)** e explicar que o valor é **para beber**. A água para higiene não tem número oficial; a app mostra só uma nota a dizer isso, sem número.
2. **Número de dias.** A DGS e a Cruz Vermelha dizem pelo menos 3 dias, a ANEPC (2018) diz 2 a 3 dias e a UE diz 72 h como mínimo.
   - **Proposta:** **3 dias por defeito**, sem opção abaixo disso. A pessoa pode subir para 7 ou 14 dias. A água é sempre 2 L × pessoas × dias. A app explica que o mínimo oficial é 3 dias e que o resto é uma escolha da pessoa.
3. **Comida.** Nenhuma fonte dá quilos ou calorias.
   - **Proposta:** a calculadora diz "comida para N dias para N pessoas", com os exemplos das fontes (enlatados, barras de cereais, frutos secos, comida que não precisa de cozinhar). **Não mostra peso nem calorias.**
4. **Crianças, bebés, idosos e animais.** As fontes mandam adaptar o kit, mas não dão quantidades.
   - **Proposta:** a calculadora pergunta se há bebés, pessoas com medicação ou equipamentos médicos e animais. Se houver, acrescenta os itens (fraldas, leite para bebés, medicação extra, energia de reserva, ração, água, transportadora) **sem quantidade**. As crianças contam como pessoas para os 2 L, porque a DGS diz "por pessoa".
5. **Duas listas.** A UE pede que se separe a reserva de casa da mochila. A ANEPC (2018) fala de uma mochila.
   - **Proposta:** a calculadora mostra **duas listas: "Reserva em casa" e "Mochila de emergência"**. Cada item indica de que fonte vem.
6. **Revisão semestral.** A Cruz Vermelha diz a cada seis meses; a ANEPC diz "regularmente".
   - **Proposta:** a revisão semestral cita a Cruz Vermelha.
7. **Lembretes de validade.** Não precisam de fonte, porque é a pessoa que escreve a data de cada produto.
   - **Proposta:** avisar **30 dias antes** e **no próprio dia** em que um produto expira. Estes valores são escolha nossa e podes mudá-los.

---

## 5. Decisões técnicas para aprovares

1. **Inventário em Room** (base de dados local), guardado só no telemóvel e fora das cópias de segurança da cloud, como o resto. Os favoritos ficam em DataStore (não vale a pena mudar).
2. **Lembretes com WorkManager**, numa verificação diária. Não uso alarmes exatos, para não pedir mais uma permissão nem gastar bateria. O aviso pode chegar algumas horas mais tarde, o que chega para validades.
3. **Permissão de notificações (Android 13+).** É a primeira permissão que a app pede:
   - só a peço quando a pessoa ligar os lembretes, com um ecrã a explicar porquê;
   - se recusar, o inventário funciona na mesma e mostra as validades a vermelho;
   - nunca a peço no arranque.
4. **Exportar e importar** o inventário (ficheiro JSON escolhido pela pessoa) fica para o marco das definições. Se o quiseres já neste marco, acrescento-o.
5. **Correção dos separadores (já feita, por testar no emulador):** tocar no separador onde já estás leva-te ao primeiro ecrã desse separador. A barra inferior também passa a assinalar o separador certo quando estás num ecrã interior (por exemplo, um guia aberto a partir do Início).

---

## 6. Como fica a calculadora (resumo)

- **Pergunta:** quantas pessoas; se há bebés, pessoas com medicação ou equipamentos médicos e animais; quantos dias (3, 7 ou 14).
- **Mostra:**
  - **Reserva em casa:** água (litros calculados, DGS), comida para N dias (sem peso), lanterna e pilhas, rádio, powerbank, primeiros socorros e medicação, higiene e saneamento, dinheiro, e os itens extra das perguntas;
  - **Mochila de emergência:** documentos, dinheiro, água e comida, lanterna, apito, rádio, powerbank, medicação, muda de roupa, contactos em papel.
- Cada item pode ser **adicionado ao inventário** com um toque. Depois, a pessoa escreve a quantidade que tem e a validade.
- Tudo aparece **"Por rever"** até haver revisão humana, como nos guias.
