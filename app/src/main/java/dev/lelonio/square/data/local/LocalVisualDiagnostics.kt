package dev.lelonio.square.data.local

import android.util.Log
import dev.lelonio.square.BuildConfig

/** Deliberately omits metadata, paths, URLs, bearer tokens and exception messages. */
object LocalVisualDiagnostics {
    enum class Stage {
        METADATA_READY, SEARCH_RESULTS, VIDEO_METADATA, MISSING_METADATA, NO_MATCH, AMBIGUOUS, ALBUM_IDENTIFIED, NO_MOTION,
        MOTION_AVAILABLE, CACHE_HIT, ANIMATION_DISABLED, OFFLINE,
        BLOCKED_VISUAL_MODE, LOAD_ERROR, FIRST_FRAME, ALBUM_SEARCH, URL_DELIVERED, COMPONENT_CREATED, SURFACE_ATTACHED, PLAYER_READY, FIRST_FRAME_TIMEOUT
    }
    private val counts = java.util.concurrent.atomic.AtomicIntegerArray(Stage.values().size)
    fun count(stage: Stage): Int = counts.get(stage.ordinal)
    fun record(stage: Stage, key: String, count: Int? = null, code: Int? = null) {
        if (BuildConfig.DEBUG) {
            counts.incrementAndGet(stage.ordinal)
            Log.d("SquareDSLocalVisual", "stage=$stage key=${key.hashCode().toUInt().toString(16)}" +
            (count?.let { " count=$it" } ?: "") + (code?.let { " code=$it" } ?: ""))
        }
    }
}
