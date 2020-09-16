package social.entourage.android.api.model

import java.util.*

class LoaderCardItem : TimestampedObject() {
    override val timestamp: Date?
        get() = null

    override fun hashString(): String {
        return "LoaderCardItem"
    }

    override val type: Int
        get() = LOADER_CARD

    override val id: Long
        get() = 0
}