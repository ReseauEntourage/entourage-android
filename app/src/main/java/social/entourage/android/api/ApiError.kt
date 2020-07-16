package social.entourage.android.api

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import retrofit2.Response
import java.io.IOException

class ApiError private constructor(code: String) {
    val code: String?

    private class ApiErrorContainer {
        val error: ApiError? = null
    }

    companion object {
        fun fromResponse(response: Response<*>): ApiError {
            response.errorBody()?.string()?.let {
                try {
                    val error: ApiError? = Gson().fromJson(it, ApiErrorContainer::class.java).error
                    if (error?.code != null) {
                        return error
                    }
                } catch (e: IOException) {
                } catch (e: JsonSyntaxException) {
                } catch (e: NullPointerException) {
                }
            }
            return ApiError("OTHER")
        }
    }

    init {
        this.code = code
    }
}