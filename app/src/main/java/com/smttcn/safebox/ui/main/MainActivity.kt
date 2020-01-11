package com.smttcn.safebox.ui.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import com.smttcn.commons.extensions.toast
import com.smttcn.commons.helpers.INTERVAL_BACK_BUTTON_QUIT_IN_MS
import android.view.Menu;
import android.view.MenuItem;
import com.google.android.material.snackbar.Snackbar
import com.smttcn.commons.activities.BaseActivity
import com.smttcn.safebox.Manager.AppDatabaseManager
import com.smttcn.safebox.Manager.StoreItemManager
import com.smttcn.safebox.MyApplication
import com.smttcn.safebox.R
import com.smttcn.safebox.ui.debug.DebugconsoleActivity
import com.smttcn.safebox.ui.settings.SettingsActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.concurrent.thread


class MainActivity : BaseActivity(), MainFragment.OnFragmentInteractionListener {

    var BackButtonPressedOnce = false
    var LastPressedBackTime = System.currentTimeMillis()
    lateinit var StoreItemListFragment : MainFragment

    companion object {
        fun isAuthenticated(): Boolean {
            return MyApplication.globalAppAuthenticated.equals("yes")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        // Todo: find how to better initialize the database when app starts
        initialize()
        AppDatabaseManager.initialize()
        StoreItemManager.initialize()
        initializeUI()
    }

    private fun initialize() {
        MyApplication.setMainContext(this)
    }

    private fun initializeUI() {
        StoreItemListFragment = MainFragment()
        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.frame_main,
                StoreItemListFragment
            )
            .commit()

    }

    override fun onBackPressed() {
        val millisecondsSinceLastPressedBack = System.currentTimeMillis() - LastPressedBackTime
        if (!BackButtonPressedOnce || millisecondsSinceLastPressedBack > INTERVAL_BACK_BUTTON_QUIT_IN_MS) {
            toast(R.string.press_back_again_to_quit, Toast.LENGTH_LONG)
            BackButtonPressedOnce = true
            LastPressedBackTime = System.currentTimeMillis()
            return
        } else {
            finishAndRemoveTask()
        }
        super.onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        when(id) {
            R.id.menu_refresh -> {
                launch {
                    StoreItemListFragment.refreshStoreItemList()
                }
                return true
            }
            R.id.menu_settings -> {
                startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                return true
            }
            R.id.menu_debug_console -> {
                startActivity(Intent(this@MainActivity, DebugconsoleActivity::class.java))
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onFragmentInteraction(uri: Uri) {
        //To change body of created functions use File | Settings | File Templates.
    }

}
