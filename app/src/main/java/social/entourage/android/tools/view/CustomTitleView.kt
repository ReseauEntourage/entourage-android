package social.entourage.android.tools.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import social.entourage.android.R
import social.entourage.android.databinding.LayoutViewTitleBinding

/**
 * Custom title view with a close button and a title
 */
class CustomTitleView : RelativeLayout {
    private var _binding: LayoutViewTitleBinding? = null
    val binding: LayoutViewTitleBinding get() = _binding!!

    constructor(context: Context?) : super(context) {
        init(null, 0)
    }

    constructor(context: Context?, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context?, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        _binding = LayoutViewTitleBinding.inflate(
            LayoutInflater.from(context), this, true
        )

        // Load attributes
        val attributes = context.obtainStyledAttributes(
                attrs, R.styleable.EntourageTitleView, defStyle, 0)
        binding.titleText.text = attributes.getString(R.styleable.EntourageTitleView_entourageTitle)
        if (attributes.hasValue(R.styleable.EntourageTitleView_android_textColor)) {
            binding.titleText.setTextColor(attributes.getColor(R.styleable.EntourageTitleView_android_textColor, ContextCompat.getColor(context, R.color.greyish_brown)))
        }
        if (attributes.hasValue(R.styleable.EntourageTitleView_entourageTitleCloseDrawable)) {
            binding.titleCloseButton.setImageDrawable(attributes.getDrawable(
                    R.styleable.EntourageTitleView_entourageTitleCloseDrawable))

        }
        if (attributes.hasValue(R.styleable.EntourageTitleView_entourageTitleCloseDrawableTint)) {
            binding.titleCloseButton.imageTintList = attributes.getColorStateList(
                        R.styleable.EntourageTitleView_entourageTitleCloseDrawableTint)
        }
        binding.titleActionButton.text = attributes.getString(R.styleable.EntourageTitleView_entourageTitleAction)
        setBackgroundResource(attributes.getResourceId(R.styleable.EntourageTitleView_android_background, R.color.background))
        binding.titleSeparator.visibility = if (attributes.getBoolean(R.styleable.EntourageTitleView_entourageShowSeparator, true)) View.VISIBLE else View.GONE
        attributes.recycle()
    }

    fun setTitle(title: String) {
        binding.titleText.text = title
    }
}