package social.entourage.android.new_v8.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by  on 08/11/2022.
 */
class HomeAction : Serializable {
    @SerializedName("name")
    var name:String = ""
    @SerializedName("image_url")
    var action_url:String? = null
    @SerializedName("type")
    private var typeString:String? = null
    @SerializedName("action")
    private var actionString:String? = null
    @SerializedName("params")
    var params:HomeActionParams? = null

    fun getAction() : ActionSummary {
        return when(actionString) {
            "show" -> ActionSummary.SHOW
            "new" -> ActionSummary.CREATE
            //index
            else -> ActionSummary.INDEX
        }
    }

    fun getType() : HomeType {
        return when(typeString) {
            "profile" -> HomeType.PROFILE
            "neighborhood" -> HomeType.NEIGHBORHOOD
            "outing" -> HomeType.OUTING
            "resource" -> HomeType.RESOURCE
            "conversation" -> HomeType.CONVERSATION
            "contribution" -> HomeType.CONTRIBUTION
            "solicitation" -> HomeType.SOLICITATION
            "poi" -> HomeType.POI
            "webview" -> HomeType.WEBVIEW
            //User
            else -> HomeType.USER
        }
    }

}