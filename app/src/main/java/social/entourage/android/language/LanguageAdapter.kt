import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.R
import social.entourage.android.databinding.LanguageItemLayoutBinding
import social.entourage.android.language.LanguageItem
import social.entourage.android.language.OnLanguageClicked


class LanguageAdapter(var context: Context, var callback: OnLanguageClicked) : RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder>() {

    private var languageItems: MutableList<LanguageItem> = arrayListOf()
    var actualClickedPosition:Int = -1
    fun setData(items:MutableList<LanguageItem>){
        this.languageItems.clear()
        this.languageItems.addAll(items)
        notifyDataSetChanged()
    }
    fun onItemChanged(position: Int){
        if(position == actualClickedPosition){
            return
        }
        if(actualClickedPosition == -1){
            actualClickedPosition = position
            this.languageItems[position].isSelected = true
            notifyItemChanged(position)
            return
        }
        this.languageItems[position].isSelected = true
        this.languageItems[actualClickedPosition].isSelected = false
        notifyItemChanged(position)
        notifyItemChanged(actualClickedPosition)
        this.actualClickedPosition = position

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        val binding = LanguageItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LanguageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        holder.bind(languageItems[position])
        if(languageItems[position].isSelected){
            actualClickedPosition = position
        }
    }

    override fun getItemCount(): Int = languageItems.size

    inner class LanguageViewHolder(private val binding: LanguageItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(languageItem: LanguageItem) {
            binding.tvTitle.text = languageItem.lang
            if(languageItem.isSelected){
                binding.buttonLang.background = context.getDrawable(R.drawable.language_layout_background_full)
                binding.ivButton.visibility = View.VISIBLE
                binding.tvTrad.visibility = View.GONE
            }else{
                binding.buttonLang.background = context.getDrawable(R.drawable.language_layout_background)
                binding.ivButton.visibility = View.GONE
                binding.tvTrad.visibility = View.VISIBLE
            }
            binding.buttonLang.setOnClickListener {
                callback.onLangChanged(languageItem)
            }
            when(languageItem.lang) {
                "Français" -> binding.icon.setImageDrawable(context.getDrawable(R.drawable.icon_fr))
                "English" -> binding.icon.setImageDrawable(context.getDrawable(R.drawable.icon_en))
                "Deutsch" -> binding.icon.setImageDrawable(context.getDrawable(R.drawable.icon_de))
                "Español" -> binding.icon.setImageDrawable(context.getDrawable(R.drawable.icon_es))
                "Polski" -> binding.icon.setImageDrawable(context.getDrawable(R.drawable.icon_pl))
                "Українська" -> binding.icon.setImageDrawable(context.getDrawable(R.drawable.icon_uk))
                "Română" -> binding.icon.setImageDrawable(context.getDrawable(R.drawable.icon_ro))
                "العربية" -> binding.icon.setImageDrawable(context.getDrawable(R.drawable.icon_ar))
                else -> binding.icon.setImageDrawable(null)
            }
            when(languageItem.lang) {
                "Français" -> binding.tvTrad.text = context.getString(R.string.lang_fr)
                "English" -> binding.tvTrad.text = context.getString(R.string.lang_en)
                "Deutsch" -> binding.tvTrad.text = context.getString(R.string.lang_de)
                "Español" -> binding.tvTrad.text = context.getString(R.string.lang_es)
                "Polski" -> binding.tvTrad.text = context.getString(R.string.lang_pl)
                "Українська" -> binding.tvTrad.text = context.getString(R.string.lang_uk)
                "Română" -> binding.tvTrad.text = context.getString(R.string.lang_ro)
                "العربية" -> binding.tvTrad.text = context.getString(R.string.lang_ar)
                else -> binding.tvTrad.text = ""
            }
        }
    }
}
