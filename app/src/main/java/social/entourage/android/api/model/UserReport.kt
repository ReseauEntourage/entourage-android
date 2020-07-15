package social.entourage.android.api.model

import com.google.gson.annotations.SerializedName

/**
 * Created by Mihai Ionescu on 12/04/2018.
 */
class UserReport(var message: String)

class UserReportResponse(@field:SerializedName("user_report") var userReport: UserReport)