package social.entourage.android.new_v8.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import social.entourage.android.R
import social.entourage.android.databinding.NewActivityPedagoListBinding

class PedagoListActivity : AppCompatActivity() {
    lateinit var binding: NewActivityPedagoListBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.new_activity_pedago_list
        )
        handleBackButton()
    }

    private fun handleBackButton() {
        binding.header.iconBack.setOnClickListener {
            finish()
        }
    }
}