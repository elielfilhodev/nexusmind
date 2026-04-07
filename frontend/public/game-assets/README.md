# Assets de jogo (League of Legends)

Estrutura recomendada para ícones espelhados do **Data Dragon** (Riot Games):

```
public/game-assets/
├── champions/     # {championKey}.png — de /cdn/{version}/img/champion/
├── items/         # {itemId}.png — de /cdn/{version}/img/item/
├── spells/        # SummonerFlash.png etc.
├── runes/         # estilos e runas por ID
└── ranks/         # bordas de elo (arte própria ou licenciada)
```

**Origem oficial:** `https://ddragon.leagueoflegends.com/` — respeite os termos de uso da Riot.

**Atualização:** alinhar pasta `version` ao registro `patch_versions` no Postgres; o job futuro (`DataDragonIngestionPort`) deve baixar o bundle do patch e popular o banco + copiar ícones para CDN (ex.: Vercel Blob) se necessário.

**MVP:** o app usa dados seed via API; esta pasta pode permanecer vazia até a ingestão automática.
