package social.entourage.android.notifications

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import social.entourage.android.R
import social.entourage.android.api.model.notification.InAppNotification
import social.entourage.android.databinding.LayoutNotifDetailBinding
import social.entourage.android.tools.utils.Utils

interface OnItemClick {
    fun onItemClick(notif: InAppNotification, position:Int)
}

class InAppListNotificationsAdapter(
    var notifs: List<InAppNotification>,
    private var onItemClickListener: OnItemClick
) : RecyclerView.Adapter<InAppListNotificationsAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutNotifDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view, parent.context)
    }

    inner class ViewHolder(val binding: LayoutNotifDetailBinding, var context:Context) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(notif: InAppNotification, position:Int) {


            binding.card.setOnClickListener {
                onItemClickListener.onItemClick(notif,position)
            }

            //HERE CHANGE NOTIF TITLE

            var titleText = ""
            var contentText = ""
            if(notif.instanceType == "solicitation" || notif.instanceType == "contribution"){
                contentText = "\"${notif.content}\""
            }else if (!notif.content.isNullOrEmpty()) {
                contentText = notif.content
            }
           if(!notif.title.isNullOrEmpty()){
                titleText = notif.title
            }

            val builder = SpannableStringBuilder()

            // Ajoute le texte de titre en gras
            val boldSpan = StyleSpan(Typeface.BOLD)
            builder.append(titleText)
            builder.setSpan(boldSpan, 0, titleText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            // Ajoute une ligne vide
            builder.append(" ")
            // Ajoute le texte de contenu en normal
            builder.append(contentText)
            // Affiche le texte dans le TextView
            binding.title.text = builder
            //binding.title.text = notif.content
            binding.date.text = notif.createdAt?.let { Utils.dateAsDurationFromNow(it,context) }

            notif.imageUrl?.let {
                Glide.with(binding.imageCard.context)
                    .load(it)
                    .error(NotificationActionManager.setPlaceHolder(notif.instanceType))
                    .transform(CircleCrop())
                    .into(binding.imageCard)
            } ?: run {
                Glide.with(binding.imageCard.context)
                    .load(NotificationActionManager.setPlaceHolder(notif.instanceType))
                    .transform(CircleCrop())
                    .into(binding.imageCard)
            }

            if (notif.isRead()) {
                binding.card.setBackgroundColor(binding.card.context.resources.getColor(R.color.white))
                binding.separator.setBackgroundColor(binding.separator.context.resources.getColor(R.color.beige))
            }
            else {
                binding.card.setBackgroundColor(binding.card.context.resources.getColor(R.color.beige))
                binding.separator.setBackgroundColor(binding.separator.context.resources.getColor(R.color.white))
            }
        }
    }

    override fun onBindViewHolder(holder: InAppListNotificationsAdapter.ViewHolder, position: Int) {
        holder.bind(notifs[position], position)
    }

    override fun getItemCount(): Int {
        return notifs.size
    }
}
