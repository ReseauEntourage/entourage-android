package social.entourage.android.fab_comportement

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import social.entourage.android.R
import social.entourage.android.actions.create.CreateActionActivity
import social.entourage.android.databinding.FabActitivyLayoutBinding
import social.entourage.android.tools.utils.Const

enum class FabActionType {
    ACTION, GROUP
}

class FabActivity : AppCompatActivity() {

    private lateinit var binding: FabActitivyLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FabActitivyLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val actionType = FabActionType.ACTION // Remplacez par la valeur souhaitée

        val items = when (actionType) {
            FabActionType.ACTION -> listOf(
                ListItem(
                    R.drawable.new_ic_create_demand,
                    getString(R.string.action_menu_create_demand)
                ),
                ListItem(
                    R.drawable.new_ic_create_contrib,
                    getString(R.string.action_menu_create_contrib)
                )
            )
            FabActionType.GROUP -> listOf(
                ListItem(
                    R.drawable.new_create_post,
                    getString(R.string.create_post)
                ),
                ListItem(
                    R.drawable.new_create_event,
                    getString(R.string.create_event)
                )
            )
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = FabBottomMenudapter(items)
        // Animation pour faire apparaître les éléments un par un
        binding.recyclerView.layoutAnimation = AnimationUtils.loadLayoutAnimation(
            this, R.anim.layout_animation_fall_down
        )

        // FAB
        binding.createAction.setOnClickListener {
            // Votre code pour gérer le clic sur le FAB
        }

        // Rotation de l'icône du FAB
        rotateFabIcon()
    }

    private fun rotateFabIcon() {
        val rotate = ObjectAnimator.ofFloat(binding.createAction, View.ROTATION, 0f, 405f)
        rotate.duration = 1000
        rotate.start()
    }

    private fun goSolicitation(){
        val intent = Intent(this, CreateActionActivity::class.java)
        intent.putExtra(Const.IS_ACTION_DEMAND, true)
        startActivityForResult(intent, 0)
    }
    private fun goContrib(){
        val intent = Intent(this, CreateActionActivity::class.java)
        intent.putExtra(Const.IS_ACTION_DEMAND, false)
        startActivityForResult(intent, 0)
    }

}