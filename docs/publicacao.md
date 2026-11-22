# Publicação na Google Play: guia e respostas propostas (Marco 9b)

> Data: 2026-11-22. Tudo o que está aqui é **proposta para validares**. Os formulários da Play Console és tu que os submetes. Fontes das regras: ver [marco-9-fontes.md](marco-9-fontes.md) §1.

## 0. Antes de começar (bloqueios)

- [ ] Revisão jurídica do **aviso legal** e da **política de privacidade** ([pt](privacidade.md), [en](privacy.md)).
- [ ] **Contacto** para a política e para a ficha da loja (email). Obrigatório na Play.
- [ ] Pesquisa do **nome** no INPI e no EUIPO (existe uma "Resilia-Wellness").
- [ ] Licença dos dados do **SNS** e resposta do **IPMA** (logótipo e horas UTC).
- [ ] Decidir a **versão**: hoje é `versionName 0.1.0`, `versionCode 1`. Proposta para a primeira publicação: `1.0.0` (1).

## 1. Chave de upload (fazes tu, uma vez)

A chave **nunca entra no repositório** (`*.jks` e `*.keystore` estão no `.gitignore`). Cria-a fora da pasta do projeto e guarda uma cópia de segurança. Se a perderes, a Play App Signing permite pedir uma nova chave de upload.

```powershell
keytool -genkeypair -v -keystore "$HOME\chaves\upload.jks" -alias upload -keyalg RSA -keysize 4096 -validity 10000
```

Para gerar o App Bundle assinado, numa sessão do PowerShell:

```powershell
$env:RESILIA_KEYSTORE = "$HOME\chaves\upload.jks"; $env:RESILIA_KEY_ALIAS = "upload"; $env:RESILIA_KEYSTORE_PASSWORD = Read-Host "Palavra-passe do keystore"; $env:RESILIA_KEY_PASSWORD = Read-Host "Palavra-passe da chave"; .\gradlew :app:bundleRelease
```

O ficheiro fica em `app/build/outputs/bundle/release/app-release.aab`. Sem estas variáveis, o build release sai sem assinatura, como no CI.

Na Play Console, ativa a **Play App Signing**: a Google guarda a chave de assinatura final e tu só usas a chave de upload.

**Tamanho (medido a 2026-11-22):**
- O App Bundle tem 5,98 MB, dos quais 3,75 MB são o mapa de ofuscação do R8, que a Play guarda para os relatórios de erros mas não envia aos utilizadores.
- O código e os recursos que chegam ao telemóvel são cerca de 2,1 MB.
- A APK universal tem 2,55 MB.
- Não há código nativo próprio, por isso o download é praticamente igual em todas as arquiteturas.

## 2. Política de privacidade (URL público)

A Play exige um URL público que não seja PDF. A proposta é o **GitHub Pages**:
1. tornar o repositório público (secção 7);
2. no GitHub, ir a Settings → Pages → "Deploy from a branch" → `main` / `/docs`;
3. os URL ficam `https://<conta>.github.io/<repositório>/privacidade.html` e `…/privacy.html`.

Dentro da app, a política já está em Definições → Privacidade.

## 3. Data safety (proposta de respostas)

| Pergunta | Resposta proposta | Porquê |
|---|---|---|
| A app recolhe ou partilha algum dos tipos de dados pedidos? | **Não** | Tudo fica no telemóvel. A Google diz que o tratamento feito só no dispositivo "does not need to be disclosed". |
| Pedidos ao IPMA (o endereço IP chega ao IPMA) | Não declarar | A FAQ da Google diz para declarar o IP conforme o uso; a app não recebe, não guarda e não usa o IP. **Confirma tu, ou com aconselhamento.** |
| Os dados são cifrados em trânsito? | Sim | Só HTTPS. O tráfego sem cifra está bloqueado. |
| Os utilizadores podem pedir que os dados sejam apagados? | Não se aplica (sem dados recolhidos) | Apagar = desinstalar ou limpar os dados da app; está na política. |
| Política de privacidade | URL do GitHub Pages | |

## 4. Declaração de apps de saúde (Health apps declaration)

É obrigatória para todas as apps. A app tem:
- informação de saúde e segurança, com fontes oficiais (DGS, ANEPC, SNS 24);
- apoio emocional baseado no guia da Ordem dos Psicólogos, da DGS e da ANEPC;
- fichas médicas guardadas só no telemóvel.

**Não** faz diagnóstico, tratamento nem monitorização.

Proposta:
- escolher na consola as categorias que correspondem a **informação e referência de saúde** e a **bem-estar emocional**. Confirma os nomes exatos no formulário, que eu não consigo ver;
- declarar que **não é dispositivo médico**;
- pôr na descrição a frase que a política pede (está nas secções 6.2 e 6.3).

## 5. Classificação etária e público-alvo

- **Classificação etária (questionário IARC).** Proposta: categoria "Referência, notícias ou educação" e "Não" a todas as perguntas sobre violência, linguagem, drogas, jogo, compras e interação entre utilizadores. Os cenários (incêndios, sismos, cheias) são texto informativo, sem imagens. Confirma pergunta a pergunta.
- **Público-alvo — decisão tua.** Proposta: **18 anos ou mais**. As atividades da Calma são para fazer com crianças, mas a app é usada por adultos. Se escolheres idades abaixo dos 13, a app entra nas regras "Families" da Play, que trazem obrigações extra.
- **Categoria — decisão tua.** Proposta: **Ferramentas**. "Medicina" ou "Saúde e fitness" dariam uma ideia errada do que a app faz.

