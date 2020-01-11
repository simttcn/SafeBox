package com.smttcn.safebox

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import com.smttcn.safebox.Manager.AppDatabaseManager
import com.smttcn.safebox.Manager.StoreItemManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import timber.log.Timber

class MyApplication : Application(), Application.ActivityLifecycleCallbacks, CoroutineScope by MainScope(){

    companion object {
        var globalAppAuthenticated: String = "no"
        lateinit var mainActivityContext: Context
        private var instance: MyApplication? = null

        fun getMainContext(): Context {
            return mainActivityContext
        }

        fun setMainContext(context: Context): Unit {
            mainActivityContext = context

        }

        fun getAppContext() : Context = instance!!.applicationContext

    }

    var activityReferences: Int = 0
    var isActivityChangingConfigurations: Boolean = false

    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()
        activityReferences = 0
        isActivityChangingConfigurations = false

        registerActivityLifecycleCallbacks(this)
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
    }

    override fun onTerminate() {
        super.onTerminate()
        AppDatabaseManager.getDb().close()
    }


    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) { }

    override fun onActivityStarted(activity: Activity) {
        // Tracing activity status to determine if the app still in the foreground
        //      in order to set the Authentication status and trigger the login screen
        if (++activityReferences == 1 && !isActivityChangingConfigurations) {
            // App enters foreground
            globalAppAuthenticated = "no"
        }
    }

    override fun onActivityResumed(activity: Activity) { }

    override fun onActivityPaused(activity: Activity) { }

    override fun onActivityStopped(activity: Activity) {
        // Tracing activity status to determine if the app still in the foreground
        //      in order to set the Authentication status and trigger the login screen
        isActivityChangingConfigurations = activity.isChangingConfigurations();
        if (--activityReferences == 0 && !isActivityChangingConfigurations) {
            // App enters background
            globalAppAuthenticated = "no"
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) { }

    override fun onActivityDestroyed(activity: Activity) { }
}