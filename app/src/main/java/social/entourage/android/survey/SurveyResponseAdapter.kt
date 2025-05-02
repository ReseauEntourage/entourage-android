package social.entourage.android.survey

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import social.entourage.android.EntourageApplication
import social.entourage.android.api.model.Survey
import social.entourage.android.api.model.SurveyResponse
import social.entourage.android.api.model.SurveyResponseUser
import social.entourage.android.api.model.SurveyResponsesListWrapper
import social.entourage.android.databinding.LayoutSectionSurveyResponseItemBinding
import social.entourage.android.databinding.LayoutVoterSurveyResponseItemBinding
import social.entourage.android.groups.details.members.OnItemShowListener
import social.entourage.android.tools.utils.Const
import social.entourage.android.user.UserProfileActivity


class SurveyResponseAdapter(
    private val survey: Survey,
    private val responsesList: SurveyResponsesListWrapper,
    private var onItemShowListener: OnItemShowListener,
    val context: Context
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_SECTION = 0
        const val VIEW_TYPE_ITEM = 1
    }

    private val items = mutableListOf<Any>()


    init {
        populateItems(responsesList)
    }

    private fun populateItems(responsesList: SurveyResponsesListWrapper) {

        val myVoteMap = mutableMapOf<Int, Boolean>()
        for (i in ResponseSurveyActivity.myVote.indices) {
            myVoteMap[i] = ResponseSurveyActivity.myVote[i]
        }
        for (i in ResponseSurveyActivity.myVote.size until survey.choices.size) {
            myVoteMap[i] = false
        }


        survey.choices.forEachIndexed { index, choice ->
            items.add(choice)
            responsesList.responses.getOrNull(index)?.let { usersForChoice ->
                if (usersForChoice.isNotEmpty()) {
                    val filteredUsersForChoice = usersForChoice.filterNot { it.id == EntourageApplication.me(context)?.id }
                    items.addAll(filteredUsersForChoice)
                }
            }
            if (myVoteMap[index]!!) {
                val myUser = EntourageApplication.me(context)
                if (myUser != null) { // Check if user is retrieved successfully
                    items.add(SurveyResponseUser(myUser.id, "fr", myUser.displayName!!, myUser.avatarURL, myUser.roles!!.toList()))
                }
            }
        }
    }


    fun updateResponses(newResponsesList: SurveyResponsesListWrapper) {
        items.clear()
        populateItems(newResponsesList)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is String -> VIEW_TYPE_SECTION
            is SurveyResponseUser -> VIEW_TYPE_ITEM
            else -> throw IllegalArgumentException("Unknown item type")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_SECTION -> {
                val binding = LayoutSectionSurveyResponseItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                SectionViewHolder(binding)
            }
            VIEW_TYPE_ITEM -> {
                val binding = LayoutVoterSurveyResponseItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ItemViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (holder) {
            is SectionViewHolder -> {
                val section = items[position] as String
                holder.binding.tvTitleQuestionOption.text = section
                // Trouver l'index correct de la section dans survey.choices pour récupérer le nombre de votes
                val sectionIndex = survey.choices.indexOf(section)
                var voteCount = if (sectionIndex != -1) survey.summary[sectionIndex] else 0
                holder.binding.numberVote.text = "$voteCount vote${if (voteCount > 1) "s" else ""}"
            }
            is ItemViewHolder -> {
                val responseUser = items[position] as SurveyResponseUser
                holder.binding.name.text = responseUser.displayName
                if (responseUser.avatarUrl != null) {
                    // Utilise Glide ou Picasso pour charger l'image dans l'ImageView
                    Glide.with(holder.binding.root.context)
                        .load(responseUser.avatarUrl)
                        .circleCrop()
                        .into(holder.binding.picture)
                }
                holder.binding.layout.setOnClickListener {
                    onItemShowListener.onShowConversation(responseUser.id)
                }
                val isMe = EntourageApplication.get().me()?.id == responseUser.id
                if(isMe) {
                    holder.binding.contact.visibility = android.view.View.GONE
                }else {
                    holder.binding.contact.visibility = android.view.View.VISIBLE
                }
            }
        }
    }

    override fun getItemCount() = items.size
}

class SectionViewHolder(val binding: LayoutSectionSurveyResponseItemBinding) : RecyclerView.ViewHolder(binding.root)
class ItemViewHolder(val binding: LayoutVoterSurveyResponseItemBinding) : RecyclerView.ViewHolder(binding.root)


