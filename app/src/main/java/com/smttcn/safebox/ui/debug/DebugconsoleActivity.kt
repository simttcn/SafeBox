package com.smttcn.safebox.ui.debug

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.smttcn.commons.activities.BaseActivity
import com.smttcn.commons.extensions.getFilenameFromPath
import com.smttcn.safebox.R
import com.smttcn.safebox.ui.settings.SettingsFragment
import com.smttcn.safebox.viewmodel.DebugViewModel

class DebugconsoleActivity : BaseActivity() {

    private lateinit var debugViewModel: DebugViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        InitActivity()
        initActivityUI()
    }

    fun InitActivity() {
        setContentView(R.layout.activity_debugconsole)

        val textView: TextView = findViewById(R.id.text_debug)
        textView.movementMethod = ScrollingMovementMethod()

        debugViewModel = ViewModelProviders.of(this).get(DebugViewModel::class.java)
//        debugViewModel.text.observe(this, Observer {
//            textView.text = it
//        })
        debugViewModel.allDbItems.observe(this, Observer { item ->
            val str : StringBuilder = StringBuilder()

            item?.forEach {
                str.append(it.fileName + "\n" + it.hashedFileName + "\n" + it.fullPathWithFilename + "\n" + it.fullPathWithFilename.getFilenameFromPath() + "\n\n")
            }

            textView.text = str.toString()
        })

    }

    fun initActivityUI() {
        supportActionBar?.title = getString(R.string.title_activity_debugconsole)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

}
