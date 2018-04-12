package social.entourage.android.api.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Mihai Ionescu on 12/04/2018.
 */
public class UserReport {

    public String message;

    public UserReport(String message) {
        this.message = message;
    }

    // ----------------------------------
    // WRAPPER
    // ----------------------------------

    public static class UserReportWrapper {

        @SerializedName("user_report")
        UserReport userReport;

        public UserReportWrapper(UserReport userReport) {
            this.userReport = userReport;
        }

    }

}
