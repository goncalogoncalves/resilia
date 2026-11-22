# Marco 4 — Relatório

> Data: 2026-11-21 · Estado: **aprovado a 2026-11-21**
> Fontes e propostas aprovadas em [marco-4-fontes.md](marco-4-fontes.md).

## O que ficou feito

- **Plano familiar** (Preparar → Plano familiar). Todos os campos são opcionais e em texto livre (D12).
  - **Pessoas da casa:** nome, relação, telefone e notas, mais uma **ficha médica** com grupo sanguíneo, alergias, medicação, condições relevantes e médico.
  - Pontos de encontro (perto de casa e fora da zona), pessoa de contacto, quem faz o quê, como sair de casa, onde se desliga a eletricidade, a água e o gás, contactos úteis e zona de concentração ou abrigo.
  - Cada secção mostra a sugestão das fontes, "Por rever" e "Fonte: …". As sugestões estão em `content/plan/plan.json` e o CI exige uma fonte oficial para cada uma.
  - **Contactos úteis:** os nacionais (Marco 2), com fonte, e os que a pessoa acrescenta (escola, médico, vizinhos). Até 3 podem ir para o cartão de carteira.
  - Tudo é guardado ao escrever; não há botão "Guardar" no plano.
- **Cifra da ficha médica:**
  - AES-256-GCM, com uma chave gerada no Android Keystore que não sai do telemóvel;
  - na base de dados fica só o texto cifrado. Confirmei-o no emulador: "Penicilina" não aparece no ficheiro `resilia.db`; o nome da pessoa aparece, porque não é cifrado.
- **Bloqueio opcional das fichas médicas** (desligado por defeito):
  - pede o PIN, padrão, palavra-passe ou impressão digital do telemóvel antes de mostrar as fichas e antes de criar o cartão A4 com dados médicos;
  - também é preciso desbloquear para ligar ou desligar o bloqueio;
  - o desbloqueio dura só enquanto a pessoa estiver no ecrã.
- **Em papel** (Preparar → Cartão em papel, e o atalho "Cartão de emergência em papel" no Início). Três PDF, cada um com **Imprimir**, **Partilhar** e **Guardar**:
  - **Cartão A4:**
    - o plano completo; os campos vazios saem como linhas para preencher à mão;
    - os contactos úteis com a fonte de cada um;
    - a lista do kit (calculadora do Marco 3) com caixas para assinalar;
    - "Em emergência, liga 112" e o aviso de responsabilidade.
  - **Cartão de carteira:** frente e verso do tamanho de um cartão bancário (85,6 × 54 mm), lado a lado numa folha A4, com linhas de corte e de dobra.
    - **Frente:** 112, a pessoa de contacto e os pontos de encontro.
    - **Verso:** os contactos escolhidos, com linhas em branco para os que faltarem.
  - **Caderno de guias:** capa com o índice, todos os guias passo a passo, a fonte e "Por rever" em cada passo, e a lista das fontes com URL e datas.
  - **Dados médicos** só vão para o A4 quando se escolhem por pessoa, e o ecrã avisa que quem encontrar o papel os pode ler.
- **Modo emergência:** o botão "Cartão de papel (em breve)" passou a **"O meu plano familiar"**.
  - Mostra no ecrã, em letra grande, os pontos de encontro, a pessoa de contacto, onde se desliga a luz, a água e o gás, e os contactos.
  - **Sem dados médicos.**
- **Revisão semestral:** passa a incluir o plano familiar, no texto do cartão e na notificação. Proposta §4.2 aprovada.
- **Fontes:**
  - entidade nova `MUNICIPAL`, que conta como oficial;
  - 2 fontes novas: Câmaras de Matosinhos e Valongo.

## Medições

| Métrica | Valor |
|---|---|
| APK release (R8 + shrink) | 2,36 MB (+95 KB face ao Marco 3) |
| Testes | 86, todos a passar (+13) |
| Lint | 0 erros. 10 avisos, todos de versões mais recentes disponíveis. |
| Bateria | Não medida. Não há trabalho novo em background. Os PDF só são criados quando a pessoa toca num botão. |

Verificado no emulador (Android 14, build de debug; a release tem o mesmo código):
- pessoa com ficha médica, que fica cifrada na base de dados;
- bloqueio com PIN, com a ficha escondida até desbloquear;
- pontos de encontro e pessoa de contacto;
- os 3 PDF na pré-visualização de impressão do Android;
- atalho do Início;
- plano no modo emergência.

## Decisões tomadas durante o marco

1. **Bloqueio feito com APIs do próprio Android**, sem a biblioteca `androidx.biometric`. A biblioteca trazia o AppCompat e acrescentava cerca de 700 KB à APK. Ficou assim:
   - **Android 10 ou mais recente:** o `BiometricPrompt` do sistema, que aceita impressão digital ou PIN;
   - **Android 8 e 9:** o ecrã do sistema "confirma o bloqueio de ecrã".
   - A permissão `USE_BIOMETRIC` é normal: o Android concede-a na instalação e nunca pergunta nada (§6.2).
2. **No modo emergência, o plano aparece no ecrã e não se imprime.** Em stress imprimir não ajuda; o cartão de papel é o que já foi impresso antes.
3. **Sem dados médicos no modo emergência**, mesmo com o bloqueio desligado. O modo emergência pode ser visto por outras pessoas.
4. **Fechar a janela de uma pessoa sem tocar em "Guardar" descarta as alterações**, como acontece no inventário.
5. **Base de dados v2 com migração:** o inventário do Marco 3 mantém-se, o que está testado. O schema 2 está em `core/data/schemas`.
6. **Os PDF são criados na cache da app.** Cada novo PDF apaga os anteriores, e a cache nunca entra em backups. Só saem do telemóvel se a pessoa os partilhar, imprimir ou guardar.

## Incidente durante o teste

Ao testar no emulador, um toque caiu na sugestão de "colar" do teclado, e o emulador colou texto que estava na área de transferência do teu computador (nomes de ficheiros). **Cancelei a janela sem guardar.** Apaguei a captura de ecrã em que o texto aparecia e limpei a área de transferência do emulador. Nada disso ficou na app, no repositório nem neste relatório.

## O que precisas de validar

1. **O texto das 8 sugestões** em [`content/plan/plan.json`](../content/plan/plan.json).
2. **A entidade `MUNICIPAL`** aparece na app como "Câmara Municipal de …". Confirma que estás de acordo.
3. **Os três PDF:** gera-os no teu telemóvel e imprime pelo menos o cartão de carteira, para confirmar o tamanho real e a dobra.
4. **O limite da cifra**, que tem de ficar claro: protege os ficheiros copiados do telemóvel, mas **não** protege de quem tem o telemóvel desbloqueado na mão, a menos que o bloqueio esteja ligado.

Capturas de ecrã: [docs/screenshots/marco-4/](screenshots/marco-4/). O ecrã do PIN não aparece porque o Android bloqueia capturas desse ecrã.

## O que fica para depois

- **Exportar e importar o inventário e o plano** (§3 da proposta, com cifra opcional): marco das definições.
- **O cartão de carteira não leva dados médicos.** Se quiseres um cartão por pessoa com a ficha médica, posso acrescentá-lo.
