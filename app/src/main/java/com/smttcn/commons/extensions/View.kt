package com.smttcn.commons.extensions

import android.view.View
import android.view.ViewTreeObserver

// wait for the layout to finish laying out the items, including RecyclerView items
inline fun View.waitForLayout(crossinline f: () -> Unit) = with(viewTreeObserver) {
    addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            viewTreeObserver.removeOnGlobalLayoutListener(this)
            f()
        }
    })
}