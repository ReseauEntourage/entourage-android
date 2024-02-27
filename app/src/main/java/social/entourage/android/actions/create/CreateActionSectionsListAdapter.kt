package social.entourage.android.actions.create

import android.content.Intent
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.BuildConfig
import social.entourage.android.R
import social.entourage.android.api.model.ActionSection
import social.entourage.android.api.model.EventUtils
import social.entourage.android.databinding.LayoutActionCreateCatInfosBinding
import social.entourage.android.databinding.LayoutActionCreateCatItemBinding
import social.entourage.android.home.pedago.PedagoDetailActivity
import social.entourage.android.tools.utils.Const

interface OnItemCheckListener {
    fun onItemCheck(position: Int)
}

class CreateActionSectionsListAdapter(
    private var actionsList: List<ActionSection>,
    private val isDemand:Boolean,
    private val onItemClick: OnItemCheckListener,
) : RecyclerView.Adapter<CreateActionSectionsListAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ViewDataBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(category: ActionSection, position:Int) {
            val bindingView = binding as LayoutActionCreateCatItemBinding
            if (category.isSelected) {
                bindingView.title.typeface = Typeface.create(bindingView.title.typeface,Typeface.BOLD)
            }
            else {
                bindingView.title.typeface = Typeface.create(bindingView.title.typeface,Typeface.NORMAL)
            }
            if(category.id != null){
                bindingView.title.text = EventUtils.showTagTranslated( bindingView.root.context,category.id)
                bindingView.subtitle.text = EventUtils.showSubTagTranslated( bindingView.root.context,category.id)
            }
            bindingView.checkBox.isChecked = category.isSelected
            bindingView.icon.setImageResource(category.icon)

            bindingView.layout.setOnClickListener {
                onItemClick.onItemCheck(position)
            }
        }

        fun bindInfo() {
            val bindingView = binding as LayoutActionCreateCatInfosBinding
            val _ctx = bindingView.layoutInfos.context
            val actionText = if (isDemand) _ctx.getString(R.string.action_name_demand) else _ctx.getString(R.string.action_name_contrib)

            val preLinkText = String.format(_ctx.getString(R.string.action_crete_cat_about), actionText)
            val linkText = _ctx.getString(R.string.action_create_cat_about_link)
            val fullText = preLinkText + linkText
            val spannedText = HtmlCompat.fromHtml(fullText, HtmlCompat.FROM_HTML_MODE_LEGACY)

            val spannableString = SpannableStringBuilder(spannedText).apply {
                val underlineStart = indexOf(linkText)
                val underlineEnd = underlineStart + linkText.length

                if (underlineStart >= 0) {
                    setSpan(object : ClickableSpan() {
                        override fun onClick(widget: View) {
                            val intent = Intent(_ctx, PedagoDetailActivity::class.java)
                            intent.putExtra(Const.ID, BuildConfig.PEDAGO_ACTION_SECTION_ID)
                            _ctx.startActivity(intent)
                        }

                        override fun updateDrawState(ds: TextPaint) {
                            super.updateDrawState(ds)
                            ds.isUnderlineText = true
                            ds.color = _ctx.resources.getColor(R.color.orange, null) // Utilisez la couleur orange de vos ressources
                        }
                    }, underlineStart, underlineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }

            bindingView.uiTvMessageInfo.text = spannableString
            bindingView.uiTvMessageInfo.movementMethod = LinkMovementMethod.getInstance()

            bindingView.layoutInfos.setOnClickListener {
                onItemClick.onItemCheck(-1)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = if (viewType == 1) {
            LayoutActionCreateCatInfosBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)
        } else {
            LayoutActionCreateCatItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)
        }
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (getItemViewType(position) == 1) {
            holder.bindInfo()
        } else {
            holder.bind(actionsList[position], position)
        }
    }

    override fun getItemCount(): Int {
        return actionsList.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        if (position == actionsList.size) return 1

        return 0
    }
}