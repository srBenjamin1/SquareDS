# Comprobación instrumentada local

Runner basado únicamente en `android.app.Instrumentation` y UiAutomation del SDK. No modifica Gradle, la aplicación de producción ni sus dependencias. El APK auxiliar usa la clave debug estándar, que debe coincidir con el APK debug instalado.

Usarlo exclusivamente en un emulador desechable con Música local configurada, permiso de audio concedido y fixtures sintéticos indexados. El runner rechaza hardware diferente de ranchu/goldfish. Cambia `player_open` a false para que el reproductor restaurado no tape los controles que se prueban.

Fixtures de esta ejecución: tonos generados con ffmpeg, una canción de Billie Eilish (`BIRDS OF A FEATHER`, `HIT ME HARD AND SOFT`), 16 álbumes de `ZZZ Sample` llamados `Album 01` a `Album 16`, y muestras con metadatos de Ariana Grande, Dua Lipa y Taylor Swift. Son tonos, no copias de sus grabaciones. El modo `layout-nomini` vacía únicamente la cola sintética del emulador a través de la MediaSession existente y comprueba que el mini-reproductor desaparece. No ejecutarlo con una cola que se quiera conservar.

La prueba de menú espera la identidad verificada de Billie Eilish; requiere acceso al catálogo o esa decisión ya guardada en el emulador. `layout` espera que `Midnights` sea el último álbum por orden alfabético y que exista el mini-reproductor.

```bash
bash tools/local-visuals-smoke/build.sh
adb -s emulator-5554 install --no-incremental -r /tmp/squareds-visual-smoke/smoke.apk
adb -s emulator-5554 shell am instrument -w -e mode conventional \
  dev.lelonio.square.visualsmoke/dev.lelonio.square.visualsmoke.VisualSmoke
adb -s emulator-5554 shell am instrument -w -e mode thor \
  dev.lelonio.square.visualsmoke/dev.lelonio.square.visualsmoke.VisualSmoke
adb -s emulator-5554 shell am instrument -w -e mode layout \
  dev.lelonio.square.visualsmoke/dev.lelonio.square.visualsmoke.VisualSmoke
adb -s emulator-5554 shell am instrument -w -e mode layout-nomini \
  dev.lelonio.square.visualsmoke/dev.lelonio.square.visualsmoke.VisualSmoke
```

El script de compilación admite `ANDROID_SDK_ROOT`/`ANDROID_HOME` y un directorio temporal de salida como primer argumento. Emplea el SDK 37.0 y build-tools 36.0.0 existentes en este proyecto.

Exigir `PASS total=…` y ausencia de `FAIL`/`Process crashed`: el código de salida del comando `am` por sí solo no prueba que las aserciones hayan pasado. No se omiten controles si no aparecen. Las esperas de controles son acotadas. El foco de Compose se verifica por el diálogo que activa A; no se confunde con el foco de accesibilidad.

Se comprueban cuadrícula sin controles de asociación, Info original, cierre conservando el perfil, D-Pad/A, B y desplazamientos de stick. El modo layout comprueba límites de la última tarjeta frente a la barra y guarda capturas. El runner inicia la actividad Thor en el mismo display del emulador; esto no reproduce dos pantallas físicas ni el firmware AYN Thor.

Las capturas se guardan en `files/visual-smoke-*.png` del paquete objetivo y se pueden recuperar con `adb exec-out run-as dev.lelonio.square cat …`. No se leen preferencias de credenciales, archivos musicales originales ni tokens. Los resultados de catálogo y los fallos MediaCodec se comprueban por separado mediante los logs seguros de la aplicación.

## Regresión de Biblioteca

`LibrarySmoke` usa las utilidades del runner anterior y abre la pestaña real mediante tacto. La prueba de Biblioteca no requiere los fixtures de artistas/Motion descritos arriba. Preparar un emulador nuevo con el APK debug instalado, conceder READ_MEDIA_AUDIO e instalar el APK auxiliar reconstruido con `build.sh`. No usar una instalación con datos que se quieran conservar.

```bash
adb -s emulator-5554 shell pm grant dev.lelonio.square android.permission.READ_MEDIA_AUDIO
python tools/local-visuals-smoke/run-library.py
```

