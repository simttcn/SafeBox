package com.smttcn.safebox.ui.debug

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.smttcn.commons.activities.BaseActivity
import com.smttcn.safebox.R
import com.smttcn.safebox.viewmodel.DebugViewModel
import com.smttcn.safebox.viewmodel.FileItemViewModel

class DebugconsoleActivity : BaseActivity() {

    private val debugViewModel: DebugViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        InitActivity()
        initActivityUI()
    }

    fun InitActivity() {
        setContentView(R.layout.activity_debugconsole)

        val textView: TextView = findViewById(R.id.text_debug)
        textView.movementMethod = ScrollingMovementMethod()

        debugViewModel.text.observe(this, Observer {
            textView.text = it
        })

    }

    private fun initActivityUI() {
        supportActionBar?.title = getString(R.string.title_activity_debugconsole)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

}
