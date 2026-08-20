# TinyPlayer

## Deploy

After making any code change, always deploy (install) it to a real device or emulator.

```bash
./gradlew installDebug
```

- Requires a connected Android device or emulator (check with `adb devices`).
- Always complete this install step before verifying or testing any change.

## Overview

TinyPlayer is a local (offline) music player app. Single Gradle module (`:app`), package `io.github.yyagi.tinyplayer`, minSdk 34 / compileSdk & targetSdk 36. 100% Kotlin, UI built entirely with Jetpack Compose (Material 3). Source root: `app/src/main/java/io/github/yyagi/tinyplayer/`.

## Tech Stack

- **Playback**: Media3 (`ExoPlayer` wrapped in a `MediaSessionService`, controlled from the UI via `MediaController`).
- **Persistence**: Room, used only for playlists. Songs are never persisted — they're queried live from `MediaStore` each time. `SharedPreferences` (via `PlaybackStateStore`) remembers the last-played song/position and up to 5 recent per-song positions across process death.
- **Images**: Coil3 for album art.
- **Navigation**: `navigation-compose`.
- **DI**: No Hilt/Koin — a hand-rolled manual DI container (`AppContainer`).
- **Architecture**: MVVM-ish. Plain `androidx.lifecycle.ViewModel`s expose `StateFlow`s; each has a companion `factory()` implementing `ViewModelProvider.Factory` manually (no `@HiltViewModel`).
- **Strings**: UI text is hardcoded Japanese in composables, not `strings.xml`.

## Package Layout

| Package | Contents |
|---|---|
| *(root)* | `MainActivity.kt`, `TinyPlayerApplication.kt` |
| `data/` | `AppContainer.kt` — manual DI container |
| `data/db/` | Room: `AppDatabase`, `PlaylistDao`, `PlaylistEntity`, `PlaylistSongCrossRef`, `PlaylistRepository` |
| `data/song/` | `Song.kt` (domain model), `SongRepository.kt` (MediaStore queries) |
| `media/` | `PlaybackService.kt` (MediaSessionService/ExoPlayer), `PlayerController.kt` (MediaController wrapper), `PlaybackStateStore.kt` (SharedPreferences-backed playback position persistence) |
| `ui/artists/` | `ArtistsScreen`, `ArtistDetailScreen` |
| `ui/components/` | Shared composables: `SongListItem`, `AlbumArtThumbnail`, `MiniPlayerBar`, `AddToPlaylistDialog`, `SleepTimerDialog` |
| `ui/library/` | `LibraryScreen`, `LibraryViewModel` (shared by Library/Artists/ArtistDetail screens) |
| `ui/navigation/` | `Destinations.kt`, `TinyPlayerNavHost.kt` |
| `ui/nowplaying/` | `NowPlayingScreen`, `NowPlayingViewModel` |
| `ui/permission/` | `PermissionGate.kt` |
| `ui/playlists/` | `PlaylistsScreen`/`PlaylistsViewModel`, `PlaylistDetailScreen`/`PlaylistDetailViewModel` |
| `ui/theme/` | Compose Material 3 theme definitions |
| `ui/util/` | `DurationFormat.kt` — duration/subtitle string formatting shared by list rows and Now Playing; `ScrollClickGuard.kt` — suppresses song taps that land right after a fling is stopped |

## Key Classes

- **`PlaybackService`** (`media/PlaybackService.kt`) — `ExoPlayer` + `MediaSessionService`. Includes a custom extractor config enabling constant-bitrate seeking so duration/seek work for raw ADTS AAC streams; pauses playback when audio output becomes noisy (e.g. headphones unplugged) via `setHandleAudioBecomingNoisy(true)`.
- **`PlayerController`** (`media/PlayerController.kt`) — wraps a `MediaController`, exposes `PlaybackUiState` as a `StateFlow`. Polls playback position every 500ms; remembers the last playback position of up to 5 recently played songs (persisted via `PlaybackStateStore`) and resumes from there; also owns the sleep timer (`setSleepTimer`/`cancelSleepTimer`).
- **`PlaybackStateStore`** (`media/PlaybackStateStore.kt`) — `SharedPreferences` wrapper persisting the last-played song/position and the recent per-song position map, so playback state survives process death.
- **`SongRepository`** (`data/song/SongRepository.kt`) — queries songs from `MediaStore` on demand; no DB persistence.
- **`PlaylistRepository`** (`data/db/PlaylistRepository.kt`) — combines Room DAO flows with `SongRepository.songs` to build playlist contents; also implements M3U playlist import and export (matching songs by file name).
- **`TinyPlayerNavHost`** (`ui/navigation/TinyPlayerNavHost.kt`) — the Compose navigation graph; builds each screen's ViewModel via its `factory()`.
- Every ViewModel follows the same pattern: dependencies passed directly to the constructor, with a companion `factory()` manually implementing `ViewModelProvider.Factory`.

## Testing

`app/src/test` and `app/src/androidTest` still only contain the default Android Studio template tests — there is no real test coverage yet.
