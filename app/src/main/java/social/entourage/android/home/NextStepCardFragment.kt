package social.entourage.android.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import social.entourage.android.databinding.FragmentNextStepCardBinding

class NextStepCardFragment : Fragment() {

    private var _binding: FragmentNextStepCardBinding? = null
    private val binding get() = _binding!!

    private lateinit var presenter: NextStepPresenter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNextStepCardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter = ViewModelProvider(this).get(NextStepPresenter::class.java)

        presenter.nextStep.observe(viewLifecycleOwner) { nextStep ->
            if (nextStep != null) {
                binding.nextStepCardRoot.visibility = View.VISIBLE
                binding.tvNextStepTitle.text = nextStep.title
                binding.tvNextStepLabel.text = "VOTRE PROCHAIN PAS"
                binding.btnNextStepCta.text = nextStep.ctaLabel

                if (!nextStep.reason.isNullOrBlank()) {
                    binding.tvNextStepReason.text = nextStep.reason
                    binding.tvNextStepReason.visibility = View.VISIBLE
                } else {
                    binding.tvNextStepReason.visibility = View.GONE
                }

                binding.btnNextStepCta.setOnClickListener {
                    presenter.completeStep(nextStep.id)
                }

                binding.btnNextStepDismiss.setOnClickListener {
                    presenter.dismissStep(nextStep.id)
                }
            } else {
                binding.nextStepCardRoot.visibility = View.GONE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.loadNextStep()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "NextStepCardFragment"

        fun newInstance(): NextStepCardFragment {
            return NextStepCardFragment()
        }
    }
}
