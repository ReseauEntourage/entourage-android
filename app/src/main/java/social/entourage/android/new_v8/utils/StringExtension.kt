package social.entourage.android.new_v8.utils

import android.text.Editable

fun Editable.trimEnd(): String {
    return this.trim { it <= ' ' }.toString()
}