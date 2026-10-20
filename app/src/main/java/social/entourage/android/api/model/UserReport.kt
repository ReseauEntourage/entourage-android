package social.entourage.android.api.model
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as KSerializable


/**
 * Created by Mihai Ionescu on 12/04/2018.
 */
@KSerializable
class UserReport(var message: String = "", var signals: MutableList<String> = mutableListOf())

@KSerializable
class UserReportWrapper(
    @SerialName("user_report") var userReport: UserReport
)