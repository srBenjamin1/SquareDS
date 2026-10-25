package dev.lelonio.square.debug

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import dev.lelonio.square.SquareApplication
import dev.lelonio.square.data.PreferencesStore
import dev.lelonio.square.data.local.LocalTrack
import dev.lelonio.square.ui.components.MotionCover
import dev.lelonio.square.ui.player.PlayerVisualPolicy
import kotlinx.coroutines.launch

/** Debug-only rendering probe. Never connects to or changes the audio session. */
class MotionProbeActivity : ComponentActivity() {
    private var status by mutableStateOf("Resolving album")
    private var url by mutableStateOf<String?>(null)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            dev.lelonio.square.ui.theme.SquareTheme(darkTheme = true) {
                Column(Modifier.fillMaxSize().padding(16.dp)) {
                    Text(status, color = androidx.compose.ui.graphics.Color.White)
                    val source = url
                    if (source != null) MotionCover(source, Modifier.weight(1f).fillMaxWidth(), fit = true,
                        onFirstFrame = { status = "PASS Motion first frame" },
                        onFailure = { status = "FAIL Motion decoder $it" })
                }
            }
        }
        lifecycleScope.launch {
            try {
                val app = application as SquareApplication
                val track = app.localMusic.awaitCatalog().allByUri.values.firstOrNull {
                    it.title.equals("Mama", true) && it.artist.equals("My Chemical Romance", true)
                } ?: LocalTrack(9999999, "", "Mama", "My Chemical Romance", "The Black Parade", "My Chemical Romance", durationMs = 279027, year = "2006")
                val found = app.localVisuals.motion(track)
                url = PlayerVisualPolicy.motionUrl(found, true, true, PreferencesStore.CoverSource.APPLE_MUSIC, true, false, true)
                status = if (url == null) "FAIL Motion no URL" else "Motion URL delivered"
            } catch (failure: Exception) {
                if (failure is kotlinx.coroutines.CancellationException) throw failure
                status = "FAIL Motion lookup ${failure.javaClass.simpleName}"
            }
        }
    }
}
