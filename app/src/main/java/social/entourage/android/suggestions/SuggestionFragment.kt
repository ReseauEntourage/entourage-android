package social.entourage.android.suggestions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.api.model.Suggestion
import social.entourage.android.databinding.FragmentSuggestionsBinding

class SuggestionFragment : Fragment() {

    private var _binding: FragmentSuggestionsBinding? = null
    private val binding get() = _binding!!

    private lateinit var suggestionsViewModel: SuggestionsViewModel
    private lateinit var suggestionAdapter: SuggestionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSuggestionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        suggestionsViewModel = ViewModelProvider(this).get(SuggestionsViewModel::class.java)

        suggestionAdapter = SuggestionAdapter(object : SuggestionAdapterListener {
            override fun onInfoTap(suggestion: Suggestion) {
                SuggestionReasonBottomSheet.newInstance(suggestion)
                    .show(childFragmentManager, "reason")
            }
            override fun onCtaTap(suggestion: Suggestion) {
                // Navigate to item detail based on type
            }
        })

        binding.rvSuggestions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = suggestionAdapter

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (!recyclerView.canScrollVertically(1)) {
                        suggestionsViewModel.loadSuggestions(reset = false)
                    }
                }
            })
        }

        binding.ivBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        observeViewModel()
        suggestionsViewModel.loadSuggestions(reset = true)
    }

    private fun observeViewModel() {
        suggestionsViewModel.suggestions.observe(viewLifecycleOwner) { list ->
            suggestionAdapter.resetData(list)
        }

        suggestionsViewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }
    }
}
