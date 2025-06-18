package social.entourage.android.small_talks

import android.app.Application
import androidx.collection.ArrayMap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.model.*
import social.entourage.android.api.model.MembersWrapper
import social.entourage.android.api.request.*
import social.entourage.android.enhanced_onboarding.InterestForAdapter
import timber.log.Timber
import java.io.File
import java.io.IOException
import social.entourage.android.api.model.UserSmallTalkRequestWithMatchData


data class SmallTalkStep(
    val title: String,
    val subtitle: String,
    val items: List<InterestForAdapter>
)

class SmallTalkViewModel(application: Application) : AndroidViewModel(application) {

    private val context = getApplication<Application>().applicationContext
    // Réseau SmallTalk API
    private val request: SmallTalkRequest = EntourageApplication.get().apiModule.smallTalkRequest
    val userRequest = MutableLiveData<UserSmallTalkRequest?>()
    val userRequests = MutableLiveData<List<UserSmallTalkRequest>>()
    val smallTalks = MutableLiveData<List<SmallTalk>>()
    val participants = MutableLiveData<List<User>>()
    val messages = MutableLiveData<List<Post>>()
    val createdMessage = MutableLiveData<Post?>()
    val matchResult = MutableLiveData<SmallTalkMatchResponse?>()
    val requestDeleted = MutableLiveData<Boolean>()
    val smallTalkDetail = MutableLiveData<SmallTalk?>()
    val almostMatches = MutableLiveData<List<UserSmallTalkRequestWithMatchData>>()
    val shouldLeave = MutableLiveData<Boolean>()
    val messageDeleted = MutableLiveData<Boolean>()
    private var currentPage = 1
    private val messagesPerPage = 50
    private var isLoading = false
    private var isLastPage = false

    private val steps = listOf(
        SmallTalkStep(
            title = context.getString(R.string.small_talk_step_title_1),
            subtitle = context.getString(R.string.small_talk_step_subtitle_1),
            items = listOf(
                InterestForAdapter(R.drawable.ic_duo, context.getString(R.string.small_talk_step1_item1_title), context.getString(R.string.small_talk_step1_item1_subtitle), false, "1"),
                InterestForAdapter(R.drawable.ic_quatuor, context.getString(R.string.small_talk_step1_item2_title), context.getString(R.string.small_talk_step1_item2_subtitle), false, "2")
            )
        ),
        SmallTalkStep(
            title = context.getString(R.string.small_talk_step_title_2),
            subtitle = context.getString(R.string.small_talk_step_subtitle_2),
            items = listOf(
                InterestForAdapter(R.drawable.ic_local, context.getString(R.string.small_talk_step2_item1_title), context.getString(R.string.small_talk_step2_item1_subtitle), false, "3"),
                InterestForAdapter(R.drawable.ic_global, context.getString(R.string.small_talk_step2_item2_title), context.getString(R.string.small_talk_step2_item2_subtitle), false, "4")
            )
        ),
        //OK here 999 is a fake ID in order to adapt the adaptable item in a way that the term adaptability means nothing , as the design want us to make sames things with multiple kind of non sens differents things
        SmallTalkStep(
            title = context.getString(R.string.small_talk_step_title_3),
            subtitle = context.getString(R.string.small_talk_step_subtitle_3),
            items = listOf(
                InterestForAdapter(R.drawable.ic_male, context.getString(R.string.small_talk_step3_item1_title), context.getString(R.string.small_talk_step3_item1_subtitle), false, "5"),
                InterestForAdapter(R.drawable.ic_female, context.getString(R.string.small_talk_step3_item2_title), context.getString(R.string.small_talk_step3_item2_subtitle), false, "6"),
                InterestForAdapter(R.drawable.ic_non_binary, context.getString(R.string.small_talk_step3_item3_title), context.getString(R.string.small_talk_step3_item3_subtitle), false, "7")
            )
        ),
        SmallTalkStep(
            title = context.getString(R.string.small_talk_step_title_4),
            subtitle = context.getString(R.string.small_talk_step_subtitle_4),
            items = listOf(
                InterestForAdapter(999, context.getString(R.string.small_talk_step4_item1_title), context.getString(R.string.small_talk_step4_item1_subtitle), false, "8"),
                InterestForAdapter(999, context.getString(R.string.small_talk_step4_item2_title), context.getString(R.string.small_talk_step4_item2_subtitle), false, "9")
            )
        ),
        SmallTalkStep(
            title = context.getString(R.string.small_talk_step_interest_title),
            subtitle = context.getString(R.string.small_talk_step_interest_subtitle),
            items = listOf(
                InterestForAdapter(R.drawable.ic_onboarding_interest_sport, context.getString(R.string.interest_sport), "", false, "sport"),
                InterestForAdapter(R.drawable.ic_onboarding_interest_name_animaux, context.getString(R.string.interest_animaux), "", false, "animaux"),
                InterestForAdapter(R.drawable.ic_onboarding_interest_name_rencontre_nomade, context.getString(R.string.interest_marauding), "", false, "marauding"),
                InterestForAdapter(R.drawable.ic_onboarding_interest_name_bien_etre, context.getString(R.string.interest_bien_etre), "", false, "bien-etre"),
                InterestForAdapter(R.drawable.ic_onboarding_interest_name_cuisine, context.getString(R.string.interest_cuisine), "", false, "cuisine"),
                InterestForAdapter(R.drawable.ic_onboarding_interest_name_art, context.getString(R.string.interest_culture), "", false, "culture"),
                InterestForAdapter(R.drawable.ic_onboarding_interest_name_nature, context.getString(R.string.interest_nature), "", false, "nature"),
                InterestForAdapter(R.drawable.ic_onboarding_interest_name_jeux, context.getString(R.string.interest_jeux), "", false, "jeux"),
                InterestForAdapter(R.drawable.ic_onboarding_interest_name_activite_manuelle, context.getString(R.string.interest_activites_onboarding), "", false, "activites"),
                InterestForAdapter(R.drawable.ic_onboarding_interest_name_autre, context.getString(R.string.interest_other), "", false, "other")
            )
        )
    )

