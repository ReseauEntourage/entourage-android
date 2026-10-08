package social.entourage.android.events.create

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import social.entourage.android.R
import social.entourage.android.api.model.EventUtils
import social.entourage.android.databinding.FragmentCreateEventPreviewBinding
import social.entourage.android.language.LanguageManager
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.px
import java.io.File
import java.time.format.DateTimeFormatter

/**
 * Aperçu avant publication, partagé par la création (« Publier ») et l'édition (« Modifier »).
 * Le libellé de l'action finale est porté par le conteneur ; cet écran n'affiche que l'événement
 * tel que les participants le verront, d'après l'état du [CreateEventViewModel].
 */
class CreateEventPreviewFragment : Fragment() {

    private var _binding: FragmentCreateEventPreviewBinding? = null
    val binding: FragmentCreateEventPreviewBinding get() = _binding!!

    private val viewModel: CreateEventViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateEventPreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        // L'état peut avoir changé depuis une étape précédente : on relit le formulaire à chaque affichage.
        _binding?.let { render() }
    }

    private fun render() {
        val form = viewModel.form
        renderImage(form)
        binding.previewTitle.text = form.title
        binding.previewDescription.text = form.description
        binding.previewDate.text = dateLine(form)
        binding.previewPlace.text = if (form.online) form.eventUrl else form.address
        renderChips(form)
        renderGroupsNudge(form)
    }

    private fun renderImage(form: CreateEventForm) {
        val source: Any = form.localPhotoPath?.let { File(it) }
            ?: form.photoUrl?.let { Uri.parse(it) }
            ?: R.drawable.ic_event_placeholder
        Glide.with(this)
            .load(source)
            .transform(CenterCrop(), RoundedCorners(Const.ROUNDED_CORNERS_IMAGES.px))
            .into(binding.previewImage)
    }

    private fun dateLine(form: CreateEventForm): String {
        val locale = LanguageManager.getLocaleFromPreferences(requireContext())
        val date = CreateEventForm.parseDate(form.date) ?: return ""
        val start = CreateEventForm.parseTime(form.startTime)
        val end = CreateEventForm.parseTime(form.endTime)
        val dateText = DateTimeFormatter.ofPattern("EEE d MMM", locale).format(date)
        if (start == null || end == null) return dateText
        val timeFormat = DateTimeFormatter.ofPattern(getString(R.string.events_time), locale)
        return getString(
            R.string.create_event_preview_time_range,
            dateText, timeFormat.format(start), timeFormat.format(end)
        )
    }

    /** Pastilles d'accessibilité / de public renseignées, puis catégories. */
    private fun renderChips(form: CreateEventForm) {
        val chips = mutableListOf<String>()
        // Les pastilles fauteuil et famille s'affichent ici même si elles ne sont pas encore envoyées à l'API.
        if (form.wheelchairAccessible && !form.online) chips.add(getString(R.string.create_event_preview_chip_wheelchair))
        if (form.familyFriendly) chips.add(getString(R.string.create_event_preview_chip_family))
        if (form.reservedFemale) chips.add(getString(R.string.create_event_preview_chip_female))
        form.interests.forEach { chips.add(EventUtils.showTagTranslated(requireContext(), it)) }

        binding.previewChips.removeAllViews()
        binding.previewChips.isVisible = chips.isNotEmpty()
        val inflater = LayoutInflater.from(requireContext())
        chips.forEach { label ->
            val chip = inflater.inflate(R.layout.item_create_event_preview_chip, binding.previewChips, false) as TextView
            chip.text = label
            binding.previewChips.addView(chip)
        }
    }

    private fun renderGroupsNudge(form: CreateEventForm) {
        val count = form.neighborhoodIds.size
        binding.previewGroupsNudge.isVisible = count > 0
        if (count > 0) {
            binding.previewGroupsNudge.text =
                resources.getQuantityString(R.plurals.create_event_preview_groups_nudge, count, count)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
