package social.entourage.android.afterLogin.groups

import android.os.Parcel
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.api.model.GroupMember
import social.entourage.android.api.model.Status
import social.entourage.android.api.model.notification.Translation
import social.entourage.android.groups.GroupModel

@SmallTest
@RunWith(AndroidJUnit4::class)
class GroupModelParcelableTest {

    private fun parcelRoundTrip(original: GroupModel): GroupModel {
        val parcel = Parcel.obtain()
        try {
            original.writeToParcel(parcel, 0)
            parcel.setDataPosition(0)
            return GroupModel.CREATOR.createFromParcel(parcel)
        } finally {
            parcel.recycle()
        }
    }

    @Test
    fun writeToParcel_thenCreateFromParcel_preservesAllFields_withUuidV2() {
        val original = GroupModel(
            id = 42,
            name = "Groupe de test",
            nameTranslations = Translation("Test group", "Groupe de test", "fr", "en"),
            uuid_v2 = "3f6a5b2e-1234-4a11-9abc-000000000001",
            members_count = 12,
            address = "12 rue de Paris",
            interests = mutableListOf("sport", "cuisine"),
            description = "Description du groupe",
            descriptionTranslations = Translation(
                "Group description",
                "Description du groupe",
                "fr",
                "en"
            ),
            members = mutableListOf(
                GroupMember(
                    id = 1,
                    displayName = "Jean Dupont",
                    avatarUrl = "https://example.com/a.png",
                    isBirthday = true
                )
            ),
            member = true,
            admin = true,
            recurrence = 7,
            status = Status.OPEN,
        )

        val restored = parcelRoundTrip(original)

        Assert.assertEquals(original, restored)
    }

    @Test
    fun writeToParcel_thenCreateFromParcel_preservesAllFields_withoutUuidV2() {
        val original = GroupModel(
            id = 43,
            name = "Autre groupe",
            nameTranslations = null,
            uuid_v2 = null,
            members_count = 3,
            address = "5 avenue de Lyon",
            interests = mutableListOf("lecture"),
            description = "Autre description",
            descriptionTranslations = null,
            members = mutableListOf(),
            member = false,
            admin = false,
            recurrence = 0,
            status = Status.CLOSED,
        )

        val restored = parcelRoundTrip(original)

        Assert.assertEquals(original, restored)
    }
}