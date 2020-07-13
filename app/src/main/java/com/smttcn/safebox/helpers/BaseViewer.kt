package com.smttcn.safebox.helpers

import android.content.Context
import android.view.View
import com.smttcn.commons.models.FileDirItem

abstract class BaseViewer() {

    internal lateinit var _parentContext: Context
    internal lateinit var _view: View
    internal lateinit var _file: FileDirItem

    abstract fun initialize(context: Context, view: View, fileDirItem: FileDirItem)
    abstract fun view(password: CharArray)
}