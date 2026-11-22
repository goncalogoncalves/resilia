# Marco 2 — Relatório

> Data: 2026-11-20 · Estado: **aprovado a 2026-11-20**
> Fontes aprovadas em [marco-2-fontes.md](marco-2-fontes.md).

## O que ficou feito

- **Guia "Falha de eletricidade" completo**, em pt-PT e en-GB:
  - **Antes:** 10 passos. **Durante:** 14 passos. **Depois:** 6 passos.
  - **"Faz isto agora"** no modo emergência: lanternas e não velas; nunca usar um gerador dentro de casa; 112 se houver suspeita de monóxido de carbono; 112 se a vida de alguém depender de um equipamento sem energia; frigorífico e congelador fechados.
  - Todos os passos de segurança e saúde citam uma fonte oficial (o CI verifica). Os passos práticos sem fonte mostram "Sem fonte oficial".
  - Todo o conteúdo está marcado como **"Por rever"**: ninguém o reviu ainda.
- **Fontes:** 11 fontes em `content/sources.json`, cada uma com entidade, título, URL, data do documento (quando existe) e data de consulta.
  - Cada passo mostra "Fonte: DGS" (por exemplo). Ao tocar, abre-se uma janela com o título, as datas e o botão "Abrir página original".
  - No fim de cada guia há a secção "Fontes deste guia".
- **Contactos úteis** em `content/contacts/national.json`:
  - 112, SNS 24, CIAV, E-REDES (avarias) e EDA (Açores), cada um com fonte.
  - Disponíveis em Ferramentas → Contactos úteis e no modo emergência, onde ficam escondidos até se tocar em "Contactos úteis".
  - "Ligar" abre o marcador com o número preenchido. A app não liga sozinha e não pede permissões.
  - A Madeira tem uma nota a explicar que o número de avarias da EEM ainda não está confirmado.
- **Motor de conteúdo:**
  - **Pesquisa** em título, palavras-chave, resumo e texto dos passos, sem distinguir acentos, com todas as palavras obrigatórias e em qualquer ordem. "gerador casa" encontra o guia.
  - **Favoritos:** a estrela no topo de cada guia. Os favoritos aparecem primeiro na lista de guias e ficam guardados só no telemóvel.
- **Validação (CI):**
  - Há um schema novo para contactos.
  - Regras novas: nenhum contacto pode existir sem fonte; o número só pode ter dígitos e espaços; a data do documento tem de ser válida.
  - Uma fonte de operador de rede (E-REDES, EDA) **não conta** como oficial para passos de segurança ou saúde.

## Medições

| Métrica | Valor |
|---|---|
| APK release (R8 + shrink) | 1,78 MB (+34 KB face ao Marco 1) |
| Testes | 47, todos a passar (+11) |
| Lint | 0 erros. 7 avisos, todos de versões mais recentes disponíveis. |
| Bateria | Não medida. Este marco não acrescenta trabalho em background nem rede. |

Verificado no emulador (Android 14, build release):
- guia com fontes por passo e janela de fontes;
- favorito;
- Contactos úteis e "Ligar", que abre o marcador com 808 24 24 24;
- modo emergência com os 5 passos de "Faz isto agora" e os contactos.

## Decisões tomadas durante o marco

1. **Duas entidades novas nas fontes:**
   - `GOV` (Governo de Portugal), que conta como **oficial**;
   - `UTILITY` (operadores como E-REDES e EDA), que **não conta como oficial** para conselhos de segurança e serve só para os contactos deles.
2. **Favoritos guardados em DataStore, não em Room.** São apenas uma lista de IDs. Room entra no Marco 3 com o inventário; se fizer sentido, mudamos os favoritos nessa altura.
3. **Contactos no modo emergência ficam escondidos atrás de um botão,** para não afastar o "Ligar 112" e a lanterna do topo.
4. **A data dos folhetos sem dia** ficou com dia 1. O folheto de sismos é de novembro de 2018 e o da EDA é de julho de 2024; no ficheiro ficaram 2018-11-01 e 2024-07-01.
5. **O passo "desligar aparelhos por causa dos picos de tensão"** tem uma nota visível: "Conselho comum, mas não encontrámos uma fonte oficial que o confirme".

## O que precisas de validar

1. **O texto dos 30 passos** em [`content/scenarios/power-outage.json`](../content/scenarios/power-outage.json). Confirma sobretudo:
   - os passos de saúde sobre medicamentos e equipamentos médicos (`before.09`, `before.10`, `during.06`);
   - a lista de "Faz isto agora".
2. **Os contactos** em [`content/contacts/national.json`](../content/contacts/national.json).
3. **EEM (Madeira):** continua sem número. Se tiveres acesso a uma fonte oficial, junto o número.
4. **Revisão humana:** quem vai rever o conteúdo para o marcar como `reviewed`? Até lá, tudo aparece "Por rever".

Capturas de ecrã: [docs/screenshots/marco-2/](screenshots/marco-2/).

## Problema encontrado, não corrigido (vem do Marco 1)

Se abrires um guia a partir do Início, mudares de separador e voltares a tocar em "Início", a app volta ao guia em vez de mostrar o ecrã inicial. É o comportamento normal do Android (lembra-se de onde estavas), mas pode confundir. Proponho que tocar no separador onde já estás te leve ao topo dele.
