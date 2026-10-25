# SquareDS

**An unofficial fork of Square for dual-screen Android devices, developed and tested on the AYN Thor.**

SquareDS adapts [Square by Lelonio](https://github.com/Lelonio/Square), an unofficial Spotify Premium and YouTube Music client with a Liquid Glass interface, for dual-screen Android handhelds.

It adds dedicated dual-screen playback, an organized local music library, controller navigation, Apple Music Motion Artwork support, and the option to run the full player on the lower display.

The original Square features remain available, including Spotify Connect, synchronized lyrics, audio effects, listening history, and a Rust playback core based on librespot.

![SquareDS running on the AYN Thor](https://github.com/srBenjamin1/SquareDS/blob/master/docs/screenshots/IMG_20261025_134427039.jpg?raw=true)

**[Download SquareDS](https://github.com/srBenjamin1/SquareDS/releases) · [Features](#features) · [Screenshots](#screenshots) · [Installation](#installation) · [Original project](#original-project)**

---

## About SquareDS

SquareDS started as an attempt to make better use of both screens on the AYN Thor.

The idea came after seeing a Spotify client developed for the Nintendo 3DS. I had been looking for a music player that could take advantage of the Thor's two screens, and eventually discovered Square.

Rather than creating a completely new music player, I decided to adapt Square's existing interface and playback functionality.

SquareDS preserves the original project's Liquid Glass design while introducing layouts and controls designed around dual-screen handhelds.

**Tested device:** AYN Thor, using its original Android firmware without root.

Other dual-screen Android devices have not yet been verified.

## Features

### Dual-screen playback

**Upper screen — Music player**

- Persistent music player with album artwork and playback controls.
- Apple Music Motion Artwork in landscape.
- Music video switching when supported.
- Synchronized lyrics and translation.
- Interactive artist and album metadata.
- Playback progress and queue controls.
- Liquid Glass visual effects.

The upper player remains visible while browsing music on the lower screen.

**Lower screen — Navigation and tools**

- Home, New, Radio, Library, and Search, depending on the selected music source.
- Albums, artists, songs, and playlists.
- Queue and playback controls.
- Equalizer and audio effects.
- Lyrics, song information, and credits.
- Settings and source selection.

Both screens share the same playback session.

### Lower-screen-only mode

SquareDS includes an optional mode that moves the full player interface to the lower display, leaving the upper screen available for other applications.

- Switch between dual-screen and lower-screen-only modes.
- Keep the current playback session, queue, position, and playback state.
- Browse the library and open playback tools from the lower display.
- Use a landscape player inspired by Square's original interface.
- Access synchronized lyrics in a separate panel.

The application does not control the launcher or start other applications. Behavior when opening games depends on Android and the launcher.

### Music sources

**Spotify Premium**

- Personal library and playlists.
- Spotify Connect.
- Listening history and discovery.
- Supported music videos and song credits.

**YouTube Music**

- Music catalogue and search.
- Optional account authentication.
- Albums, artists, and playlists.
- Supported videos and discovery features.

**Local Music**

- Offline playback from device storage.
- Organized albums, artists, songs, genres, folders, and playlists.
- Local music search and listening history.
- Embedded album artwork.
- Artist photographs and logos when a reliable catalogue match is available.
- Optional music-folder selection through Android's Storage Access Framework.
- Apple Music Motion Artwork for compatible local albums.
- The original Square local file browser remains available.

By default, the local library discovers accessible music through Android's MediaStore. Manually selected folders are optional.

### Apple Music Motion Artwork

SquareDS supports animated album artwork in the landscape player.

For local music, it can search for a matching Apple Music album using album and artist metadata, with additional checks when needed.

When a compatible animated resource is available, it can be displayed without replacing the original artwork embedded in the file.

If no reliable match is found, SquareDS keeps the local static cover.

Availability depends on catalogue results, connectivity, and artwork preferences. Explicit online artwork lookups may use mobile data when animation is enabled.

### Controller navigation

SquareDS includes controller-oriented navigation for the AYN Thor.

- D-Pad and left analog stick navigation.
- Liquid Glass focus indicators.
- A to activate and B to go back.
- Shoulder-button navigation shortcuts.
- Touchscreen interaction preserved independently of controller focus.

Controller support continues to be refined and may vary by screen and feature.

### Playback and audio

- Synchronized lyrics and translation.
- Playback queue.
- Shuffle and repeat.
- Crossfade and sleep timer.
- Equalizer and audio effects.
- Playback speed and pitch.
- Reverb and audio presets.
- Karaoke vocal reduction where supported.
- Background playback.
- Spotify Connect.

Feature availability depends on the selected music source.

### Interface

- Liquid Glass materials.
- Dynamic colors.
- Blur, refraction, and tint effects.
- Floating navigation elements.
- Landscape layouts for dual-screen devices.
- Interactive artist and album metadata.
- Localization inherited from the original Square project, with additional work in this fork.

## Screenshots

### Dual-screen experience

Real photos and screenshots of SquareDS running on the AYN Thor.

![SquareDS dual-screen experience](https://github.com/srBenjamin1/SquareDS/blob/master/docs/screenshots/IMG_20261020_041622466.jpg?raw=true)

### Navigation

![SquareDS lower-screen navigation](https://github.com/srBenjamin1/SquareDS/blob/master/docs/screenshots/Screenshot_20261025-132951.png?raw=true)

### Music sources and local playback

| Music sources | Local music |
| :---: | :---: |
| ![Spotify, YouTube Music and Local Music](https://github.com/srBenjamin1/SquareDS/blob/master/docs/screenshots/Fuentes.png?raw=true) | ![SquareDS local music playback](https://github.com/srBenjamin1/SquareDS/blob/master/docs/screenshots/ReproduccionLocal.png?raw=true) |

### Lower-screen-only mode

SquareDS can move its player to the lower display while leaving the upper screen available for other applications.

| Lower-screen player | Gaming on the upper screen |
| :---: | :---: |
| ![SquareDS lower-screen-only player](https://github.com/srBenjamin1/SquareDS/blob/master/docs/screenshots/OnlyBottomScreen.png?raw=true) | ![A game running on the upper screen while SquareDS runs below](https://github.com/srBenjamin1/SquareDS/blob/master/docs/screenshots/IMG_20261025_133443579.jpg?raw=true) |

### Apple Music Motion Artwork

Animated album artwork in the SquareDS player.

**[Watch the Motion Artwork demonstration](https://github.com/srBenjamin1/SquareDS/blob/master/docs/screenshots/Motion%20Artwork.mp4)**

### Music video playback

SquareDS supports switching between music playback and compatible music videos.

![SquareDS music video playback](https://github.com/srBenjamin1/SquareDS/blob/master/docs/screenshots/Video-test.png?raw=true)

### Additional screenshots

| Dual-screen setup | Player interface |
| :---: | :---: |
| ![SquareDS on AYN Thor](https://github.com/srBenjamin1/SquareDS/blob/master/docs/screenshots/IMG_20261020_041622466.jpg?raw=true) | ![SquareDS player interface](https://github.com/srBenjamin1/SquareDS/blob/master/docs/screenshots/Screenshot_20261020-030401.png?raw=true) |

| Original navigation | Additional interface |
| :---: | :---: |
| ![SquareDS original navigation interface](https://github.com/srBenjamin1/SquareDS/blob/master/docs/screenshots/Screenshot_20261020-041140.png?raw=true) | ![SquareDS additional interface](https://github.com/srBenjamin1/SquareDS/blob/master/docs/screenshots/Screenshot_20261020-0412072.jpg?raw=true) |

## Installation

### Requirements

- Android 8.0 or later.
- A compatible Android device.
- ARM64 APK recommended for AYN Thor.
- Spotify Premium for Spotify playback.
- Internet access for streaming and online artwork features.

Local music playback does not require an internet connection.

### Download

**[Download the latest SquareDS release](https://github.com/srBenjamin1/SquareDS/releases)**

Available APK variants may include:

- **ARM64 (arm64-v8a):** recommended for AYN Thor.
- **ARMv7 (armeabi-v7a).**
- **Universal.**

Install the APK using Android's package installer.

Existing SquareDS installations can be updated without uninstalling when the package identifier, signing certificate, and Android version requirements are compatible.

### Dual-screen setup

1. Install and open SquareDS.
2. Complete any required music-service setup.
3. Use the upper screen for playback and the lower screen for navigation.
4. Enable lower-screen-only mode from the player or Settings when you want the full interface on the lower display.

SquareDS does not require root or Shizuku.

## Development status

SquareDS is an independently maintained community fork.

The project has been tested on the AYN Thor. Compatibility with other dual-screen Android devices has not yet been verified.

Some features remain under active development, including controller navigation, local artwork matching, and behavior across different display configurations.

Updates may take time because development is carried out during my available free time.

Bug reports, testing feedback, and contributions are welcome.

**[Report an issue](https://github.com/srBenjamin1/SquareDS/issues) · [Download releases](https://github.com/srBenjamin1/SquareDS/releases)**

## Building from source

SquareDS uses Kotlin, Jetpack Compose, Gradle, Rust, Librespot, and the Android NDK.

The development environment includes Java 21, the Android SDK, Android NDK, Rust with cargo-ndk, CMake, and the Gradle Wrapper.

Build a debug APK with:

```bash
./gradlew :app:assembleDebug --no-configuration-cache
```

The ARM64 debug APK is normally generated at:

```text
app/build/outputs/apk/debug/app-arm64-v8a-debug.apk
```

Refer to the repository's development documentation for additional information.

## Original project

SquareDS is based on **[Square by Lelonio](https://github.com/Lelonio/Square)**.

The original Square project and its contributors developed the music integrations, playback engine, Liquid Glass interface, and much of the underlying functionality.

SquareDS focuses on adapting that work to dual-screen Android hardware and expanding the local music experience.

This is an unofficial fork and is not an official Square release.

## Disclaimer

SquareDS is not affiliated with, endorsed by, or connected to Spotify, Google, YouTube Music, Apple, or AYN.

It inherits unofficial service integrations from Square. These integrations may be affected by changes to third-party services and their terms of use.

Use the application at your own risk. No warranty is provided.

## Credits and acknowledgements

Special thanks to [Lelonio](https://github.com/Lelonio) and all contributors to the original Square project.

The project also relies on work from:

- [librespot](https://github.com/librespot-org/librespot) — Spotify playback engine.
- [Metrolist](https://github.com/mostafaalagamy/Metrolist) — InnerTube integration.
- [NewPipeExtractor](https://github.com/TeamNewPipe/NewPipeExtractor) — YouTube extraction.
- [Spicy Lyrics](https://github.com/Spikerko/spicy-lyrics) — Lyrics presentation.
- [AndroidLiquidGlass](https://github.com/Kyant0/AndroidLiquidGlass) — Liquid Glass materials.
- [Phosphor Icons](https://phosphoricons.com) — Iconography.
- [Bungee](https://github.com/kupix/bungee) — Time stretching.

Additional credits and licensing information are available in the original Square repository.

## License

GPL-3.0-or-later, inherited from the original Square project.

See [LICENSE](LICENSE).

The lyrics implementation derived from Spicy Lyrics retains its applicable AGPL-3.0 licensing terms.

All upstream copyright notices and third-party license requirements remain applicable.
