package social.entourage.android.suggestions

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.api.model.Suggestion
import social.entourage.android.api.model.SuggestionsResponse
import timber.log.Timber

class SuggestionsViewModel(application: Application) : AndroidViewModel(application) {

    private val request = EntourageApplication.get().apiModule.suggestionsRequest

    val suggestions = MutableLiveData<List<Suggestion>>()
    val isLoading = MutableLiveData<Boolean>()
    val hasError = MutableLiveData<Boolean>()

    private var currentPage = 1
    private var totalPages = 1
    private val perPage = 10

    val canLoadMore: Boolean
        get() = currentPage < totalPages

    fun loadSuggestions(reset: Boolean = false) {
        if (isLoading.value == true) return

        if (reset) {
            currentPage = 1
            totalPages = 1
        }

        if (!reset && !canLoadMore) return

        isLoading.value = true
        hasError.value = false

        request.getSuggestions(page = currentPage, per = perPage)
            .enqueue(object : Callback<SuggestionsResponse> {
                override fun onResponse(
                    call: Call<SuggestionsResponse>,
                    response: Response<SuggestionsResponse>
                ) {
                    isLoading.value = false
                    if (response.isSuccessful) {
                        val body = response.body()
                        val meta = body?.meta
                        if (meta != null) {
                            totalPages = meta.totalPages
                        }
                        val newItems = body?.suggestions ?: emptyList()
                        if (reset) {
                            suggestions.value = newItems
                        } else {
                            val current = suggestions.value ?: emptyList()
                            suggestions.value = current + newItems
                        }
                        if (reset || canLoadMore.not()) {
                            // stay at current page for next call
                        }
                        currentPage++
                    } else {
                        hasError.value = true
                        Timber.e("getSuggestions error: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<SuggestionsResponse>, t: Throwable) {
                    isLoading.value = false
                    hasError.value = true
                    Timber.e(t, "getSuggestions failure")
                }
            })
    }
}
