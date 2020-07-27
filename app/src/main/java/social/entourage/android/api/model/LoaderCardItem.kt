package social.entourage.android.api.model

import java.util.*

class LoaderCardItem : TimestampedObject() {
    override fun getTimestamp(): Date? {
        return null
    }

    override fun hashString(): String {
        return "LoaderCardItem"
    }

    override fun getType(): Int {
        return LOADER_CARD
    }

    override fun getId(): Long {
        return 0
    }
}