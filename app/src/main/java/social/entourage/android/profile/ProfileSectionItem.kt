package social.entourage.android.profile

sealed class ProfileSectionItem {
    data class Separator(val title: String) : ProfileSectionItem()
    data class Item(
        val iconRes: Int, // Drawable resource ID
        val title: String,
        val subtitle: String
    ) : ProfileSectionItem()
}