package io.artly.data

import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NoticesTest {

    @Test
    fun remembersDismissedNotices() {
        val settings = MapSettings()
        val notices = Notices(settings)
        assertFalse(notices.placeNamesDismissed.value)

        notices.dismissPlaceNames()

        assertTrue(notices.placeNamesDismissed.value)
        assertTrue(Notices(settings).placeNamesDismissed.value)
    }
}
