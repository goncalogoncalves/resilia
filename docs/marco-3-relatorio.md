# Marco 3 — Relatório

> Data: 2026-11-20 · Estado: **aprovado a 2026-11-21**
> Fontes e propostas aprovadas em [marco-3-fontes.md](marco-3-fontes.md).

## O que ficou feito

- **Calculadora de kit** (Preparar → Calculadora de kit):
  - pergunta quantas pessoas há em casa (1 a 20) e para quantos dias (3, 7 ou 14);
  - pergunta se há um bebé, alguém com medicação ou equipamentos médicos e animais;
  - as respostas ficam guardadas no telemóvel.
  - Mostra duas listas: **Reserva em casa** (15 itens possíveis) e **Mochila de emergência** (9 itens possíveis).
  - **Água:** a única quantidade calculada é 2 L × pessoas × dias (DGS). Por exemplo, 4 pessoas durante 3 dias dá 24 L. Uma nota diz que este valor é só para beber.
  - **Comida:** a app mostra "Para 4 pessoas durante 3 dias", sem peso nem calorias, com uma nota a dizer que não há uma quantidade oficial.
  - Cada item mostra "Fonte: …", que abre a janela de fontes, e o selo "Por rever".
  - O botão "Adicionar ao inventário" passa a "No inventário" depois de usado.
- **Inventário** (Preparar → O meu inventário):
  - adicionar, editar e apagar produtos, com confirmação antes de apagar;
  - cada produto tem nome, quantidade (texto livre), lista (casa ou mochila) e validade (com escolha de data);
  - a lista vem ordenada pela validade mais próxima;
  - estado de cada produto: "Expira daqui a N dias" (a cor muda a 30 dias), "Expira hoje", "Expirou há N dias" ou "Sem validade".
- **Revisão semestral:**
  - o cartão "Revisão do kit" mostra a próxima data: 6 meses depois da última revisão ou, se nunca houve revisão, depois do primeiro produto (Cruz Vermelha);
  - o botão "Já revi" recomeça a contagem.
- **Lembretes:**
  - um interruptor no inventário;
  - antes de o Android pedir a permissão, aparece um ecrã a explicar para que serve;
  - se a pessoa recusar, tudo continua a funcionar e as validades aparecem na mesma;
  - **verificação uma vez por dia** com WorkManager: avisa 30 dias antes, no próprio dia e quando é altura de rever o kit;
  - cada aviso é enviado uma só vez por data;
  - mudar a validade de um produto volta a ativar o aviso;
  - no ecrã bloqueado a notificação mostra só "Lembrete do Resilia", sem nomes de produtos.
- **Separadores (Marco 1):**
  - tocar no separador onde já estás leva-te ao primeiro ecrã desse separador;
  - num guia aberto a partir do Início, a barra inferior assinala o Início.
- **Correção visual nos links "Fonte":**
  - quando o texto ocupava duas linhas, o botão cortava a primeira letra ("Ↄ mínimo", "Ⅎonte"). Isto também acontecia nos guias do Marco 2;
  - agora há um único componente, `SourcesLink`, usado nos guias, nos contactos, na calculadora e na revisão.

## Medições

| Métrica | Valor |
|---|---|
| APK release (R8 + shrink) | 2,26 MB (+480 KB face ao Marco 2; quase tudo vem do Room e do WorkManager) |
| Testes | 73, todos a passar (+26) |
| Lint | 0 erros. 7 avisos, todos de versões mais recentes disponíveis. |
| Bateria | Não medida. O único trabalho em background é uma verificação por dia, só com os lembretes ligados. Não usa rede nem alarmes exatos. |

Verificado no emulador (Android 14, build release):
- correção dos separadores;
- calculadora com 4 pessoas, que dá 24 L de água;
- botão "Adicionar ao inventário";
- produto editado com 6 L e validade a 30/11, que aparece como "Expira daqui a 10 dias";
- revisão do kit marcada para 20/05/2027;
- ecrã de explicação, pedido de permissão do Android e notificação "1 produto expira nos próximos 30 dias".

## Decisões tomadas durante o marco

1. **Bebé e animais sem quantidade.** As fontes não dão nenhuma. Os itens aparecem com a nota "Não encontrámos uma quantidade oficial".
2. **Tirei a transportadora para animais**, que estava na proposta aprovada. A Cruz Vermelha só fala em ração, água, medicamentos e um objeto de conforto, e a transportadora não tem fonte.
3. **Não pus um item de "aquecimento alternativo",** embora a UE fale em "alternative heating/cooling". Sem uma fonte portuguesa a dizer como o fazer em segurança, podia levar alguém a usar braseiras ou geradores dentro de casa, que o guia de falha de eletricidade proíbe. Ficou só "roupa adequada ao tempo e cobertores" (Cruz Vermelha).
4. **Data do manual da Cruz Vermelha:** ficou 2024-12-01, porque o ficheiro foi publicado na pasta de dezembro de 2024 e não tem dia. Segue a mesma regra dos folhetos do Marco 2.
5. **A quantidade não vem preenchida** quando adicionas um item a partir da calculadora. A calculadora diz o que deves ter; o inventário regista o que tens.
6. **Base de dados `resilia.db`:** fica fora dos backups porque a regra existente já exclui o domínio `database`. O schema é exportado para `core/data/schemas`, para testar migrações no futuro.
7. **Ícone da notificação provisório:** é um ícone genérico do Android. Fica para quando houver o ícone final (vem do Marco 1).

## O que precisas de validar

1. **O texto dos 24 itens** em [`content/kit/kit.json`](../content/kit/kit.json). Confirma sobretudo:
   - a nota da água;
   - o item de medicação ("Fala com o teu médico ou farmacêutico").
2. **As decisões 2 e 3** (transportadora e aquecimento).
3. **Os avisos de validade** (30 dias antes e no próprio dia). São escolha nossa e não vêm de uma fonte.
4. **A explicação antes da permissão** ([captura 06](screenshots/marco-3/06-explicacao.png)).

Capturas de ecrã: [docs/screenshots/marco-3/](screenshots/marco-3/).

## O que fica para depois

- **Exportar e importar o inventário:** marco das definições (§5.4 das fontes).
- **Tocar na notificação:** abre a app no ecrã inicial, não no inventário. Posso mudar isto quando houver mais tipos de notificação (alertas, Marco 7).
