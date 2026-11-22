---
title: Política de privacidade
lang: pt-PT
---

# Política de privacidade

> Última atualização: 2026-11-22. [English version](privacy.md).

Esta política aplica-se à app Android de preparação para emergências publicada a partir deste repositório. O nome da app está no topo do [README](../README.md).

## Resumo

- Sem conta, sem anúncios, sem analytics e sem rastreio.
- **Não temos nenhum servidor.** Não recebemos nenhum dado teu.
- O que escreves fica **só no teu telemóvel**.

## Dados que ficam no telemóvel

| Dados | Onde | Notas |
|---|---|---|
| Inventário do kit e respostas da calculadora | Base de dados da app | |
| Plano familiar, pessoas e contactos | Base de dados da app | |
| Fichas médicas | Base de dados da app | **Cifradas** com uma chave que só existe neste telemóvel (Android Keystore) |
| Locais | Base de dados da app | Escritos por ti; a app não pede a localização |
| Zona dos avisos e outras preferências | Preferências da app | |

Estes dados ficam **excluídos dos backups na cloud e das transferências entre telemóveis**. Se desinstalares a app ou limpares os dados dela, desaparecem.

## Ligações à internet

A app só usa a internet para **descarregar ficheiros públicos do IPMA** (avisos meteorológicos e risco de incêndio), e só se escolheres uma zona:
- descarrega o **ficheiro nacional**: o pedido não leva a tua zona, o teu concelho nem nenhum identificador;
- como em qualquer pedido na internet, o servidor do IPMA vê o **endereço IP** do teu telemóvel. O IPMA trata esses registos segundo as suas próprias regras: ver as [condições de utilização do IPMA](https://api.ipma.pt/).

Não há mais nenhuma ligação. Não usamos Google Play Services, Firebase nem bibliotecas de publicidade ou de estatística.

## Quando abres outra app

"Ligar", "Abrir na app de mapas", "Partilhar", "Imprimir" e as ligações das fontes abrem outra app (marcador, mapas, navegador, impressão). A app passa-lhe só o necessário: o número, as coordenadas ou a morada, o PDF ou o endereço da página. O que acontece a seguir segue as regras dessa outra app.

## Exportar e importar

Se exportares os teus dados, o ficheiro fica onde escolheres. Com palavra-passe, o ficheiro é cifrado (AES-256-GCM). **Com fichas médicas, a palavra-passe é obrigatória.** Se perderes a palavra-passe, ninguém consegue abrir o ficheiro.

## Permissões

| Permissão | Para quê |
|---|---|
| Notificações | Lembretes de validade do kit e avisos do IPMA, só se os ligares |
| Internet | Descarregar os ficheiros do IPMA |
| Biometria | Bloqueio opcional das fichas médicas com o bloqueio do telemóvel |

A app **nunca pede** localização, câmara, contactos, microfone nem chamadas. A lanterna funciona sem a permissão da câmara.

## Crianças

A app não pede nem guarda dados de crianças para além do que escreveres no plano familiar, que fica só no teu telemóvel.

## Alterações

As alterações a esta política ficam no histórico deste repositório.

## Contacto

goncalofbg@live.com.pt
