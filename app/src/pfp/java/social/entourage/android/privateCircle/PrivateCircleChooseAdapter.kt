package social.entourage.android.privateCircle

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.R
import social.entourage.android.api.model.map.Entourage
import java.util.*

/**
 * Created by Mihai Ionescu on 05/06/2018.
 */
class PrivateCircleChooseAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private class PrivateCircleChooseViewHolder(itemView: View, onCheckedChangeListener: OnCheckedChangeListener?) : RecyclerView.ViewHolder(itemView) {
        var privateCircleIcon: ImageView
        var privateCircleTitle: TextView
        var privateCircleCheckbox: CheckBox?

        init {
            privateCircleIcon = itemView.findViewById(R.id.privatecircle_icon)
            privateCircleTitle = itemView.findViewById(R.id.privatecircle_title)
            privateCircleCheckbox = itemView.findViewById(R.id.privatecircle_checkbox)
            if (privateCircleCheckbox != null) {
                privateCircleCheckbox!!.setOnCheckedChangeListener(onCheckedChangeListener)
            }
            itemView.setOnClickListener {
                if (privateCircleCheckbox != null) {
                    privateCircleCheckbox!!.isChecked = !privateCircleCheckbox!!.isChecked
                }
            }
        }
    }

    private val privateCircleList: MutableList<Entourage>? = ArrayList()
    var selectedPrivateCircle = AdapterView.INVALID_POSITION
        private set
    private val onCheckedChangeListener = OnCheckedChangeListener()
    fun addPrivateCircleList(privateCircleList: List<Entourage>?) {
        this.privateCircleList!!.addAll(privateCircleList!!)
        selectedPrivateCircle = 0
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_privatecircle_choose_item, parent, false)
        return PrivateCircleChooseViewHolder(view, onCheckedChangeListener)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val entourage = getItemAt(position)
        if (entourage != null) {
            val viewHolder = holder as PrivateCircleChooseViewHolder
            val context = viewHolder.itemView.context
            viewHolder.privateCircleIcon.setImageDrawable(entourage.getIconDrawable(context))
            if (position == selectedPrivateCircle) {
                viewHolder.privateCircleTitle.setTypeface(viewHolder.privateCircleTitle.typeface, Typeface.BOLD)
            } else {
                viewHolder.privateCircleTitle.typeface = Typeface.create(viewHolder.privateCircleTitle.typeface, Typeface.NORMAL)
            }
            viewHolder.privateCircleTitle.text = entourage.title

            // set the tag to null so that oncheckedchangelistener exits when populating the view
            viewHolder.privateCircleCheckbox!!.tag = null
            // set the check state
            viewHolder.privateCircleCheckbox!!.isChecked = position == selectedPrivateCircle
            // set the tag to the item position
            viewHolder.privateCircleCheckbox!!.tag = position
        }
    }

    override fun getItemCount(): Int {
        return privateCircleList!!.size
    }

    fun getItemAt(position: Int): Entourage? {
        return if (privateCircleList == null || position < 0 || position >= privateCircleList.size) {
            null
        } else privateCircleList[position]
    }

    /**
     * Listener for checkboxes. At most one checkbox can be checked.
     */
    private inner class OnCheckedChangeListener : CompoundButton.OnCheckedChangeListener {
        override fun onCheckedChanged(compoundButton: CompoundButton, isChecked: Boolean) {
            // if no tag, exit
            if (compoundButton.tag == null) {
                return
            }
            // get the position
            val position = compoundButton.tag as Int
            selectedPrivateCircle = if (position == selectedPrivateCircle) {
                AdapterView.INVALID_POSITION
            } else {
                position
            }
            notifyDataSetChanged()
        }
    }
}