package com.gmail.zajcevserg.maptestapp.model.application


import android.util.Log
import com.gmail.zajcevserg.maptestapp.model.database.DataItem


fun String.log() {
    Log.d("myLog", this)
}

inline fun Iterable<DataItem>.noneExceptHeader(predicate: (DataItem.LayerItem) -> Boolean): Boolean {
    if (this is Collection && isEmpty()) return true
    for (element in this) {
        if (element is DataItem.LayerItem) {
            if (predicate(element)) return false
        }
    }
    return true
}

inline fun Iterable<DataItem>.allExceptHeader(predicate: (DataItem.LayerItem) -> Boolean): Boolean {
    if (this is Collection && isEmpty()) return true
    for (element in this) {
        if (element is DataItem.LayerItem) {
            if (!predicate(element)) return false
        }
    }
    return true
}

inline fun Iterable<DataItem>.forEachExceptHeader(action: (DataItem.LayerItem) -> Unit) {
    for (element in this) {
        if (element is DataItem.LayerItem) action(element)
    }
}

inline fun Iterable<DataItem>.findExceptHeader(
    predicate: (DataItem.LayerItem) -> Boolean
): DataItem.LayerItem? {
    for (element in this) {
        if (element is DataItem.LayerItem) {
            if (predicate(element)) return element
        }
    }
    return null
}


inline fun Iterable<DataItem>.filterExceptHeader(
    predicate: (DataItem.LayerItem) -> Boolean
): List<DataItem.LayerItem> {
    val destination = ArrayList<DataItem.LayerItem>()
    for (element in this) {
        if (element is DataItem.LayerItem) {
            if (predicate(element)) destination.add(element)
        }
    }
    return destination
}

inline fun Iterable<DataItem>.anyExceptHeader(
    predicate: (DataItem.LayerItem) -> Boolean
): Boolean {
    if (this is Collection && isEmpty()) return false
    for (element in this) {
        if (element is DataItem.LayerItem) {
            if (predicate(element)) return true
        }
    }
    return false
}

