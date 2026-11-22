# Marco 5 — Relatório

> Data: 2026-11-21 · Estado: **aprovado a 2026-11-21**
> Fontes e propostas aprovadas em [marco-5-fontes.md](marco-5-fontes.md).

## O que ficou feito

As Ferramentas passaram a ter estas entradas: lanterna, contactos úteis, ecrã como luz, sinal de luz SOS, apito e fazer a bateria durar.

- **Ecrã como luz:**
  - a área do ecrã fica branca ou quente (âmbar), e um toque muda a cor;
  - brilho no máximo e ecrã sempre ligado, só enquanto estiver aberto. Ao sair, volta tudo ao que estava;
  - **nunca pisca.**
- **Sinal de luz SOS:**
  - pisca ··· ––– ··· com a lanterna, ou com o ecrã se o telemóvel não tiver lanterna;
  - cada ponto dura 0,25 s, o que dá **no máximo 2 flashes por segundo**, abaixo do limite de 3 (W3C). Há um teste que garante isto;
  - mostra "Não encontrámos uma fonte oficial que recomende este sinal. Se puderes, liga 112" e o aviso para quem é sensível a luzes a piscar;
  - **a lanterna desliga sempre ao sair do ecrã.** O registo da câmara no emulador confirmou-o.
- **Apito:**
  - som agudo (3,15 kHz) durante meio segundo, com meio segundo de pausa, gerado pela app, sem ficheiros de áudio;
  - toca no canal de alarme para se ouvir bem;
  - mostra três notas: "Não encostes o telemóvel ao ouvido", "Um apito verdadeiro não gasta bateria e ouve-se mais longe" (com a fonte ANEPC) e "Sem fonte oficial sobre como usar sinais sonoros";
  - o nome "sirene" saiu.
- **Fazer a bateria durar:**
  - nível de bateria e um botão que abre a poupança de bateria do Android. No emulador abriu o ecrã certo, e a app não muda definições sozinha;
  - o guia de 10 passos aprovado, em `content/tools/battery.json`:
    - **passos 1 a 5:** fontes oficiais (ANEPC, DGS, UE). Os passos 4 e 5 dizem em que situações a ANEPC dá o conselho;
    - **passos 6 a 10:** definições do telemóvel, com a fonte "Google (Ajuda do Android)" e a etiqueta **"Fonte não oficial"**;
    - o modo de avião leva a nota "Desliga-o para ligar 112".
- **Cartão de passo partilhado (`StepCard`):** passou para o design system e serve agora aos guias e às ferramentas. Mostra "Fonte não oficial" quando um passo só cita fontes não oficiais, o que nos guias nunca acontece em passos de segurança e saúde, porque o CI o impede.
- **Fontes:**
  - 2 novas da ANEPC (Depressão Charles, 10/11/2026; Serra da Estrela, 23/02/2026);
  - 2 não oficiais (Ajuda do Android e W3C).

## Medições

| Métrica | Valor |
|---|---|
| APK release (R8 + shrink) | 2,39 MB (+31 KB face ao Marco 4) |
| Testes | 91, todos a passar (+5) |
| Lint | 0 erros. 10 avisos, todos de versões mais recentes disponíveis. |
| Bateria | Não medida. As ferramentas só gastam energia enquanto estão abertas e param ao sair (lanterna, som e brilho). Nada corre em background. |

Verificado no emulador (Android 14, build de debug):
- ecrã-luz branco e quente;
- SOS a piscar a lanterna e a desligá-la ao sair;
- apito a tocar e a parar (confirmado pelo serviço de áudio do Android);
- nível de bateria e o botão que abre a poupança de energia;
- os 10 passos do guia com fonte, "Por rever" e "Fonte não oficial".

## Decisões tomadas durante o marco

1. **O apito usa o canal de alarme do Android**, que tem volume próprio, para continuar a ouvir-se quando o volume dos media está baixo. Respeita esse volume e o modo "não incomodar" tal como o sistema o aplica.
2. **No SOS com lanterna, o ecrã pode ser desligado** e o sinal continua enquanto a app estiver nesse ecrã. Se a app for para segundo plano, o Android pode parar o sinal. Não pus o sinal em background, para não gastar bateria sem a pessoa saber.
3. **O ecrã-luz ocupa a área entre a barra de cima e a barra de baixo**, e as barras continuam visíveis para se poder sair facilmente.
4. **Avisos de pessoa sensível a luzes:** o texto diz "não olhes para ela" e não diz mais nada, porque não temos fonte de saúde portuguesa sobre epilepsia fotossensível.

## O que precisas de validar

1. **O texto dos 10 passos** em [`content/tools/battery.json`](../content/tools/battery.json).
2. **As notas do SOS e do apito** (capturas [03](screenshots/marco-5/03-sos.png) e [04](screenshots/marco-5/04-apito.png)).
3. **O volume do apito no teu telemóvel:** no emulador não dá para avaliar o volume real.

Capturas de ecrã: [docs/screenshots/marco-5/](screenshots/marco-5/).
