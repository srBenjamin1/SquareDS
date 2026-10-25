# Motion local por álbum y reproductor inferior

Fecha: 2026-10-25.

Intervención sobre `feature/local-music-library`, conservando los cambios locales previos de Square 2.6.2. Sin commits, push ni Release. Versión, SDK, NDK, Rust, Gradle y actualizador no se cambiaron durante esta intervención.

## Causas comprobadas en Thor

La validación automatizada anterior no acreditaba el funcionamiento físico completo. La inspección de la Thor confirmó las etiquetas reales de Mama: título Mama, artista/albumArtist My Chemical Romance, álbum The Black Parade, año 2006 y duración 279027 ms. La caché contenía el vídeo cuadrado compartido: el fallo no era simplemente «álbum desconocido».

Se encontraron dos bloqueos distintos:

1. **Red medida:** el hotspot Wi-Fi de la Thor estaba marcado como metered. AppleCatalog bloqueaba la consulta local antes de buscar el álbum. El primer probe terminó en METADATA_READY → LOAD_ERROR. Solo Motion local solicitado con animación habilitada puede consultar ahora sobre red medida; offline sigue bloqueado. La selección de animaciones del usuario se respeta en MainViewModel/PlayerVisualPolicy. La política de las fuentes remotas y de asociaciones/imágenes automáticas conserva sus valores por defecto. La consulta explícita de Motion puede consumir datos, incluido el bundle del token si aún no está cacheado.
2. **Entrega del resultado cacheado:** `viewModelScope.launch` podía ejecutar el camino de caché sin suspender, antes de asignar el nuevo Job al campo. `nowPlayingArtJob?.isActive` consultaba el trabajo anterior, cancelado, y descartaba el resultado. Los logs mostraban CACHE_HIT sin URL_DELIVERED ni componente de vídeo. Ahora se valida la cancelación del contexto de la propia coroutine con `currentCoroutineContext().ensureActive()`.

La diferencia entre URLs verticales de ediciones y su cuadrado común, detectada anteriormente, sigue cubierta por la política local de fallback; no explicaba por sí sola el fallo físico. Se conserva esa corrección sin modificar la selección Motion remota.

## Identificación centrada en álbum

AppleCatalog busca álbum + artista del álbum antes de recurrir a canciones. LocalAlbumMatching normaliza Unicode, acentos, puntuación y espacios. Clasifica coincidencia exacta, misma raíz con Deluxe/Remastered/Expanded/Anniversary y similitud de edición suficientemente alta. Exige identidad de artista normalizada y rechaza álbumes ajenos o variantes live/instrumental/remix/tributo incompatibles.

Título de canción, año y posición de pista frente al trackCount aportan evidencia adicional cuando existen. Si hay álbum y albumArtist, el título de la canción puede faltar. Un álbum etiquetado sin correspondencia fiable no se sustituye por una recopilación de esa canción, y los errores transitorios no se guardan como ausencias de Motion. No se requiere duración exacta, álbum completo local ni relaciones completas de canciones. Se consultan hasta cuatro candidatos plausibles para acotar el tráfico. Se prefiere el recurso compartido; si no existe, se elige la edición con evidencia superior. Un empate contradictorio conserva la portada estática. Una edición plausible con Motion puede servir cuando otra no ofrece vídeo.

La caché conserva su almacenamiento y registros, con revisión de evidencia `motion-album-v4` para volver a resolver entradas anteriores. Mantiene storefront en la clave y los límites de reutilización de seis horas para positivos/quince minutos para negativos y búsquedas vacías.

## Evidencia de renderizado

El probe debug utiliza LocalVisualResolver → AppleCatalog → PlayerVisualPolicy → **MotionCover real**, sin sesión de audio. Debe recibir `onRenderedFirstFrame`; obtener una URL no basta.

La interfaz normal también se ensaya, incluyendo la entrega de caché desde MainViewModel. Los diagnósticos usan etapas, hashes efímeros y códigos de error; omiten nombres, rutas, URLs, tokens y credenciales. READY/FIRST_FRAME se registran una vez por componente, con timeout de primer fotograma. Los contadores debug son acotados al enum de etapas.

El primer fotograma se comprobó en la Thor tanto en el probe como en la interfaz normal, incluidos los cambios de pantalla. La portada incrustada permanece debajo y el vídeo se retira visualmente ante fallo de reproducción. Se mantienen vídeo 1:1 en landscape, fallback local vertical, exclusión con videoclip y audio independiente.

## Arquitectura del modo inferior

`ThorPresentation` es un StateFlow de presentación **solo durante la sesión del proceso**, inicialmente DUAL. No escribe preferencias de reproducción ni de cola. Al reiniciar el proceso, la política segura es volver al modo dual configurado por el usuario.

- **DUAL:** MainActivity permanece como reproductor superior; ThorActivity es navegación/herramientas inferior.
- **LOWER_ONLY:** la misma ThorActivity cambia a SquareApp completo con PlayerScreen convencional. MainActivity retira su composición visual y mueve su tarea al fondo, liberando el display superior. No controla el launcher ni inicia otras aplicaciones.
- **Retorno:** ThorDisplayManager trae al frente la MainActivity existente mediante su configuración singleTask y el display original. ThorActivity también es singleTask: se reutiliza la misma instancia.

Si no puede abrirse el panel o Android rechaza ocultar la tarea superior, la selección vuelve a DUAL. PlaybackService no se cierra ni se reinicia. Cada pantalla mantiene su conexión de controlador; el servicio continúa siendo dueño de una sola MediaSession y motor.

