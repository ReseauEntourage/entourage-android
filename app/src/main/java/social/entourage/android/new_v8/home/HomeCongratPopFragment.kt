package social.entourage.android.new_v8.home

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.view.*
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import social.entourage.android.R
import social.entourage.android.databinding.FragmentHomeCongratPopBinding
import social.entourage.android.new_v8.models.HomeAction
import social.entourage.android.new_v8.utils.px

class HomeCongratPopFragment : DialogFragment() {
    private val ARGS_Actions = "Actions"
    private val animFadeDuration: Long = 2000

    private var _binding: FragmentHomeCongratPopBinding? = null
    val binding: FragmentHomeCongratPopBinding get() = _binding!!

    private var actions:ArrayList<HomeAction>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            actions = it.getSerializable(ARGS_Actions) as? ArrayList<HomeAction>
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeCongratPopBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        populateViews()
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.let { window ->
            val params: ViewGroup.LayoutParams = window.attributes
            params.width =  ViewGroup.LayoutParams.MATCH_PARENT
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT
            window.attributes = params as WindowManager.LayoutParams
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    private fun populateViews() {
        actions?.let {
            binding.uiLayoutList.isInvisible = true
            binding.uiLayoutList1.isVisible = false
            binding.uiLayoutList2.isVisible = false
            binding.uiLayoutList3.isVisible = false

            val countStr = if (it.count() > 1) getString(R.string.home_congrat_title_plural,it.count()) else  getString(R.string.home_congrat_title_single,it.count())

            binding.uiTitleStart.text = countStr
            binding.uiuTitleList.text = countStr

            if (1 <= it.count()) {
                binding.uiLayoutList1.isVisible = true
                binding.uiTitleList1.text = it[0].name
                Glide.with(binding.uiImageList1.context)
                    .load(it[0].imageURL)
                    .error(R.drawable.new_illu_header_group)
                    .transform(CenterCrop(), RoundedCorners(14.px))
                    .into(binding.uiImageList1)
            }
            if (2 <= it.count()) {
                binding.uiLayoutList2.isVisible = true
                binding.uiTitleList2.text = it[1].name
                Glide.with(binding.uiImageList2.context)
                    .load(it[1].imageURL)
                    .error(R.drawable.new_illu_header_group)
                    .transform(CenterCrop(), RoundedCorners(14.px))
                    .into(binding.uiImageList2)
            }
            if (3 <= it.count()) {
                binding.uiLayoutList3.isVisible = true
                binding.uiTitleList3.text = it[2].name
                Glide.with(binding.uiImageList3.context)
                    .load(it[2].imageURL)
                    .error(R.drawable.new_illu_header_group)
                    .transform(CenterCrop(), RoundedCorners(14.px))
                    .into(binding.uiImageList3)
            }

            binding.animationView.addAnimatorUpdateListener { update ->
                if (update.animatedFraction == 1f) {
                    showMiddleView()
                    binding.animationView.removeAllAnimatorListeners()
                }
            }

            binding.uiPopClose.setOnClickListener {
                dismiss()
            }

            binding.animationView.playAnimation()

        } ?: run { dismiss() }
    }

    private fun showMiddleView() {
        binding.uiLayoutList.apply {
            alpha = 0f
            visibility = View.VISIBLE

            animate()
                .alpha(1f)
                .setDuration(animFadeDuration / 2)
                .setListener(null)
        }

        binding.uiLayoutAnim.animate()
            .alpha(0f)
            .setDuration(animFadeDuration / 2)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    binding.uiLayoutAnim.visibility = View.GONE
                }
            })

        val timer = object: CountDownTimer(4500, 1000) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                showEndView()
            }
        }
        timer.start()
    }

    private fun showEndView() {
        binding.uiLayoutEnd.apply {
            alpha = 0f
            visibility = View.VISIBLE

            animate()
                .alpha(1f)
                .setDuration(animFadeDuration)
                .setListener(null)
        }

        binding.uiLayoutList.animate()
            .alpha(0f)
            .setDuration(animFadeDuration)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    binding.uiLayoutAnim.visibility = View.GONE
                }
            })

        val timer = object: CountDownTimer(3500, 1000) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                dismiss()
            }
        }
        timer.start()
    }

    companion object {
        val TAG: String? = HomeCongratPopFragment::class.java.simpleName

        @JvmStatic
        fun newInstance(congratulations: ArrayList<HomeAction>) =
            HomeCongratPopFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARGS_Actions, congratulations)
                }
            }
    }
}