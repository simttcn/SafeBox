package com.smttcn.safebox.helpers

import android.content.Context
import android.view.View
import com.smttcn.commons.models.FileDirItem

abstract class BaseViewerHelpers(context: Context, view: View, file: FileDirItem) {

    internal var _parentContext: Context
    internal var _view: View
    internal var _file: FileDirItem

    init{
        _parentContext = context
        _view = view
        _file = file
    }

    abstract fun view()
    abstract fun isSupported(): Boolean

}