package social.entourage.android.api.model

import android.content.Context

/**
 * Created by mihaiionescu on 01/02/2017.
 */
abstract class BaseOrganization {
    abstract fun getTypeAsString(context: Context): String
    abstract val name: String?
    abstract val largeLogoUrl: String?

    /*class CustomComparator : Comparator<BaseOrganization> {
        override fun compare(organization1: BaseOrganization, organization2: BaseOrganization): Int {
            val name1 = organization1.name ?: return -1
            val name2 = organization2.name ?: return 1
            return name1.compareTo(name2)
        }
    }*/

    companion object {
        const val TYPE_ORGANIZATION = 0
        const val TYPE_PARTNER = 1
    }
}