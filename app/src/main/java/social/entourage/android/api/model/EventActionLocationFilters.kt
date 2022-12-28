package social.entourage.android.api.model

import android.content.Context
import social.entourage.android.EntourageApplication
import social.entourage.android.R

/**
 * Created by Me on 22/09/2022.
 */
class EventActionLocationFilters() : java.io.Serializable {
    private var radius_distance:Int? = null
    private var address: Address? = null
    private var shortname:String? = null

    private var selectedType: EventFilterType = EventFilterType.PROFILE

    init {
        setCurrentProfileFilters()
    }

    fun resetToDefault() {
        setCurrentProfileFilters()
    }

    fun getFilterButtonString(context: Context) : String {
        if (!shortname.isNullOrEmpty() && radius_distance != null) {
            return "${shortname!!} - ${radius_distance!!} km"
        }
        return context.getString(R.string.location)
    }

    fun modifyRadius(radius:Int) {
        radius_distance = radius
    }

    fun modifyType(type: EventFilterType) {
        this.selectedType = type
    }

    fun modifyAddress(address: Address?) {
        this.address = address
    }
    fun modifiyShortname(shortname:String?) {
        this.shortname = shortname
    }

    fun modifyFilter(shortname:String?, address: Address?, radius_distance:Int?, type: EventFilterType) {
        this.selectedType = type
        this.shortname = shortname
        this.address = address
        this.radius_distance = radius_distance
    }

    fun filterType() : EventFilterType {
        return  selectedType
    }

    fun addressName() : String {
        return address?.displayAddress ?: "-"
    }

    fun shortName() : String {
        return shortname ?: "-"
    }
    fun travel_distance() : Int {
        return radius_distance ?: 0
    }

    fun latitude() : Double? {
        return address?.latitude
    }

    fun longitude() : Double? {
        return address?.longitude
    }

    fun validate() : Boolean {
        if (address == null || radius_distance == null) return false
        return true
    }

    private fun setCurrentProfileFilters() {
        val user = EntourageApplication.get().me()
        user?.let { user ->
            user.address?.let { address ->
                this.address = Address(address.latitude,address.longitude,address.displayAddress)
                shortname = address.displayAddress
            }
            radius_distance = user.travelDistance
            selectedType = EventFilterType.PROFILE
        }
    }
}

enum class EventFilterType {
    PROFILE,
    GOOGLE,
    GPS
}