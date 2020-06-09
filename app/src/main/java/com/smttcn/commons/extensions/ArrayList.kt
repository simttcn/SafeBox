package com.smttcn.commons.extensions

import java.util.*
import kotlin.collections.ArrayList

fun <T> ArrayList<T>.moveLastItemToFront() {
    val last = removeAt(size - 1)
    add(0, last)
}

fun <T> ArrayList<T>.duplicate() : ArrayList<T> {
    var new = ArrayList<T>()
    new.addAll(this)
    return new
}
