package social.entourage.android.api

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.annotations.Expose
import social.entourage.android.api.model.BaseEntourage
import social.entourage.android.api.model.BaseEntourage.BaseEntourageJsonAdapter
import social.entourage.android.api.model.feed.NewsfeedItem
import social.entourage.android.api.model.feed.NewsfeedItem.NewsfeedItemJsonAdapter

/** Configuration Gson partagée par Retrofit (et par les tests de parsing JSON). */
fun createApiGson(): Gson {
    return GsonBuilder()
        .addSerializationExclusionStrategy(object : ExclusionStrategy {
            override fun shouldSkipField(fieldAttributes: FieldAttributes): Boolean {
                val expose = fieldAttributes.getAnnotation(Expose::class.java) ?: return false
                return !expose.serialize
            }

            override fun shouldSkipClass(aClass: Class<*>?): Boolean {
                return false
            }
        }).addDeserializationExclusionStrategy(object : ExclusionStrategy {
            override fun shouldSkipField(fieldAttributes: FieldAttributes): Boolean {
                val expose = fieldAttributes.getAnnotation(Expose::class.java) ?: return false
                return !expose.deserialize
            }

            override fun shouldSkipClass(aClass: Class<*>?): Boolean {
                return false
            }
        })
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        .registerTypeAdapter(NewsfeedItem::class.java, NewsfeedItemJsonAdapter())
        .registerTypeAdapter(BaseEntourage::class.java, BaseEntourageJsonAdapter())
        .create()
}
