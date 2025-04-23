package social.entourage.android.small_talks

import android.app.Application
import androidx.collection.ArrayMap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
    val messages = MutableLiveData<List<ChatMessage>>()
    val createdMessage = MutableLiveData<ChatMessage?>()
    val matchResult = MutableLiveData<SmallTalkMatchResponse?>()
    val requestDeleted = MutableLiveData<Boolean>()
    val smallTalkDetail = MutableLiveData<SmallTalk?>()


    private val steps = listOf(
        SmallTalkStep(
            title = context.getString(R.string.small_talk_step_title_1),
            subtitle = context.getString(R.string.small_talk_step_subtitle_1),
            items = listOf(
                InterestForAdapter(R.drawable.ic_onboarding_interest_name_autre, context.getString(R.string.small_talk_step1_item1_title), context.getString(R.string.small_talk_step1_item1_subtitle), false, "1"),
                InterestForAdapter(R.drawable.ic_onboarding_interest_name_autre, context.getString(R.string.small_talk_step1_item2_title), context.getString(R.string.small_talk_step1_item2_subtitle), false, "2")
            )
        ),
        SmallTalkStep(
            title = context.getString(R.string.small_talk_step_title_2),
            subtitle = context.getString(R.string.small_talk_step_subtitle_2),
            items = listOf(
                InterestForAdapter(R.drawable.ic_onboarding_interest_name_autre, context.getString(R.string.small_talk_step2_item1_title), context.getString(R.string.small_talk_step2_item1_subtitle), false, "3"),
                InterestForAdapter(R.drawable.ic_onboarding_interest_name_autre, context.getString(R.string.small_talk_step2_item2_title), context.getString(R.string.small_talk_step2_item2_subtitle), false, "4")
            )
        ),
        SmallTalkStep(
            title = context.getString(R.string.small_talk_step_title_3),
            subtitle = context.getString(R.string.small_talk_step_subtitle_3),
            items = listOf(
                InterestForAdapter(R.drawable.ic_onboarding_interest_name_autre, context.getString(R.string.small_talk_step3_item1_title), "", false, "5"),
                InterestForAdapter(R.drawable.ic_onboarding_interest_name_autre, context.getString(R.string.small_talk_step3_item2_title), "", false, "6"),
                InterestForAdapter(R.drawable.ic_onboarding_interest_name_autre, context.getString(R.string.small_talk_step3_item3_title), "", false, "7")
            )
        ),
        SmallTalkStep(
            title = context.getString(R.string.small_talk_step_title_4),
            subtitle = context.getString(R.string.small_talk_step_subtitle_4),
            items = listOf(
                InterestForAdapter(R.drawable.ic_onboarding_interest_name_autre, context.getString(R.string.small_talk_step4_item1_title), context.getString(R.string.small_talk_step4_item1_subtitle), false, "8"),
                InterestForAdapter(R.drawable.ic_onboarding_interest_name_autre, context.getString(R.string.small_talk_step4_item2_title), context.getString(R.string.small_talk_step4_item2_subtitle), false, "9")
            )
        ),
        SmallTalkStep(
            title = context.getString(R.string.onboarding_interest_title),
            subtitle = context.getString(R.string.onboarding_interest_content),
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
        val previousIndex = (_currentStepIndex.value ?: 0) - 1
        if (previousIndex >= 0) {
            _currentStepIndex.value = previousIndex
            _currentStep.value = steps[previousIndex]
        }
    }

    fun getStepProgress(): Float = ((_currentStepIndex.value ?: 0) + 1).toFloat() / steps.size.toFloat()
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

    fun deleteRequest(id: String) {
        request.deleteUserSmallTalkRequest(id).enqueue(object : Callback<ResponseBody> {
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
    fun listChatMessages(id: String, page: Int? = null, per: Int? = null) {
        request.listChatMessages(id, page, per).enqueue(object : Callback<ChatMessageListWrapper> {
            override fun onResponse(call: Call<ChatMessageListWrapper>, response: Response<ChatMessageListWrapper>) {
                messages.value = response.body()?.messages ?: emptyList()
            }
            override fun onFailure(call: Call<ChatMessageListWrapper>, t: Throwable) {
                messages.value = emptyList()
            }
        })
    }

    /**
     * 5️⃣ Créer un message
     */
    fun createChatMessage(id: String, params: ArrayMap<String, Any>) {
        request.createChatMessage(id, params).enqueue(object : Callback<ChatMessageWrapper> {
            override fun onResponse(call: Call<ChatMessageWrapper>, response: Response<ChatMessageWrapper>) {
                createdMessage.value = response.body()?.chatMessage
                // Optionnel : rafraîchir la liste
                listChatMessages(id)
            }
            override fun onFailure(call: Call<ChatMessageWrapper>, t: Throwable) {
                createdMessage.value = null
            }
        })
    }

    /**
     * 6️⃣ Mettre à jour un message
     */
    fun updateChatMessage(smallTalkId: String, messageId: String, params: ArrayMap<String, Any>) {
        request.updateChatMessage(smallTalkId, messageId, params).enqueue(object : Callback<ChatMessageWrapper> {
            override fun onResponse(call: Call<ChatMessageWrapper>, response: Response<ChatMessageWrapper>) {
                // On peut soit renvoyer le message mis à jour, soit rafraîchir la liste entière
                listChatMessages(smallTalkId)
            }
            override fun onFailure(call: Call<ChatMessageWrapper>, t: Throwable) {
                // Optionnel : gérer l’erreur
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
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // Optionnel : gérer l’erreur
            }
        })
    }


}