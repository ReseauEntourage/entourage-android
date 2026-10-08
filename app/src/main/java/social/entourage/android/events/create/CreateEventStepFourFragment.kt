package social.entourage.android.events.create

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.TextView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import social.entourage.android.api.MetaDataRepository
import social.entourage.android.api.model.EventUtils
import social.entourage.android.api.model.Interest
import social.entourage.android.api.model.Tags
import social.entourage.android.databinding.FragmentCreateEventStepFourBinding
import social.entourage.android.R
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.utils.px

/** Étape 4 : catégories de l'événement, en pastilles sélectionnables. */
class CreateEventStepFourFragment : Fragment() {

    private var _binding: FragmentCreateEventStepFourBinding? = null
    val binding: FragmentCreateEventStepFourBinding get() = _binding!!

    private val viewModel: CreateEventViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateEventStepFourBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        MetaDataRepository.metaData.observe(viewLifecycleOwner, ::renderInterests)
        viewModel.errors.observe(viewLifecycleOwner) {
            binding.categoriesError.bindError(it[CreateEventField.CATEGORIES])
        }
        if (!viewModel.isEdition) {
            AnalyticsEvents.logEvent(AnalyticsEvents.Event_create_4)
        }
    }

    private fun renderInterests(tags: Tags?) {
        binding.interestsGroup.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())
        tags?.interests?.forEach { tag ->
            val id = tag.id ?: return@forEach
            val chip = inflater.inflate(
                R.layout.item_create_event_interest_chip, binding.interestsGroup, false
            ) as TextView
            chip.text = EventUtils.showTagTranslated(requireContext(), id)
            chip.setCompoundDrawablesRelative(iconFor(id), null, null, null)
            chip.isSelected = viewModel.form.interests.contains(id)
            chip.setOnClickListener {
                val interests = viewModel.form.interests
                if (interests.contains(id)) interests.remove(id) else interests.add(id)
                chip.isSelected = interests.contains(id)
                viewModel.onFormChanged()
            }
            binding.interestsGroup.addView(chip)
        }
    }

    private fun iconFor(id: String): Drawable? =
        ContextCompat.getDrawable(requireContext(), Interest.getIconFromId(id))?.mutate()?.apply {
            val size = ICON_SIZE_DP.px
            setBounds(0, 0, size, size)
        }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private companion object {
        const val ICON_SIZE_DP = 20
    }
}
