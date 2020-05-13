package social.entourage.android.tour.choice

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.choice_tour_card.view.*
import social.entourage.android.R
import social.entourage.android.api.model.tour.Tour
import social.entourage.android.api.model.tour.TourType
import social.entourage.android.tour.choice.ChoiceAdapter.ChoiceViewHolder
import java.text.SimpleDateFormat
import java.util.*

/**
 * Items Adapter for the ChoiceFragment RecyclerView
 * @see ChoiceFragment
 */
class ChoiceAdapter(private val itemListener: RecyclerViewClickListener, var tours: List<Tour>)
    : RecyclerView.Adapter<ChoiceViewHolder>() {
    override fun getItemCount(): Int {
        return tours.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChoiceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.choice_tour_card, parent, false)
        return ChoiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChoiceViewHolder, position: Int) {
        val tour = tours[position]
        when (tour.tourType) {
            TourType.MEDICAL.typeName -> {
                holder.itemView.choice_card_image.setImageResource(R.drawable.ic_medical_active)
                holder.itemView.choice_card_type.setText(R.string.tour_type_medical)
            }
            TourType.ALIMENTARY.typeName -> {
                holder.itemView.choice_card_image.setImageResource(R.drawable.ic_distributive_active)
                holder.itemView.choice_card_type.setText(R.string.tour_type_alimentary)
            }
            TourType.BARE_HANDS.typeName -> {
                holder.itemView.choice_card_image.setImageResource(R.drawable.ic_social_active)
                holder.itemView.choice_card_type.setText(R.string.tour_type_bare_hands)
            }
        }
        holder.itemView.choice_card_date.text = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)
                .format(tour.getStartTime())
        holder.itemView.choice_card_organization.text = tour.organizationName
    }

    inner class ChoiceViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        override fun onClick(v: View) {
            itemListener.recyclerViewListClicked(tours[this.layoutPosition])
        }

        init {
            itemView.choice_card_view.setOnClickListener(this)
        }
    }

    interface RecyclerViewClickListener {
        fun recyclerViewListClicked(tour: Tour)
    }

}