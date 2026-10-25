# SquareDS v1.4

SquareDS v1.4 incorpora el reproductor opcional en la pantalla inferior de AYN Thor, la resolución de Motion Artwork local por álbum y mejoras integradas selectivamente de Square 2.6.2.

## Cambios

- Reproductor en pantalla inferior: alternancia con el modo dual mediante un botón y una opción de Ajustes. Conserva la misma sesión, cola y posición; libera la pantalla superior sin controlar otras aplicaciones. Al reiniciar el proceso se restaura el modo dual.
- Interfaz inferior con portada, metadatos, progreso, transportes, aleatorio/repetición y paneles originales de letras, cola, efectos e información.
- Motion Artwork local centrado en álbum y artista, con normalización, ediciones compatibles y evidencia complementaria de canción/año/pista. Mantiene la portada incrustada cuando la asociación no es fiable.
- Corregidos el descarte de resultados de caché y el bloqueo de consultas de Motion local en redes medidas. La animación local solicitada puede consumir datos del hotspot; offline y las preferencias de animación se respetan.
- Corregida la restauración de la barra inferior y el mini-reproductor al cambiar de modo, incluidas colas vacías; destinos específicos para Spotify, YouTube Music y Música local conservados.
- Separación de 8 dp entre los botones de cierre y cambio de pantalla en el modo inferior.
- Mejoras de Square 2.6.2: continuación tras videoclip Spotify, duración propia del vídeo, seek en pausa, progreso Liquid Glass, swipe de portada, fallback de cristal en dispositivos de poca memoria, logos de artistas sobre datos móviles y correcciones de rechazo de claves/sesión desconectada.

## Validación y límites

- 344 pruebas JVM y compilación debug satisfactorias.
- Mama — My Chemical Romance — The Black Parade: primer fotograma comprobado físicamente en AYN Thor, tanto en el probe como en la interfaz normal.
- Cambios dual → inferior → dual comprobados con botones reales, mismas actividades, una sesión y cola/posición conservadas con reproducción pausada. Paneles Cola/Efectos/Créditos/Letra probados por tacto.
- La última corrección de restauración de barra está cubierta por cinco regresiones automatizadas; su validación física específica sigue pendiente.
- Los APK Release firmados se generan con GitHub Actions. Las pruebas físicas anteriores corresponden a compilaciones debug; no se afirma una prueba física de estos binarios Release. Quedan pendientes ensayos prolongados con reproducción continua, mando, las tres fuentes y cambios de red.

## Problema conocido

Sigue pendiente el cierre de arranque/restauración en segundo plano por BackgroundServiceStartNotAllowedException al conectar PlaybackService desde MainViewModel. La reparación de navegación no modifica ese flujo.

## Instalación

Versión Android: **2.6.2-squareds.1**, código **60**, con certificado compatible con las publicaciones anteriores de SquareDS.

**ARM64 recomendado para AYN Thor.** Se incluyen también ARMv7 y universal.
