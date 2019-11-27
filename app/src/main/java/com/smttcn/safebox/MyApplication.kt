package com.smttcn.safebox

import android.app.Application

class MyApplication : Application() {
    companion object {
        var globalAppAuthenticated: String = "no"
    }
}