package social.entourage.android.onboarding.asso


import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_onboard_asso_search.*
import social.entourage.android.EntourageApplication.Companion.get
import social.entourage.android.R
import social.entourage.android.api.OnboardingAPI
import social.entourage.android.api.model.Partner
import social.entourage.android.tools.hideKeyboard
import social.entourage.android.tools.view.CustomProgressDialog
import timber.log.Timber


class OnboardingAssoSearchActivity : AppCompatActivity() {

    var searchRvAdapter:OnboardingAssoSearchAdapter? = null

    var arrayAssos = ArrayList<Partner>()
    var arrayAssosSearch = ArrayList<Partner>()
    var isFiltered = false
    var selectedAsso:Partner? = null

    lateinit var alertDialog: CustomProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboard_asso_search)
        alertDialog = CustomProgressDialog(this)
        Timber.d("Taille Array on act : ${arrayAssos.size}")
        setupViews()
        getAssosList()
    }

    /********************************
     * Network
     ********************************/

    fun getAssosList() {
        alertDialog.show(R.string.onboard_waiting_dialog)
        OnboardingAPI.getInstance().getAssociationsList { arrayAssociations ->
            alertDialog.dismiss()
            if (arrayAssociations != null) {
                arrayAssos.clear()
                arrayAssos.addAll(arrayAssociations)
                searchRvAdapter?.reloadDatas(arrayAssos)
            }
        }
    }

    /********************************
     * Methods
     ********************************/

    fun setupViews() {
        ui_asso_search_main_layout?.setOnTouchListener { view, _ ->
            view.hideKeyboard()
            view.performClick()
            true
        }

        ui_bt_asso_search_cancel?.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        ui_bt_asso_search_validate?.setOnClickListener {
            if (selectedAsso == null) return@setOnClickListener
            val intent = Intent()
            intent.putExtra("partner",selectedAsso)
            setResult(Activity.RESULT_OK,intent)
            finish()
        }

        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        ui_asso_search_rv?.setHasFixedSize(true)
        ui_asso_search_rv?.layoutManager = linearLayoutManager

        searchRvAdapter = OnboardingAssoSearchAdapter(this,arrayAssos) { position ->
            selectedAsso = if(isFiltered) arrayAssosSearch[position] else arrayAssos[position]
        }

        ui_asso_search_rv?.adapter = searchRvAdapter

        ui_asso_search_et_search?.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(s: Editable) {
                arrayAssosSearch.clear()
                selectedAsso = null

                arrayAssosSearch = arrayAssos.filter { asso -> asso.name?.contains(s,true)==true } as ArrayList<Partner>
                if (s.isEmpty()) {
                    arrayAssosSearch.clear()
                    isFiltered = false
                }
                else {
                    if (arrayAssosSearch.size == 0) {
                        val newAsso = Partner()
                        newAsso.name = s.toString()
                        newAsso.isCreation = true
                        arrayAssosSearch.add(newAsso)
                    }
                    isFiltered = true
                }
                searchRvAdapter?.updateDatas(isFiltered,arrayAssosSearch)
            }
        })
    }
}