## 6. Ficha da loja

### 6.1 Título (até 30 caracteres)

- pt-PT: `Resilia — emergências`
- en-GB: `Resilia — emergencies`

### 6.2 Descrição curta (até 80 caracteres)

- pt-PT: `Prepara-te e responde a emergências em Portugal. Sem internet, sem conta.` (73)
- en-GB: `Prepare for and respond to emergencies in Portugal. Offline, no account.` (72)

### 6.3 Descrição completa

**pt-PT:**

> Uma app gratuita e de código aberto para te preparares e responderes a emergências em Portugal: falha de eletricidade, tempestades, cheias, sismos, calor, frio e incêndios rurais.
>
> • Guias com passos curtos para antes, durante e depois, com a fonte oficial de cada passo (ANEPC, DGS, AGIF e outras).
> • Modo emergência: ecrã preto, letra grande, o essencial e o botão para ligar 112 sempre à vista.
> • Kit de emergência: calculadora para a tua casa, inventário com validades e lembretes.
> • Plano familiar com fichas médicas cifradas e cartões em papel (A4 e carteira) para quando a bateria acabar.
> • Avisos meteorológicos e risco de incêndio do IPMA para a tua zona.
> • Os teus locais e a lista oficial dos hospitais com urgência do SNS.
> • Lanterna, ecrã-luz, sinal SOS, apito e um guia para a bateria durar.
> • Calma: apoio emocional e atividades sem ecrã para fazer às escuras.
>
> Sem conta, sem anúncios, sem rastreio. Os teus dados ficam só no telemóvel.
>
> IMPORTANTE: em emergência, liga 112. Esta app dá informação geral, não substitui os serviços de emergência, a proteção civil nem profissionais de saúde, e os seus autores não se responsabilizam pelo uso que lhe é dado. Esta app não é um dispositivo médico e não diagnostica, trata, cura nem previne nenhuma doença. Algum conteúdo ainda está marcado "Por rever".

**en-GB:**

> A free, open-source app to prepare for and respond to emergencies in Portugal: power cuts, storms, floods, earthquakes, heat, cold and wildfires.
>
> • Guides with short steps for before, during and after, each with its official source (ANEPC, DGS, AGIF and others).
> • Emergency mode: black screen, large text, only the essentials and the button to call 112 always in view.
> • Emergency kit: a calculator for your household, an inventory with expiry dates and reminders.
> • Family plan with encrypted medical sheets and paper cards (A4 and wallet) for when the battery runs out.
> • IPMA weather warnings and fire risk for your area.
> • Your places and the official list of SNS hospitals with A&E.
> • Torch, screen light, SOS signal, whistle and a guide to make the battery last.
> • Calm: emotional support and screen-free activities you can do in the dark.
>
> No account, no ads, no tracking. Your data stays on your phone.
>
> IMPORTANT: in an emergency, call 112. This app gives general information. It does not replace emergency services, civil protection or health professionals, and its authors accept no liability for how it is used. This app is not a medical device and does not diagnose, treat, cure, or prevent any medical condition. Some content is still marked "To be reviewed".

### 6.4 Imagens

- Ícone de 512 px e imagem de destaque de 1024 × 500: [`docs/store/pt`](store/pt) e [`docs/store/en`](store/en). Para gerar outra vez: `python tools/store/render_store_assets.py`.
- Capturas de ecrã (pelo menos 2): proponho escolher entre as dos relatórios:
  - início com aviso ([marco-7/03](screenshots/marco-7/03-inicio-com-aviso.png));
  - guia ([marco-9/06](screenshots/marco-9/06-ligacao-ao-kit.png));
  - modo emergência ([marco-9/04](screenshots/marco-9/04-emergencia-letra-200-112-fixo.png));
  - locais ([marco-8/03](screenshots/marco-8/03-os-meus-locais.png));
  - calma ([marco-6b](screenshots/marco-6b/)).

  São de um emulador, com dados de teste.

## 7. Repositório público

Verificado a 2026-11-22 (326 ficheiros, 15 MB):
- sem caminhos locais, emails nem dados pessoais;
- sem chaves nem segredos;
- `.jks`, `.keystore`, `keystore.properties` e `local.properties` estão no `.gitignore`;
- licenças presentes: `LICENSE` (Apache-2.0), `LICENSE-CONTENT` (CC BY-SA 4.0), OFL da letra;
- README com resumo em inglês;
- **ainda não há nenhum commit**.

Passos (fazes tu ou pedes-me):
1. o primeiro commit (só quando me disseres);
2. criar ou ligar o repositório no GitHub e fazer push (fazes tu);
3. tornar o repositório público (fazes tu);
4. ativar o GitHub Pages (secção 2).

## 8. Teste fechado

Se a tua conta de programador for **pessoal e criada depois de 13/11/2023**, tens de ter **pelo menos 12 testadores** inscritos num teste fechado **durante 14 dias seguidos** antes de pedir acesso à produção.
