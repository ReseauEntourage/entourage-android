package social.entourage.android.api.model

import kotlinx.serialization.Serializable

@Serializable
data class SalesforceEnterprise(
    val Id: String? = null,
    val Name: String? = null,
    val Type_org__c: String? = null
)

@Serializable
data class SalesforceEvent(
    val Id: String? = null,
    val Name: String? = null
)