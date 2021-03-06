package social.entourage.android.onboarding.asso

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_onboarding_asso_fill.*
import social.entourage.android.R
import social.entourage.android.api.model.Partner
import social.entourage.android.onboarding.OnboardingCallback
import social.entourage.android.tools.hideKeyboardFromLayout
import social.entourage.android.tools.log.AnalyticsEvents

private const val ARG_PARAM1 = "param1"

class OnboardingAssoFillFragment : Fragment() {

    private var currentAssoInfo: Partner? = null

    private var callback: OnboardingCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            currentAssoInfo = it.getSerializable(ARG_PARAM1) as? Partner
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_onboarding_asso_fill, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()

        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_VIEW_ONBOARDING_PRO_SIGNUP)
    }

    /********************************
     * Methods
     ********************************/

    fun setupViews() {
        onboard_asso_fill_mainlayout?.setOnFocusChangeListener { v, hasFocus ->
            v.hideKeyboardFromLayout()
        }

        ui_layout_asso_fill_location?.setOnClickListener {
            val intent = Intent(requireContext(),OnboardingAssoSearchActivity::class.java)
            startActivityForResult(intent,10)
        }

        ui_onboard_asso_fill_function?.setOnFocusChangeListener { v, hasFocus ->
            processEditText(ui_onboard_asso_fill_function,true)
        }

        ui_onboard_asso_fill_function?.setOnEditorActionListener { _, event, _ ->
            if (event == EditorInfo.IME_ACTION_DONE) {
                processEditText(ui_onboard_asso_fill_function,true)
            }
            false
        }

        ui_onboard_asso_fill_function?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                processEditText(ui_onboard_asso_fill_function,true)
            }
        })

        ui_onboard_asso_fill_postal_code?.setOnFocusChangeListener { v, hasFocus ->
           processEditText(ui_onboard_asso_fill_postal_code,false)
        }

        ui_onboard_asso_fill_postal_code?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                processEditText(ui_onboard_asso_fill_postal_code,true)
            }
        })

        updateAssoNameLabel()
        ui_onboard_asso_fill_postal_code?.setText(currentAssoInfo?.postalCode ?: "")
        ui_onboard_asso_fill_function?.setText(currentAssoInfo?.userRoleTitle ?: "")

        updateDelegateButtonNext()
    }

    fun processEditText(editText: EditText?,isFunction:Boolean) {
        if (currentAssoInfo == null) {
            currentAssoInfo = Partner()
        }

        if (isFunction) {
            currentAssoInfo?.userRoleTitle = editText?.text.toString()
        }
        else {
            currentAssoInfo?.postalCode = editText?.text.toString()
        }

        updateDelegateButtonNext()
        callback?.updateAssoInfos(currentAssoInfo)
    }

    fun updateAssoNameLabel() {
        currentAssoInfo?.name?.let { name ->
            ui_onboard_asso_fill_asso_name?.text = name
            ui_onboard_asso_fill_asso_name?.setTextColor(ResourcesCompat.getColor(resources,R.color.onboard_black_36,null))
        } ?: run {
            ui_onboard_asso_fill_asso_name?.text = getString(R.string.onboard_asso_fill_name_placeholder)
            ui_onboard_asso_fill_asso_name?.setTextColor(ResourcesCompat.getColor(resources,R.color.quantum_grey,null))
        }
    }

    fun updateDelegateButtonNext() {
        if (currentAssoInfo?.name?.length ?:0 > 0) {
            callback?.updateButtonNext(true)
        }
        else {
            callback?.updateButtonNext(false)
        }
    }

    fun updateAssoInfo() {
        updateAssoNameLabel()
        ui_onboard_asso_fill_postal_code?.setText(currentAssoInfo?.postalCode ?: "")

        updateDelegateButtonNext()

        currentAssoInfo?.let { callback?.updateAssoInfos(it) } ?: callback?.updateAssoInfos(Partner())
    }

    /********************************
     * Activity return
     ********************************/

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            data?.let {
                val _assoInfo = data.getSerializableExtra("partner") as? Partner
                processionUpdate(_assoInfo)
            }
        }
    }

    fun processionUpdate(newAsso:Partner?) {
        if (newAsso != null) {
            if (currentAssoInfo == null) {
                currentAssoInfo = newAsso
            }
            else {
                currentAssoInfo?.name= newAsso.name
                currentAssoInfo?.postalCode = newAsso.postalCode
                currentAssoInfo?.id = newAsso.id
                currentAssoInfo?.isCreation = newAsso.isCreation
            }

            if (ui_onboard_asso_fill_function?.text.toString().isNotEmpty()) {
                currentAssoInfo?.userRoleTitle = ui_onboard_asso_fill_function.text.toString()
            }
            updateAssoInfo()
        }
    }

    /********************************
     * Overrides
     ********************************/

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = (activity as? OnboardingCallback)
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
    }

    /********************************
     * Companion
     ********************************/
    companion object {
        fun newInstance(assoInfos: Partner?) =
                OnboardingAssoFillFragment().apply {
                    arguments = Bundle().apply {
                        putSerializable(ARG_PARAM1,assoInfos)
                    }
                }
    }
}