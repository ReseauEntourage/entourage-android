package social.entourage.android.api.model

import kotlinx.serialization.Serializable

@Serializable
data class SalesforceEnterprise(
    val Id: String,
    val Name: String,
    val Type_org__c: String
)

@Serializable
data class SalesforceEvent(
    val Id: String,
    val Name: String
)