    private val _currentStepIndex = MutableLiveData(0)
    val currentStepIndex: LiveData<Int> = _currentStepIndex

    private val _currentStep = MutableLiveData(steps[0])
    val currentStep: LiveData<SmallTalkStep> = _currentStep

    fun goToNextStep() {
        val nextIndex = (_currentStepIndex.value ?: 0) + 1
        if (nextIndex < steps.size) {
            _currentStepIndex.value = nextIndex
            _currentStep.value = steps[nextIndex]
        }
    }

    fun goToPreviousStep() {
        val current = _currentStepIndex.value ?: 0
        if (current > 0) {
            // On peut reculer d’une étape
            val previousIndex = current - 1
            _currentStepIndex.value = previousIndex
            _currentStep.value = steps[previousIndex]
        } else {
            // On est déjà à la 1ère étape, on quitte
            shouldLeave.postValue(true)
        }
    }

    fun getStepProgress(): Float {
        val current = (_currentStepIndex.value ?: 0) + 1
        val total = steps.size.toFloat()
        return (current / total) * 0.9f
    }

    fun isLastStep(): Boolean = (_currentStepIndex.value ?: 0) == steps.lastIndex



    fun listUserRequests() {
        request.listUserSmallTalkRequests().enqueue(object : Callback<UserSmallTalkRequestListWrapper> {
            override fun onResponse(call: Call<UserSmallTalkRequestListWrapper>, response: Response<UserSmallTalkRequestListWrapper>) {
                userRequests.value = response.body()?.requests ?: emptyList()
            }
            override fun onFailure(call: Call<UserSmallTalkRequestListWrapper>, t: Throwable) {
                userRequests.value = emptyList()
            }
        })
    }


    fun createRequest(req: UserSmallTalkRequest) {
        request.createUserSmallTalkRequest(UserSmallTalkRequestWrapper(req)).enqueue(object : Callback<UserSmallTalkRequestWrapper> {
            override fun onResponse(call: Call<UserSmallTalkRequestWrapper>, response: Response<UserSmallTalkRequestWrapper>) {
                userRequest.value = response.body()?.request
            }
            override fun onFailure(call: Call<UserSmallTalkRequestWrapper>, t: Throwable) {
                userRequest.value = null
            }
        })
    }

    fun updateRequest(id: String, updates: ArrayMap<String, Any>) {
        request.updateUserSmallTalkRequest(id, updates).enqueue(object : Callback<UserSmallTalkRequestWrapper> {
            override fun onResponse(call: Call<UserSmallTalkRequestWrapper>, response: Response<UserSmallTalkRequestWrapper>) {
                userRequest.value = response.body()?.request
            }
            override fun onFailure(call: Call<UserSmallTalkRequestWrapper>, t: Throwable) {
                userRequest.value = null
            }
        })
    }

