# Marco 8a — Relatório

> Data: 2026-11-21 · Estado: **aprovado a 2026-11-22**
> Fontes e propostas aprovadas em [marco-8-fontes.md](marco-8-fontes.md). O mapa segue o [ADR 0001](adr/0001-mapa-offline.md): opção A agora, opção B (MapLibre e mapas por distrito) no Marco 8b.

## O que ficou feito

- **Os meus locais**, em Preparar → Locais:
  - criar, editar e apagar (com confirmação);
  - tipos: casa, trabalho ou escola, ponto de encontro, hospital ou centro de saúde, farmácia, bombeiros, abrigo, outro;
  - nome, morada, notas e coordenadas opcionais.
- **Coordenadas sem pedir localização.** Lê:
  - números escritos ("38.7167, -9.1333", também com vírgula decimal);
  - ligações `geo:`;
  - ligações de partilha do Google Maps, do OpenStreetMap e do Organic Maps.

  Mostra "Lido como: …" antes de guardar.
- **Distância e direção a partir de casa**, calculadas no telemóvel ("744 m a norte de casa").
- **Abrir na app de mapas e copiar:**
  - "Abrir na app de mapas" (`geo:`) abre nas coordenadas, ou pesquisa pela morada quando não há coordenadas;
  - sem app de mapas, aparece uma mensagem;
  - "Copiar coordenadas" e "Copiar morada".
- **Hospitais com urgência (SNS):**
  - 94 unidades, agrupadas pela região tal como o SNS a publica e ordenadas por localidade;
  - pesquisa sem acentos nem maiúsculas;
  - para cada uma: tipo de urgência mais alto, morada e "Abrir na app de mapas" (pesquisa pela morada);
  - no topo, os contactos 112 e SNS 24, com as fontes;
  - "Dados do SNS, atualizados a 17/06/2026" e "Por rever".
- **Modo emergência:** novo botão "Os meus locais", com a lista em letra grande e os botões de mapas.
- **Cartão A4 em papel:** nova secção "Locais", com tipo, nome, morada e coordenadas.
- **Novo script `tools/data/sns_urgencias.py`**, que gera `content/places/sns-urgencias.json` a partir da API do Portal da Transparência do SNS. Usa só a biblioteca padrão do Python e não copia telefones, emails nem coordenadas.
- **CI:** novo esquema `hospitals.schema.json` e regras novas (fonte oficial, datas, ids únicos).
- **Conteúdo na versão 0.9.0.**
- **Base de dados na versão 4**, com a tabela `place`. A migração 3 → 4 foi testada e a tabela fica excluída dos backups.
- **Novo módulo `feature:places`.**
- **Sem permissões novas.**

## Medições

| Métrica | Valor |
|---|---|
| APK release (R8 + shrink, depois do zipalign) | 2,49 MB (Marco 7: 2,44 MB) |
| Testes | 129, todos a passar (+9): leitura de coordenadas, distância e direção, lista do SNS, regras, repositório, migração e ecrãs |
| Lint | 0 erros. 10 avisos, todos de versões mais recentes disponíveis. |
| Validador de conteúdo | "Content OK" |
| Bateria | Não medida. Não corre nada em segundo plano. |

Verificado no emulador (Android 14, build de debug):
- lista vazia;
- novo local "casa" com coordenadas escritas;
- ponto de encontro com uma ligação do OpenStreetMap ("Lido como: 38.71390, -9.13940");
- "744 m a norte de casa";
- "Abrir na app de mapas" abriu o Google Maps do emulador no ponto certo, com o nome;
- lista do SNS com pesquisa "evora";
- "Abrir na app de mapas" de um hospital: o Google Maps encontrou o hospital pela morada;
- "Os meus locais" no modo emergência;
- PDF A4 com a secção "Locais" (confirmei o texto dentro do PDF).

Não guardei capturas do Google Maps porque são conteúdo de terceiros. Os dois locais de teste ("Casa de teste" na Praça do Comércio e "Rossio") ficaram no emulador.

## Decisões tomadas durante o marco

1. **As coordenadas do SNS não são usadas (decidido contigo).** No conjunto de dados do SNS, **68 das 94 unidades têm várias coordenadas diferentes**, algumas a centenas de km. Exemplos:
   - o H. Prof. Doutor Fernando Fonseca (Amadora) aparece também no Porto e em Mogadouro;
   - o H. Portimão aparece em 4 sítios.

   Como a morada, o código postal e a localidade eram consistentes, a lista não tem distâncias e "Abrir na app de mapas" pesquisa pela morada. Isto substitui a ordenação por distância da decisão 3.
2. **A região aparece como o SNS a publica** ("ARS Norte", "SRS Açores"…), sem a mudar.
3. **Um local novo começa como "Casa"** se ainda não houver casa; senão, como "Ponto de encontro".
4. **A distância só aparece se houver um local "Casa" com coordenadas.** Sem isso, aparece a dica "Junta coordenadas à tua casa…".

## O que precisas de validar

1. **A licença do conjunto de dados do SNS.** O portal não indica nenhuma. Continua "por confirmar".
2. **Comunicar ao SNS o problema das coordenadas**, se quiseres. Os exemplos estão na decisão 1.
3. **Os textos dos ecrãs**, sobretudo a nota "Ainda não há mapa dentro da app…".
4. **O Marco 8b (mapa offline)** precisa do repositório público para descarregar os mapas do GitHub Releases. A alternativa é a Play Asset Delivery. Tens de decidir quando.

Capturas de ecrã: [docs/screenshots/marco-8/](screenshots/marco-8/).
