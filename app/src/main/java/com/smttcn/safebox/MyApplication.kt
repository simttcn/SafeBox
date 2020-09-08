package com.smttcn.safebox

import android.app.Activity
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import com.smttcn.commons.manager.FileManager
import com.smttcn.commons.helpers.BaseConfig
import com.smttcn.commons.helpers.PREFS_KEY

class MyApplication : Application(), Application.ActivityLifecycleCallbacks{

    companion object {
        private var instance: MyApplication? = null
        private var _authenticated: Boolean = false
        private var _mainActivityContext: Context? = null
        private var _baseConfig: BaseConfig? = null
        private var _isSharingItem: Boolean = false

        var mainActivityContext: Context
            get() =_mainActivityContext!!
            set(value) { _mainActivityContext = value }

        val applicationContext: Context
            get() = instance!!.applicationContext

        val baseConfig: BaseConfig
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

        var authenticated: Boolean
            get() {
                // We will always force app password whenever password hash is found in the config
                if (baseConfig.appPasswordHash.length > 0)
                    baseConfig.appPasswordEnabled = true

                return baseConfig.appPasswordEnabled == false || _authenticated
            }
            set(value) { _authenticated = value }

        var isSharingItem: Boolean
            get() = _isSharingItem
            set(value) {_isSharingItem = value}

        var isDebug: Boolean
            get() = BuildConfig.DEBUG
            private set(_) {}

        fun lockApp() {
            _authenticated = false

            if (_isSharingItem) {
                // in the process of sharing item so do not clear cache
                // reset the flag so we will clear the cache next time
                _isSharingItem = false
            } else {
                FileManager.deleteCacheShareFolder()
            }
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

        FileManager.emptyCacheFolder()
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
        isActivityChangingConfigurations = activity.isChangingConfigurations
        if (--activityReferences == 0 && !isActivityChangingConfigurations) {
            // App enters background
            lockApp()
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) { }

    override fun onActivityDestroyed(activity: Activity) { }

}