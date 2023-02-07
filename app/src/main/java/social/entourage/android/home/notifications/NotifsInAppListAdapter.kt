package social.entourage.android.home.notifications

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import kotlinx.android.synthetic.main.new_notif_detail.view.*
import social.entourage.android.R
import social.entourage.android.api.model.NotifInApp
import social.entourage.android.message.push.PushNotificationLinkManager
import social.entourage.android.tools.utils.Utils
import timber.log.Timber

interface OnItemClick {
    fun onItemClick(notif: NotifInApp, position:Int)
}

class NotifsInAppListAdapter(
    var notifs: List<NotifInApp>,
    private var onItemClickListener: OnItemClick
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.new_notif_detail, parent, false)
        return ViewHolder(view, parent.context)
    }

    inner class ViewHolder(val binding: View, var context:Context) :
        RecyclerView.ViewHolder(binding) {
        fun bind(notif: NotifInApp, position:Int) {

            Timber.wtf("wtf " + notif.instanceString )

            binding.card.setOnClickListener {
                onItemClickListener.onItemClick(notif,position)
            }
            binding.title.text = notif.content
            binding.date.text = notif.createdAt?.let { Utils.dateAsDurationFromNow(it,context) }

            notif.imageUrl?.let {
                Glide.with(binding.image_card.context)
                    .load(it)
                    .error(PushNotificationLinkManager().setPlaceHolder(notif.instanceString))
                    .transform(CircleCrop())
                    .into(binding.image_card)
            } ?: run {
                Glide.with(binding.image_card.context)
                    .load(PushNotificationLinkManager().setPlaceHolder(notif.instanceString))
                    .transform(CircleCrop())
                    .into(binding.image_card)
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

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? ViewHolder)?.bind(notifs[position], position)
    }

    override fun getItemCount(): Int {
        return notifs.size
    }
}
