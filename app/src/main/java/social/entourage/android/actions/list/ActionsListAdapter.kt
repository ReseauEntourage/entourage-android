package social.entourage.android.actions.list

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import social.entourage.android.R
import social.entourage.android.actions.detail.ActionDetailActivity
import social.entourage.android.api.model.Action
import social.entourage.android.api.model.ActionSection
import social.entourage.android.api.model.ActionUtils
import social.entourage.android.databinding.LayoutContribItemBinding
import social.entourage.android.databinding.LayoutDemandItemBinding
import social.entourage.android.tools.displayDistance
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.px

class ActionsListAdapter(
    var userId: Int?,
    //private val isContrib:Boolean,
    var context: Context
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var groupsList: MutableList<Action> = mutableListOf()
    var isModContrib = false
    fun resetData(groupsList: MutableList<Action>, isContrib: Boolean) {
        this.isModContrib = isContrib
        this.groupsList.clear()
        this.groupsList.addAll(groupsList)
        notifyDataSetChanged()
    }
    override fun getItemViewType(position: Int): Int {
        return  if (isModContrib) TYPE_CONTRIB else TYPE_DEMAND
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TYPE_CONTRIB) {
            val view = LayoutContribItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolderContrib(view)
        }
        else {
            val view = LayoutDemandItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolderDemand(view)
        }
    }

    inner class ViewHolderContrib(val binding: LayoutContribItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(action: Action) {
            binding.layoutContrib.setOnClickListener { view ->
                (view.context as? Activity)?.startActivityForResult(
                    Intent(view.context, ActionDetailActivity::class.java)
                        .putExtra(Const.ACTION_ID, action.id)
                        .putExtra(Const.ACTION_TITLE,action.title)
                        .putExtra(Const.IS_ACTION_DEMAND,false)
                        .putExtra(Const.IS_ACTION_MINE, action.isMine()),
                    0
                )
            }

            binding.name.text = action.title
            if(action.distance != null){
                binding.distance.text = action.displayDistance(context)

            }else{
                binding.distance.text = "xx km"
            }
            binding.location.text = action.metadata?.displayAddress
            binding.date.text = action.dateFormattedString(binding.root.context)
            action.imageUrl?.let {
                Glide.with(binding.image.context)
                    .load(it)
                    .error(R.drawable.ic_placeholder_action)
                    .apply(RequestOptions().override(90.px, 90.px))
                    .transform(CenterCrop(), RoundedCorners(20.px))
                    .into(binding.image)
            } ?: run {
                Glide.with(binding.image.context)
                    .load(R.drawable.ic_placeholder_action)
                    .apply(RequestOptions().override(90.px, 90.px))
                    .transform(CenterCrop(), RoundedCorners(20.px))
                    .into(binding.image)
            }
        }
    }

    inner class ViewHolderDemand(val binding: LayoutDemandItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(action: Action) {
            binding.layoutDemand.setOnClickListener { view->
                (view.context as? Activity)?.startActivityForResult(
                    Intent(view.context, ActionDetailActivity::class.java)
                        .putExtra(Const.ACTION_ID, action.id)
                        .putExtra(Const.ACTION_TITLE,action.title)
                        .putExtra(Const.IS_ACTION_DEMAND,true)
                        .putExtra(Const.IS_ACTION_MINE, action.isMine()),
                    0
                )
            }
            val isArabic = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                binding.root.resources.configuration.locales[0].language == "ar"
            } else {
                binding.root.resources.configuration.locale.language == "ar"
            }

            // Appliquer les propriétés en fonction de la langue
            if (isArabic) {
                binding.demandUsername.layoutDirection = View.LAYOUT_DIRECTION_RTL
                binding.demandUsername.gravity = Gravity.END
                binding.demandUsername.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
                binding.demandUsername.textDirection = View.TEXT_DIRECTION_RTL
            } else {
                binding.demandUsername.layoutDirection = View.LAYOUT_DIRECTION_LTR
                binding.demandUsername.gravity = Gravity.START
                binding.demandUsername.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
                binding.demandUsername.textDirection = View.TEXT_DIRECTION_LTR
            }

            binding.demandTitle.text = action.title
            if(action.sectionName != null){
                binding.demandSectionName.text = ActionUtils.showTagTranslated(context,action.sectionName!!)
            }
            binding.demandSectionPic.setImageDrawable( AppCompatResources.getDrawable(  binding.root.context, ActionSection.getIconFromId(action.sectionName)))
            binding.demandUsername.text = action.author?.userName
            binding.demandLocation.text = action.metadata?.displayAddress
            binding.demandDate.text = action.dateFormattedString(binding.root.context)

            action.author?.avatarURLAsString?.let { avatarURL ->
                Glide.with(binding.demandPict.context)
                    .load(avatarURL)
                    .error(R.drawable.placeholder_user)
                    .circleCrop()
                    .into(binding.demandPict)
            } ?: run {
                Glide.with(binding.demandPict.context)
                    .load(R.drawable.placeholder_user)
                    .circleCrop()
                    .into(binding.demandPict)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == TYPE_CONTRIB) {
            (holder as? ViewHolderContrib)?.bind(groupsList[position])
        }
        else {
            (holder as? ViewHolderDemand)?.bind(groupsList[position])
        }
    }

    override fun getItemCount(): Int {
        return groupsList.size
    }

    companion object {
        const val TYPE_CONTRIB = 0
        const val TYPE_DEMAND = 1
    }
}
