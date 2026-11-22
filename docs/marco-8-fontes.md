# Marco 8 — Locais e mapa offline: fontes e propostas para validar

> Data de consulta: 2026-11-21 · Estado: **aprovado a 2026-11-21** (fontes, propostas A da §3, decisões técnicas da §4)

O Marco 0 previa para este marco "Locais manuais + lista por concelho; mapa offline após aprovação". A proposta para o mapa está no **[ADR 0001](adr/0001-mapa-offline.md)**. Este documento trata dos **locais**.

---

## 1. Fontes verificadas

| ID proposto | Entidade | Documento | Usar para |
|---|---|---|---|
| `sns-urgencias` | SNS (`GOV`) | [Caracterização das Valências de Urgência (Portal da Transparência do SNS)](https://transparencia.sns.gov.pt/explore/dataset/caracterizacao-das-valencias-de-urgencia/) | Hospitais com serviço de urgência: nome, tipo de urgência, morada, código postal, localidade e coordenadas |
| `ipma-risco-incendio` | IPMA | Já existe (Marco 7) | Coordenadas de referência de cada concelho, para ordenar os hospitais por distância |
| `osm` | OTHER | [OpenStreetMap — diretrizes de atribuição](https://wiki.osmfoundation.org/wiki/Licence/Attribution_Guidelines) e [política de tiles](https://operations.osmfoundation.org/policies/tiles/) | Só no 8b (mapa) |

**Sobre o conjunto de dados do SNS (lido hoje pela API do portal):**
- Tem 1 077 linhas, uma por valência, que correspondem a **94 unidades hospitalares**:
  - ARS Norte 31;
  - Lisboa e Vale do Tejo 22;
  - Centro 20;
  - Alentejo 11;
  - Algarve 6;
  - Açores 3;
  - Madeira 1.
- Tipos de urgência: básica (99 linhas), médico-cirúrgica (524), polivalente (232) e polivalente com centro de trauma (222).
- Última modificação: 2026-06-17.
- Também traz **regras de acesso por valência**: "por iniciativa do utente", "via SNS 24", "por iniciativa dos cuidados primários"…

**Por confirmar:**
- **Licença:** o portal não indica licença neste conjunto de dados e a página de termos que procurei dá erro 404.
- **Atualidade:** as regiões ainda aparecem como "ARS", por isso os dados podem não refletir a reorganização mais recente do SNS. Não fui confirmar essa reorganização, por isso fica como dúvida.

---

## 2. O que procurei e **não** encontrei

| Pergunta | Resultado |
|---|---|
| Lista nacional aberta de **quartéis de bombeiros** com localização | No dados.gov.pt só há listas municipais (Cascais, Lisboa) e contagens do INE. Não há lista nacional. |
| Lista nacional aberta de **farmácias** com localização | Só listas municipais (Cascais, Águeda) e contagens do INE. |
| Lista oficial de **abrigos ou pontos de encontro** da proteção civil | Não encontrei nada nacional. Os planos municipais variam de câmara para câmara. |
| **Concelho** de cada hospital | O SNS dá a freguesia e o código postal, não o concelho. Proponho ordenar por **distância** ao concelho escolhido nos avisos (§3, decisão 3). |

---

## 3. Decisões que preciso que tomes

**1. Mapa offline** (detalhe no [ADR 0001](adr/0001-mapa-offline.md))
- **A+B (proponho):** agora só a opção A (locais sem mapa embutido, sem custo de tamanho). Depois o **8b**, com MapLibre e mapas por distrito (20–35 MB cada) descarregados do GitHub Releases, o que obriga a ter o repositório público.
- B já: fazer tudo neste marco, com a Play Asset Delivery enquanto o repositório for privado.
- Só A: sem mapa embutido.

**2. Os meus locais** (em Preparar → Locais)
- **A (proponho):** criar, editar e apagar locais. Cada local tem:
  - tipo: casa, trabalho ou escola, ponto de encontro, hospital ou centro de saúde, farmácia, bombeiros, abrigo, outro;
  - nome, morada e notas;
  - coordenadas opcionais.

  **Não pede localização.** As coordenadas entram de uma de três formas: escritas à mão, coladas a partir de uma ligação de partilha de uma app de mapas (`geo:`, Google Maps, OpenStreetMap, Organic Maps), ou, no 8b, tocando no mapa.
- B: só nome e morada, sem coordenadas.

**3. Hospitais com urgência (SNS)**

> Alterado durante a implementação, com a tua aprovação: **sem coordenadas e sem distâncias**, porque as coordenadas do SNS não são consistentes (ver [relatório](marco-8-relatorio.md), decisão 1).

- **A (proponho):** uma lista dentro da app com as 94 unidades. Se escolheste um concelho nos avisos, aparecem ordenadas por distância a esse concelho (em linha reta). Cada uma mostra o nome, o tipo de urgência mais alto, a morada e "Abrir na app de mapas". Por cima, sempre: **"Risco de vida: liga 112. Sem risco de vida: o SNS 24 (808 24 24 24) dá aconselhamento de saúde."** É o que já dizem os contactos, com as fontes de lá.
  - Nota: tinha pensado escrever "em muitas urgências o acesso é feito pelo SNS 24", mas os dados não o provam: as colunas "via SNS 24" e "por iniciativa do utente" têm exatamente os mesmos valores. Por isso não o escrevo. Fica com "dados do SNS de 2026-06-17 · por rever".
  - **Não mostro as regras de acesso por valência:** são muitas linhas por hospital e podem mudar. A frase acima remete para o SNS 24.
  - **Não mostro os telefones dos hospitais:** a regra é "nenhum número sem fonte" e não validei cada um.
- B: não incluir a lista do SNS por causa da licença por confirmar. Ficam só os locais manuais.

**4. Onde aparecem os locais**
- **A (proponho):**
  - em Preparar → "Locais";
  - no **modo emergência**, um botão "Os meus locais" (lista grande, com "Abrir na app de mapas");
  - no cartão de papel A4 do Marco 4, uma secção "Locais" com nome e morada.
- B: só em Preparar.

**5. Distância e direção sem GPS**
- **A (proponho):** se a casa tiver coordenadas, cada local mostra a distância e a direção a partir de casa ("2,3 km a sudoeste"). É cálculo no telemóvel, sem localização.
- B: sem distâncias.

---

## 4. Decisões técnicas para aprovares

1. Tabela `place` no Room (versão 4 da base de dados), excluída dos backups como o resto: id, tipo, nome, morada, notas, latitude, longitude, data de criação.
2. A lista do SNS entra como conteúdo (`content/places/sns-urgencias.json`), gerada por um script a partir da API do portal (versionado em `tools/`). É validada pelo CI: coordenadas dentro de Portugal e fonte oficial. Só se atualiza com versões da app.
3. "Abrir na app de mapas" usa `geo:lat,lng?q=lat,lng(nome)`. Se não houver app, oferece copiar as coordenadas.
4. Sem permissões novas.
5. O módulo `feature:places` fica separado.
