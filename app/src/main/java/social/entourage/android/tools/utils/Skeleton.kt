package social.entourage.android.tools.utils

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.R

fun ViewGroup.showSkeleton() {
    val skeletonOverlay = ConstraintLayout(context).apply {
        layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT)
        id = R.id.skeleton_overlay_id // Assurez-vous que cet ID est unique et défini dans vos ressources.
        setBackgroundColor(Color.WHITE) // Ou toute autre couleur pour l'arrière-plan de votre squelette
        elevation = 20f  // Assurez-vous que cette élévation est supérieure à celle des autres vues pour la superposition
    }
    this.addView(skeletonOverlay)
    recurseAndReplaceWithSkeleton(this, skeletonOverlay,context)
}

private fun recurseAndReplaceWithSkeleton(originalViewGroup: ViewGroup, skeletonOverlay: ConstraintLayout,context: Context) {
    for (i in 0 until originalViewGroup.childCount) {
        val child = originalViewGroup.getChildAt(i)

        val skeletonView = View(context).apply {
            layoutParams = ConstraintLayout.LayoutParams(child.width, child.height).also { lp ->
                lp.dimensionRatio = "H,1:1" // Pour les carrés, ou ajustez selon le type de vue
                lp.topToTop = child.id
                lp.startToStart = child.id
                lp.endToEnd = child.id
                lp.bottomToBottom = child.id
            }
            setBackgroundColor(Color.LTGRAY) // Couleur pour l'effet squelette
        }

        skeletonOverlay.addView(skeletonView)

        // Si l'enfant est un ViewGroup, récursivement remplacer ses enfants aussi
        if (child is ViewGroup) {
            recurseAndReplaceWithSkeleton(child, skeletonOverlay,context)
        }
    }
}

