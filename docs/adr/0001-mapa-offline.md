# ADR 0001 — Mapa offline

> Data: 2026-11-21 · Estado: **aceite a 2026-11-21**: A no Marco 8a; B (MapLibre + mapas por distrito no GitHub Releases) no Marco 8b

## Contexto

O brief pede um mapa que funcione sem rede, para ver os locais importantes (casa, ponto de encontro, hospital), e diz: "propõe a abordagem … e espera a minha aprovação antes de implementar". As regras que se aplicam são:
- sem backend próprio;
- sem rastreio;
- dados só no telemóvel;
- sem permissão de localização (decisão do Marco 0);
- distribuição só na Google Play, sem Google Play Services nem Firebase (D3).

## O que medi (2026-11-21)

### Biblioteca: MapLibre Native Android 13.7.1 (Maven Central, BSD-2)

- O AAR tem 18,7 MB. As bibliotecas nativas comprimidas ocupam 4,56 MB em arm64-v8a, 3,98 MB em armeabi-v7a e 4,6 MB em x86/x86_64. O código Java/Kotlin ocupa 0,71 MB e depende do OkHttp.
- Com o App Bundle, a Play entrega só a arquitetura do telemóvel. A minha **estimativa é de mais 5 a 6 MB no download e cerca de 13 MB instalada em arm64**. Hoje a app tem 2,44 MB. Esta estimativa vem do AAR e tem de ser confirmada com uma build real.
- Lê PMTiles diretamente de um ficheiro no telemóvel (`pmtiles://file://…`), segundo a documentação oficial (PMTiles.md). Não precisa de servidor de tiles.

### Mapa base: Protomaps (OpenStreetMap)

- É um mapa vetorial do planeta, num só ficheiro PMTiles (build de 2026-11-21, 139,7 GB).
- Licença: "ODbL Produced Work (OpenStreetMap attribution required)". O código e os estilos são BSD-3 e CC0.
- O tamanho foi **medido** a ler os índices desse ficheiro com pedidos parciais (script `pmsize.py`, 18 pedidos, 1,6 MB lidos). Os retângulos dos distritos são aproximações minhas e incluem algum mar e Espanha, por isso os valores estão um pouco por excesso:

| Região | até zoom 12 | até zoom 13 | **até zoom 14** | até zoom 15 |
|---|---|---|---|---|
| Continente (inteiro) | 84 MB | 177 MB | **336 MB** | 637 MB |
| Madeira (com Porto Santo) | 1,4 MB | 2,8 MB | **5,3 MB** | 12,6 MB |
| Açores (todas as ilhas) | 3,2 MB | 6,5 MB | **12,3 MB** | 23,8 MB |
| Distrito de Lisboa (aprox.) | 6,5 MB | 12,9 MB | **25,1 MB** | 52,5 MB |
| Distrito do Porto (aprox.) | 9,4 MB | 17,6 MB | **32,6 MB** | 62,7 MB |
| Distrito de Faro (aprox.) | 5,4 MB | 11,1 MB | **20,3 MB** | 37,2 MB |
| Distrito de Bragança (aprox.) | 8,6 MB | 17,5 MB | **31,3 MB** | 54,3 MB |

O MapLibre consegue ampliar para lá do último zoom do ficheiro, com menos pormenor. No zoom 14 já se veem ruas e números de polícia nas cidades.

### Alojamento sem backend

- **Servidores de tiles do OpenStreetMap:** a política **proíbe** "offline use" e qualquer "download for offline". Não servem.
- **GitHub Releases:** cada ficheiro tem de ter menos de 2 GiB e não há limite de tamanho total nem de tráfego. **O repositório tem de ser público** para a app descarregar sem conta, e hoje é privado.
- **Play Asset Delivery** (on-demand): até 1,5 GB por pacote e 30 GB no total, servido pela Google Play. Precisa da biblioteca Play Asset Delivery, que é da Google mas não é o Google Play Services. Os mapas só se atualizam com versões da app.

## Opções

| | A. Sem mapa embutido | B. MapLibre + mapa por distrito, a pedido | C. Como B, mas Portugal inteiro dentro da app |
|---|---|---|---|
| O que a pessoa vê | Lista de locais com distância e direção ("1,2 km a nordeste"), "Abrir na app de mapas" e "Copiar coordenadas" | O mesmo, mais um mapa com os locais. Descarrega 1 ou mais distritos (20–35 MB cada; ilhas 5–12 MB) | Mapa em todo o lado, sem descarregar |
| Tamanho | +0 MB | App +5–6 MB (download); mapas à parte | App +350 MB |
| Funciona sem rede | Só se a app de mapas da pessoa tiver mapas offline | Sim, depois de descarregar | Sim |
| Alojamento | Nenhum | GitHub Releases (repositório público) ou Play Asset Delivery | Play |
| Trabalho de manutenção | Nenhum | Um workflow mensal no GitHub Actions corta os distritos da build Protomaps (`pmtiles extract`) e publica os ficheiros com os SHA-256 | Igual ao B |
| Riscos | Depende de outra app | Tipos de letra e ícones do mapa têm de vir dentro da app (por verificar); o mapa pode ficar desatualizado | Tamanho inaceitável |

## Proposta

**A já, B em seguida:**

1. **Marco 8a (agora):** locais e tudo o que não precisa de mapa, ou seja a opção A. O que se faz aqui serve também para o B.
2. **Marco 8b (depois de aprovares):** MapLibre e mapas por distrito até ao zoom 14, descarregados do **GitHub Releases** (precisa do repositório público) e verificados por SHA-256. A Play Asset Delivery fica como alternativa se preferires não abrir o repositório antes do Marco 9.
3. **A opção C fica excluída.**

## Atribuição

- No canto do mapa: "© OpenStreetMap", com ligação para a licença. As diretrizes da OSMF aceitam qualquer canto.
- Em "Sobre e licenças": ODbL e Protomaps (BSD-3/CC0).

## Consequências

- O 8a não muda o tamanho da app nem as permissões.
- O 8b acrescenta cerca de 5–6 MB ao download e uma ligação a `github.com`, ou à Play, apenas quando a pessoa pede para descarregar um mapa.
