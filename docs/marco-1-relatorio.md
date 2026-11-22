# Marco 1 — Relatório

> Data: 2026-11-20 · Estado: concluído, aguarda validação

## O que ficou feito

- **Projeto Gradle multi-módulo** (AGP 9.5.1, Gradle 9.6, Kotlin 2.3.21) com plugins de convenção em `build-logic/`.
  - `core:domain` e `core:content`: Kotlin puro, sem Android (preparados para KMP).
  - `core:data`: assets de conteúdo, DataStore (estado da app), lanterna.
  - `core:designsystem`: tema Normal (azul-petróleo, claro/escuro) e Emergência (preto, alto contraste), Atkinson Hyperlegible, componentes acessíveis.
  - `feature:home`, `guides`, `emergency`, `prepare`, `morale`, `tools`, `settings`.
- **Aviso legal obrigatório** na primeira abertura (com versão, para voltar a pedir aceitação se o texto mudar), rodapé permanente nos guias e no modo emergência, e ecrã "Aviso legal".
- **Navegação** com 5 destinos (Início, Guias, Preparar, Calma, Ferramentas) + Definições. Qualquer guia está a 1 toque do Início.
- **Modo Emergência**: ativa a partir do Início ou de um guia, persiste se a app fechar, ecrã único com "Faz isto agora", Ligar 112 (abre o marcador, sem permissão), lanterna (sem permissão de câmara), mudar situação, sair com confirmação.
- **Guias**: lista com pesquisa (ignora acentos), detalhe com separadores Antes/Durante/Depois, etiqueta "Por rever".
- **Conteúdo**: `/content` com manifest, fontes, cenário provisório de falha de eletricidade (só passos práticos, todos "por rever") e JSON Schemas.
- **Validação de conteúdo** em duas camadas: JSON Schema + regras (idiomas pt-PT/en, referências entre ficheiros, fonte oficial obrigatória em passos `safety`/`health`, metadados de revisão, ficheiros fora do manifest).
- **CI** (GitHub Actions): verificação do nome da app, validação de conteúdo, testes, lint, APK de debug.
- **Privacidade**: sem permissões no manifest, `allowBackup=false`, exclusão total em `dataExtractionRules`.
- README, CONTRIBUTING (incluindo como contribuir conteúdo), LICENSE (Apache-2.0), LICENSE-CONTENT (CC BY-SA 4.0), licença OFL do tipo de letra.

## Medições

| Métrica | Valor |
|---|---|
| APK release (R8 + shrink), ficheiro | 1,75 MB |
| APK release, tamanho de download estimado | 1,48 MB |
| Testes | 36, todos a passar (domínio, regras de conteúdo, validador, UI do modo emergência com Robolectric, pesquisa, escolha de modo) |
| Lint | 0 erros; 7 avisos, todos sobre versões mais recentes disponíveis |
| Bateria | Não medida: o Marco 1 não tem trabalho em background, rede nem animações. A medição real (Power Profiler / batterystats num telemóvel físico) começa no Marco 3 (lembretes) e no Marco 7 (alertas). |

Verificado num emulador Android 14 com a build release: aviso legal, Início em pt-PT, guia, modo emergência (persistência após reabrir), saída com confirmação, letra do sistema a 200%.

## Decisões tomadas durante o marco

1. **compileSdk 37.2 / targetSdk 36.** As bibliotecas Compose atuais exigem compilar com o SDK 37.2. O targetSdk ficou em 36 (o exigido pela Play Store); subir para 37 implica rever as mudanças de comportamento do Android 17.
2. **Revisão como objeto** `review: {status, reviewedAt, reviewedBy}` em vez de três campos soltos. Mesmo significado, mais fácil de validar.
3. **Estado de revisão do cenário é calculado**: um cenário só é "revisto" se ele e todos os passos o forem.
4. **Lanterna antecipada** (era do Marco 5): é pequena e o modo emergência precisava dela. SOS, sirene e ecrã-luz continuam no Marco 5.
5. **Módulo `feature:prepare`** é o hub de Preparar; kit, plano familiar, papel e locais terão módulos próprios nos marcos 3, 4 e 8.
6. **Strings de UI**: `values/` = inglês (en-GB), `values-pt/` = português de Portugal (serve também quem tiver o telemóvel em pt-BR). O idioma pode ser escolhido por app nas definições do Android 13+.
7. **Sem cor dinâmica** (Material You): a identidade fica consistente em todos os telemóveis.
8. **Símbolo provisório** no ícone: escudo suave com uma casa e janela iluminada.

## O que precisas de validar

1. **Texto do aviso legal** (pt e en), em `feature/settings/src/main/res/values*/strings.xml` — deve ser revisto por alguém com formação jurídica.
2. **Ícone provisório** e paleta (vê no telemóvel ou em `app/src/main/res/drawable/ic_launcher_foreground.xml`).
3. **Comportamento do modo emergência**: o botão "Sair" está no fundo do ecrã, com confirmação. Preferes no topo?
4. **Primeiro commit**: o repositório git está inicializado, mas nada foi commitado nem enviado para o GitHub.
