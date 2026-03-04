package social.entourage.android.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.R

class HomeSectionButtonAdapter(private var onClick: () -> Unit) : RecyclerView.Adapter<HomeSectionButtonAdapter.ButtonViewHolder>() {
    var buttonText: String = ""
    var isVisible: Boolean = false

    fun update(buttonText: String, isVisible: Boolean, newOnClick: (() -> Unit)? = null) {
        this.buttonText = buttonText
        this.isVisible = isVisible
        if (newOnClick != null) {
            this.onClick = newOnClick
        }
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return if (isVisible) 1 else 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ButtonViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.home_section_button, parent, false)
        return ButtonViewHolder(view)
    }

    override fun onBindViewHolder(holder: ButtonViewHolder, position: Int) {
        holder.buttonTitle.text = buttonText
        holder.itemView.setOnClickListener { onClick() }
    }

    class ButtonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val buttonTitle: TextView = view.findViewById(R.id.btn_title)
    }
}