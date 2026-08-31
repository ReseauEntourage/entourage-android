package social.entourage.android.tools.utils

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.AnimRes
import androidx.core.content.IntentCompat
import androidx.core.os.BundleCompat
import androidx.core.os.ParcelCompat
import java.io.Serializable

/**
 * Single home for Tier 3 compat helpers.
 */

/**
 * Kept on the deprecated API on purpose.
 * The API 34 replacement (overrideActivityTransition) inverts the call site:
 * it must be invoked on the activity being opened/closed, not on the caller.
 * Migrating all sites is an animation refactor tracked separately.
 * Safe: overridePendingTransition is deprecated, not removed, and still
 * functions on API 34+.
 */
@Suppress("DEPRECATION")
fun Activity.overrideTransitionCompat(@AnimRes enter: Int, @AnimRes exit: Int) =
    overridePendingTransition(enter, exit)

inline fun <reified T : Serializable> Intent.serializableExtra(key: String): T? =
    IntentCompat.getSerializableExtra(this, key, T::class.java)

inline fun <reified T : Parcelable> Intent.parcelableExtra(key: String): T? =
    IntentCompat.getParcelableExtra(this, key, T::class.java)

inline fun <reified T : Serializable> Bundle.serializableCompat(key: String): T? =
    BundleCompat.getSerializable(this, key, T::class.java)

inline fun <reified T : Parcelable> Bundle.parcelableCompat(key: String): T? =
    BundleCompat.getParcelable(this, key, T::class.java)

inline fun <reified T : Parcelable> Parcel.readParcelableCompat(loader: ClassLoader?): T? =
    ParcelCompat.readParcelable(this, loader, T::class.java)

inline fun <reified T : Serializable> Parcel.readSerializableCompat(loader: ClassLoader?): T? =
    ParcelCompat.readSerializable(this, loader, T::class.java)

inline fun <reified T> Parcel.readListCompat(list: MutableList<T>, loader: ClassLoader?) =
    ParcelCompat.readList(this, list, loader, T::class.java)
