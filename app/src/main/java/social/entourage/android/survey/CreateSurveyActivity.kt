package social.entourage.android.survey

import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.widget.Toast
import social.entourage.android.R
import social.entourage.android.base.BaseActivity
import social.entourage.android.databinding.CreateSurveyActivityBinding
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.CustomAlertDialog

class CreateSurveyActivity: BaseActivity() {

    private lateinit var binding: CreateSurveyActivityBinding
    private var groupId:Int? = 0
    private var eventId:Int? = 0
    private lateinit var surveyPresenter: SurveyPresenter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CreateSurveyActivityBinding.inflate(layoutInflater)
        initView()
        surveyPresenter = SurveyPresenter()
        setObserver()
        groupId = intent.getIntExtra(Const.GROUP_ID, 0) // Utilise 0 comme valeur par défaut si GROUP_ID n'est pas fourni
        eventId = intent.getIntExtra(Const.EVENT_ID, 0) // Utilise 0 comme valeur par défaut si GROUP_ID n'est pas fourni
        setContentView(binding.root)
    }

    private fun setObserver(){
        surveyPresenter.isSurveySent.observe(this, ::onSurveySent)
    }

    private fun initView() {

        //binding.editTextOption2.visibility = View.GONE
        binding.editTextOption3.visibility = View.GONE
        binding.editTextOption4.visibility = View.GONE
        binding.editTextOption5.visibility = View.GONE
        setEditTextVisibility()
        binding.validateBtn.setOnClickListener {
            if(groupId != null && groupId != 0){
                AnalyticsEvents.logEvent(AnalyticsEvents.Clic_Group_Validate_Poll)
            }else{
                AnalyticsEvents.logEvent(AnalyticsEvents.Clic_Event_Validate_Poll)
            }
            validateSurvey()
        }
        binding.cancelButton.setOnClickListener {
            showCancelPopUp()
        }
        binding.iconBack.setOnClickListener {
            showCancelPopUp()
        }
        applyItalicHintToEditText()
        binding.editTextQuestion.requestFocus()

    }

    private fun validateSurvey(){
        val content = binding.editTextQuestion.text.toString()
        val choices = getChoices()

        if (choices.size < 2) {
            // Afficher un message d'erreur, car un sondage doit avoir au moins 2 choix.
            Toast.makeText(this, "Veuillez fournir au moins deux choix pour le sondage.", Toast.LENGTH_LONG).show()
            return
        }
        if (content.isEmpty()) {
            // Afficher un message d'erreur si la question du sondage est vide.
            Toast.makeText(this, "Veuillez entrer une question pour le sondage.", Toast.LENGTH_LONG).show()
            return
        }
        // Vérifie si c'est un sondage pour un groupe ou un événement et appelle la méthode correspondante
        when {
            groupId != null && groupId != 0 -> {
                surveyPresenter.createSurveyInGroup(groupId!!, content, choices, binding.switchMultipleChoice.isChecked)
            }
            eventId != null && eventId != 0 -> {
                surveyPresenter.createSurveyInEvent(eventId!!, content, choices, binding.switchMultipleChoice.isChecked)
            }
            else -> {
                Toast.makeText(this, "Erreur: Identifiant de groupe ou d'événement non spécifié.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun onSurveySent(isSent:Boolean){
        if (isSent) {
            // Afficher un message de succès si le sondage a été envoyé avec succès
            Toast.makeText(this, "Sondage créé avec succès", Toast.LENGTH_LONG).show()
            finish()
        } else {
            // Afficher un message d'erreur si le sondage n'a pas été envoyé
            Toast.makeText(this, "Erreur lors de l'envoi du sondage.", Toast.LENGTH_LONG).show()
        }
    }

    private fun applyItalicHintToEditText() {
        val editTexts = listOf(binding.editTextQuestion, binding.editTextOption1, binding.editTextOption2, binding.editTextOption3, binding.editTextOption4, binding.editTextOption5)

        editTexts.forEach { editText ->
            val hint = editText.hint.toString()
            val spannableHint = SpannableString(hint).apply {
                setSpan(StyleSpan(android.graphics.Typeface.ITALIC), 0, hint.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            editText.hint = spannableHint

        }
    }

    private fun getChoices(): List<String> {
        // Créer une liste vide pour stocker les choix
        val choices = mutableListOf<String>()

        // Ajouter le texte de chaque EditText à la liste s'il n'est pas vide
        if (binding.editTextOption1.text.toString().isNotEmpty()) {
            choices.add(binding.editTextOption1.text.toString())
        }
        if (binding.editTextOption2.text.toString().isNotEmpty()) {
            choices.add(binding.editTextOption2.text.toString())
        }
        if (binding.editTextOption3.text.toString().isNotEmpty()) {
            choices.add(binding.editTextOption3.text.toString())
        }
        if (binding.editTextOption4.text.toString().isNotEmpty()) {
            choices.add(binding.editTextOption4.text.toString())
        }
        if (binding.editTextOption5.text.toString().isNotEmpty()) {
            choices.add(binding.editTextOption5.text.toString())
        }

        // Retourner la liste des choix non vides
        return choices
    }




    private fun showCancelPopUp(){
        CustomAlertDialog.showWithCancelFirst(this,
            getString(R.string.popup_survey_title),
            getString(R.string.popup_survey_content),
            getString(R.string.popup_survey_btn_leave),
            {

            },
            {
                finish()
            })
    }

    private fun setEditTextVisibility(){

        //EDITEXT 1 MAKES EDITEXT 2 VISIBLE
        binding.editTextOption1.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Pas besoin d'implémentation ici
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Si du texte est entré, rends editTextOption2 visible
                if (s.toString().isNotEmpty()) {
                    binding.editTextOption2.visibility = View.VISIBLE
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // Pas besoin d'implémentation ici
            }
        })

        //EDITEXT 2 MAKES EDITEXT 3 VISIBLE
        binding.editTextOption2.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Pas besoin d'implémentation ici
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Si du texte est entré, rends editTextOption3 visible
                if (s.toString().isNotEmpty()) {
                    binding.editTextOption3.visibility = View.VISIBLE
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // Pas besoin d'implémentation ici
            }
        })

        //EDITEXT 3 MAKES EDITEXT 4 VISIBLE
        binding.editTextOption3.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Pas besoin d'implémentation ici
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Si du texte est entré, rends editTextOption3 visible
                if (s.toString().isNotEmpty()) {
                    binding.editTextOption4.visibility = View.VISIBLE
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // Pas besoin d'implémentation ici
            }
        })

        //EDITEXT 4 MAKES EDITEXT 5 VISIBLE
        binding.editTextOption4.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Pas besoin d'implémentation ici
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Si du texte est entré, rends editTextOption3 visible
                if (s.toString().isNotEmpty()) {
                    binding.editTextOption5.visibility = View.VISIBLE
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // Pas besoin d'implémentation ici
            }
        })
    }


}