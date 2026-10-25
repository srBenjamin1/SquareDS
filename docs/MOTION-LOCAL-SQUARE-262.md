# Motion local y selección de cambios de Square 2.6.2

> Informe de la fase anterior. La corrección y evidencia física posterior están en [Motion por álbum y reproductor inferior](MOTION-ALBUM-LOWER-PLAYER.md). Las pruebas JVM de esta fase no demostraban el funcionamiento físico completo.

Fecha de cierre: 2026-10-25. Rama `feature/local-music-library`, punto inicial `3a369c4`. Se conservaron `.codex/` y `.github/copilot-instructions.md`, que ya estaban sin seguimiento. Sin commits, push, merges ni Release durante esta intervención. Solo se obtuvo la etiqueta upstream para inspeccionarla.

## Resolución de Motion local

La ruta es SquareApp → MainViewModel.loadNowPlayingArt → LocalVisualResolver.motion → AppleCatalog.localMotion. PlayerVisualPolicy selecciona el recurso según orientación/preferencias; MotionCover presenta un vídeo silencioso separado del reproductor de audio. La composición de herramientas inferior retorna antes de crear vídeos.

Caso concreto indicado por el usuario: «Mama», My Chemical Romance, *The Black Parade*. Las consultas públicas US y GT confirmaron dos ediciones exactas (álbumes `1156310650` y `209388277`) con el mismo vídeo cuadrado y dos URLs verticales distintas. `sharedMotion` retenía correctamente solo el cuadrado común; sin embargo, PlayerVisualPolicy exigía `motionUrl` para vertical y descartaba ese recurso fiable. La regresión nueva reproduce esa combinación y verifica el resultado para ambas orientaciones y para fuentes remotas. No se ha comprobado todavía con las etiquetas del archivo físico del usuario.

Otras causas constatadas en el código anterior:

- La caché persistida admitía cualquier resultado positivo indefinidamente (`artwork != null`), incluyendo URLs de reproducción antiguas. Los negativos se conservaban una semana y las búsquedas vacías intermedias un día.
- El parser elegía primero una fila editorial por su existencia, aunque no contuviera una URL válida; no llegaba a otra variante utilizable.
- La búsqueda de canciones usaba artista y título, dejando el álbum solo para ordenar las primeras 25 respuestas.

Ahora la búsqueda usa título, artista y álbum primero; si no hay coincidencia del álbum, consulta artista y título. No depende del número de canciones que posea el usuario ni de que el álbum esté completo. Los homónimos, versiones live y ediciones ambiguas continúan sin asociarse arbitrariamente.

`LocalMotionPolicy` separa la evidencia de Motion de las asociaciones de artistas: añade revisión de política y storefront, invalida resultados anteriores y limita los positivos a seis horas y los negativos a quince minutos. La caché de búsquedas respeta los mismos plazos. Son límites para reutilización en la siguiente consulta, no un temporizador de descargas en segundo plano.

El parser local elige la primera URL HTTPS utilizable entre las variantes existentes y conserva el vídeo cuadrado por separado. No cambia el parser Motion de Spotify/YouTube. PlayerVisualPolicy añade un fallback al cuadrado exclusivamente cuando SquareApp identifica una pista local; las fuentes remotas mantienen el comportamiento anterior. Si varias ediciones del álbum etiquetado coinciden exactamente, una ausencia de vídeo no invalida el recurso de otra edición verificada, pero los vídeos disponibles contradictorios siguen requiriendo una URL compartida. MotionCover no se modificó: mantiene portada estática debajo, aparición tras primer fotograma y retirada del vídeo ante error. La portada local no se sustituye por otra imagen remota.

## Comparación upstream

