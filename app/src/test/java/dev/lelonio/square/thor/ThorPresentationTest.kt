package dev.lelonio.square.thor

import dev.lelonio.square.thor.display.ThorPresentation
import org.junit.After
import org.junit.Assert.*
import org.junit.Test

class ThorPresentationTest {
    @After fun reset() = ThorPresentation.reset()
    @Test fun lowerModeAndReturnAreSessionOnlyPresentationChanges() {
        ThorPresentation.reset()
        assertEquals(ThorPresentation.Mode.DUAL, ThorPresentation.mode.value)
        ThorPresentation.select(ThorPresentation.Mode.LOWER_ONLY)
        assertEquals(ThorPresentation.Mode.LOWER_ONLY, ThorPresentation.mode.value)
        ThorPresentation.select(ThorPresentation.Mode.LOWER_ONLY)
        assertEquals(ThorPresentation.Mode.LOWER_ONLY, ThorPresentation.mode.value)
        ThorPresentation.reset()
        assertEquals(ThorPresentation.Mode.DUAL, ThorPresentation.mode.value)
    }
}
