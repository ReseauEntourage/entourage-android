package social.entourage.android.events.list

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.databinding.ActivityWelcomeEventsListBinding
import social.entourage.android.events.EventsPresenter
import social.entourage.android.api.model.Events

class WelcomeEventsListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeEventsListBinding
    private lateinit var eventsPresenter: EventsPresenter
    private lateinit var eventsAdapter: AllEventAdapter
    private var myId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeEventsListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val type = intent.getStringExtra("TYPE") ?: "papotages"

        eventsPresenter = ViewModelProvider(this).get(EventsPresenter::class.java)
        myId = EntourageApplication.me(this)?.id
        eventsAdapter = AllEventAdapter(myId, this)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@WelcomeEventsListActivity)
            adapter = eventsAdapter
        }

        // Branchement du nouveau bouton retour
        binding.btnBack.setOnClickListener {
            finish()
        }

        if (type == "webinar") {
            binding.tvWelcomeListTitle.text = getString(R.string.welcome_webinar_list_title)
            binding.tvWelcomeListSubtitle.text = getString(R.string.welcome_webinar_list_subtitle)
            eventsPresenter.getEventsWebinar.observe(this, ::handleEventsResponse)
            eventsPresenter.getEventsWebinar()
        } else {
            binding.tvWelcomeListTitle.text = getString(R.string.welcome_papotages_list_title)
            binding.tvWelcomeListSubtitle.text = getString(R.string.welcome_papotages_list_subtitle)
            eventsPresenter.getEventsPapotages.observe(this, ::handleEventsResponse)
            eventsPresenter.getEventsPapotages()
        }
    }

    private fun handleEventsResponse(events: MutableList<Events>?) {
        binding.progressBar.visibility = View.GONE
        if (events != null && events.isNotEmpty()) {
            eventsAdapter.resetData(events)
            binding.emptyStateLayout.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        } else {
            binding.emptyStateLayout.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
        }
    }
}