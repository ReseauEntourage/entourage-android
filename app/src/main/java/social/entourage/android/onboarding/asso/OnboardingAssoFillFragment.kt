package social.entourage.android.onboarding.asso

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.core.content.res.ResourcesCompat
import kotlinx.android.synthetic.main.fragment_onboarding_asso_fill.*
import social.entourage.android.R
import social.entourage.android.api.model.Partner
import social.entourage.android.onboarding.OnboardingCallback
import social.entourage.android.tools.hideKeyboardFromLayout

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

        ui_onboard_asso_fill_postal_code?.setOnFocusChangeListener { v, hasFocus ->
           processEditText(ui_onboard_asso_fill_postal_code,false)
        }

        updateAssoNameLabel()
        ui_onboard_asso_fill_postal_code?.setText(currentAssoInfo?.postal_code ?: "")
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
            currentAssoInfo?.postal_code = editText?.text.toString()
        }

        updateDelegateButtonNext()
        callback?.updateAssoInfos(currentAssoInfo)
    }

    fun updateAssoNameLabel() {
        if (currentAssoInfo?.name != null) {
            ui_onboard_asso_fill_asso_name?.text = currentAssoInfo!!.name
            ui_onboard_asso_fill_asso_name?.setTextColor(ResourcesCompat.getColor(resources,R.color.onboard_black_36,null))
        }
        else {
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
        ui_onboard_asso_fill_postal_code?.setText(currentAssoInfo?.postal_code ?: "")

        updateDelegateButtonNext()

        if (currentAssoInfo != null) {
            callback?.updateAssoInfos(currentAssoInfo!!)
        }
        else {
            callback?.updateAssoInfos(Partner())
        }
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
                currentAssoInfo?.name = newAsso.name
                currentAssoInfo?.postal_code = newAsso.postal_code
                currentAssoInfo?.id = newAsso.id
                currentAssoInfo?.isCreation = newAsso.isCreation
            }

            if (ui_onboard_asso_fill_function?.text.toString().length > 0) {
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
        @JvmStatic
        fun newInstance(assoInfos: Partner?) =
                OnboardingAssoFillFragment().apply {
                    arguments = Bundle().apply {
                        putSerializable(ARG_PARAM1,assoInfos)
                    }
                }
    }
}