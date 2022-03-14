package entourage.social.android.profile

import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.fragment.app.Fragment
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import entourage.social.android.databinding.FragmentMyProfileBinding


class MyProfileFragment : Fragment() {
    private var _binding: FragmentMyProfileBinding? = null
    val binding: FragmentMyProfileBinding get() = _binding!!

    private val interests = listOf(
        "sport", "menuiserie", "jeux de société", "musique", "foot", "musique"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeInterests()
        setProgressBarIndicator()
    }


    private fun initializeInterests() {
        binding.interests.apply {
            val layoutManagerFlex = FlexboxLayoutManager(context)
            layoutManagerFlex.flexDirection = FlexDirection.ROW
            layoutManagerFlex.justifyContent = JustifyContent.CENTER
            layoutManager = layoutManagerFlex
            adapter = InterestsAdapter(interests)
        }
    }

    private fun setProgressBarIndicator() {
        val progressBar = binding.progressBar
        val score = binding.progressbarValue
        score.text = progressBar.progress.toString()

        val maxSizePoint = Point()
        val maxX = maxSizePoint.x
        progressBar.setOnSeekBarChangeListener(object :
            OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar,
                progressValue: Int,
                fromUser: Boolean
            ) {
                val value: Int =
                    progressValue * (progressBar.width - 2 * progressBar.thumbOffset) / progressBar.max
                score.text = progressValue.toString()
                val textViewX: Int = value - score.width / 2
                val finalX =
                    if (score.width + textViewX > maxX) maxX - score.width - 16 else textViewX + 16 /*your margin*/
                score.x = if (finalX < 0) 16F /*your margin*/ else finalX.toFloat()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

    }
}