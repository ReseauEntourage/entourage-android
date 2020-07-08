package social.entourage.android.api.model

import com.google.gson.annotations.SerializedName

/**
 * Created by Jr (MJ-DEVS) on 26/06/2020.
 */

class EntourageReport(var message: String) {

    // ----------------------------------
    // WRAPPER
    // ----------------------------------
    class EntourageReportWrapper(@field:SerializedName("entourage_report") var entourageReport: EntourageReport)

}