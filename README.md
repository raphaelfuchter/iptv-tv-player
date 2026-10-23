# IPTV Player (Android TV)

Player IPTV básico para Android TV. Construído do zero com stack Android padrão
— reprodução e layout em grade inspirados em players populares, sem código ou
assets de terceiros.

## Stack

- **Kotlin + Jetpack Compose for TV** (`androidx.tv:tv-material`) — UI com foco
  por D-pad.
- **Media3 / ExoPlayer** (`media3-exoplayer`, `media3-exoplayer-hls`) —
  reprodução de HLS, DASH e streams progressivos.
- **OkHttp + kotlinx.serialization** — download de M3U e API Xtream Codes.

## Funciona

- Carregar playlist via **M3U URL** (parser de `#EXTINF` com `tvg-logo`,
  `group-title`, `tvg-id`).
- Login **Xtream Codes** (`get_live_streams`), monta URL `/live/user/pass/id.ts`.
- Grade de canais focável (5 colunas), seleção abre player em tela cheia.

## Ainda não (próximos passos)

- EPG / guia de programação.
- Persistência de playlists (Room).
- Categorias / busca / favoritos.
- VOD e séries (Xtream).

## Build

Requer Android Studio ou Gradle local. O wrapper (`gradlew`, `gradle-wrapper.jar`)
não está versionado — gere com:

```bash
gradle wrapper --gradle-version 8.9
./gradlew assembleDebug
```

Ou abra a pasta no Android Studio (Giraffe+). `compileSdk 34`, `minSdk 21`,
JDK 17.

## Instalar na TV

```bash
./gradlew installDebug      # com a Android TV pareada via adb
```

## Licença

MIT.
