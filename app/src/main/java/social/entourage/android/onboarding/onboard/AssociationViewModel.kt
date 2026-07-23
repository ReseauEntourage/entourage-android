package social.entourage.android.onboarding.onboard

import androidx.collection.ArrayMap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.api.model.Partner
import social.entourage.android.api.model.PartnerCreateBody
import social.entourage.android.api.model.PartnerCreateWrapper
import social.entourage.android.api.model.PartnerResponse
import social.entourage.android.api.request.Event
import social.entourage.android.api.request.PartnersWrapper
import social.entourage.android.api.request.UserRequest

class AssociationViewModel : ViewModel() {

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _associationNames = MutableLiveData<List<String>>(listOf("Autre"))
    val associationNames: LiveData<List<String>> = _associationNames

    private val _errorMessageText = MutableLiveData<Event<String>>()
    val errorMessageText: LiveData<Event<String>> = _errorMessageText

    private val _errorMessageRes = MutableLiveData<Event<Int>>()
    val errorMessageRes: LiveData<Event<Int>> = _errorMessageRes

    private val _success = MutableLiveData<Event<Unit>>()
    val success: LiveData<Event<Unit>> = _success

    private var partners: List<Partner> = emptyList()

    private val userRequest: UserRequest
        get() = EntourageApplication.get().apiModule.userRequest

    private val associationsRequest
        get() = EntourageApplication.get().apiModule.associationsRequest

    fun loadAssociations() {
        _loading.postValue(true)
        associationsRequest.getAllAssociations().enqueue(object : Callback<PartnersWrapper> {
            override fun onResponse(call: Call<PartnersWrapper>, response: Response<PartnersWrapper>) {
                _loading.postValue(false)
                if (!response.isSuccessful) {
                    _errorMessageText.postValue(Event("Erreur réseau (${response.code()})"))
                    return
                }
                val list = response.body()?.partners.orEmpty()
                partners = list
                val names = ArrayList<String>()
                names.add("Autre")
                names.addAll(list.mapNotNull { it.name }.map { it.trim() }.filter { it.isNotEmpty() })
                _associationNames.postValue(names.distinct())
            }

            override fun onFailure(call: Call<PartnersWrapper>, t: Throwable) {
                _loading.postValue(false)
                _errorMessageText.postValue(Event(t.message ?: "Erreur réseau"))
            }
        })
    }

    fun findPartnerByName(name: String): Partner? {
        val trimmed = name.trim()
        return partners.firstOrNull { (it.name ?: "").trim() == trimmed }
    }

    fun joinAssociation(partner: Partner, postalCodeFallback: String?) {
        val partnerId = partner.id.toInt()
        if (partnerId <= 0) {
            _errorMessageText.postValue(Event("Association invalide"))
            return
        }

        _loading.postValue(true)

        val pcFromPartner = partner.postalCode?.trim().orEmpty()
        val pcFromFallback = postalCodeFallback?.trim().orEmpty()
        val postalCode = pcFromPartner.ifEmpty { pcFromFallback }

        val roleTitle = partner.userRoleTitle?.trim().orEmpty()

        val body = ArrayMap<String, Any>().apply {
            put("partner_id", partnerId)
            if (postalCode.isNotEmpty()) put("postal_code", postalCode)
            if (roleTitle.isNotEmpty()) put("partner_role_title", roleTitle)
        }

        userRequest.updateAssoInfos(body).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                _loading.postValue(false)
                if (!response.isSuccessful) {
                    _errorMessageText.postValue(Event("Erreur réseau (${response.code()})"))
                    return
                }
                _success.postValue(Event(Unit))
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                _loading.postValue(false)
                _errorMessageText.postValue(Event(t.message ?: "Erreur réseau"))
            }
        })
    }

    fun createAssociation(
        name: String,
        address: String?,
        latitude: Double?,
        longitude: Double?,
        postalCode: String?
    ) {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            _errorMessageText.postValue(Event("Nom invalide"))
            return
        }

        _loading.postValue(true)

        val payload = PartnerCreateBody(
            name = trimmedName,
            description = trimmedName,
            address = address?.trim()?.takeIf { it.isNotEmpty() },
            latitude = latitude,
            longitude = longitude
        )

        val wrapper = PartnerCreateWrapper(partner = payload)

        associationsRequest.createAssociation(wrapper).enqueue(object : Callback<PartnerResponse> {
            override fun onResponse(call: Call<PartnerResponse>, response: Response<PartnerResponse>) {
                if (!response.isSuccessful) {
                    _loading.postValue(false)
                    _errorMessageText.postValue(Event("Erreur réseau (${response.code()})"))
                    return
                }

                val createdPartnerId = response.body()?.partner?.id?.toInt()?.takeIf { it > 0 }
                val pc = postalCode?.trim().orEmpty()

                if (createdPartnerId == null) {
                    _loading.postValue(false)
                    _success.postValue(Event(Unit))
                    return
                }

                if (pc.isEmpty()) {
                    _loading.postValue(false)
                    _success.postValue(Event(Unit))
                    return
                }

                val joinBody = ArrayMap<String, Any>().apply {
                    put("partner_id", createdPartnerId)
                    put("postal_code", pc)
                }

                userRequest.updateAssoInfos(joinBody).enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, resp: Response<ResponseBody>) {
                        _loading.postValue(false)
                        if (!resp.isSuccessful) {
                            _errorMessageText.postValue(Event("Erreur réseau (${resp.code()})"))
                            return
                        }
                        _success.postValue(Event(Unit))
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        _loading.postValue(false)
                        _errorMessageText.postValue(Event(t.message ?: "Erreur réseau"))
                    }
                })
            }

            override fun onFailure(call: Call<PartnerResponse>, t: Throwable) {
                _loading.postValue(false)
                _errorMessageText.postValue(Event(t.message ?: "Erreur réseau"))
            }
        })
    }
}
