package social.entourage.android.actions.create

import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.new_action_create_cat_infos.view.*
import kotlinx.android.synthetic.main.new_action_create_cat_item.view.*
import social.entourage.android.BuildConfig
import social.entourage.android.R
import social.entourage.android.api.model.ActionSection
import social.entourage.android.api.model.EventUtils
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

    inner class ViewHolder(val binding: View) :
        RecyclerView.ViewHolder(binding) {
        fun bind(category: ActionSection, position:Int) {
            if (category.isSelected) {
                binding.title.typeface = Typeface.create(binding.title.typeface,Typeface.BOLD)
            }
            else {
                binding.title.typeface = Typeface.create(binding.title.typeface,Typeface.NORMAL)
            }
            if(category.id != null){
                binding.title.text = EventUtils.showTagTranslated( binding.context,category.id!!)
                binding.subtitle.text = EventUtils.showSubTagTranslated( binding.context,category.id!!)
            }
            binding.checkBox.isChecked = category.isSelected
            binding.icon.setImageResource(category.icon)

            binding.layout.setOnClickListener {
                onItemClick.onItemCheck(position)
            }
        }

        fun bindInfos() {
            val _ctx = binding.layout_infos.context
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
                            intent.putExtra(Const.ID, BuildConfig.PEDAGO_ACTION_SECTION_ID.toInt())
                            _ctx.startActivity(intent)
                        }

                        override fun updateDrawState(ds: TextPaint) {
                            super.updateDrawState(ds)
                            ds.isUnderlineText = true
                            ds.color = _ctx.resources.getColor(R.color.orange) // Utilisez la couleur orange de vos ressources
                        }
                    }, underlineStart, underlineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }

            binding.ui_tv_message_info.text = spannableString
            binding.ui_tv_message_info.movementMethod = LinkMovementMethod.getInstance()

            binding.layout_infos.setOnClickListener {
                onItemClick.onItemCheck(-1)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (viewType == 1) {
            val view = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.new_action_create_cat_infos, parent, false)

            return ViewHolder(view)
        }
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.new_action_create_cat_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (getItemViewType(position) == 1) {
            holder.bindInfos()
            return
        }

        holder.bind(actionsList[position], position)
    }

    override fun getItemCount(): Int {
        return actionsList.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        if (position == actionsList.size) return 1

        return 0
    }
}