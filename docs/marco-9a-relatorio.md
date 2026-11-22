# Marco 9a — Relatório

> Data: 2026-11-22 · Estado: **aprovado a 2026-11-22**
> Propostas aprovadas em [marco-9-fontes.md](marco-9-fontes.md) (§2, propostas A).

## O que ficou feito

- **Ligações ao kit nos passos.** Debaixo do passo aparece "No kit: Lanternas e pilhas extra, Powerbank carregado", e tocar abre a calculadora do kit. Aparece nos 6 passos que têm ligação.
- **Exportar e importar dados**, em Definições → "Exportar e importar dados":
  - o ficheiro JSON leva o inventário, o plano familiar (com as fichas médicas), os locais, a casa da calculadora e as definições dos avisos;
  - **com fichas médicas a palavra-passe é obrigatória**; sem fichas é opcional (mínimo 8 caracteres, com confirmação);
  - cifra AES-256-GCM, com a chave derivada da palavra-passe por PBKDF2-HMAC-SHA256 (600 000 iterações, sal aleatório). Os campos do cabeçalho também ficam autenticados;
  - a pessoa escolhe onde guardar através do seletor de ficheiros do Android, sem permissões novas;
  - **importar substitui os dados, depois de confirmação.** O ficheiro é decifrado e validado antes de se apagar o que quer que seja: com palavra-passe errada, ficheiro danificado ou ficheiro de uma versão mais recente, aparece "Não mudou nada";
  - o resultado aparece no topo do ecrã e é anunciado pelo TalkBack.
- **Idioma.** Em Definições → "Idioma" abre-se o ecrã de idioma por app do Android (13 ou mais). Nas versões mais antigas, a app segue o idioma do sistema.
- **Privacidade.** Novo ecrã em Definições com a política de privacidade (rascunho, "por rever" jurídico). A Google Play exige-a também dentro da app.
- **Sobre e licenças:** passa a indicar os dados do IPMA e do SNS.
- **Ícone e imagens da loja:**
  - ícone monocromático próprio para o Android 13 ou mais (antes, a casa desaparecia dentro do escudo);
  - `tools/store/render_store_assets.py` desenha o ícone de 512 px e a imagem de destaque (1024 × 500) a partir das mesmas formas do ícone da app, em `docs/store/pt` e `docs/store/en`.
- **Correções da auditoria de acessibilidade:** ver a secção seguinte.

## Auditoria de acessibilidade

**Como fiz:**
- **Contraste:** calculado a partir das cores do tema; todos os pares estão acima de 4,5:1 (tabela em [marco-9-fontes.md](marco-9-fontes.md)).
- **Ecrãs reais no emulador, com letra a 200 %:** um script lê a árvore de acessibilidade de cada ecrã e assinala elementos tocáveis sem rótulo ou com menos de 48 dp. Ecrãs vistos:
  - início, guias, guia, preparar, calculadora do kit, inventário, plano familiar e cartão em papel;
  - calma e apoio emocional;
  - ferramentas, contactos, ecrã-luz, SOS, apito e bateria;
  - definições, exportar e importar, privacidade;
  - avisos e definições dos avisos;
  - locais, novo local e hospitais;
  - modo emergência, com as folhas de locais e de atividades.
- **Modo escuro:** início e guia.

**O que encontrei e corrigi:**
1. **Com letra a 200 %, o botão "Ligar 112" do modo emergência ficava fora do ecrã**, e era preciso fazer scroll para o ver. Agora está **fixo no fundo** e visível sempre; o teste passou a exigir isso.
2. **O resultado de exportar ou importar aparecia no fundo do ecrã**, abaixo dos botões, e não se via. Passou para o topo e é anunciado pelo TalkBack.

**O restante:** 0 problemas. Os poucos casos assinalados pelo script eram elementos cortados a meio do scroll, que confirmei nas capturas.

**O que não funcionou:** acrescentei o Accessibility Test Framework aos testes de ecrã, mas **no Robolectric não deteta nada**. Fiz um teste de propósito com um ícone clicável de 20 dp e sem descrição, e passou na mesma. Retirei-o, para não ficarem no projeto verificações que não verificam nada.

**Fica para validares:** uma volta com o **TalkBack num telemóvel real**, de preferência feita por alguém que o use no dia a dia.

## Medições

| Métrica | Valor |
|---|---|
| APK release (R8 + shrink, depois do zipalign) | 2,55 MB (Marco 8a: 2,49 MB) |
| Testes | 138, todos a passar (+9): cifra e formato do ficheiro, exportar num "telemóvel" e importar noutro, ligação ao kit, faixa de aviso, definições e privacidade |
| Lint | 0 erros. Os avisos são só de versões mais recentes disponíveis. |
| Validador de conteúdo | "Content OK" |
| Bateria (avisos) | **Uma verificação demorou 1,24 s** no emulador, com 4 descargas (avisos e risco de incêndio para 3 dias). De 3 em 3 horas são 8 verificações por dia, cerca de 10 s de trabalho, só com rede. **O consumo em mAh não se mede no emulador**: fica para validares num telemóvel. |

**Verificado no emulador** (Android 14, build de debug):
- ligação ao kit no guia da tempestade, a abrir a calculadora;
- exportar com palavra-passe: ficheiro de 1,5 KB, sem texto legível. Confirmei que "Penicilina" não aparece no ficheiro;
- importar com palavra-passe errada ("Não mudou nada") e depois com a certa ("Dados importados"; os locais voltaram);
- botão "Idioma" a abrir o ecrã do Android, com "Português (Portugal)" sugerido;
- letra a 200 % e modo escuro.

No fim repus a letra a 100 %, o modo claro e a bateria normal no emulador.

**Nota:** durante a auditoria vi que o teu telemóvel estava ligado por depuração sem fios. **Não instalei nem corri nada nele**; todos os comandos foram só para o emulador.

## Decisões tomadas durante o marco

1. **600 000 iterações no PBKDF2.** É uma escolha minha, para tornar lento adivinhar a palavra-passe. Num telemóvel lento, exportar ou importar com palavra-passe pode demorar alguns segundos, e o ecrã avisa disso.
2. **Importar não é uma única transação.** Primeiro valida tudo e só depois apaga e escreve. Se a app fosse fechada a meio da escrita, os dados podiam ficar incompletos, mas bastava importar outra vez o mesmo ficheiro.
3. **As imagens da loja são geradas por script** a partir do símbolo provisório, e não desenhadas à mão.
4. **Removi o Accessibility Test Framework** pelos motivos da auditoria acima.

## O que precisas de validar

1. **O texto da privacidade** (Definições → Privacidade), de preferência com revisão jurídica. O contacto está "a definir".
2. **TalkBack e consumo de bateria num telemóvel real.**
3. **As imagens da loja** em `docs/store/`.
4. **Exportar num telemóvel e importar noutro**, se tiveres dois.

Capturas de ecrã: [docs/screenshots/marco-9/](screenshots/marco-9/).
