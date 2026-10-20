package social.entourage.android.api

import retrofit2.Response
import java.io.IOException
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import social.entourage.android.api.json.AppJson

@Serializable
class ApiError private constructor(val code: String? = null) {

    @Serializable
    private class ApiErrorContainer {
        val error: ApiError? = null
    }

    companion object {
        fun fromResponse(response: Response<*>): ApiError {
            response.errorBody()?.string()?.let {
                try {
                    val error: ApiError? = AppJson.decode<ApiErrorContainer>(it).error
                    if (error?.code != null) {
                        return error
                    }
                } catch (e: IOException) {
                } catch (e: SerializationException) {
                } catch (e: NullPointerException) {
                }
            }
            return ApiError("OTHER")
        }
    }

}