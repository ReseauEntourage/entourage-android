package social.entourage.android.actions

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import social.entourage.android.api.model.ActionSectionFilters
import social.entourage.android.databinding.ActivityActionCatFiltersBinding
import social.entourage.android.tools.updatePaddingTopForEdgeToEdge

class ActionCategoriesFiltersActivity : AppCompatActivity() {

    private var currentFilters: ActionSectionFilters? = null

    private lateinit var binding: ActivityActionCatFiltersBinding

    private lateinit var actionCategoryAdapter: ActionCategoriesFiltersListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityActionCatFiltersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentFilters = intent.getSerializableExtra(CATEGORIES_FILTERS) as? ActionSectionFilters

        setupViews()
        initializeActionCat()
        updatePaddingTopForEdgeToEdge(binding.header.headerLayout)
    }

    private fun setupViews() {
        binding.validate.button.setOnClickListener {
            onSaveFilters()
        }
        binding.header.headerIconBack.setOnClickListener {
            onCancel()
        }
    }

    private fun initializeActionCat() {
        actionCategoryAdapter = ActionCategoriesFiltersListAdapter(
            currentFilters?.getSections() ?: ArrayList()
        )

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