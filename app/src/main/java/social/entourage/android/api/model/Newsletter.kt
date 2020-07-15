package social.entourage.android.api.model

import com.google.gson.annotations.SerializedName

class Newsletter (var email: String, var isActive: Boolean) {
    class NewsletterWrapper(@field:SerializedName("newsletter_subscription") var newsletter: Newsletter)
}