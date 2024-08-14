package com.smttcn.safebox.ui.settings

import android.os.Bundle
import com.smttcn.commons.activities.BaseActivity
import com.smttcn.safebox.MyApplication
import com.smttcn.safebox.ui.main.MainActivity
import com.smttcn.safebox.R

class SettingsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!MyApplication.authenticated) finish()
        initActivity()
        initActivityUI()
    }

    fun initActivity() {
        setContentView(R.layout.activity_settings)
    }

    fun initActivityUI() {
        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.settings,
                SettingsFragment()
            )
            .commit()

        supportActionBar?.title = getString(R.string.title_activity_settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

}