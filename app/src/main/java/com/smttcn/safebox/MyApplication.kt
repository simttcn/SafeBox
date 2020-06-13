package com.smttcn.safebox

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import com.smttcn.commons.Manager.FileManager
import com.smttcn.commons.helpers.BaseConfig
import com.smttcn.commons.helpers.PREFS_KEY

class MyApplication : Application(), Application.ActivityLifecycleCallbacks{

    companion object {
        private var instance: MyApplication? = null
        private var _authenticated: Boolean = false
        private var _mainActivityContext: Context? = null
        private var _baseConfig: BaseConfig? = null

        var mainActivityContext: Context
            get() =_mainActivityContext!!
            set(value) { _mainActivityContext = value }

        var applicationContext: Context
            get() = instance!!.applicationContext
            private set(value) {}

        var baseConfig: BaseConfig
            get() {
                return if (_baseConfig != null)
                    _baseConfig!!
                else {
                    if (instance != null) {
                        val prefs = instance!!.applicationContext.getSharedPreferences(
                            PREFS_KEY,
                            Context.MODE_PRIVATE
                        )
                        _baseConfig = BaseConfig.newInstance(prefs)
                    }
                    _baseConfig!!
                }
            }
            private set(value) {}

        var authenticated: Boolean
            get() {
                return baseConfig.appPasswordEnabled == false || _authenticated
            }
            set(value) { _authenticated = value }

        fun lockApp() {
            _authenticated = false
            FileManager.deleteCache()
        }
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

        FileManager.deleteCache()
    }


    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) { }

    override fun onActivityStarted(activity: Activity) {
        // Tracing activity status to determine if the app still in the foreground
        //      in order to set the Authentication status and trigger the login screen
        if (++activityReferences == 1 && !isActivityChangingConfigurations) {
            // App enters foreground
            lockApp()
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
            lockApp()
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) { }

    override fun onActivityDestroyed(activity: Activity) { }
}