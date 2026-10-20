package social.entourage.android.api.model
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as KSerializable


/**
 * Created by Jr (MJ-DEVS) on 26/06/2020.
 */

@KSerializable
class EntourageReport(var message: String = "") {

    // ----------------------------------
    // WRAPPER
    // ----------------------------------
    @KSerializable
    class EntourageReportWrapper(@SerialName("entourage_report") var entourageReport: EntourageReport)

}