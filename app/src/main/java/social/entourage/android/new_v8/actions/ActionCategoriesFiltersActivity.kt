package social.entourage.android.new_v8.actions

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import social.entourage.android.R
import social.entourage.android.databinding.NewActivityActionCatFiltersBinding
import social.entourage.android.new_v8.models.ActionSection
import social.entourage.android.new_v8.models.ActionSectionFilters

class ActionCategoriesFiltersActivity : AppCompatActivity() {

    private var currentFilters: ActionSectionFilters? = null

    private lateinit var binding: NewActivityActionCatFiltersBinding

    private lateinit var actionCategoryAdapter: ActionCategoriesFiltersListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(
            this,
            R.layout.new_activity_action_cat_filters
        )

        currentFilters = intent.getSerializableExtra(CATEGORIES_FILTERS) as? ActionSectionFilters

        setupViews()
        initializeActionCat()
    }

    private fun setupViews() {
        binding.validate.button.setOnClickListener {
            onSaveFilters()
        }
        binding.header.iconBack.setOnClickListener {
            onCancel()
        }
    }

    private fun initializeActionCat() {
        actionCategoryAdapter = ActionCategoriesFiltersListAdapter(currentFilters?.getSections() ?:ArrayList<ActionSection>())

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = actionCategoryAdapter
        }
    }

    private fun onSaveFilters() {
        val intent = Intent()
        intent.putExtra(CATEGORIES_FILTERS, currentFilters)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun onCancel() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }
}