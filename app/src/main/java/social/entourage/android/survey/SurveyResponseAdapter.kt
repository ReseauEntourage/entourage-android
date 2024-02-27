package social.entourage.android.survey

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.api.model.Survey
import social.entourage.android.api.model.SurveyResponse
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
        // Construire la liste des éléments avec des sections
        /*survey.choices.forEachIndexed { index, choice ->
            items.add(choice) // La section
            // Ajoutez tous les utilisateurs qui ont répondu à cette section
            val userResponses = responsesList.responses.filter { it.response == index }
            items.addAll(userResponses)
        }*/
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is String -> VIEW_TYPE_SECTION
            is SurveyResponse -> VIEW_TYPE_ITEM
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

            }
            is ItemViewHolder -> {
                val response = items[position] as SurveyResponse

            }
        }
    }

    override fun getItemCount() = items.size
}





class SectionViewHolder(val binding: LayoutSectionSurveyResponseItemBinding) : RecyclerView.ViewHolder(binding.root)

class ItemViewHolder(val binding: LayoutVoterSurveyResponseItemBinding) : RecyclerView.ViewHolder(binding.root)
