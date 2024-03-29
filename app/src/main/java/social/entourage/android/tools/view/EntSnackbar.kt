package social.entourage.android.tools.view

import android.view.View
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat

import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar

import social.entourage.android.R

object EntSnackbar {
    fun make(view: View, @StringRes resId: Int, @BaseTransientBottomBar.Duration duration: Int): Snackbar {
        val temp = Snackbar.make(view, resId, duration)
        temp.view.setBackgroundColor(ResourcesCompat.getColor(view.resources, R.color.background_toast, null))
        return temp
    }
}