    fun deleteRequest() {
        request.deleteUserSmallTalkRequest().enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                requestDeleted.value = response.isSuccessful
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                requestDeleted.value = false
            }
        })
    }

    fun matchRequest(id: String) {
        request.matchUserSmallTalkRequest(id).enqueue(object : Callback<SmallTalkMatchResponse> {
            override fun onResponse(call: Call<SmallTalkMatchResponse>, response: Response<SmallTalkMatchResponse>) {
                matchResult.value = response.body()
            }
            override fun onFailure(call: Call<SmallTalkMatchResponse>, t: Throwable) {
                matchResult.value = null
            }
        })
    }

    fun listSmallTalks() {
        request.listSmallTalks().enqueue(object : Callback<SmallTalkListWrapper> {
            override fun onResponse(call: Call<SmallTalkListWrapper>, response: Response<SmallTalkListWrapper>) {
                smallTalks.value = response.body()?.smallTalks ?: emptyList()
            }
            override fun onFailure(call: Call<SmallTalkListWrapper>, t: Throwable) {
                smallTalks.value = emptyList()
            }
        })
    }

    fun getSmallTalk(id: String) {
        request.getSmallTalk(id).enqueue(object : Callback<SmallTalkWrapper> {
            override fun onResponse(call: Call<SmallTalkWrapper>, response: Response<SmallTalkWrapper>) {
                smallTalkDetail.value = response.body()?.smallTalk
            }
            override fun onFailure(call: Call<SmallTalkWrapper>, t: Throwable) {
                smallTalkDetail.value = null
            }
        })
    }

    fun listAlmostMatches() {
        request.listAlmostMatches().enqueue(object : Callback<UserSmallTalkRequestWithMatchDataWrapper> {
            override fun onResponse(
                call: Call<UserSmallTalkRequestWithMatchDataWrapper>,
                response: Response<UserSmallTalkRequestWithMatchDataWrapper>
            ) {
                almostMatches.value = response.body()?.requests.orEmpty()
            }

            override fun onFailure(call: Call<UserSmallTalkRequestWithMatchDataWrapper>, t: Throwable) {
                almostMatches.value = emptyList()
            }
        })
    }

    fun forceMatchRequest(smallTalkId: Int? = null, unmatch: String? = null) {
        request.forceMatchUserSmallTalkRequest(smallTalkId,unmatch)
            .enqueue(object : Callback<SmallTalkMatchResponse> {
                override fun onResponse(
                    call: Call<SmallTalkMatchResponse>,
                    response: Response<SmallTalkMatchResponse>
                ) { matchResult.value = response.body() }

                override fun onFailure(
                    call: Call<SmallTalkMatchResponse>,
                    t: Throwable
                ) { matchResult.value = null }
            })
    }

    /**
     * 2️⃣ Participants d’un smalltalk
     */
    fun listSmallTalkParticipants(id: String) {
        request.listSmallTalkParticipants(id).enqueue(object : Callback<MembersWrapper> {
            override fun onResponse(call: Call<MembersWrapper>, response: Response<MembersWrapper>) {
                participants.value = response.body()?.users ?: emptyList()
            }
            override fun onFailure(call: Call<MembersWrapper>, t: Throwable) {
                participants.value = emptyList()
            }
        })
    }

    /**
     * 3️⃣ Quitter un smalltalk
     */
    fun leaveSmallTalk(id: String) {
        request.leaveSmallTalk(id).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                // On rafraîchit la liste des smallTalks après avoir quitté
                listSmallTalks()
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // Optionnel : gérer l’erreur (toast, log…)
            }
        })
    }

    /**
     * 4️⃣ Liste des messages d’un smalltalk
     */
    fun loadInitialMessages(smallTalkId: String) {
        currentPage = 1
        isLastPage = false
        listChatMessages(smallTalkId, currentPage, messagesPerPage, reset = true)
    }

    fun loadMoreMessagesIfPossible(smallTalkId: String) {
        Timber.wtf("wtf isloading : $isLoading , isLastPage : $isLastPage")
        if (isLoading || isLastPage) return
        currentPage += 1
        listChatMessages(smallTalkId, currentPage, messagesPerPage)
    }

    fun listChatMessages(id: String, page: Int? = null, per: Int? = null, reset: Boolean = false) {
        isLoading = true
        request.listChatMessages(id, page, per).enqueue(object : Callback<ChatMessageListWrapper> {
            override fun onResponse(call: Call<ChatMessageListWrapper>, response: Response<ChatMessageListWrapper>) {
                val newMessages = response.body()?.messages ?: emptyList()
                isLoading = false
                isLastPage = newMessages.size < (per ?: messagesPerPage)
                if (reset) {
                    messages.value = newMessages
                } else {
                    // concat en haut (scroll top)
                    val combined = newMessages + (messages.value ?: emptyList())
                    messages.value = combined
                }
            }

            override fun onFailure(call: Call<ChatMessageListWrapper>, t: Throwable) {
                isLoading = false
            }
        })
    }

    /**
     * 5️⃣ Créer un message
     */
    fun createChatMessage(id: String, content: String) {
        val params = ArrayMap<String, Any>()
        params["content"] = content
        request.createChatMessage(id, params).enqueue(object : Callback<ChatMessageWrapper> {
            override fun onResponse(call: Call<ChatMessageWrapper>, response: Response<ChatMessageWrapper>) {
                createdMessage.value = response.body()?.chatMessage
                listChatMessages(id) // Optionnel : rafraîchir
            }
            override fun onFailure(call: Call<ChatMessageWrapper>, t: Throwable) {
                createdMessage.value = null
            }
        })
    }

    fun updateChatMessage(smallTalkId: String, messageId: String, newContent: String) {
        val params = ArrayMap<String, Any>()
        params["content"] = newContent
        request.updateChatMessage(smallTalkId, messageId, params).enqueue(object : Callback<ChatMessageWrapper> {
            override fun onResponse(call: Call<ChatMessageWrapper>, response: Response<ChatMessageWrapper>) {
                listChatMessages(smallTalkId)
            }
            override fun onFailure(call: Call<ChatMessageWrapper>, t: Throwable) {
                // Optionnel : gestion erreur
            }
        })
    }

    /**
     * 7️⃣ Supprimer un message
     */
    fun deleteChatMessage(smallTalkId: String, messageId: String) {
        request.deleteChatMessage(smallTalkId, messageId).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                // Après suppression, on rafraîchit la liste
                listChatMessages(smallTalkId)
                messageDeleted.postValue(response.isSuccessful)
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // Optionnel : gérer l’erreur
            }
        })
    }

    fun addMessageWithImage(smallTalkId: String, content: String?, file: File) {
        val request = RequestContent("image/jpeg")
        EntourageApplication.get().apiModule.smallTalkRequest
            .prepareAddPost(smallTalkId, request)
            .enqueue(object : Callback<PrepareAddPostResponse> {
                override fun onResponse(call: Call<PrepareAddPostResponse>, response: Response<PrepareAddPostResponse>) {
                    if (response.isSuccessful) {
                        val presignedUrl = response.body()?.presignedUrl
                        val uploadKey = response.body()?.uploadKey
                        if (presignedUrl != null && uploadKey != null) {
                            uploadFileAndSendMessage(smallTalkId, file, presignedUrl, uploadKey, content)
                        }
                    } else {
                        createdMessage.postValue(null)
                    }
                }

                override fun onFailure(call: Call<PrepareAddPostResponse>, t: Throwable) {
                    createdMessage.postValue(null)
                }
            })
    }


    private fun uploadFileAndSendMessage(
        smallTalkId: String,
        file: File,
        presignedUrl: String,
        uploadKey: String,
        content: String?
    ) {
        val requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val request = Request.Builder().url(presignedUrl).put(requestBody).build()

        EntourageApplication.get().apiModule.okHttpClient.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                createdMessage.postValue(null)
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (!response.isSuccessful) {
                    createdMessage.postValue(null)
                    return
                }

                val chatParams = ArrayMap<String, Any>()
                chatParams["image_url"] = uploadKey
                if (!content.isNullOrBlank()) chatParams["content"] = content

                val messagePayload = ArrayMap<String, Any>()
                messagePayload["chat_message"] = chatParams

                EntourageApplication.get().apiModule.smallTalkRequest
                    .createChatMessage(smallTalkId, messagePayload)
                    .enqueue(object : Callback<ChatMessageWrapper> {
                        override fun onResponse(call: Call<ChatMessageWrapper>, response: Response<ChatMessageWrapper>) {
                            createdMessage.postValue(response.body()?.chatMessage)
                            listChatMessages(smallTalkId)
                        }

                        override fun onFailure(call: Call<ChatMessageWrapper>, t: Throwable) {
                            createdMessage.postValue(null)
                        }
                    })
            }
        })
    }



}