package social.entourage.android.survey

import androidx.lifecycle.MutableLiveData
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.api.model.ChatMessageSurvey
import social.entourage.android.api.model.SurveyAttributes
import social.entourage.android.api.model.SurveyResponsesListWrapper
import social.entourage.android.api.model.SurveyResponsesWrapper
import timber.log.Timber

class SurveyPresenter {

    var isSurveySent: MutableLiveData<Boolean> = MutableLiveData()
    var isSurveyVoted: MutableLiveData<Boolean> = MutableLiveData()
    var isSurveyDeleted: MutableLiveData<Boolean> = MutableLiveData()
    var surveyResponseList: MutableLiveData<SurveyResponsesListWrapper> = MutableLiveData()
    fun postSurveyResponseGroup(groupId: Int, postId: Int, responses: List<Boolean>) {
        val surveyResponseRequest = SurveyResponsesWrapper(responses)
        EntourageApplication.get().apiModule.surveyRequest.postSurveyResponseGroup(groupId, postId, surveyResponseRequest).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    // Logique de gestion de la réussite
                    isSurveyVoted.postValue(true)
                    Timber.d("GroupPresenter: Réponse au sondage postée avec succès.")
                } else {
                    // Logique de gestion des erreurs
                    isSurveyVoted.postValue(false)
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // Logique de gestion de l'échec de la requête
                Timber.e("GroupPresenter: Échec de la requête de réponse au sondage: ${t.message}")
            }
        })
    }

    fun getSurveyResponsesForGroup(groupId: Int, postId: Int) {
        EntourageApplication.get().apiModule.surveyRequest.getSurveyResponsesForGroup(groupId, postId).enqueue(object : Callback<SurveyResponsesListWrapper> {
            override fun onResponse(call: Call<SurveyResponsesListWrapper>, response: Response<SurveyResponsesListWrapper>) {
                if (response.isSuccessful) {
                    surveyResponseList.postValue(response.body())
                    // Ici, tu peux mettre à jour l'UI avec la liste des réponses obtenues
                    Timber.d("SurveyPresenter: Réponses au sondage récupérées avec succès: ${response.body()?.responses}")
                } else {
                    Timber.e("SurveyPresenter: Erreur lors de la récupération des réponses au sondage.")
                }
            }

            override fun onFailure(call: Call<SurveyResponsesListWrapper>, t: Throwable) {
                Timber.e("SurveyPresenter: Échec de la requête de récupération des réponses au sondage: ${t.message}")
            }
        })
    }

    fun deleteSurveyResponseForGroup(groupId: Int, postId: Int) {
        EntourageApplication.get().apiModule.surveyRequest.deleteSurveyResponseForGroup(groupId, postId).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    // La réponse au sondage a été supprimée avec succès
                    Timber.d("SurveyPresenter: Réponse au sondage supprimée avec succès.")
                } else {
                    Timber.e("SurveyPresenter: Erreur lors de la suppression de la réponse au sondage.")
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Timber.e("SurveyPresenter: Échec de la requête de suppression de la réponse au sondage: ${t.message}")
            }
        })
    }

    fun createSurveyInGroup(groupId: Int, content: String, choices: List<String>, multiple: Boolean) {
        val surveyAttributes = SurveyAttributes(choices, multiple)
        val chatMessage = ChatMessageSurvey(content, surveyAttributes)

        EntourageApplication.get().apiModule.surveyRequest.createSurveyInGroup(groupId, chatMessage).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    isSurveySent.postValue(true)
                } else {
                    isSurveySent.postValue(false)
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Timber.e("SurveyPresenter: Échec de la création du sondage dans le groupe: ${t.message}")
            }
        })
    }




    fun postSurveyResponseEvent(eventId: Int, postId: Int, responses: List<Boolean>) {
        val surveyResponseRequest = SurveyResponsesWrapper(responses)
        EntourageApplication.get().apiModule.surveyRequest.postSurveyResponseEvent(eventId, postId, surveyResponseRequest).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    // Logique de gestion de la réussite
                    isSurveyVoted.postValue(true)
                    Timber.d("GroupPresenter: Réponse au sondage postée avec succès.")
                } else {
                    // Logique de gestion des erreurs
                    isSurveyVoted.postValue(false)
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // Logique de gestion de l'échec de la requête
                Timber.e("GroupPresenter: Échec de la requête de réponse au sondage: ${t.message}")
            }
        })
    }

    fun getSurveyResponsesForEvent(eventId: Int, postId: Int) {
        EntourageApplication.get().apiModule.surveyRequest.getSurveyResponsesForEvent(eventId, postId).enqueue(object : Callback<SurveyResponsesListWrapper> {
            override fun onResponse(call: Call<SurveyResponsesListWrapper>, response: Response<SurveyResponsesListWrapper>) {
                if (response.isSuccessful) {
                    surveyResponseList.postValue(response.body())
                    // Ici, tu peux mettre à jour l'UI avec la liste des réponses obtenues
                    Timber.d("SurveyPresenter: Réponses au sondage récupérées avec succès: ${response.body()?.responses}")
                } else {
                    Timber.e("SurveyPresenter: Erreur lors de la récupération des réponses au sondage.")
                }
            }

            override fun onFailure(call: Call<SurveyResponsesListWrapper>, t: Throwable) {
                Timber.e("SurveyPresenter: Échec de la requête de récupération des réponses au sondage: ${t.message}")
            }
        })
    }
    fun deleteSurveyResponseForEvent(eventId: Int, postId: Int) {
        EntourageApplication.get().apiModule.surveyRequest.deleteSurveyResponseForEvent(eventId, postId).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    // La réponse au sondage a été supprimée avec succès
                    Timber.d("SurveyPresenter: Réponse au sondage supprimée avec succès.")
                } else {
                    Timber.e("SurveyPresenter: Erreur lors de la suppression de la réponse au sondage.")
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Timber.e("SurveyPresenter: Échec de la requête de suppression de la réponse au sondage: ${t.message}")
            }
        })
    }

    fun createSurveyInEvent(eventId: Int, content: String, choices: List<String>, multiple: Boolean) {
        val surveyAttributes = SurveyAttributes(choices, multiple)
        val chatMessage = ChatMessageSurvey(content, surveyAttributes)

        EntourageApplication.get().apiModule.surveyRequest.createSurveyInEvent(eventId, chatMessage).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    isSurveySent.postValue(true)
                } else {
                    isSurveySent.postValue(false)
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Timber.e("SurveyPresenter: Échec de la création du sondage dans l'événement: ${t.message}")
            }
        })
    }
}