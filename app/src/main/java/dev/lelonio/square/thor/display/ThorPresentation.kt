package dev.lelonio.square.thor.display

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Session-only: a cold process always restores the safe dual-screen presentation. */
object ThorPresentation {
    enum class Mode { DUAL, LOWER_ONLY }
    private val _mode = MutableStateFlow(Mode.DUAL)
    val mode = _mode.asStateFlow()
    fun select(mode: Mode) { _mode.value = mode }
    fun reset() = select(Mode.DUAL)
}
