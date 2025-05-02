package social.entourage.android.language

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.R
import social.entourage.android.databinding.LanguageItemLayoutBinding


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
            notifyDataSetChanged()
            return
        }
        this.languageItems[position].isSelected = true
        this.languageItems[actualClickedPosition].isSelected = false
        notifyDataSetChanged()
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
            binding.icon.visibility = View.GONE
            when(languageItem.lang) {
                "Français" -> binding.tvTitle.text = "\uD83C\uDDEB\uD83C\uDDF7   ${languageItem.lang}"
                "English" -> binding.tvTitle.text = "\uD83C\uDDEC\uD83C\uDDE7   ${languageItem.lang}"
                "Deutsch" -> binding.tvTitle.text = "\uD83C\uDDE9\uD83C\uDDEA   ${languageItem.lang}"
                "Español" -> binding.tvTitle.text = "\uD83C\uDDEA\uD83C\uDDF8   ${languageItem.lang}"
                "Polski" -> binding.tvTitle.text = "\uD83C\uDDF5\uD83C\uDDF1   ${languageItem.lang}"
                "Українська" -> binding.tvTitle.text = "\uD83C\uDDFA\uD83C\uDDE6   ${languageItem.lang}"
                "Română" -> binding.tvTitle.text = "\uD83C\uDDF7\uD83C\uDDF4   ${languageItem.lang}"
                "العربية" -> binding.tvTitle.text = "\uD83C\uDDE6\uD83C\uDDEA   ${languageItem.lang}"
                else -> binding.icon.setImageDrawable(null)
            }
            when(languageItem.lang) {
                "Français" -> binding.tvTrad.text = "- " + context.getString(R.string.lang_fr)
                "English" -> binding.tvTrad.text = "- " +context.getString(R.string.lang_en)
                "Deutsch" -> binding.tvTrad.text = "- " +context.getString(R.string.lang_de)
                "Español" -> binding.tvTrad.text = "- " +context.getString(R.string.lang_es)
                "Polski" -> binding.tvTrad.text = "- " +context.getString(R.string.lang_pl)
                "Українська" -> binding.tvTrad.text = "- " +context.getString(R.string.lang_uk)
                "Română" -> binding.tvTrad.text = "- " +context.getString(R.string.lang_ro)
                "العربية" -> binding.tvTrad.text = "- " +context.getString(R.string.lang_ar)
                else -> binding.tvTrad.text = ""
            }
        }
    }
}