La primera matriz exige un índice local vacío. Después, generar un FLAC de silencio con ffmpeg, título `Library regression song`, artista `Library regression artist` y álbum `Library regression album`, copiarlo a Music e indexarlo mediante MediaStore. Ejecutar:

```bash
python tools/local-visuals-smoke/run-library.py --populated
```

El script acepta `--source LOCAL|SPOTIFY|YOUTUBE_MUSIC`, `--output` y `ANDROID_SERIAL` (solo `emulator-*`). Guarda el resultado de cada caso en `/tmp/squareds-library-verification` y comprueba el marcador de éxito y la ausencia de FAIL/Process crashed.

La selección de fuente usa PreferencesStore real, y la navegación se ejecuta sobre SquareApp y NavHost reales. En Spotify y YouTube se suministra por reflexión un `MainViewModel.UiState.Ready` vacío o con una playlist sintética; no se simula autenticación ni se contacta una cuenta real para obtener sus playlists. Se verifican los recuentos, filtros y alternancia grid/list. En local se usa el índice real y se abre/regresa del álbum del FLAC sintético. Finalmente se visita Buscar, se vuelve mediante Inicio (Buscar sustituye la tira completa por su campo) y se reabre Biblioteca.

Los datos de prueba solo se modifican en el emulador desechable. Las capturas quedan como `files/visual-smoke-library-{source}-{empty|populated}.png` en el paquete objetivo. Esto cubre navegación y renderizado de las bibliotecas, no la carga de cuentas remotas reales ni las dos pantallas físicas de Thor.

Para reproducir el cambio de cinco pestañas remotas a cuatro locales sin reiniciar la actividad:

```bash
python tools/local-visuals-smoke/run-library.py --source LOCAL --switch-from YOUTUBE_MUSIC
```

`--switch-from` inicia la fuente indicada y cambia PreferencesStore dentro de la misma actividad antes de abrir Biblioteca. La aserción exige que aparezca el título de Biblioteca en la mitad superior de la ventana: encontrar únicamente la etiqueta de la pestaña inferior no basta. Este caso falló en el primer APK corregido y pasó al recrear los controles de pestañas cuando cambia `visibleTabs`.

## Motion local y modo inferior en Thor

Los runners `MotionSmoke` y `ThorModeSmoke` son una excepción al requisito de emulador de los fixtures anteriores: admiten hardware real y no cambian preferencias, cola ni posición ni ordenan reproducir audio. Instrumentation inicia su proceso de prueba; no se deben ejecutar mientras se depura otro proceso de la app.

```bash
adb -s SERIAL install --no-incremental -r /tmp/squareds-visual-smoke/smoke.apk
adb -s SERIAL shell am instrument -w \
  dev.lelonio.square.visualsmoke/dev.lelonio.square.visualsmoke.MotionSmoke
adb -s SERIAL shell am instrument -w \
  dev.lelonio.square.visualsmoke/dev.lelonio.square.visualsmoke.ThorModeSmoke
```

`MotionSmoke` usa la actividad exclusivamente debug, no exportada, `MotionProbeActivity`. Resuelve el álbum de Mama desde los metadatos locales (o metadatos de prueba en un emulador), pasa el recurso por PlayerVisualPolicy y compone MotionCover real. Exige el callback de primer fotograma y espera la aparición antes de capturar `cache/motion-first-frame.png`. No crea una MediaSession ni un reproductor de audio. Puede consumir datos para consultar el catálogo y mostrar el vídeo si la conexión es medida.

`ThorModeSmoke` requiere modo Thor habilitado, dos displays encendidos y una cola restaurada. Usa los botones accesibles reales para alternar modos, toca Cola/Efectos/Créditos/Letra y confirma las mismas instancias de MainActivity/ThorActivity, cola/índice, posición y playWhenReady. Finalmente exige URL_DELIVERED y FIRST_FRAME en la interfaz normal. No representa un ensayo de audio continuo cuando la sesión inicial está pausada. El runner no sustituye las pruebas manuales de mando ni las del launcher/firmware.

Los marcadores PASS y ausencia de FAIL son obligatorios; el código de salida de `am instrument` no basta. La actividad de probe no forma parte de compilaciones Release. Se puede retirar el APK auxiliar con `adb uninstall dev.lelonio.square.visualsmoke` sin desinstalar la aplicación.
