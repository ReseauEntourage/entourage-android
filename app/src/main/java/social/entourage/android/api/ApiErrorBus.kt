package social.entourage.android.api

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class HttpApiError(val code: Int)

object ApiErrorBus {
    private val _errors = MutableSharedFlow<HttpApiError>(extraBufferCapacity = 1)
    val errors: SharedFlow<HttpApiError> = _errors.asSharedFlow()

    fun emit(error: HttpApiError) {
        _errors.tryEmit(error)
    }
}
