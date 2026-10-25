package dev.lelonio.square.thor

import androidx.compose.animation.core.Animatable
import dev.lelonio.square.data.MusicSource
import dev.lelonio.square.ui.Routes
import dev.lelonio.square.ui.SourceTabs
import dev.lelonio.square.ui.navigationChromeAlpha
import dev.lelonio.square.ui.restoreThorNavigationChrome
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ThorNavigationChromeTest {
    @Test fun lowerToDualRestoresNavigationAndClosesOnlyTheObsoleteOverlay() = runBlocking {
        val player = Animatable(1f)
        val tools = Animatable(1f)
        var toolsOpen = true
        restoreThorNavigationChrome(false, true, player, tools) {
            assertEquals(0f, player.value, 0f)
            assertEquals(0f, tools.value, 0f)
            toolsOpen = false
        }
        assertEquals(0f, player.value, 0f)
        assertEquals(0f, tools.value, 0f)
        assertFalse(toolsOpen)
        assertEquals(1f, navigationChromeAlpha(false, player.value, 1f), 0f)
    }
    @Test fun dualToLowerPreservesFullPlayerAndRestoresBarWhenItIsCollapsed() = runBlocking {
        val player = Animatable(0f)
        val tools = Animatable(1f)
        var toolsOpen = true
        restoreThorNavigationChrome(true, true, player, tools) { toolsOpen = false }
        assertEquals(1f, player.value, 0f)
        assertEquals(0f, tools.value, 0f)
        assertFalse(toolsOpen)
        assertEquals(0f, navigationChromeAlpha(true, player.value, 1f), 0f)
        player.snapTo(0f)
        assertEquals(1f, navigationChromeAlpha(true, player.value, 1f), 0f)
    }
    @Test fun dualBarDoesNotReadAStalePlayerProgressWhileTheModeEffectSettles() {
        assertEquals(1f, navigationChromeAlpha(false, 1f, 1f), 0f)
        assertEquals(0f, navigationChromeAlpha(false, 1f, 0f), 0f) // Settings keeps its original policy.
    }
    @Test fun emptyQueueNeverLeavesNavigationBehindAnUndrawnPlayer() = runBlocking {
        for (lowerPlayer in listOf(true, false)) {
            val player = Animatable(1f)
            val tools = Animatable(1f)
            restoreThorNavigationChrome(lowerPlayer, false, player, tools) {}
            assertEquals(0f, player.value, 0f)
            assertEquals(0f, tools.value, 0f)
            assertEquals(1f, navigationChromeAlpha(false, player.value, 1f), 0f)
        }
    }
    @Test fun repeatedSwitchesPreserveEverySourcesDestinationsAndSelectedRoute() = runBlocking {
        for (source in MusicSource.values()) {
            val routes = SourceTabs.routes(source)
            val selected = routes[2]
            val player = Animatable(0f)
            val tools = Animatable(0f)
            repeat(3) {
                restoreThorNavigationChrome(true, true, player, tools) {}
                player.snapTo(0f)
                assertEquals(1f, navigationChromeAlpha(true, player.value, 1f), 0f)
                restoreThorNavigationChrome(false, true, player, tools) {}
                assertEquals(1f, navigationChromeAlpha(false, player.value, 1f), 0f)
                assertEquals(routes, SourceTabs.routes(source))
                assertEquals(selected, SourceTabs.valid(source, selected))
            }
            val expected = if (source == MusicSource.LOCAL)
                listOf(Routes.HOME, Routes.NEW, Routes.LIBRARY, Routes.SEARCH)
            else listOf(Routes.HOME, Routes.NEW, Routes.RADIO, Routes.LIBRARY, Routes.SEARCH)
            assertEquals(expected, routes)
        }
    }
}
