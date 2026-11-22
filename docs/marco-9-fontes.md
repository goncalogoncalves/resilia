# Marco 9 — Acessibilidade, otimização e publicação: fontes e propostas para validar

> Data de consulta: 2026-11-22 · Estado: **aprovado a 2026-11-22** (divisão 9a/9b, propostas A da §2, plano da §3)

O Marco 0 definiu o Marco 9 como "Acessibilidade, otimização, docs, publicação" e disse que a acessibilidade seria testada em todos os marcos, ficando para o 9 a auditoria final. Ficaram também para aqui três coisas: as ligações do kit nos passos (Marco 6), o logótipo (D11) e a verificação do nome. A **exportação e importação de dados**, que aprovaste para a v1 no Marco 0 (§3), ainda não está feita.

Proponho dividir em dois:
- **9a:** acessibilidade e o que falta na app;
- **9b:** publicação, com muitos passos que só tu podes fazer.

---

## 1. Fontes verificadas

| Fonte | O que diz (resumo) |
|---|---|
| [Android — Make apps more accessible](https://developer.android.com/guide/topics/ui/accessibility/apps) | Contraste de pelo menos 4,5:1 no texto normal e 3:1 no resto; alvos de toque de pelo menos 48 × 48 dp |
| [Google Play — Target API level requirements](https://support.google.com/googleplay/android-developer/answer/11926878?hl=en) | Desde 31/08/2026, as apps novas e as atualizações têm de ter o **target API 36**. A Resilia já tem. |
| [Google Play — Data safety](https://support.google.com/googleplay/android-developer/answer/10787469?hl=en) | Todas as apps preenchem o formulário, mesmo as que não recolhem dados, e **têm de ter política de privacidade**. Dados tratados só no telemóvel não precisam de ser declarados. |
| [Google Play — Health Content and Services](https://support.google.com/googleplay/android-developer/answer/16679511?hl=en) | A política de privacidade tem de estar num **URL público, que não seja PDF**, e também dentro da app. As apps de saúde que não são dispositivo médico têm de dizer na descrição que a app "is not a medical device and does not diagnose, treat, cure, or prevent any medical condition". |
| [Google Play — Health apps declaration](https://support.google.com/googleplay/android-developer/answer/14738291?hl=en) | **Obrigatória para todas as apps**, mesmo as que não têm funções de saúde |
| [Google Play — Testing requirements](https://support.google.com/googleplay/android-developer/answer/14151465?hl=en) | Contas pessoais criadas depois de 13/11/2023: é obrigatório um **teste fechado com pelo menos 12 testadores durante 14 dias seguidos** antes de publicar |
| [Google Play — Developer account information](https://support.google.com/googleplay/android-developer/answer/13628312?hl=en) | Conta com nome legal e morada verificados, email e telefone confirmados e uma taxa única de registo |

### Auditoria já feita (2026-11-22)

- **Contraste**, calculado a partir das cores do tema. Todos os pares estão **acima de 4,5:1**:

  | Tema | Pior par | Restantes pares |
  |---|---|---|
  | Claro | 6,26 (vermelho de erro) | entre 6,5 e 16,4 |
  | Escuro | 7,59 | entre 7,6 e 14,3 |
  | Modo emergência | 7,57 (botão 112) | 17 a 21 |
  | Avisos | 5,0 (branco no vermelho) | 9,0 (laranja) e 14,9 (amarelo) |

  A borda dos chips no tema claro tem 4,34:1, acima dos 3:1 que se pedem fora do texto.
- **Código:** não há botões de ícone sem descrição, nem cliques sem papel acessível, nem tamanhos de letra em dp. A única exceção é o tamanho mínimo da etiqueta da barra inferior (10 dp), que é de propósito para caber sempre.
- **Lint:** 0 erros.
- **Ligações ao kit:** só **6 de 133 passos** têm `relatedKitItemIds`.
- **Nome "Resilia":** não encontrei nenhuma app com esse nome na Google Play. Existe uma **"Resilia-Wellness"** (app de bem-estar emocional, divulgada no Product Hunt e com ligação para a App Store). **Falta pesquisar marcas no INPI e no EUIPO**, e essa pesquisa tens de ser tu a fazer.

---

## 2. Marco 9a — decisões que preciso que tomes

**1. Auditoria de acessibilidade**
- **A (proponho):** acrescentar **verificações automáticas** (Accessibility Test Framework nos testes de ecrã que já existem) e fazer uma volta manual no emulador a:
  - letra a 200 %;
  - modo escuro;
  - leitura dos rótulos pelo TalkBack (pelo que o sistema expõe) em início, guia, modo emergência, kit, plano, avisos e locais.

  Corrijo o que aparecer e registo tudo no relatório. **Um teste real com TalkBack, num telemóvel, por uma pessoa que o use, fica para validares.**
- B: só a volta manual.

**2. Ligações ao kit nos passos**
- **A (proponho):** por baixo do passo aparece "No kit: Lanterna, Pilhas", e tocar abre a calculadora do kit. Funciona nos 6 passos que têm ligação.
- B: não mostrar e tirar o campo dos dados.

**3. Exportar e importar dados (aprovado para a v1 no Marco 0)**
- **A (proponho):** em Definições → "Exportar dados" / "Importar dados":
  - o ficheiro tem inventário, plano familiar, locais e definições;
  - é guardado onde a pessoa escolher, sem permissões novas;
  - **a palavra-passe é obrigatória sempre que há fichas médicas.** O ficheiro fica cifrado com AES-GCM e uma chave derivada da palavra-passe. Sem fichas médicas, a cifra é opcional;
  - importar **substitui** os dados atuais, depois de confirmação.
- B: exportar sem cifra nenhuma. Não recomendo, por causa das fichas médicas.

**4. Escolher o idioma**
- **A (proponho):** em Definições, um botão "Idioma" que abre a escolha de idioma por app do Android (13 ou mais). Nas versões mais antigas a app segue o idioma do sistema, que é o que já acontece.
- B: um seletor dentro da app, que obriga a acrescentar a biblioteca AppCompat.

**5. Logótipo e ícone (D11)**
- **A (proponho):** fica o símbolo provisório (escudo suave com uma casa), com acabamento: ícone monocromático para o Android 13 ou mais, e o ícone de 512 px e a imagem de destaque para a loja.
- B: um designer faz o logótipo e eu integro-o.

**6. Bateria (prometida no Marco 0 e ainda não medida)**
- **A (proponho):** medir no emulador com `dumpsys batterystats` quantas vezes a app acorda e quanto tempo trabalha em 24 h com os avisos ligados. **O consumo real em mAh só se mede num telemóvel**, e isso fica para validares.

---

## 3. Marco 9b — publicação (o que faço eu, o que fazes tu)

| Passo | Quem |
|---|---|
| Conta de programador Google Play (identidade verificada, taxa única) | **Tu** |
| Chave de upload (keystore). **Nunca entra no repositório**; o Gradle lê-a de variáveis de ambiente. Play App Signing ativo. | Eu preparo o Gradle e as instruções; **tu crias e guardas a chave** |
| Build do App Bundle (`bundleRelease`) e tamanho do download por arquitetura | Eu |
| **Política de privacidade** (pt e en): dados só no telemóvel, sem conta nem analytics, ligações ao IPMA só para descarregar ficheiros públicos. Página no GitHub Pages, o que obriga a ter o **repositório público**. | Eu escrevo; **tu validas, de preferência com revisão jurídica** |
| Respostas propostas para o formulário Data safety ("sem dados recolhidos") e para a Health apps declaration | Eu proponho; **tu submetes** |
| Ficha da loja (pt e en): descrição com o aviso de que não é dispositivo médico, capturas de ecrã, ícone e imagem de destaque | Eu preparo; **tu validas** |
| Questionário de classificação etária | **Tu**, com as respostas que eu proponho |
| Teste fechado com **12 testadores durante 14 dias** (se a tua conta for pessoal e recente) | **Tu** |
| Tornar o repositório público (README em inglês, LICENSE, CONTRIBUTING, histórico sem segredos) | Eu verifico; **tu tornas público** |

**Dúvida por confirmar:** se os pedidos ao IPMA, que levam o endereço IP como qualquer pedido de rede, contam como "recolha" para o Data safety. Proponho declarar "sem dados recolhidos" e explicar isso na política de privacidade. Confirma tu na consola, ou com aconselhamento.

---

## 4. Bloqueios que recomendo resolver antes de publicar

1. **Revisão jurídica** do aviso legal e da política de privacidade (pendente desde o Marco 1).
2. **Revisão humana do conteúdo**, ou pelo menos aceitar publicar com tudo "Por rever" (D4 permite).
3. **Pesquisa do nome** no INPI e no EUIPO.
4. **Licença dos dados do SNS** (Marco 8a) e **resposta do IPMA** (Marco 7).
5. **EEM (Madeira)** e o custo da chamada para o SNS 24: continuam sem fonte e não são mostrados.
