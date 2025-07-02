package social.entourage.android.comment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable
import android.os.Build
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.model.Post
import social.entourage.android.databinding.*
import social.entourage.android.discussions.DetailConversationActivity
import social.entourage.android.language.LanguageManager
import social.entourage.android.profile.ProfileFullActivity
import social.entourage.android.report.DataLanguageStock
import social.entourage.android.tools.setHyperlinkClickable
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.px
import timber.log.Timber
import java.text.SimpleDateFormat

enum class CommentsTypes(val code: Int) {
    TYPE_LEFT(0),
    TYPE_RIGHT(1),
    TYPE_DATE(2),
    TYPE_DETAIL(3)
}

interface OnItemClickListener {
    fun onItemClick(comment: Post)
    fun onCommentReport(commentId: Int?, isForEvent: Boolean, isForGroup: Boolean, isMe: Boolean, commentLang: String)
    fun onShowWeb(url: String) // si tu veux ouvrir un navigateur ou gérer autrement
}

class CommentsListAdapter(
    var context: Context,
    private var commentsList: List<Post>,
    private var postAuthorId: Int,
    var isOne2One: Boolean,
    var isConversation: Boolean,
    private var currentParentPost: Post?,
    var onItemClick: OnItemClickListener,
) : RecyclerView.Adapter<CommentsListAdapter.ViewHolder>() {

    var isForEvent: Boolean = false
    var isForGroup: Boolean = false

    // Pour savoir si l'utilisateur veut la version traduite ou originale
    // On inverse si l'ID est dans translationExceptions
    private val translationExceptions = mutableSetOf<Int>()

    fun initiateList() {
        // Vérifie la config "translatedByDefault"
        val translatedByDefault = context.getSharedPreferences(
            context.getString(R.string.preference_file_key),
            Context.MODE_PRIVATE
        ).getBoolean("translatedByDefault", true)

        // Marque tout post qui a "contentTranslations" comme potentiellement traduisible
        if (translatedByDefault) {
            commentsList.forEach {
                if (it.contentTranslations != null) {
                    translationExceptions.add(it.id!!)
                }
            }
        }
        notifyDataSetChanged()
    }

    fun setForEvent() {
        isForEvent = true
    }
    fun setForGroup() {
        isForGroup = true
    }


    private fun findFirstMessagePosition(): Int {
        val total = itemCount
        for (i in 0 until total) {
            val vt = getItemViewType(i)
            if (vt == CommentsTypes.TYPE_LEFT.code || vt == CommentsTypes.TYPE_RIGHT.code) {
                return i
            }
        }
        return total
    }

    fun translateItem(commentId: Int) {
        // On inverse la logique pour cet ID
        if (translationExceptions.contains(commentId)) {
            translationExceptions.remove(commentId)
        } else {
            translationExceptions.add(commentId)
        }
        notifyItemChanged(commentsList.indexOfFirst { it.id == commentId } + 1)
    }

    inner class ViewHolder(val binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bindDate(comment: Post) {
            val bindingDate = binding as LayoutCommentItemDateBinding
            val pos = bindingAdapterPosition
            val firstMsgPos = findFirstMessagePosition()

            // Tous les séparateurs avant celui juste avant le premier message : on les dégage
            if (pos < firstMsgPos - 1) {
                bindingDate.root.layoutParams = RecyclerView.LayoutParams(0, 0)
                bindingDate.root.visibility = View.GONE
                return
            }

            // sinon, on affiche normalement
            bindingDate.root.visibility = View.VISIBLE
            bindingDate.publicationDay.text = comment.datePostText
        }

        // ----------------------------------------------------------------------------------------
        // bindLeft : Affiche un commentaire "à gauche"
        // ----------------------------------------------------------------------------------------
        fun bindLeft(comment: Post) {
            val bindingLeft = binding as LayoutCommentItemLeftBinding
            val isMe = (comment.user?.userId == EntourageApplication.get().me()?.id)

            // Détermine si on veut la version traduite ou originale
            val sharedPrefs = context.getSharedPreferences(
                context.getString(R.string.preference_file_key),
                Context.MODE_PRIVATE
            )
            val isTranslatedByDefault = sharedPrefs.getBoolean("translatedByDefault", true)
            val isTranslated = if (translationExceptions.contains(comment.id)) {
                !isTranslatedByDefault
            } else {
                isTranslatedByDefault
            }

            // Récupère le contenu
            var contentToShow = getFinalContent(comment, isTranslated)
            if (contentToShow.isNullOrEmpty()) contentToShow = ""

            // Cas "deleted"/"offensive"
            if (comment.status in listOf("deleted", "offensive", "offensible")) {
                handleDeletedOrOffensiveLeft(bindingLeft, comment, isMe)
                return
            } else {
                bindingLeft.comment.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            }

            // HTML → Spanned + Clickables
            val spanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(contentToShow, Html.FROM_HTML_MODE_LEGACY)
            } else {
                Html.fromHtml(contentToShow)
            }
            val clickableSpannable = makeLinksClickable(spanned)

            // Affichage du texte
            bindingLeft.comment.text = clickableSpannable
            bindingLeft.comment.movementMethod = LinkMovementMethod.getInstance()
            bindingLeft.comment.linksClickable = true

            // Couleurs
            bindingLeft.comment.background = if (isMe)
                ContextCompat.getDrawable(context, R.drawable.new_comment_background_orange)
            else
                ContextCompat.getDrawable(context, R.drawable.new_comment_background_beige)
            bindingLeft.comment.setTextColor(ContextCompat.getColor(context, R.color.black))
            bindingLeft.comment.setLinkTextColor(ContextCompat.getColor(context, R.color.bright_blue))

            // Actions
            handleReportLogicLeft(bindingLeft, comment, isMe)
            handleDateAndErrorLeft(bindingLeft, comment)
            handleUserLeft(bindingLeft, comment, isMe)

            // Style conversation
            if (isConversation) {
                bindingLeft.authorName.setTextColor(ContextCompat.getColor(context, R.color.light_orange))
                bindingLeft.publicationDate.setTextColor(ContextCompat.getColor(context, R.color.light_orange))
            }

            // Gestion de l'image
            if (!comment.imageUrl.isNullOrEmpty()) {
                bindingLeft.commentImageContainer.visibility = View.VISIBLE

                Glide.with(context)
                    .asBitmap()
                    .load(comment.imageUrl)
                    .into(object : com.bumptech.glide.request.target.CustomTarget<Bitmap>() {
                        override fun onResourceReady(resource: Bitmap, transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?) {
                            val isPortrait = resource.height > resource.width
                            val screenWidth = (context as Activity).resources.displayMetrics.widthPixels

                            // Redimensionne l'image - CORRECTION: création sécurisée des layoutParams
                            val currentImageParams = bindingLeft.commentImage.layoutParams
                            val newImageParams = when (currentImageParams) {
                                is ViewGroup.MarginLayoutParams -> currentImageParams.apply {
                                    width = if (isPortrait) screenWidth / 2 else ViewGroup.LayoutParams.MATCH_PARENT
                                    height = ViewGroup.LayoutParams.WRAP_CONTENT
                                }
                                else -> ViewGroup.MarginLayoutParams(
                                    if (isPortrait) screenWidth / 2 else ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                                ).also {
                                    // Copie les marges si possible
                                    if (currentImageParams is ViewGroup.MarginLayoutParams) {
                                        it.setMargins(
                                            currentImageParams.leftMargin,
                                            currentImageParams.topMargin,
                                            currentImageParams.rightMargin,
                                            currentImageParams.bottomMargin
                                        )
                                    }
                                }
                            }
                            bindingLeft.commentImage.layoutParams = newImageParams
                            bindingLeft.commentImage.setImageBitmap(resource)

                            // Ajuste messageContainer - CORRECTION: gestion sécurisée
                            val currentContainerParams = bindingLeft.messageContainer.layoutParams
                            val newContainerParams = when (currentContainerParams) {
                                is ViewGroup.MarginLayoutParams -> currentContainerParams.apply {
                                    width = newImageParams.width
                                }
                                else -> ViewGroup.MarginLayoutParams(newImageParams.width, currentContainerParams.height).also {
                                    // Copie les marges si possible
                                    if (currentContainerParams is ViewGroup.MarginLayoutParams) {
                                        it.setMargins(
                                            currentContainerParams.leftMargin,
                                            currentContainerParams.topMargin,
                                            currentContainerParams.rightMargin,
                                            currentContainerParams.bottomMargin
                                        )
                                    }
                                }
                            }
                            bindingLeft.messageContainer.layoutParams = newContainerParams
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            bindingLeft.commentImage.setImageDrawable(placeholder)
                        }
                    })

                bindingLeft.commentImage.setOnClickListener {
                    val intent = Intent(context, ImageZoomActivity::class.java)
                    intent.putExtra("image_url", comment.imageUrl)
                    context.startActivity(intent)
                }
                val hasOnlyImage = contentToShow.isBlank() && !comment.imageUrl.isNullOrEmpty()
                if (hasOnlyImage) {
                    bindingLeft.comment.visibility = View.GONE
                    bindingLeft.comment.layoutParams.height = 0
                    bindingLeft.messageContainer.setBackgroundResource(0)
                } else {
                    bindingLeft.comment.visibility = View.VISIBLE
                }

                bindingLeft.commentImage.setOnLongClickListener {
                    val commentLang = comment.contentTranslations?.fromLang ?: ""
                    DataLanguageStock.updateContentToCopy(comment.content ?: "")
                    onItemClick.onCommentReport(comment.id, isForEvent, isForGroup, isMe, commentLang)
                    true
                }


            } else {
                bindingLeft.commentImageContainer.visibility = View.GONE
                bindingLeft.commentImage.setImageDrawable(null)

                // Reset de l'image - CORRECTION: création sécurisée
                val currentImageParams = bindingLeft.commentImage.layoutParams
                val resetImageParams = when (currentImageParams) {
                    is ViewGroup.MarginLayoutParams -> currentImageParams.apply {
                        width = ViewGroup.LayoutParams.WRAP_CONTENT
                        height = ViewGroup.LayoutParams.WRAP_CONTENT
                    }
                    else -> ViewGroup.MarginLayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).also {
                        // Copie les marges si possible
                        if (currentImageParams is ViewGroup.MarginLayoutParams) {
                            it.setMargins(
                                currentImageParams.leftMargin,
                                currentImageParams.topMargin,
                                currentImageParams.rightMargin,
                                currentImageParams.bottomMargin
                            )
                        }
                    }
                }
                bindingLeft.commentImage.layoutParams = resetImageParams

                // Reset safe pour messageContainer - CORRECTION: gestion sécurisée
                val currentContainerParams = bindingLeft.messageContainer.layoutParams
                val resetContainerParams = when (currentContainerParams) {
                    is ViewGroup.MarginLayoutParams -> currentContainerParams.apply {
                        width = ViewGroup.LayoutParams.WRAP_CONTENT
                    }
                    else -> ViewGroup.MarginLayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        currentContainerParams.height
                    ).also {
                        // Copie les marges si possible
                        if (currentContainerParams is ViewGroup.MarginLayoutParams) {
                            it.setMargins(
                                currentContainerParams.leftMargin,
                                currentContainerParams.topMargin,
                                currentContainerParams.rightMargin,
                                currentContainerParams.bottomMargin
                            )
                        }
                    }
                }
                bindingLeft.messageContainer.layoutParams = resetContainerParams
            }

            // Taille dynamique - CORRECTION: gestion sécurisée
            val currentCommentParams = bindingLeft.comment.layoutParams
            val newCommentParams = when (currentCommentParams) {
                is ViewGroup.MarginLayoutParams -> currentCommentParams.apply {
                    height = ViewGroup.LayoutParams.WRAP_CONTENT
                }
                else -> ViewGroup.MarginLayoutParams(currentCommentParams.width, ViewGroup.LayoutParams.WRAP_CONTENT).also {
                    // Copie les marges si possible
                    if (currentCommentParams is ViewGroup.MarginLayoutParams) {
                        it.setMargins(
                            currentCommentParams.leftMargin,
                            currentCommentParams.topMargin,
                            currentCommentParams.rightMargin,
                            currentCommentParams.bottomMargin
                        )
                    }
                }
            }
            bindingLeft.comment.layoutParams = newCommentParams
            bindingLeft.comment.requestLayout()

            // Style auto
            if (comment.messageType == "auto") {
                bindingLeft.messageContainer.setBackgroundResource(R.drawable.comment_message_auto_background)
                bindingLeft.comment.setBackgroundResource(R.drawable.comment_message_auto_background)
                bindingLeft.authorName.text = binding.root.context.getString(R.string.message_auto)
            } else {
                bindingLeft.messageContainer.setBackgroundResource(R.drawable.new_comment_background_beige)
                bindingLeft.comment.setBackgroundResource(R.drawable.new_comment_background_beige)
            }
        }



        // ----------------------------------------------------------------------------------------
        // bindRight : Affiche un commentaire "à droite"
        // ----------------------------------------------------------------------------------------
        fun bindRight(comment: Post) {
            val bindingRight = binding as LayoutCommentItemRightBinding
            val isMe = (comment.user?.userId == EntourageApplication.get().me()?.id)

            // Détermine si on veut la version traduite ou originale
            val sharedPrefs = context.getSharedPreferences(
                context.getString(R.string.preference_file_key),
                Context.MODE_PRIVATE
            )
            val isTranslatedByDefault = sharedPrefs.getBoolean("translatedByDefault", true)
            val isTranslated = if (translationExceptions.contains(comment.id)) {
                !isTranslatedByDefault
            } else {
                isTranslatedByDefault
            }

            // Récupère la chaîne final
            var contentToShow = getFinalContent(comment, isTranslated)

            // Gère statuts "deleted"/"offensif"
            if (comment.status in listOf("deleted", "offensive", "offensible")) {
                handleDeletedOrOffensiveRight(bindingRight, comment, isMe)
                return
            } else {
                // Masquer l'icône de suppression si le commentaire n'est pas en état "deleted"
                bindingRight.comment.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            }

            if (contentToShow.isNullOrEmpty()) contentToShow = ""

            // On parse en HTML
            val spanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(contentToShow, Html.FROM_HTML_MODE_LEGACY)
            } else {
                Html.fromHtml(contentToShow)
            }

            // Remplacement des URLSpan par des ClickableSpan
            val clickableSpannable = makeLinksClickable(spanned)

            // On assigne le texte final
            bindingRight.comment.text = clickableSpannable
            bindingRight.comment.movementMethod = LinkMovementMethod.getInstance()
            bindingRight.comment.linksClickable = true

            // Couleur de fond
            bindingRight.comment.background = if (isMe) {
                ContextCompat.getDrawable(context, R.drawable.new_comment_background_orange)
            } else {
                ContextCompat.getDrawable(context, R.drawable.new_comment_background_beige)
            }

            // Couleur du texte normal
            bindingRight.comment.setTextColor(ContextCompat.getColor(context, R.color.black))
            // Couleur des liens
            bindingRight.comment.setLinkTextColor(ContextCompat.getColor(context, R.color.bright_blue))

            // Bouton "report" + long clic => signale
            handleReportLogicRight(bindingRight, comment, isMe)

            // Date + gestion "error" si pas de date
            handleDateAndErrorRight(bindingRight, comment)

            // Gère l'auteur (photo, nom...)
            handleUserRight(bindingRight, comment, isMe)

            // Couleurs conversation
            if (isConversation) {
                bindingRight.authorName.setTextColor(
                    ContextCompat.getColor(context, R.color.light_orange)
                )
                bindingRight.publicationDate.setTextColor(
                    ContextCompat.getColor(context, R.color.light_orange)
                )
            }
            if (!comment.imageUrl.isNullOrEmpty()) {
                bindingRight.commentImageContainer.visibility = View.VISIBLE

                Glide.with(context)
                    .asBitmap()
                    .load(comment.imageUrl)
                    .into(object : com.bumptech.glide.request.target.CustomTarget<Bitmap>() {
                        override fun onResourceReady(resource: Bitmap, transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?) {
                            val isPortrait = resource.height > resource.width
                            val screenWidth = (context as Activity).resources.displayMetrics.widthPixels

                            val layoutParams = bindingRight.commentImage.layoutParams
                            layoutParams.width = if (isPortrait) screenWidth / 2 else ViewGroup.LayoutParams.MATCH_PARENT
                            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                            bindingRight.commentImage.layoutParams = layoutParams

                            bindingRight.commentImage.setImageBitmap(resource)
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            bindingRight.commentImage.setImageDrawable(placeholder)
                        }
                    })

                bindingRight.commentImage.setOnClickListener {
                    val intent = Intent(context, ImageZoomActivity::class.java)
                    intent.putExtra("image_url", comment.imageUrl)
                    context.startActivity(intent)
                }

                val hasOnlyImage = contentToShow.isBlank() && !comment.imageUrl.isNullOrEmpty()
                if (hasOnlyImage) {
                    bindingRight.comment.visibility = View.GONE
                    bindingRight.comment.layoutParams.height = 0
                    bindingRight.messageContainer.setBackgroundResource(0)
                } else {
                    bindingRight.comment.visibility = View.VISIBLE
                }

                bindingRight.commentImage.setOnLongClickListener {
                    val commentLang = comment.contentTranslations?.fromLang ?: ""
                    DataLanguageStock.updateContentToCopy(comment.content ?: "")
                    onItemClick.onCommentReport(comment.id, isForEvent, isForGroup, isMe, commentLang)
                    true
                }

            } else {
                bindingRight.commentImageContainer.visibility = View.GONE
            }
        }

        // ----------------------------------------------------------------------------------------
        // bindDetails : affichage du "post parent" (TYPE_DETAIL)
        // ----------------------------------------------------------------------------------------
        fun bindDetails(comment: Post) {
            val bindingDetail = binding as LayoutCommentDetailPostTopBinding
            bindingDetail.commentPost.text = comment.content

            if (comment.status.equals("deleted")) {
                bindingDetail.commentPost.text = context.getString(R.string.deleted_publi)
            }
            bindingDetail.commentPost.setHyperlinkClickable()

            comment.createdTime?.let {
                val locale = LanguageManager.getLocaleFromPreferences(context)
                bindingDetail.publicationDatePost.text =
                    "le " + SimpleDateFormat("dd.MM.yyyy", locale).format(it)
            }

            comment.imageUrl?.let { avatarURL ->
                bindingDetail.photoPost.visibility = View.VISIBLE
                Glide.with(bindingDetail.photoPost.context)
                    .load(avatarURL)
                    .transform(CenterCrop(), RoundedCorners(Const.ROUNDED_CORNERS_IMAGES.px))
                    .placeholder(R.drawable.new_group_illu)
                    .error(R.drawable.new_group_illu)
                    .into(bindingDetail.photoPost)
            } ?: run {
                bindingDetail.photoPost.visibility = View.GONE
            }

            bindingDetail.authorNamePost.text = comment.user?.displayName ?: "-"

            comment.user?.avatarURLAsString?.let { avatarURL ->
                Glide.with(bindingDetail.imagePost.context)
                    .load(avatarURL)
                    .placeholder(R.drawable.placeholder_user)
                    .error(R.drawable.placeholder_user)
                    .circleCrop()
                    .into(bindingDetail.imagePost)
            } ?: run {
                Glide.with(bindingDetail.imagePost.context)
                    .load(R.drawable.placeholder_user)
                    .circleCrop()
                    .into(bindingDetail.imagePost)
            }
        }

        // ----------------------------------------------------------------------------------------
        // Sous-fonctions pour statuts "deleted/offensive", report, date, user...
        // ----------------------------------------------------------------------------------------
        private fun handleDeletedOrOffensiveLeft(
            binding: LayoutCommentItemLeftBinding,
            comment: Post,
            isMe: Boolean
        ) {
            // Icône grise
            val drawable = ContextCompat.getDrawable(context, R.drawable.ic_comment_deleted)
            val vectorDrawable = DrawableCompat.wrap(drawable!!) as VectorDrawable
            val width = 12
            val height = (width * vectorDrawable.intrinsicHeight) / vectorDrawable.intrinsicWidth
            val scaledDrawable = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(scaledDrawable)
            vectorDrawable.setBounds(8, 0, canvas.width - 8, canvas.height)
            vectorDrawable.draw(canvas)
            val grayDrawable = vectorDrawable.mutate()
            grayDrawable.setColorFilter(
                ContextCompat.getColor(context, R.color.grey_deleted_icon),
                PorterDuff.Mode.SRC_IN
            )
            grayDrawable.setBounds(8, 0, scaledDrawable.width - 8, scaledDrawable.height)

            // Applique l’icône au commentaire
            binding.comment.setCompoundDrawablesWithIntrinsicBounds(grayDrawable, null, null, null)
            binding.comment.compoundDrawablePadding = 16

            // Texte
            binding.comment.text = when {
                comment.status == "offensive" || comment.status == "offensible" ->
                    context.getString(R.string.offensive_message)
                isOne2One || DetailConversationActivity.isSmallTalkMode ->
                    context.getString(R.string.deleted_message)
                else ->
                    context.getString(R.string.deleted_comment)
            }

            // Fond gris
            binding.messageContainer.setBackgroundResource(R.drawable.new_comment_background_grey)
            binding.comment.background =
                ContextCompat.getDrawable(context, R.drawable.new_comment_background_grey)

            // Couleur grise
            binding.comment.setTextColor(ContextCompat.getColor(context, R.color.grey_deleted_icon))

            // Cache image
            binding.commentImage.setImageDrawable(null)
            binding.commentImageContainer.visibility = View.GONE

            // Sécurise le layoutParams sans cast foireux
            val layoutParams = binding.messageContainer.layoutParams
            val safeParams = if (layoutParams is ViewGroup.MarginLayoutParams) {
                layoutParams
            } else {
                ViewGroup.MarginLayoutParams(layoutParams)
            }
            safeParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
            binding.messageContainer.layoutParams = safeParams
            binding.comment.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            binding.comment.requestLayout()
        }



        private fun handleDeletedOrOffensiveRight(
            binding: LayoutCommentItemRightBinding,
            comment: Post,
            isMe: Boolean
        ) {
            val drawable = ContextCompat.getDrawable(context, R.drawable.ic_comment_deleted)
            val vectorDrawable = DrawableCompat.wrap(drawable!!) as VectorDrawable
            val width = 12
            val height = (width * vectorDrawable.intrinsicHeight) / vectorDrawable.intrinsicWidth
            val scaledDrawable = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(scaledDrawable)
            vectorDrawable.setBounds(8, 0, canvas.width - 8, canvas.height)
            vectorDrawable.draw(canvas)
            val grayDrawable = vectorDrawable.mutate()
            grayDrawable.setColorFilter(
                ContextCompat.getColor(context, R.color.grey_deleted_icon),
                PorterDuff.Mode.SRC_IN
            )
            grayDrawable.setBounds(8, 0, scaledDrawable.width - 8, scaledDrawable.height)
            binding.comment.setCompoundDrawablesWithIntrinsicBounds(grayDrawable, null, null, null)
            binding.comment.compoundDrawablePadding = 16

            if (isOne2One || DetailConversationActivity.isSmallTalkMode) {
                binding.comment.text = context.getString(R.string.deleted_message)
            } else {
                binding.comment.text = context.getString(R.string.deleted_comment)
            }
            if (comment.status in listOf("offensive", "offensible")) {
                binding.comment.text = context.getString(R.string.offensive_message)
            }
            binding.messageContainer.setBackgroundResource(R.drawable.new_comment_background_grey)
            binding.comment.background =
                ContextCompat.getDrawable(context, R.drawable.new_comment_background_grey)
            binding.comment.setTextColor(ContextCompat.getColor(context, R.color.grey_deleted_icon))
            binding.commentImageContainer.visibility = View.GONE
        }

        private fun handleReportLogicLeft(
            binding: LayoutCommentItemLeftBinding,
            comment: Post,
            isMe: Boolean
        ) {
            binding.report.setOnClickListener {
                val commentLang = comment.contentTranslations?.fromLang ?: ""
                DataLanguageStock.updateContentToCopy(comment.content ?: "")
                onItemClick.onCommentReport(comment.id, isForEvent, isForGroup, isMe, commentLang)
            }
            // Long click => report

            if (comment.status !in listOf("deleted", "offensive", "offensible")) {
                val commentLang = comment.contentTranslations?.fromLang ?: ""
                binding.comment.setOnLongClickListener {
                    DataLanguageStock.updateContentToCopy(comment.content ?: "")
                    onItemClick.onCommentReport(comment.id, isForEvent,isForGroup, isMe, commentLang)
                    true
                }
            }
            // Si c'est "moi" ou "isConversation", on masque "report"
            if (isMe || isConversation) {
                binding.report.visibility = View.GONE
            } else {
                binding.report.visibility = View.VISIBLE
            }
        }

        private fun handleReportLogicRight(
            binding: LayoutCommentItemRightBinding,
            comment: Post,
            isMe: Boolean
        ) {
            binding.report.setOnClickListener {
                val commentLang = comment.contentTranslations?.fromLang ?: ""
                DataLanguageStock.updateContentToCopy(comment.content ?: "")
                onItemClick.onCommentReport(comment.id, isForEvent,isForGroup, isMe, commentLang)
            }
            if (comment.status !in listOf("deleted", "offensive", "offensible")) {
                val commentLang = comment.contentTranslations?.fromLang ?: ""
                binding.comment.setOnLongClickListener {
                    DataLanguageStock.updateContentToCopy(comment.content ?: "")
                    onItemClick.onCommentReport(comment.id, isForEvent,isForGroup, isMe, commentLang)
                    true
                }
            }
            if (isMe || isConversation) {
                binding.report.visibility = View.GONE
            } else {
                binding.report.visibility = View.VISIBLE
            }
        }

        private fun handleDateAndErrorLeft(
            binding: LayoutCommentItemLeftBinding,
            comment: Post
        ) {
            comment.createdTime?.let {
                binding.informationLayout.visibility = View.VISIBLE
                binding.error.visibility = View.GONE
                val locale = LanguageManager.getLocaleFromPreferences(context)
                if (isConversation) {
                    binding.publicationDate.text = SimpleDateFormat("HH'h'mm", locale).format(it)
                } else {
                    binding.publicationDate.text = "le " + SimpleDateFormat(
                        binding.root.context.getString(R.string.comments_date),
                        locale
                    ).format(it)
                }
            } ?: run {
                // Pas de date => affiche "error" + onClick => resend
                binding.informationLayout.visibility = View.GONE
                binding.error.visibility = View.VISIBLE
                binding.error.setOnClickListener {
                    onItemClick.onItemClick(comment)
                }
            }
        }

        private fun handleDateAndErrorRight(
            binding: LayoutCommentItemRightBinding,
            comment: Post
        ) {
            comment.createdTime?.let {
                binding.informationLayout.visibility = View.VISIBLE
                binding.error.visibility = View.GONE
                val locale = LanguageManager.getLocaleFromPreferences(context)
                if (isConversation) {
                    binding.publicationDate.text = SimpleDateFormat("HH'h'mm", locale).format(it)
                } else {
                    binding.publicationDate.text = "le " + SimpleDateFormat(
                        binding.root.context.getString(R.string.comments_date),
                        locale
                    ).format(it)
                }
            } ?: run {
                binding.informationLayout.visibility = View.GONE
                binding.error.visibility = View.VISIBLE
                binding.error.setOnClickListener {
                    onItemClick.onItemClick(comment)
                }
            }
        }

        private fun handleUserLeft(
            binding: LayoutCommentItemLeftBinding,
            comment: Post,
            isMe: Boolean
        ) {
            comment.user?.let { user ->
                binding.authorName.text = if (isMe) "" else user.displayName
                user.avatarURLAsString?.let { url ->
                    Glide.with(binding.root.context)
                        .load(url)
                        .placeholder(R.drawable.placeholder_user)
                        .error(R.drawable.placeholder_user)
                        .apply(RequestOptions().override(25.px, 25.px))
                        .circleCrop()
                        .into(binding.image)
                } ?: run {
                    Glide.with(binding.root.context)
                        .load(R.drawable.placeholder_user)
                        .apply(RequestOptions().override(25.px, 25.px))
                        .circleCrop()
                        .into(binding.image)
                }
            }
            binding.image.setOnClickListener {
                comment.user?.userId?.let { userId ->
                    ProfileFullActivity.isMe = false
                    ProfileFullActivity.userId = userId.toString()
                    (binding.image.context as? Activity)?.startActivityForResult(
                        Intent(binding.image.context, ProfileFullActivity::class.java).putExtra(
                            Const.USER_ID, userId
                        ), 0
                    )
                }
            }
        }

        private fun handleUserRight(
            binding: LayoutCommentItemRightBinding,
            comment: Post,
            isMe: Boolean
        ) {
            comment.user?.let { user ->
                binding.authorName.text = if (isMe) "" else user.displayName
                user.avatarURLAsString?.let { url ->
                    Glide.with(binding.root.context)
                        .load(url)
                        .placeholder(R.drawable.placeholder_user)
                        .error(R.drawable.placeholder_user)
                        .apply(RequestOptions().override(25.px, 25.px))
                        .circleCrop()
                        .into(binding.image)
                } ?: run {
                    Glide.with(binding.root.context)
                        .load(R.drawable.placeholder_user)
                        .apply(RequestOptions().override(25.px, 25.px))
                        .circleCrop()
                        .into(binding.image)
                }
            }
            binding.image.setOnClickListener {
                comment.user?.userId?.let { userId ->
                    ProfileFullActivity.isMe = false
                    ProfileFullActivity.userId = userId.toString()
                    (binding.image.context as? Activity)?.startActivityForResult(
                        Intent(binding.image.context, ProfileFullActivity::class.java).putExtra(
                            Const.USER_ID, userId
                        ), 0
                    )
                }
            }
        }
    }

    // --------------------------------------------------------------------------------------------
    // Détermine s'il y a un "post parent"
    // --------------------------------------------------------------------------------------------
    fun updateData(currentParentPost: Post?) {
        this.currentParentPost = currentParentPost
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = when (viewType) {
            CommentsTypes.TYPE_DETAIL.code -> LayoutCommentDetailPostTopBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            CommentsTypes.TYPE_LEFT.code -> LayoutCommentItemLeftBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            CommentsTypes.TYPE_RIGHT.code -> LayoutCommentItemRightBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            else -> LayoutCommentItemDateBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        }
        return ViewHolder(layout)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comment = if (position == 0 && hasCurrentPost()) {
            currentParentPost
        } else {
            commentsList[ if (hasCurrentPost()) (position - 1) else position ]
        }
        if (comment == null) return

        when (getItemViewType(position)) {
            CommentsTypes.TYPE_DETAIL.code -> holder.bindDetails(comment)
            CommentsTypes.TYPE_LEFT.code -> holder.bindLeft(comment)
            CommentsTypes.TYPE_RIGHT.code -> holder.bindRight(comment)
            CommentsTypes.TYPE_DATE.code -> holder.bindDate(comment)
        }
    }

    private fun hasCurrentPost(): Boolean {
        return currentParentPost != null
    }

    override fun getItemCount(): Int {
        val addOne = if (hasCurrentPost()) 1 else 0
        return commentsList.size + addOne
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0 && hasCurrentPost()) {
            return CommentsTypes.TYPE_DETAIL.code
        }
        val realPos = if (hasCurrentPost()) position - 1 else position

        if (commentsList[realPos].isDatePostOnly) {
            return CommentsTypes.TYPE_DATE.code
        }
        // Qui a posté ? => on détermine gauche/droite
        return if (commentsList[realPos].user?.id?.toInt() == postAuthorId) {
            CommentsTypes.TYPE_RIGHT.code
        } else {
            CommentsTypes.TYPE_LEFT.code
        }
    }

    // --------------------------------------------------------------------------------------------
    // Fonction utilitaire pour renvoyer la chaîne la plus adaptée (HTML ou normal,
    // original ou traduction). On en profite pour nettoyer les <p> qui rajoutent
    // des sauts de ligne non désirés.
    // --------------------------------------------------------------------------------------------
    private fun getFinalContent(comment: Post, isTranslated: Boolean): String {
        Timber.d("getFinalContent => isTranslated=$isTranslated contentHtml='${comment.contentHtml}' content='${comment.content}'")

        // 1) On choisit la source
        val baseString = if (!isTranslated) {
            // On veut l'original
            if (!comment.contentHtml.isNullOrBlank()) {
                comment.contentHtml
            } else {
                comment.content
            }
        } else {
            // On veut la traduction
            val htmlTranslation = comment.contentTranslationsHtml?.translation
            if (!htmlTranslation.isNullOrBlank()) {
                htmlTranslation
            } else {
                val normalTranslation = comment.contentTranslations?.translation
                if (!normalTranslation.isNullOrBlank()) {
                    normalTranslation
                } else {
                    // fallback
                    comment.contentHtml
                }
            }
        } ?: ""

        // 2) On supprime/transforme les <p> qui ajoutent des sauts de ligne
        val finalString = fixHtmlSpacing(baseString)
        return finalString
    }

    // --------------------------------------------------------------------------------------------
    // Remplace les balises <p> ... </p> pour éviter les énormes sauts de ligne.
    // Ici on les supprime purement et simplement.
    // --------------------------------------------------------------------------------------------
    private fun fixHtmlSpacing(html: String): String {
        // On nettoie les espaces et sauts de ligne en début/fin de chaîne
        var result = html.trim()
        // On remplace les balises <p> par des <br> pour conserver un saut de ligne
        result = result.replace(Regex("<p[^>]*>"), "<br>")
        result = result.replace(Regex("</p>"), "")

        // On remplace chaque retour à la ligne par <br>
        result = result.replace("\n", "<br>")
        // On ne regroupe PAS les <br> consécutifs, on laisse ainsi plusieurs <br> pour conserver les sauts multiples.
        // Vous pouvez éventuellement retirer les éventuels <br> inutiles en début ou fin de chaîne si besoin.
        result = result.replaceFirst("^(<br>\\s*)+", "")
        result = result.replaceFirst("(<br>\\s*)+$", "")

        return result
    }
    // --------------------------------------------------------------------------------------------
    // Convertit un Spanned contenant des URLSpan en un SpannableStringBuilder contenant
    // des ClickableSpan. Ainsi on peut gérer soi-même les clics (ex: deeplink in-app).
    // --------------------------------------------------------------------------------------------
    private fun makeLinksClickable(spanned: Spanned): Spannable {
        val urlSpans = spanned.getSpans(0, spanned.length, URLSpan::class.java)
        if (urlSpans.isEmpty()) {
            // Pas de lien -> on renvoie le spanned d'origine
            return spanned as Spannable
        }
        val sb = SpannableStringBuilder(spanned)
        for (span in urlSpans) {
            val start = sb.getSpanStart(span)
            val end = sb.getSpanEnd(span)
            val flags = sb.getSpanFlags(span)
            val url = span.url
            // On enlève l'URLSpan d'origine
            sb.removeSpan(span)
            // On met un ClickableSpan perso
            sb.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    // TODO: gère toi-même ce que tu veux faire sur le clic
                    // Par ex. si c'est un lien vers un profil user => extraits l'ID
                    // et ouvre la bonne Activity in-app. Ou appelle onShowWeb(url).
                    onItemClick.onShowWeb(url)
                }

                // Optionnel : personnalise l'apparence du lien (souligné, etc.)
                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = true // si tu veux souligner
                }
            }, start, end, flags)
        }
        return sb
    }
}
