package social.entourage.android.accessibility

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import social.entourage.android.databinding.BottomSheetAccessibilityBinding

/**
 * Profil > Paramètres > Accessibilité : propose le mode "contraste amélioré". Le choix est enregistré
 * tout de suite ; l'activité qui affiche la feuille se recrée pour appliquer les couleurs, les autres
 * activités de la pile se recréent à leur retour (voir EntourageApplication).
 */
class AccessibilityBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetAccessibilityBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetAccessibilityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.switchHighContrast.isChecked = AccessibilitySettings.highContrast

        binding.btnCross.setOnClickListener { dismiss() }
        // Toute la ligne bascule le réglage, pas seulement l'interrupteur
        binding.rowHighContrast.setOnClickListener {
            binding.switchHighContrast.toggle()
        }
        binding.switchHighContrast.setOnCheckedChangeListener { _, enabled ->
            if (enabled == AccessibilitySettings.highContrast) return@setOnCheckedChangeListener
            val host = activity
            AccessibilitySettings.setHighContrast(requireContext().applicationContext, enabled)
            dismiss()
            host?.recreate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "AccessibilityBottomSheet"

        fun newInstance() = AccessibilityBottomSheet()
    }
}
