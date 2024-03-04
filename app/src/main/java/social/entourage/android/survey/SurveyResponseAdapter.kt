package social.entourage.android.survey

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import social.entourage.android.api.model.Survey
import social.entourage.android.api.model.SurveyResponse
import social.entourage.android.api.model.SurveyResponseUser
import social.entourage.android.api.model.SurveyResponsesListWrapper
import social.entourage.android.databinding.LayoutSectionSurveyResponseItemBinding
import social.entourage.android.databinding.LayoutVoterSurveyResponseItemBinding


class SurveyResponseAdapter(
    private val survey: Survey,
    private val responsesList: SurveyResponsesListWrapper
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
        survey.choices.forEachIndexed { index, choice ->
            items.add(choice) // Ajoute le choix comme en-tête de section
            responsesList.responses.getOrNull(index)?.let { usersForChoice ->
                if (usersForChoice.isNotEmpty()) {
                    items.addAll(usersForChoice) // Ajoute tous les utilisateurs pour ce choix
                }
            }
        }
        Log.wtf("SurveyResponseAdapter", "items: " + Gson().toJson(items))
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
                val voteCount = if (sectionIndex != -1) survey.summary[sectionIndex] else 0
                holder.binding.numberVote.text = "$voteCount vote${if (voteCount > 1) "s" else ""}"
            }
            is ItemViewHolder -> {
                val responseUser = items[position] as SurveyResponseUser
                holder.binding.name.text = responseUser.displayName
                if (responseUser.avatarUrl != null) {
                    // Utilise Glide ou Picasso pour charger l'image dans l'ImageView
                    Glide.with(holder.binding.root.context)
                        .load(responseUser.avatarUrl)
                        .into(holder.binding.picture)
                }
                // Gérer l'affichage des rôles communautaires
            }
        }
    }

    override fun getItemCount() = items.size
}

class SectionViewHolder(val binding: LayoutSectionSurveyResponseItemBinding) : RecyclerView.ViewHolder(binding.root)
class ItemViewHolder(val binding: LayoutVoterSurveyResponseItemBinding) : RecyclerView.ViewHolder(binding.root)
