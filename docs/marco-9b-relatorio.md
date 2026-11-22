# Marco 9b — Relatório (preparação da publicação)

> Data: 2026-11-22 · Estado: **por aprovar**
> Plano aprovado em [marco-9-fontes.md](marco-9-fontes.md) §3.

## O que ficou feito

- **Assinatura da versão de release:**
  - o Gradle lê a chave de upload de variáveis de ambiente (`RESILIA_KEYSTORE`, `RESILIA_KEY_ALIAS`, `RESILIA_KEYSTORE_PASSWORD`, `RESILIA_KEY_PASSWORD`);
  - sem elas, o build release sai sem assinatura, como no CI;
  - a chave nunca entra no repositório.
- **App Bundle:** `./gradlew :app:bundleRelease` funciona e passou a correr no CI, sem assinatura.
- **Política de privacidade em página web:** [`docs/privacidade.md`](privacidade.md) e [`docs/privacy.md`](privacy.md), prontas para o GitHub Pages. É o mesmo conteúdo da app, com mais pormenor (tabela dos dados, permissões, pedidos ao IPMA). É um rascunho "por rever" jurídico e o contacto está "a definir".
- **Guia de publicação:** [`docs/publicacao.md`](publicacao.md), com:
  - bloqueios antes de publicar;
  - criação da chave de upload (comando `keytool`);
  - comando do build assinado;
  - configuração do GitHub Pages;
  - **respostas propostas** para o Data safety, a declaração de apps de saúde, a classificação etária, o público-alvo e a categoria;
  - **ficha da loja em pt-PT e en-GB**, com título, descrição curta e descrição completa. A descrição tem o aviso de responsabilidade e a frase "não é um dispositivo médico" que a política da Play pede;
  - imagens e passos para tornar o repositório público.
- **Verificação do repositório (326 ficheiros, 15 MB):**
  - sem caminhos locais, emails, segredos nem chaves;
  - o `.gitignore` já exclui chaves e `local.properties`;
  - os ficheiros do IPMA usados nos testes têm agora uma nota de origem.

## Medições

| Métrica | Valor |
|---|---|
| App Bundle | 5,98 MB, dos quais 3,75 MB são o mapa do R8, que não vai para os utilizadores. O que vai para o telemóvel são cerca de 2,2 MB. |
| APK release | 2,55 MB (sem alterações desde o 9a) |
| Código nativo próprio | Nenhum: o download é praticamente igual em todas as arquiteturas |

**Não medi o tamanho exato de download por telemóvel**, que a Play Console mostra depois do upload, porque o `bundletool` não está instalado.

## Decisões tomadas durante o marco

1. **Sem assinatura no CI.** O CI compila o bundle sem chave; o build assinado é feito só no teu computador.
2. **Correção na ficha da loja.** No rascunho escrevi que os guias citam "INEM e IPMA", mas fui contar e os passos dos guias citam ANEPC (101), AGIF (19), DGS (12), gov.pt (5), operadores (2) e SNS 24 (1). Corrigi para "ANEPC, DGS, AGIF e outras".
3. **Não inventei os nomes das categorias** do formulário da declaração de saúde, porque não os consigo ver fora da consola. Descrevi o que escolher.

## O que precisas de fazer ou decidir

1. **Criar a chave de upload** e guardá-la fora do projeto (comando em [publicacao.md](publicacao.md) §1).
2. **Decidir:**
   - a versão (proposta `1.0.0`);
   - o público-alvo (proposta 18 anos ou mais);
   - a categoria (proposta Ferramentas);
   - o email de contacto.
3. **Revisão jurídica** do aviso legal e da política de privacidade.
4. **Pesquisa do nome** no INPI e no EUIPO.
5. **Primeiro commit:** só quando me disseres. Depois, tu fazes o push, tornas o repositório público e ativas o GitHub Pages.
6. **Conta na Play Console**, formulários e, se se aplicar, o teste fechado com 12 testadores durante 14 dias.
