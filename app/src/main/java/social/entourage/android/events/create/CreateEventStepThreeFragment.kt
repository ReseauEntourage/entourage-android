package social.entourage.android.events.create

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import social.entourage.android.R
import social.entourage.android.databinding.FragmentCreateEventStepThreeBinding
import social.entourage.android.databinding.ItemCreateEventOptionCardBinding
import social.entourage.android.tools.log.AnalyticsEvents

/**
 * Étape 3, « Où et pour qui » : présentiel / en ligne, adresse ou lien, nombre de places et
 * cartes de public et d'accessibilité.
 */
class CreateEventStepThreeFragment : Fragment() {

    private var _binding: FragmentCreateEventStepThreeBinding? = null
    val binding: FragmentCreateEventStepThreeBinding get() = _binding!!

    private val viewModel: CreateEventViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateEventStepThreeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpCards()
        setUpModeToggle()
        setUpPlace()
        setUpPlaceLimit()
        observeViewModel()
        render()
        if (!viewModel.isEdition) {
            AnalyticsEvents.logEvent(AnalyticsEvents.Event_create_3)
        }
    }

    override fun onResume() {
        super.onResume()
        // Au retour de la sélection d'adresse, le ViewModel porte la nouvelle adresse.
        _binding?.let { renderPlace() }
    }

    // --- Présentiel / en ligne --------------------------------------------------------------

    private fun setUpModeToggle() {
        binding.faceToFace.setOnClickListener { setOnline(false) }
        binding.online.setOnClickListener { setOnline(true) }
    }

    private fun setOnline(online: Boolean) {
        // Passer en ligne annule la carte fauteuil (sans objet) : voir CreateEventForm.setOnlineMode.
        viewModel.form.setOnlineMode(online)
        viewModel.onFormChanged()
        render()
    }

    private fun setUpPlace() {
        binding.location.setOnClickListener {
            findNavController().navigate(R.id.action_create_event_fragment_to_edit_action_zone_fragment)
        }
        binding.eventUrl.doAfterTextChanged {
            viewModel.form.eventUrl = it?.toString() ?: ""
            viewModel.onFormChanged()
        }
    }

    // --- Nombre de places -------------------------------------------------------------------

    private fun setUpPlaceLimit() {
        binding.placesPlus.setOnClickListener {
            val form = viewModel.form
            form.placeLimit = (form.placeLimit ?: 0) + 1
            onPlaceLimitChanged()
        }
        binding.placesMinus.setOnClickListener {
            val form = viewModel.form
            val current = form.placeLimit ?: return@setOnClickListener
            // En dessous de 1, on revient à « Non » (pas de limite).
            form.placeLimit = if (current > 1) current - 1 else null
            onPlaceLimitChanged()
        }
    }

    private fun onPlaceLimitChanged() {
        renderPlaceLimit()
        viewModel.onFormChanged()
    }

    private fun renderPlaceLimit() {
        val limit = viewModel.form.placeLimit
        binding.placesValue.text = limit?.toString() ?: getString(R.string.create_event_places_none)
        binding.placesMinus.alpha = if (limit == null) 0.4f else 1f
    }

    // --- Cartes -----------------------------------------------------------------------------

    private fun setUpCards() {
        bindCard(
            binding.cardWheelchair, R.drawable.ic_create_event_wheelchair,
            R.string.create_event_card_wheelchair, R.string.create_event_card_wheelchair_desc
        ) { viewModel.form.wheelchairAccessible = !viewModel.form.wheelchairAccessible }
        bindCard(
            binding.cardFamily, R.drawable.ic_create_event_family,
            R.string.create_event_card_family, R.string.create_event_card_family_desc
        ) { viewModel.form.familyFriendly = !viewModel.form.familyFriendly }
        bindCard(
            binding.cardFemale, R.drawable.ic_create_event_female,
            R.string.create_event_card_female, null
        ) { viewModel.form.reservedFemale = !viewModel.form.reservedFemale }
    }

    private fun bindCard(
        card: ItemCreateEventOptionCardBinding,
        @DrawableRes icon: Int,
        @StringRes title: Int,
        @StringRes description: Int?,
        toggle: () -> Unit
    ) {
        card.cardIcon.setImageResource(icon)
        card.cardTitle.setText(title)
        if (description != null) {
            card.cardDescription.setText(description)
            card.cardDescription.isVisible = true
        }
        card.card.setOnClickListener {
            toggle()
            viewModel.onFormChanged()
            renderCards()
        }
    }

    private fun renderCards() {
        val form = viewModel.form
        binding.cardWheelchair.card.isSelected = form.wheelchairAccessible
        binding.cardFamily.card.isSelected = form.familyFriendly
        binding.cardFemale.card.isSelected = form.reservedFemale
        // La carte fauteuil n'a pas de sens pour un événement en ligne.
        binding.cardWheelchair.card.isVisible = !form.online
        binding.cardsTitle.setText(
            if (form.online) R.string.create_event_public else R.string.create_event_access_and_public
        )
    }

    // --- Affichage --------------------------------------------------------------------------

    private fun render() {
        val online = viewModel.form.online
        styleToggle(binding.faceToFace, selected = !online)
        styleToggle(binding.online, selected = online)
        binding.placeLabel.setText(
            if (online) R.string.create_event_link_label else R.string.create_event_address_label
        )
        binding.location.isVisible = !online
        binding.eventUrl.isVisible = online
        if (binding.eventUrl.text.toString() != viewModel.form.eventUrl) {
            binding.eventUrl.setText(viewModel.form.eventUrl)
        }
        renderPlace()
        renderPlaceLimit()
        renderCards()
        applyErrors(viewModel.errors.value ?: emptyMap())
    }

    private fun renderPlace() {
        binding.location.text = viewModel.form.address
    }

    private fun styleToggle(view: View, selected: Boolean) {
        view.background = if (selected) {
            ContextCompat.getDrawable(requireContext(), R.drawable.bg_create_event_toggle_selected)
        } else null
        (view as android.widget.TextView).setTextColor(
            if (selected) Color.WHITE else ContextCompat.getColor(requireContext(), R.color.grey)
        )
    }

    // --- Erreurs ----------------------------------------------------------------------------

    private fun observeViewModel() {
        viewModel.errors.observe(viewLifecycleOwner, ::applyErrors)
    }

    private fun applyErrors(errors: Map<CreateEventField, Int>) {
        val place = errors[CreateEventField.PLACE]
        binding.placeError.bindError(place)
        binding.location.bindErrorState(place != null)
        binding.eventUrl.bindErrorState(place != null)
        binding.placesError.bindError(errors[CreateEventField.PLACE_LIMIT])
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
