# Blog post: Resilia (para WordPress)

> Como usar: copia o texto a partir de "Título" e cola no editor de blocos do WordPress. O editor converte os títulos, as listas e as citações em blocos.
> Todas as imagens estão em `docs/blog/`, numeradas pela ordem do texto. São de um emulador, com dados de teste. Para cada uma, carrega o ficheiro indicado na Biblioteca multimédia e usa o texto alternativo sugerido.

## Metadados

- **Título:** Resilia: uma app gratuita para te preparares para emergências em Portugal
- **Slug:** resilia-app-emergencias-portugal
- **Excerto (meta description, 155 car.):** Fiz uma app Android gratuita e de código aberto para preparação e resposta a emergências em Portugal. Funciona sem internet, sem conta e sem rastreio.
- **Categoria sugerida:** Projetos
- **Etiquetas:** Android, app, emergência, proteção civil, código aberto, privacidade, Portugal
- **Imagem de destaque:** `docs/blog/resilia-00-destaque.png` (1024 × 500)

---

# Resilia: uma app gratuita para te preparares para emergências em Portugal

Quando falha a eletricidade, a rede móvel fica lenta e a internet deixa de ser fiável, as dúvidas são sempre as mesmas: o que faço agora? Que água tenho em casa? Onde estão os contactos de que preciso? E se a bateria do telemóvel acabar?

Foi a pensar nesses momentos que fiz a **Resilia**, uma app Android gratuita e de código aberto para te preparares para emergências em Portugal e saberes o que fazer quando acontecem.

> ⚠️ **Em emergência, liga 112.** A Resilia dá informação geral. Não substitui os serviços de emergência, a proteção civil nem profissionais de saúde.

<!-- Imagem: docs/blog/resilia-01-inicio.png | alt: Ecrã inicial da Resilia com um aviso meteorológico do IPMA -->

## O que a app faz

### Guias para sete situações

Falha de eletricidade, tempestade, cheias, sismo (com tsunami), calor, frio e incêndio rural. Cada guia tem passos curtos e numerados para **antes, durante e depois**, e cada passo indica a fonte oficial de onde vem: ANEPC, DGS, AGIF e outras.

<!-- Imagem: docs/blog/resilia-02-guia.png | alt: Guia da falha de eletricidade com passos numerados e a fonte de cada passo -->

### Modo emergência

Um ecrã preto, com letra grande e só o essencial: os passos do momento, os teus contactos, os teus locais e o botão para **ligar 112, sempre à vista**. Funciona mesmo com a letra do telemóvel no tamanho máximo.

<!-- Imagem: docs/blog/resilia-03-modo-emergencia.png | alt: Modo emergência com fundo preto e o botão Ligar 112 fixo no fundo -->

### Kit de emergência

- Uma calculadora que diz o que precisas para a tua casa, consoante quantas pessoas são e para quantos dias te queres preparar (3, 7 ou 14).
- Um inventário com as validades dos produtos.
- Lembretes quando alguma coisa está quase a acabar o prazo e uma revisão de seis em seis meses.

<!-- Imagem: docs/blog/resilia-04-inventario.png | alt: Inventário do kit de emergência com validades -->

### Plano familiar

Pessoas, contactos, dois pontos de encontro (um perto de casa e outro fora da tua zona) e fichas médicas. As **fichas médicas são cifradas** no telemóvel. Tudo pode ser impresso em papel, numa folha A4 e num cartão para a carteira, para quando a bateria acabar.

<!-- Imagem: docs/blog/resilia-05-ficha-medica.png | alt: Adicionar uma pessoa ao plano familiar, com a ficha médica -->

### Avisos do IPMA

Escolhes a tua zona e a app mostra os avisos meteorológicos e o risco de incêndio do IPMA. Se quiseres, recebes uma notificação nos avisos laranja e vermelho. Para proteger a tua privacidade, a app descarrega o ficheiro nacional, por isso a tua zona nunca sai do telemóvel.

### Locais

Os teus locais importantes (casa, escola, trabalho, ponto de encontro) e a lista oficial dos hospitais com urgência do SNS. Um toque abre a morada na app de mapas.

<!-- Imagem: docs/blog/resilia-06-locais.png | alt: Lista dos meus locais com distância e direção -->

### Ferramentas

Lanterna, ecrã-luz, sinal SOS, apito e um guia para fazer a bateria durar mais. A lanterna não pede acesso à câmara.

<!-- Imagem: docs/blog/resilia-07-ferramentas.png | alt: Ecrã de ferramentas: lanterna, ecrã-luz, SOS e apito -->

### Calma

Apoio emocional, baseado em orientações da Ordem dos Psicólogos, da DGS e da ANEPC, e 24 atividades sem ecrã para fazer às escuras, sozinho ou com crianças.

<!-- Imagem: docs/blog/resilia-08-calma.png | alt: Secção Calma com atividades sem ecrã -->

## Feita para funcionar quando tudo falha

- **Sem internet:** todo o conteúdo essencial vem dentro da app. A rede só é usada para ler os avisos do IPMA.
- **Sem conta, sem anúncios, sem rastreio:** não há servidor nem estatísticas de utilização.
- **Os teus dados ficam no telemóvel** e não vão para os backups na cloud. Se quiseres mudar de telemóvel, podes exportar tudo para um ficheiro cifrado com palavra-passe.
- **Permissões mínimas:** a app nunca pede localização, câmara, contactos, microfone nem chamadas.
- **Fácil de ler sob stress:** letra grande e legível (Atkinson Hyperlegible), alto contraste, modo escuro e passos curtos.
- **Português de Portugal e inglês.**

## Honestidade sobre o conteúdo

Os textos da app resumem fontes oficiais, indicadas em cada passo, mas **não foram revistos por profissionais de saúde nem de proteção civil**. Em caso de dúvida, segue a fonte original e as indicações das autoridades.

## Código aberto

O código está no GitHub, com licença Apache 2.0, e os textos dos guias com licença CC BY-SA 4.0. Qualquer pessoa pode ver como a app funciona, sugerir correções ou contribuir.

👉 [github.com/goncalogoncalves/resilia](https://github.com/goncalogoncalves/resilia)

## Como instalar

A app ainda não está na Google Play. Entretanto, podes instalar o APK diretamente (Android 8 ou mais recente):

1. No telemóvel, abre [esta ligação para descarregar o APK](https://github.com/goncalogoncalves/resilia/releases/latest/download/resilia.apk).
2. Abre o ficheiro descarregado. O Android pede autorização para instalar a partir do navegador: aceita só para esta instalação.
3. Para atualizar, descarrega a versão nova e instala por cima. Os teus dados mantêm-se.

Na [página das versões](https://github.com/goncalogoncalves/resilia/releases) está o SHA-256 de cada ficheiro, para confirmares que não foi alterado.

## Privacidade

A política de privacidade completa está em [goncalogoncalves.github.io/resilia/privacidade.html](https://goncalogoncalves.github.io/resilia/privacidade.html).

---

Se experimentares, diz-me o que achas. Sugestões, erros e ideias são bem-vindos, nos comentários ou no GitHub.

E, mais do que instalar uma app: prepara hoje um kit, combina um ponto de encontro com a tua família e escreve num papel os números importantes. Em emergência, **liga 112**.
