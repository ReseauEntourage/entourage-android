package social.entourage.android.tools.utils

import android.text.Editable

fun Editable.trimEnd(): String {
    return this.trim { it <= ' ' }.toString()
}