package com.smttcn.safebox.helpers

import android.app.Activity
import android.content.Context
import android.view.View
import com.smttcn.commons.models.FileDirItem
import com.smttcn.safebox.databinding.RecyclerviewItemBinding

abstract class BaseViewer {

    internal lateinit var _parentActivity: Activity
    internal lateinit var _view: View
    internal lateinit var _binding: RecyclerviewItemBinding
    internal lateinit var _file: FileDirItem

    abstract fun initialize(activity: Activity, view: View, binding: RecyclerviewItemBinding, fileDirItem: FileDirItem)
    abstract fun view(password: CharArray)
}