[Square 2.6.2](https://github.com/Lelonio/Square/releases/tag/v2.6.2), commit `5df5168`, comparado con `v2.6.1`: seis commits, trece archivos. Se inspeccionaron los hunks y sus consumidores; se aplicaron los cambios de once archivos de implementación/documentación, manteniendo las líneas y componentes añadidos por SquareDS.

| Cambio de upstream | Decisión y efecto en SquareDS |
| --- | --- |
| Logos de artistas sobre datos móviles (`d5f19eb`) | Integrado en AppleCatalog: permite la variante ligera; fotografías y vídeos conservan su política de red. La descarga inicial del bundle/token sigue reservada a red no medida. |
| Aviso temprano de cuenta Spotify rechazada (`15f53cb`) | Integrado: mensaje antes de activar la alternativa web; ventana de actividad 90 s y clasificación de rechazo 70 s. No cambia proveedores locales/YouTube ni Connect. |
| No decodificar audio sin clave tras perder sesión (`15f53cb`) | Integrado en el parche vendorizado Rust; documentación actualizada. Evita enviar contenido aún cifrado al decoder. |
| Cristal en dispositivos low-RAM (`4df4c66`) | Integrado: película y borde de bajo coste en vez de una superficie totalmente transparente. Se conserva el backdrop y las adaptaciones de Thor. |
| Continuar cola tras videoclip Spotify (`b3746f3`) | Integrado en PlaybackService: autoplay opera sobre la cola Spotify aparcada; no añade URIs Spotify al reproductor de vídeo. DJ conserva su flujo. |
| Duración propia del videoclip (`b3746f3`) | Integrado: no se hereda la duración musical al vídeo, conservando identidad y resto de metadatos. |
| Seek en pausa/buffering (`d785c31`) | Integrado: escucha discontinuidades de posición, además del sondeo; sirve a ambas pantallas y a las tres fuentes. |
| Swipe sobre portada (`d785c31`) | Integrado en el stage COVER vertical; mantiene la composición landscape SongCard y sus controles de Thor. No se añade otra superficie de vídeo. |
| Progreso Liquid Glass (`d785c31`) | Integrado con duración/tiempo durante scrub y espera del seek. Adaptado a SongCard landscape y a mando: cada ajuste confirma seek y conserva paso de cinco segundos. Se mantienen los parámetros por defecto de los sliders de efectos. |
| Versión upstream 2.6.2/código 59 y changelog Android de upstream | No importados: SquareDS ya usa código 59 y su nomenclatura `2.6.1-squareds.2`; no se prepara una publicación ni se presenta como merge completo de base. |

No se modificaron actualizador, firma, SDK, dependencias, rutas de Biblioteca, navegación de créditos, índices SAF o asociaciones manuales. El fallo conocido de arranque de PlaybackService en segundo plano no forma parte de estos hunks y continúa pendiente.

## Verificación

Comando ejecutado tras todos los cambios de código:

```bash
./gradlew :app:testDebugUnitTest :app:assembleDebug --no-configuration-cache
```

327 pruebas JVM, cero fallos, errores u omitidas. Quince regresiones nuevas en `LocalMotionRegressionTest`: canción aislada, álbum ausente, prioridad del álbum, fallback, fallo de red frente a resultado vacío, homónimos/live/ambigüedad, caducidad de positivos y negativos, caché intermedia, cambio de etiquetas/storefront y parser con variante preferida vacía o URLs no válidas, Mama frente a Deluxe, ausencia de vídeo en otra edición exacta y vídeo cuadrado compartido utilizable en vertical solo para pistas locales.

Se compilaron también las bibliotecas Rust para ARM64 y ARMv7. Esto valida compilación, no una prueba real de rechazo de cuenta o de cortes de conexión. La primera ejecución tuvo un fallo en una aserción antigua de presencia de texto al extraer el parser; se conservó la inicialización explícita de campos estáticos nulos y la ejecución final pasó completa.

No se reutilizan como resultados de este APK las pruebas instrumentadas/físicas de la reparación anterior de Biblioteca. No se instaló este APK en la Thor durante esta intervención. Graphify actualizado mediante `graphify update .`; se verificó el código porque la extracción tiene advertencias de sintaxis parcial. `git diff --check` correcto.

## Pendiente en AYN Thor

Comprobar con las etiquetas reales de «Mama» de My Chemical Romance, *The Black Parade*: apertura vertical y landscape con Apple Music seleccionado, primera imagen de vídeo, cambio de canción, pausa/reanudación y regreso desde herramientas. Verificar que una sola canción basta y que, sin recurso fiable o con error de red/decoder, sigue visible la portada estática.

Repetir con una canción local cuyo recurso editorial esté disponible, con offline y animación desactivada; comprobar que Spotify/YouTube mantienen sus portadas, y probar vídeo más largo que canción, continuación de cola, seek en pausa y mando en ambas pantallas. Las búsquedas públicas del host no acreditan decodificación ni disponibilidad en la red/storefront de Thor.

## Archivos principales

- `data/AppleCatalog.kt`, `data/local/LocalVisualResolver.kt`, nuevo `data/local/LocalMotionPolicy.kt`.
- `ui/SquareApp.kt`, `ui/player/PlayerVisualPolicy.kt`, `PlayerScreen.kt`, `PlayerState.kt`, `GlassProgressBar.kt`; `ui/glass/LiquidSlider.kt`, `GlassEffect.kt`.
- `playback/PlaybackService.kt`, `LibrespotPlayer.kt`; `native/vendor/librespot-core/src/audio_key.rs`, `librespot-playback/src/player.rs`, `native/vendor/README.md`.
- Nueva prueba `app/src/test/java/dev/lelonio/square/data/local/LocalMotionRegressionTest.kt` y este informe.

## Consultas públicas y límites

[Resultados de las dos ediciones de Mama](validation/MAMA-MOTION-CATALOG.txt), sin tokens ni credenciales: el hash del cuadrado coincide entre ediciones/storefronts y los hashes verticales difieren. Se consultó también la edición Deluxe (`355446080`), que no devolvió vídeo en la consulta inicial US. Como control, el catálogo sí devolvió variantes Motion para Midnights y eternal sunshine; para HIT ME HARD AND SOFT no las devolvió en esas consultas US/GT. Añadir `platform=web` no cambió ese resultado: se descartó modificar las peticiones globales sobre esa hipótesis.

Esto demuestra disponibilidad de metadatos y la regresión de selección, no reproducción/decodificación en Android. La caché nueva no inventa vídeos ausentes. La prueba física debe confirmar primer fotograma, red, etiquetas y preferencias reales.


## APK ARM64 final

- [app-arm64-v8a-debug.apk](../app/build/outputs/apk/debug/app-arm64-v8a-debug.apk), 61,120,680 bytes.
- SHA-256: `72dcac117a51073abd8d66b469bd023c51a267aaadaf4858d951615c6a947397`.
- Paquete `dev.lelonio.square`, versión `2.6.1-squareds.2`, código 59, sin incremento de versión.
- Firma SHA-256: `25dc740e92ccfb79abe59ff5b2ac4a7853037d9237ab65b74ab924984e04bb3c`, verificada con apksigner.

Se entrega para validación física y se detiene aquí la publicación.
