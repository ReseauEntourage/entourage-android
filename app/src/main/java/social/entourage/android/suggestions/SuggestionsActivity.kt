package social.entourage.android.suggestions

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import social.entourage.android.R

class SuggestionsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_suggestions)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.suggestions_container, SuggestionFragment())
                .commit()
        }
    }
}