Se mantiene un único Animatable de expansión por SquareApp y se recalculan los estados derivados de NowPlayingSheet al cambiar su progreso. La primera prueba detectó una hoja inferior que seguía leyendo el progreso antiguo; se corrigió antes del ensayo final.

## Interfaz y acceso

El botón discreto de cambio de pantalla vive en la barra del reproductor Thor; cambia su descripción entre «Reproductor en pantalla inferior» y «Restaurar modo dual-screen». Existe también un interruptor de sesión en Ajustes del modo Thor, útil cuando no hay canción abierta.

La pantalla inferior reutiliza la composición STANDARD landscape de Square: portada/transportes/metadatos/progreso a la izquierda, letras o panel seleccionado a la derecha. Añade los controles originales de aleatorio y repetición a esa tarjeta. Usa las dimensiones/configuración reales, pesos y paneles originales, sin duplicar el reproductor superior. En ventanas más estrechas se conserva la adaptación vertical original de Square.

Se puede cerrar el reproductor para acceder a Inicio, Novedades, Radio cuando corresponda, Biblioteca, Buscar y Ajustes; los paneles originales conservan Cola, Efectos/EQ, Información y controles de letras/traducción. La sesión ThorInputSession permanece conectada también en modo STANDARD inferior. Tacto conserva la ruta que limpia foco de mando y detiene desplazamiento continuo.

## Pruebas y límites

Se ejecutó `./gradlew :app:testDebugUnitTest :app:assembleDebug --no-configuration-cache`. Las pruebas JVM incluyen álbum aislado, similitud, ediciones, artista incorrecto, recursos compartidos, Mama, caché/storefront, política de red y transiciones del estado de presentación. Resultado final: 339 pruebas, 0 fallos, 0 errores, 0 omitidas. [Resultado automatizado](validation/MOTION-ALBUM-LOWER-AUTOMATED.txt).

Los ensayos físicos son acotados: Mama, sesión restaurada pausada, red medida y ambas pantallas encendidas. El runner usa los botones reales, compara las mismas instancias de actividad, cola/índice, posición y playWhenReady y exige FIRST_FRAME en la interfaz normal. Se comprobó además una sola sesión en dumpsys y launcher visible arriba durante LOWER_ONLY.

Pendiente de validación interactiva del usuario: reproducción continua y cambios de tema durante periodos largos; D-Pad/sticks/A/B/L1/R1/L2/R2/START/SELECT físicos en ambos modos; traducción y scroll prolongado; todas las fuentes/rutas, cambios de red y fallos de decoder; ventanas estrechas/multiwindow, retirada de display y restricciones de otros launchers. No se certifican estas combinaciones por los resultados JVM ni por el ensayo táctil acotado. El fallo de arranque en segundo plano documentado previamente no se abordó aquí.

## Archivos

- Motion: AppleCatalog, MainViewModel, LocalMotionPolicy, nuevo LocalAlbumMatching, LocalVisualDiagnostics y MotionCover.
- Presentación: nuevo ThorPresentation, ThorDisplayManager, MainActivity, ThorActivity, SquareApp, PlayerScreen, NowPlayingSheet, SettingsScreen, manifest y textos EN/ES.
- Pruebas: LocalAlbumMatchingTest, ThorPresentationTest, actividad/probe solo debug, MotionSmoke y ThorModeSmoke; manifest y README del runner.
- Se conservaron los cambios anteriores en Librespot/Rust y componentes de Square 2.6.2; esta tarea no añade cambios a esos archivos.

## Evidencias

- [Probe antes de corregir la red medida](validation/MOTION-ALBUM-PHYSICAL-RED.txt) y [primer fotograma después](validation/MOTION-ALBUM-FIRST-FRAME.txt).
- [Cadena de entrega y renderizado normal](validation/MOTION-ALBUM-RENDER-TRACE.txt).
- [Ensayo táctil de modos con identidad de actividades y conservación de sesión](validation/THOR-LOWER-MODE-INSTRUMENTED.txt).
- [Actividades por display](validation/THOR-LOWER-DISPLAY-ACTIVITIES.txt): ThorActivity visible abajo y launcher arriba durante el modo inferior; [sesiones de SquareDS: una](validation/THOR-LOWER-SESSION-COUNT.txt).
- [Interfaz inferior con cola y controles](screenshots/motion-album-lower/thor-lower-player-queue.png); [recurso de Mama renderizado por el probe](screenshots/motion-album-lower/mama-first-frame-probe.png).

Una repetición del runner encontró un selector de accesibilidad ambiguo. Se acotó al paquete SquareDS, nodos visibles y zona de pestañas; la repetición final pasó. No fue un cierre de la aplicación. Se retiraron el runner temporal y sus capturas de la caché del dispositivo; se dejó la app en modo dual, sin órdenes de reproducción.

## APK ARM64

- [app-arm64-v8a-debug.apk](../app/build/outputs/apk/debug/app-arm64-v8a-debug.apk), 61,120,680 bytes.
- SHA-256: `70589e0b3c5e499d6217b1ebb9bdb13693a71e43e2187d475d73d6a52cce52a9`.
- Paquete `dev.lelonio.square`, versión `2.6.1-squareds.2`, código 59.
- Certificado SHA-256 compatible: `25dc740e92ccfb79abe59ff5b2ac4a7853037d9237ab65b74ab924984e04bb3c`.

Sin commits, push ni Release. Se entrega para completar la validación física interactiva pendiente.

[Comprobación final de instalación](validation/MOTION-LOWER-FINAL-INSTALL.txt): APK instalado con el mismo SHA-256 del entregado, primer fotograma en la interfaz normal y cero FATAL EXCEPTION del proceso final. Graphify actualizado con `graphify update .`; se contrastó la extracción con el código fuente. `git diff --check` satisfactorio.
