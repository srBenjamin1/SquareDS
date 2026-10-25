package dev.lelonio.square.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D

/** The lower activity changes presentation without replacing its navigation composition. */
internal suspend fun restoreThorNavigationChrome(
    lowerPlayer: Boolean,
    hasItem: Boolean,
    playerProgress: Animatable<Float, AnimationVector1D>,
    toolsProgress: Animatable<Float, AnimationVector1D>,
    closeTools: () -> Unit,
) {
    playerProgress.snapTo(if (lowerPlayer && hasItem) 1f else 0f)
    toolsProgress.snapTo(0f)
    // Notify after snapping: recomposition may start the tools' own closing animation.
    closeTools()
}

internal fun navigationChromeAlpha(expandedPlayerVisible: Boolean, playerProgress: Float, routeChrome: Float): Float =
    (if (expandedPlayerVisible) (1f - playerProgress * 3f).coerceIn(0f, 1f) else 1f) * routeChrome
