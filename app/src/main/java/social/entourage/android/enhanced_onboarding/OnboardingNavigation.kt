package social.entourage.android.enhanced_onboarding

import android.os.Parcel
import android.os.Parcelable

sealed class OnboardingNavigation : Parcelable {

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(getTypeId())
    }

    protected abstract fun getTypeId(): Int

    companion object CREATOR : Parcelable.Creator<OnboardingNavigation> {
        const val TYPE_HOME = 0
        const val TYPE_WELCOME_GROUP = 1
        const val TYPE_EVENTS = 2
        const val TYPE_DONATIONS = 3
        const val TYPE_CREATE_ACTION_DEMAND = 4
        const val TYPE_QUIZ = 5
        const val TYPE_PROFILE = 6

        override fun createFromParcel(source: Parcel): OnboardingNavigation {
            return when (source.readInt()) {
                TYPE_WELCOME_GROUP -> WelcomeGroup
                TYPE_EVENTS -> Events
                TYPE_DONATIONS -> Donations
                TYPE_CREATE_ACTION_DEMAND -> CreateActionDemand
                TYPE_QUIZ -> Quiz
                TYPE_PROFILE -> Profile
                else -> Home
            }
        }

        override fun newArray(size: Int): Array<OnboardingNavigation?> {
            return arrayOfNulls(size)
        }
    }

    object Home : OnboardingNavigation() {
        override fun getTypeId() = TYPE_HOME
    }

    object WelcomeGroup : OnboardingNavigation() {
        override fun getTypeId() = TYPE_WELCOME_GROUP
    }

    object Events : OnboardingNavigation() {
        override fun getTypeId() = TYPE_EVENTS
    }

    object Donations : OnboardingNavigation() {
        override fun getTypeId() = TYPE_DONATIONS
    }

    object CreateActionDemand : OnboardingNavigation() {
        override fun getTypeId() = TYPE_CREATE_ACTION_DEMAND
    }

    object Quiz : OnboardingNavigation() {
        override fun getTypeId() = TYPE_QUIZ
    }

    object Profile : OnboardingNavigation() {
        override fun getTypeId() = TYPE_PROFILE
    }
}
